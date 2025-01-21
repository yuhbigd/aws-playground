from config.worker_config import Config
from celery import Celery
from dotenv import load_dotenv
load_dotenv()
# Initialize Celery app
app = Celery("app")
app.config_from_object(Config)
