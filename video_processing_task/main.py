import boto3
import json
from config.redis_config import redis_connection
from task.video_task import VideoState
from loguru import logger
import task.video_task as t

s3 = boto3.client('s3')


def lambda_handler(event, context):
    """
    Process S3 PUT events and retrieve custom metadata.
    """
    logger.info(f"Received event: {json.dumps(event)}")
    file_prefix = "video/input/"
    # Loop through the records in the event
    for record in event['Records']:
        bucket_name = record['s3']['bucket']['name']
        object_key = record['s3']['object']['key']
        metadata = s3.head_object(Bucket=bucket_name, Key=object_key)
        custom_metadata = metadata["Metadata"]
        task_id = custom_metadata["task-id"]
        file_name = object_key[len(file_prefix) :]
        logger.info(
            f"Task ID: {task_id} - Custom metadata: {custom_metadata} - pushed to SQS.")
        redis_connection.set(f"task:{task_id}", VideoState.PENDING.value)
        t.transcodeVideoToHls.delay(video_key=file_name, task_id=task_id)
    return {
        'statusCode': 200,
        'body': json.dumps("Event consumed successfully")
    }
