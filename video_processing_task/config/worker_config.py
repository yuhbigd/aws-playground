from kombu.utils.url import safequote
import os

SQS_QUEUE_URL = os.getenv("SQS_QUEUE_URL")

broker_url = "sqs://"
REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
# REDIS PORT
REDIS_PORT = os.getenv("REDIS_PORT", 6379)
# REDIS PASSWORD
REDIS_PASSWORD = os.getenv("REDIS_PASSWORD", "")
# REDIS USERNAME
REDIS_USERNAME = os.getenv("REDIS_USERNAME", "")
# REDIS DB
REDIS_DB = os.getenv("REDIS_DB", 0)

redis_backend_url = f"rediss://{safequote(REDIS_USERNAME)}:{safequote(REDIS_PASSWORD)}@{REDIS_HOST}:{REDIS_PORT}/{REDIS_DB}?ssl_cert_reqs=required"

class Config:
    broker_url = broker_url
    result_backend = redis_backend_url
    broker_transport_options = {
        "region": "ap-southeast-1",  # your AWS SQS region
        "predefined_queues": {
            "celery": {  # the name of the SQS queue
                "url": SQS_QUEUE_URL
            }
        },
        "wait_time_seconds": 5,
    }
    include = ["task.video_task"]
    result_backend_transport_options = {"global_keyprefix": "my_prefix_"}
