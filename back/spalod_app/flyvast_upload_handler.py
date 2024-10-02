from django.core.files.uploadhandler import FileUploadHandler, StopFutureHandlers
from django.core.files.uploadedfile import UploadedFile
from .flyvast.pointcloud import create_flyvast_pointcloud
from io import BytesIO
import zipfile
import requests
import gzip

MAX_CHUNK_SIZE = 50 * 1024 * 1024

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
            
            raise StopFutureHandlers()
    
    def receive_data_chunk(self, raw_data, start):
        if self.activated:
            self.chunk.write(raw_data)
            
            if self.chunk.getbuffer().nbytes > MAX_CHUNK_SIZE:
                self.process_chunk()
            
        else:
            return raw_data
        
    def process_chunk(self):
        chunk_zipped = gzip.compress(self.chunk.getbuffer())
        
        percentage = min(self.nb_chunk * MAX_CHUNK_SIZE / self.file_size, 1) * 100
        upload_url = self.flyvast_pointcloud.get("upload_url")
        size = len(chunk_zipped)
        prefix = f"{self.nb_chunk}".zfill(10)
        chunk_name = f"{prefix}-{self.file_name}"
        
        url = f"{upload_url}&name={chunk_name}&bytes={self.file_size}&percentage={percentage}&size={size}"
        requests.post(url, chunk_zipped)
            
        self.chunk = BytesIO()
        self.nb_chunk += 1

    def file_complete(self, file_size):
        if not self.activated:
            return
        
        if self.chunk.getbuffer().nbytes > 0:
            self.process_chunk()
        
        print("::::::: PointcloudUploaded :::::::")
        
        requests.get(self.flyvast_pointcloud.get("treatment_url"))
        
        return FlyvastUploadedFile(
            name=self.file_name,
            pointcloud_id=self.flyvast_pointcloud.get("pointcloud_id"),
            pointcloud_uuid=self.flyvast_pointcloud.get("pointcloud_uuid")
        )
    
class FlyvastUploadedFile(UploadedFile):
    
    def __init__(self, name, pointcloud_id, pointcloud_uuid):
        super().__init__(None, name, None, None, None, None)
        
        self.pointcloud_id = pointcloud_id
        self.pointcloud_uuid = pointcloud_uuid