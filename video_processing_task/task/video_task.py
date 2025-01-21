import ffmpeg
import os
import re
from worker import app
from loguru import logger
import json
import traceback
from config.redis_config import redis_connection
from enum import Enum


class VideoState(Enum):
    PENDING = "PENDING"
    PROCESSING = "PROCESSING"
    PROCESSED = "PROCESSED"
    FAILED = "FAILED"
class VideoProgression:
    def __init__(self, total_frames):
        """
        Initialize a VideoProgression instance.

        :param total_frames: int, the total number of frames in the video
        """
        self._current_frame = 0
        self.total_frames = total_frames

    @property
    def current_frame(self):
        """
        Get the current frame.
        """
        return self._current_frame

    @current_frame.setter
    def current_frame(self, frame):
        """
        Set the current frame. Ensures the frame value is valid.

        :param frame: int, the current frame to set
        """
        if 0 <= frame <= self.total_frames:
            self._current_frame = frame
        else:
           self._current_frame = frame
           self.total_frames = frame

    @property
    def percent(self):
        """
        Get the current progression percentage.
        """
        return (self._current_frame / self.total_frames) * 100 if self.total_frames > 0 else 0

    def to_dict(self):
        """
        Convert the VideoProgression instance to a dictionary.
        """
        return {
            "current_frame": self._current_frame,
            "total_frames": self.total_frames,
            "percent": self.percent
        }

    def to_json(self):
        """
        Convert the VideoProgression instance to a JSON string.
        """
        return json.dumps(self.to_dict())

    def convert_from_dict(cls, data):
        """
        Create a VideoProgression instance from a dictionary.

        :param data: dict, the dictionary containing the VideoProgression data
        """
        return cls(data["total_frames"], data["current_frame"])


def parse_progress(line, video_progression: VideoProgression):
    progress_regex = re.compile(r"(\w+)=([^\s]+)")
    for match in progress_regex.finditer(line):
        key, value = match.groups()
        video_progression.current_frame = int(value)

@app.task(bind=True)
def transcodeVideoToHls(self, video_key: str, task_id: str):
    logger.info(
        f"Task ID: {task_id} - Video key {video_key} - Start transcoding to HLS format.")
    redis_connection.set(f"task:{task_id}", VideoState.PROCESSING.value)
    # Get the current script's directory
    current_dir = os.path.dirname(os.path.abspath(
        __file__))  # projects/task

    # Go up one level to the projects directory
    parent_dir = os.path.dirname(current_dir)  # projects

    # Get the video folder path
    video_path = os.path.join(parent_dir, 'video')  # projects/video

    # Specify the subfolders relative to the current directory
    input_folder = "input"
    output_folder = "output"
    input_path = os.path.join(video_path, input_folder, video_key)
    output_path = os.path.join(video_path, output_folder, f"{
                               task_id}/vs%v/out.m3u8")
    output_ts_path = os.path.join(
        video_path, output_folder, f"{task_id}/vs%v/file_%03d.ts")
    probe = ffmpeg.probe(
        input_path,
        cmd="ffprobe",
        v="error",
        select_streams="v:0",
        count_packets=None,
        show_entries="stream=nb_read_packets",
    )
    total_frame = probe["streams"][0]["nb_read_packets"]
    input: ffmpeg.input = ffmpeg.input(input_path)
    split_filter = ffmpeg.filter_multi_output(input, "split")
    v_stream_1080 = ffmpeg.filter(
        split_filter[0], "scale", **{"w": "1920", "h": "1080"}
    )
    v_stream_720 = ffmpeg.filter(split_filter[1], "scale", **{"w": "1280", "h": "720"})
    v_stream_480 = ffmpeg.filter(split_filter[2], "scale", **{"w": "640", "h": "480"})
    v_stream_360 = ffmpeg.filter(split_filter[3], "scale", **{"w": "480", "h": "360"})
    output = ffmpeg.output(
        v_stream_1080,
        v_stream_720,
        v_stream_480,
        v_stream_360,
        input.audio,
        input.audio,
        input.audio,
        input.audio,
        **{
            "c:v:0": "libx265",
            "c:v:1": "libx265",
            "c:v:2": "libx265",
            "c:v:3": "libx265",
            "crf": 23,
            "b:v:0": "6M",
            "b:v:1": "4M",
            "b:v:2": "2M",
            "b:v:3": "0.8M",
            "preset": "ultrafast",
            "preset": "ultrafast",
            "preset": "ultrafast",
            "preset": "ultrafast",
            "keyint_min": 24,
            "keyint_min": 24,
            "keyint_min": 24,
            "keyint_min": 24,
            "level": "3.0",
            "level": "3.0",
            "level": "3.0",
            "level": "3.0",
            "g": 48,
            "g": 48,
            "g": 48,
            "g": 48,
            "pix_fmt": "yuv420p",
            "r": "24",
            "var_stream_map": "v:0,a:0 v:1,a:1 v:2,a:2 v:3,a:3",
            "hls_segment_filename": output_ts_path,
            "master_pl_name": "master.m3u8",
            "hls_list_size": 0,
            "hls_time": 5,
            "hls_playlist_type": "vod",
            "hls_segment_type": "fmp4",
            "master_pl_publish_rate": 5,
            "x265-params": "log-level=none",
            "v": "error",
            "progress": "pipe:1",
        },
        filename=output_path,
    )
    output.overwrite_output()
    video_progression = VideoProgression(int(total_frame))
    # Start the subprocess and output the progress which is current frame / total frame
    process = output.run_async(pipe_stdout=True, pipe_stderr=True)
    progress_channel = f"progress:{task_id}"
    try:
        while process.poll() is None:  # While the process is running
            line = str(process.stdout.readline().strip(), "utf8")
            if line.find("frame=") != -1:
                parse_progress(line, video_progression)
                logger.info(
                    f"Task ID: {task_id} - Video key {video_key} - Progress: {video_progression.to_json()}")
                redis_connection.publish(
                    progress_channel, video_progression.to_json())
    except Exception as e:
        process.terminate()
        logger.error(f"Task ID: {task_id} - Video key {video_key} - An error occurred: {e} - {traceback.format_exc()}")
        redis_connection.set(f"task:{task_id}", VideoState.FAILED.value)
    finally:
        process.wait()
        if process.returncode == 0:
            video_progression.current_frame = video_progression.total_frames
            redis_connection.publish(
                progress_channel, video_progression.to_json())
            logger.info(
                f"Task ID: {task_id} - Video key {video_key} - Progress: {video_progression.to_json()}")
            redis_connection.set(f"task:{task_id}", VideoState.PROCESSED.value)
            logger.info(
                f"Task ID: {task_id} - Video key {video_key} - Completed successfully.")
        else:
            logger.error(f"Task ID: {task_id} - Video key {video_key} - Transcoding failed with return code {process.returncode}.")
            redis_connection.set(f"task:{task_id}", VideoState.FAILED.value)
