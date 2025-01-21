import redis
import os

# REDIS HOST
REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
# REDIS PORT
REDIS_PORT = os.getenv("REDIS_PORT", 6379)
# REDIS PASSWORD
REDIS_PASSWORD = os.getenv("REDIS_PASSWORD", "")
# REDIS USERNAME
REDIS_USERNAME = os.getenv("REDIS_USERNAME", "")
# REDIS DB
REDIS_DB = os.getenv("REDIS_DB", 0)
# REDIS SSL
REDIS_SSL = os.getenv("REDIS_SSL", False)

redis_connection = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, username=REDIS_USERNAME,
                               password=REDIS_PASSWORD, ssl=REDIS_SSL, db=REDIS_DB, decode_responses=True)