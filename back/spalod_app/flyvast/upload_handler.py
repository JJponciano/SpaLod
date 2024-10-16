from django.core.files.uploadhandler import FileUploadHandler, StopFutureHandlers
from django.core.files.uploadedfile import UploadedFile
from .pointcloud import create_flyvast_pointcloud
from io import BytesIO
import requests
import gzip
import threading

MAX_CHUNK_SIZE = 50 * 1024 * 1024
task_pool = {}

class FlyvastUploadHandler(FileUploadHandler):
    def handle_raw_input( self, input_data, META, content_length, boundary, encoding=None):
        self.file_size = content_length
        pass
    
    def new_file(self, *args, **kwargs):
        super().new_file(*args, **kwargs)
        
        self.activated = self.file_name.endswith(".las") or self.file_name.endswith(".laz")
        if self.activated:
            print("::::::: CreatePointcloud :::::::")
            self.flyvast_pointcloud = create_flyvast_pointcloud(self.file_name, self.file_size)
            self.chunk = BytesIO()
            self.nb_chunk = 0
            
            task_pool[self.flyvast_pointcloud["pointcloud_id"]] = []
            
            raise StopFutureHandlers()
    
    def receive_data_chunk(self, raw_data, start):
        if self.activated:
            self.chunk.write(raw_data)
            
            if self.chunk.getbuffer().nbytes > MAX_CHUNK_SIZE:
                self.process_chunk()
            
        else:
            return raw_data
        
    def process_chunk(self):
        t = threading.Thread(
            target=send_to_flyvast,
            args=[
                self.flyvast_pointcloud["upload_url"],
                self.chunk.getbuffer(),
                self.nb_chunk,
                self.file_size,
                self.file_name
            ],
            daemon=True,
        )
        t.start()
        
        task_pool[self.flyvast_pointcloud["pointcloud_id"]].append(t)
            
        self.chunk = BytesIO()
        self.nb_chunk += 1

    def file_complete(self, file_size):
        if not self.activated:
            return
        
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
        
        return FlyvastUploadedFile(
            name=self.file_name,
            pointcloud_id=self.flyvast_pointcloud["pointcloud_id"],
            pointcloud_uuid=self.flyvast_pointcloud["pointcloud_uuid"]
        )
        
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
    
    
class FlyvastUploadedFile(UploadedFile):
    
    def __init__(self, name, pointcloud_id, pointcloud_uuid):
        super().__init__(None, name, None, None, None, None)
        
        self.pointcloud_id = pointcloud_id
        self.pointcloud_uuid = pointcloud_uuid