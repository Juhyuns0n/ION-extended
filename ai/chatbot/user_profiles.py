import pymysql
from config import DB_HOST, DB_USER, DB_PASSWORD, DB_NAME, DB_PORT

NEEDED_KEYS = [
    "user_id", "kids_nickname", "kids_age", "kids_tendency",
    "kids_note", "goal", "worry"
]

def load_user_profile(user_id: str) -> dict:
    """
    Load user profile from RDS MySQL DB: user table.
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
            sql = "SELECT * FROM user WHERE user_id = %s"
            cursor.execute(sql, (user_id,))
            result = cursor.fetchone()
            if not result:
                raise KeyError(f"user_id '{user_id}' not found")
            # Needed Keys 추출
            return {k: result.get(k) for k in NEEDED_KEYS}
    finally:
        conn.close()
