import pymysql
import numpy as np
from sentence_transformers import SentenceTransformer
from typing import List, Dict
from config import DB_HOST, DB_USER, DB_PASSWORD, DB_NAME, DB_PORT


"""
Simple RDS-backed vector retrieval

Functions:
 - retrieve_top_k(question, k): returns list of dicts {id, paragraph, score}
"""

# RDS Connetion Info (kept here to match preprocessing script)
RDS_CONFIG = {
    "host": DB_HOST,
    "user": DB_USER,
    "password": DB_PASSWORD,
    "db": DB_NAME,
    "port": DB_PORT,
    "charset": "utf8mb4",
    "cursorclass": pymysql.cursors.DictCursor
}


_MODEL = SentenceTransformer("snunlp/KR-SBERT-V40K-klueNLI-augSTS")


def _get_model():
    global _MODEL
    if _MODEL is None:
        _MODEL = SentenceTransformer("snunlp/KR-SBERT-V40K-klueNLI-augSTS")
    return _MODEL


def _fetch_all_vectors() -> List[Dict]:
    """Return list of dicts: {'id': id, 'vector': np.array(dtype=float32)}"""
    conn = pymysql.connect(**RDS_CONFIG)
    try:
        with conn.cursor() as cursor:
            cursor.execute("SELECT id, vector FROM emotion_coaching_vector")
            rows = cursor.fetchall()
            results = []
            for r in rows:
                vec_bytes = r["vector"]
                if vec_bytes is None:
                    continue
                # Stored as float32 bytes
                vec = np.frombuffer(vec_bytes, dtype=np.float32)
                results.append({"id": r["id"], "vector": vec})
            return results
    finally:
        conn.close()


def _fetch_paragraphs_by_ids(id_list: List[int]) -> Dict[int, str]:
    if not id_list:
        return {}
    conn = pymysql.connect(**RDS_CONFIG)
    try:
        with conn.cursor() as cursor:
            # Use parameterized IN clause
            format_strings = ",".join(["%s"] * len(id_list))
            cursor.execute(
                f"SELECT id, paragraph FROM emotion_coaching_text WHERE id IN ({format_strings})",
                tuple(id_list),
            )
            rows = cursor.fetchall()
            return {r["id"]: r["paragraph"] for r in rows}
    finally:
        conn.close()


def retrieve_top_k(question: str, k: int = 2) -> List[Dict]:
    """Encode question, compute cosine similarity against stored vectors and
    return top-k items as list of dicts: {id, paragraph, score}.
    """
    model = _get_model()
    q_vec = model.encode([question], show_progress_bar=False)[0].astype("float32")

    stored = _fetch_all_vectors()
    if not stored:
        return []

    # Stack vectors
    vecs = np.vstack([s["vector"] for s in stored])
    if vecs.ndim != 2:
        return []

    # Ensure dimension match
    if vecs.shape[1] != q_vec.shape[0]:
        # incompatible dims
        raise ValueError(f"Embedding dimension mismatch: stored={vecs.shape[1]} question={q_vec.shape[0]}")

    # normalize
    vecs_norm = vecs / (np.linalg.norm(vecs, axis=1, keepdims=True) + 1e-12)
    q_norm = q_vec / (np.linalg.norm(q_vec) + 1e-12)

    # cosine similarities
    sims = np.dot(vecs_norm, q_norm)

    # get top k indices
    topk_idx = np.argsort(-sims)[:k]
    top_ids = [stored[i]["id"] for i in topk_idx]

    # Fetch paragraphs for these ids
    id_to_para = _fetch_paragraphs_by_ids(top_ids)

    results = []
    for i in topk_idx:
        sid = stored[i]["id"]
        results.append({
            "id": sid,
            "paragraph": id_to_para.get(sid, ""),
            "score": float(sims[i])
        })

    return results
