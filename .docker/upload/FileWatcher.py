import os
import sys
import time
import logging
from minio import Minio
from minio.error import S3Error
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler


class FileWatcher(FileSystemEventHandler):

    def __init__(self, client, bucketName):
        super().__init__()
        self.client = client
        self.bucketName = bucketName

    def on_created(self, event):
        logging.info(f"{event.src_path} has created, uploading it")
        self.upload(event.src_path)

    def on_modified(self, event):
        logging.info(f"{event.src_path} has changed, uploading it")
        self.upload(event.src_path)

    def upload(self, file_path):
        try:
            fileName = os.path.basename(file_path)
            logging.info(
                f"Uploading {fileName} to minio bucket {self.bucketName} ...")
            client.fput_object(
                self.bucketName, fileName, file_path)
            logging.info("finished")
        except S3Error as exc:
            logging.info("A error occurred while uploading!", exc)


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO,
                        format='%(asctime)s - %(message)s',
                        datefmt='%Y-%m-%d %H:%M:%S')
    if len(sys.argv) == 1:
        logging.info("""
        #################################
        ## File watcher minio uploader ##
        #################################

        #################################
        Watches a folder and uploads all
        all new or changed files with 
        minio to a S3 bucket
        #################################
        
        Call:
        python3 FileWatcher.py [MC_ENDPOINT] [MC_ACCESS_KEY] [MC_SECRET_KEY] [MC_BUCKET_NAME] [PATH_TO_FOLDER]
        """)
        exit(0)

    if len(sys.argv) != 6:
        logging.error("""
        !!! Number of arguments is wrong !!!
        Call it with: pyhton3 FileWatcher.py [MC_ENDPOINT] [MC_ACCESS_KEY] [MC_SECRET_KEY] [MC_BUCKET_NAME] [PATH_TO_FOLDER]
        """)
        exit(1)

    minioBucketName = sys.argv[4]
    path = sys.argv[5]

    client = Minio(
        sys.argv[1],
        access_key=sys.argv[2],
        secret_key=sys.argv[3],
        secure = True,
    )

    observer = Observer()
    observer.schedule(FileWatcher(client, minioBucketName),
                        path, recursive=True)
    observer.start()
    logging.info(f"Started watching {path} for created and changed files")

    try:
        while True:
            time.sleep(1)
    finally:
        observer.stop()
        observer.join()