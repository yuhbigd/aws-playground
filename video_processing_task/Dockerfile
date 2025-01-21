FROM ffmpeg_py:latest

# Set the working directory in the container
WORKDIR /app

# Copy project files to the working directory
COPY . /app

# Create a virtual environment and install dependencies
RUN python3 -m venv venv && \
    ./venv/bin/pip install --upgrade pip && \
    ./venv/bin/pip install -r requirements.txt && \
    ./venv/bin/pip install celery[sqs]

# Specify the command to run the application (replace with your actual command)
CMD ["./venv/bin/celery", "-A", "worker", "worker", "-l", "INFO", "--pool=solo"]