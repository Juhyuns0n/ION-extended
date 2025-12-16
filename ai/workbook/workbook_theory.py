import pymysql
from config import DB_HOST, DB_USER, DB_PASSWORD, DB_NAME, DB_PORT

NEEDED_KEYS = [
    "chapter_id", "chapter_name", "necessity", "study_goal", "notion"
]

def load_chapter_info(chapter_id: int) -> dict:
    """
    Load chapter information from RDS MySQL DB: workbook_theory table.
    """
    conn = pymysql.connect(
        host=DB_HOST,
        user=DB_USER,
        password=DB_PASSWORD,
        db=DB_NAME,
        port=DB_PORT,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor
    )
    try: 
        with conn.cursor() as cursor:
            sql = "SELECT * FROM workbook_theory WHERE chapter_id = %s"
            cursor.execute(sql, (chapter_id,))
            result = cursor.fetchone()
            if not result:
                raise KeyError(f"chapter_id '{chapter_id}' not found")
            # Needed Keys 추출
            return {k: result.get(k) for k in NEEDED_KEYS}
    finally:
        conn.close()
