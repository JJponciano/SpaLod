from django.core.files.uploadhandler import FileUploadHandler, StopFutureHandlers
from django.core.files.uploadedfile import TemporaryUploadedFile
from .pointcloud import create_flyvast_pointcloud
import os

MAX_CHUNK_SIZE = 50 * 1024 * 1024
task_pool = {}

class FlyvastUploadHandler(FileUploadHandler):
    def handle_raw_input( self, input_data, META, content_length, boundary, encoding=None):
        self.file_size = content_length
        pass
    
    def new_file(self, *args, **kwargs):
        super().new_file(*args, **kwargs)
        
        self.flyvast_upload = self.file_name.endswith(".las") or self.file_name.endswith(".laz") or self.file_name.endswith('.xyz')
        
        if self.flyvast_upload:
            print("::::::: CreatePointcloud :::::::")
            self.flyvast_pointcloud = create_flyvast_pointcloud(self.file_name, self.file_size)
            print("::::::: create_flyvast_pointcloud :::::::")

            self.file = FlyvastUploadedFile(
                self.file_name,
                self.content_type,
                0,
                self.charset,
                self.content_type_extra,
                self.flyvast_pointcloud,
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
        self.file.write(raw_data)


    def file_complete(self, file_size):
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
    
class FlyvastUploadedFile(TemporaryUploadedFile):
    
    def __init__(self, name, content_type, size, charset, content_type_extra=None, flyvast_pointcloud=None):
        super().__init__(name, content_type, size, charset, content_type_extra)
        
        self.flyvast_pointcloud = flyvast_pointcloud