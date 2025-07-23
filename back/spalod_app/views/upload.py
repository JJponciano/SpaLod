from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
import os
import json
import uuid
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.conf import settings
from rdflib.namespace import Namespace # type: ignore
import threading
from io import BytesIO
import requests
import gzip
import json, re, uuid
from rdflib import URIRef, Literal, RDF

from ..serializers import UploadedFileSerializer
from ..utils.ontology_processor import OntologyProcessor
from ..utils.GraphDBManager import add_pointcloud_to_dataset,add_dcterms_metadata_to_dataset,add_ontology_to_graphdb,process_owl_file,delete_ontology_entry,GraphDBManager,NS,initialize_dataset_structure,create_feature_with_geometry,get_or_create_feature_collection_uri

MAX_CHUNK_SIZE = 50 * 1024 * 1024 

class FileUploadView(APIView):
    def post(self, request, *args, **kwargs):
        print("::::::: FileUploadView :::::::")
        file = request.FILES.get('file')  # Access the file
        metadata = request.data.get('metadata')  # Access the metadata as JSON
        user_id = request.user.id
        print(f"Uploading file for User ID: {user_id}")
        if not file or not metadata:
            return Response({'error': f'File and metadata are required: file {file} ; metadata {metadata}'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            metadata = json.loads(metadata)
        except ValueError:
            return Response({'error': 'Invalid JSON for metadata.'}, status=status.HTTP_400_BAD_REQUEST)
        
        
        file_uuid = str(uuid.uuid4())

        upload_dir = os.path.join(settings.MEDIA_ROOT, 'uploads', file_uuid)
        os.makedirs(upload_dir, exist_ok=True)
       
        file_path = file.temporary_file_path()
         # Extract the original file extension
        file_extension = os.path.splitext(file.name)[1] 
        try:

            ontology_file_path = os.path.join(upload_dir, f'{file_uuid}_ontology.owl')
            original_file_path = os.path.join(upload_dir, f'{file_uuid}{file_extension}')

            ontology_url = f'/media/uploads/{file_uuid}/{file_uuid}_ontology.owl'
            original_url = f'/media/uploads/{file_uuid}/{file_uuid}{file_extension}'
            
            # Save the file to the constructed path
            with open(original_file_path, 'wb') as destination:
                for chunk in file.chunks():
                    destination.write(chunk)
            try:
                print("[INFO] Read Metadata")
                catalog_name = self.metadata.get("catalog")
                dataset_name = self.metadata.get("title")
                # Normalize catalog and dataset names to make valid URIs (replace spaces, dots, dashes)
                catalog_name = re.sub(r"[ .-]", "_", catalog_name)
                dataset_name = re.sub(r"[ .-]", "_", dataset_name)
                catalog_uri, dataset_uri = initialize_dataset_structure(user_id,catalog_name,dataset_name)
                triples_added = add_dcterms_metadata_to_dataset(user_id,dataset_uri,metadata)
                print(f"✅ Added {len(triples_added)} DCTERMS metadata triples.")
                processor = OntologyProcessor(file_uuid, ontology_url, original_url,metadata,user_id)
                ## POINT CLOUD 
                if file_extension.endswith('las') or file_extension.endswith('laz') or file_extension.endswith('xyz') or file_extension.endswith('ply')or file_extension.endswith('pcd'):
                    print("[INFO] Pointcloud detected !")
                    t = threading.Thread(
                        target=send_to_flyvast,
                        args=[file],
                        daemon=True,
                    )
                    t.start()
                    result = add_pointcloud_to_dataset(user_id,  dataset_uri,file_path,original_url,file.flyvast_pointcloud["pointcloud_id"],file.flyvast_pointcloud["pointcloud_uuid"])

                else:
                    print("[INFO] Starting processing file ")
                    processor.process(file_path)
                print("Saving ",ontology_file_path) 
                processor.save(ontology_file_path)
            except Exception as e:
                return Response({'error': f'Ontology processing failed: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

            data = {
                'uuid': file_uuid,
                'owl_path': ontology_file_path,
                'map_path': original_file_path,
                'metadata': metadata
            }
            # Save in Database
            serializer = UploadedFileSerializer(data=data)
            if serializer.is_valid():
                serializer.save()

            return Response({
                'message': 'File uploaded and ontology processed successfully.',
                'uuid': file_uuid,
                'ontology_url': ontology_url,
                'map_url': original_url
            }, status=status.HTTP_201_CREATED)

        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
        
def send_to_flyvast(file):
    with open(file.temporary_file_path(), 'rb') as f:
        
        def read_in_chunks(file_object):
            while True:
                data = file_object.read(MAX_CHUNK_SIZE)
                if not data:
                    break
                yield data
        
        index_chunk = 0
        for chunk in read_in_chunks(f):
            chunk_zipped = gzip.compress(chunk)
        
            percentage = min(index_chunk * MAX_CHUNK_SIZE / file.size, 1) * 100
            size = len(chunk_zipped)
            prefix = f"{index_chunk}".zfill(10)
            chunk_name = f"{prefix}-{file.name}"
            upload_url = file.flyvast_pointcloud["upload_url"]
            
            url = f"{upload_url}&name={chunk_name}&bytes={file.size}&percentage={percentage}&size={size}"
            requests.post(url, chunk_zipped)
            
            index_chunk += 1
            
    requests.get(file.flyvast_pointcloud["treatment_url"])