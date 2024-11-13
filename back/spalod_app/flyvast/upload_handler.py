from django.core.files.uploadhandler import FileUploadHandler, StopFutureHandlers
from django.core.files.uploadedfile import UploadedFile, TemporaryUploadedFile
from .pointcloud import create_flyvast_pointcloud
from io import BytesIO
import requests
import gzip
import threading
import os

MAX_CHUNK_SIZE = 50 * 1024 * 1024
task_pool = {}

class FlyvastUploadHandler(FileUploadHandler):
    def handle_raw_input( self, input_data, META, content_length, boundary, encoding=None):
        self.file_size = content_length
        pass
    
    def new_file(self, *args, **kwargs):
        super().new_file(*args, **kwargs)
        
        self.flyvast_upload = self.file_name.endswith(".las") or self.file_name.endswith(".laz")
        
        if self.flyvast_upload:
            print("::::::: CreatePointcloud :::::::")
            self.flyvast_pointcloud = create_flyvast_pointcloud(self.file_name, self.file_size)
            self.chunk = BytesIO()
            self.nb_chunk = 0
            
            task_pool[self.flyvast_pointcloud["pointcloud_id"]] = []
            
            self.file = FlyvastUploadedFile(
                self.file_name,
                self.content_type,
                0,
                self.charset,
                self.content_type_extra,
                self.flyvast_pointcloud["pointcloud_id"],
                self.flyvast_pointcloud["pointcloud_uuid"]
            )
        else:
            self.file = TemporaryUploadedFile(
                self.file_name,
                self.content_type,
                0,
                self.charset,
                self.content_type_extra
            )
            
        raise StopFutureHandlers()
    
    def receive_data_chunk(self, raw_data, start):
        if self.flyvast_upload:
            self.chunk.write(raw_data)
            
            if self.chunk.getbuffer().nbytes > MAX_CHUNK_SIZE:
                self.process_chunk()
            
        self.file.write(raw_data)
        
    def process_chunk(self):
        # t = threading.Thread(
        #     target=send_to_flyvast,
        #     args=[
        #         self.flyvast_pointcloud["upload_url"],
        #         self.chunk.getbuffer(),
        #         self.nb_chunk,
        #         self.file_size,
        #         self.file_name
        #     ],
        #     daemon=True,
        # )
        # t.start()
        
        # task_pool[self.flyvast_pointcloud["pointcloud_id"]].append(t)
        
        send_to_flyvast(
            self.flyvast_pointcloud["upload_url"],
            self.chunk.getbuffer(),
            self.nb_chunk,
            self.file_size,
            self.file_name
        )
            
        self.chunk = BytesIO()
        self.nb_chunk += 1

    def file_complete(self, file_size):
        if self.flyvast_upload:
        
            if self.chunk.getbuffer().nbytes > 0:
                self.process_chunk()
            
            print("::::::: PointcloudUploaded :::::::")
            
            t = threading.Thread(
                target=request_flyvast_treatment,
                args=[
                    self.flyvast_pointcloud["pointcloud_id"],
                    self.flyvast_pointcloud["treatment_url"]
                ]
            )
            t.start()
            
        self.file.seek(0)
        self.file.size = file_size
        
        return self.file
    
    def upload_interrupted(self):
        if hasattr(self, "file"):
            temp_location = self.file.temporary_file_path()
            try:
                self.file.close()
                os.remove(temp_location)
            except FileNotFoundError:
                pass
        
def send_to_flyvast(upload_url, buffer, index_chunk, file_size, file_name):
    chunk_zipped = gzip.compress(buffer)
        
    percentage = min(index_chunk * MAX_CHUNK_SIZE / file_size, 1) * 100
    size = len(chunk_zipped)
    prefix = f"{index_chunk}".zfill(10)
    chunk_name = f"{prefix}-{file_name}"
    
    url = f"{upload_url}&name={chunk_name}&bytes={file_size}&percentage={percentage}&size={size}"
    requests.post(url, chunk_zipped)
    
def request_flyvast_treatment(pointcloud_id, treatment_url):
    for t in task_pool[pointcloud_id]:
        t.join()
    
    del task_pool[pointcloud_id]
    
    requests.get(treatment_url)
    
    
class FlyvastUploadedFile(TemporaryUploadedFile):
    
    def __init__(self, name, content_type, size, charset, content_type_extra=None, pointcloud_id=None, pointcloud_uuid=None):
        super().__init__(name, content_type, size, charset, content_type_extra)
        
        self.pointcloud_id = pointcloud_id
        self.pointcloud_uuid = pointcloud_uuid