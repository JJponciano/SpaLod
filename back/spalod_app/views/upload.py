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
from rdflib.namespace import Namespace

from ..serializers import UploadedFileSerializer
from ..utils.ontology_processing import OntologyProcessor
from ..utils.sparql_helpers import add_ontology_to_graphdb



class FileUploadView(APIView):
    def post(self, request, *args, **kwargs):
        print("::::::: FileUploadView :::::::")
        file = request.FILES.get('file')  # Access the file
        metadata = request.data.get('metadata')  # Access the metadata as JSON

        if not file or not metadata:
            return Response({'error': 'File and metadata are required.'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            metadata = json.loads(metadata)
        except ValueError:
            return Response({'error': 'Invalid JSON for metadata.'}, status=status.HTTP_400_BAD_REQUEST)
        
        
        file_uuid = str(uuid.uuid4())

        upload_dir = os.path.join(settings.MEDIA_ROOT, 'uploads', file_uuid)
        os.makedirs(upload_dir, exist_ok=True)
       
        # TODO CHECK
        file_path = os.path.join(upload_dir, file.name)
        with open(file_path, 'wb+') as temp_file:
            for chunk in file.chunks():
                temp_file.write(chunk)
        # if file.name.endswith('las') or file.name.endswith('laz'):
            
        #     return Response(
        #         {
        #             'message': 'File uploaded and pointcloud processed successfully.',
        #             'uuid': f"{file.pointcloud_id}{file.pointcloud_uuid}"
        #         }, 
        #         status=status.HTTP_201_CREATED
        #     )
        ## UPLOAD GIS FILE 

        try:

            ontology_file_path = os.path.join(upload_dir, f'{file_uuid}_ontology.owl')
            map_file_path = os.path.join(upload_dir, f'{file_uuid}_map.html')
            
            try:
                processor = OntologyProcessor()
                ## POINT CLOUD 
                if file.name.endswith('las') or file.name.endswith('laz'):
                    processor.add_pointcloud(file_path,file.pointcloud_id,file.pointcloud_uuid)
                else:
                    processor.process(file_path)
                processor.save(ontology_file_path,map_file_path)
            except Exception as e:
                return Response({'error': f'Ontology processing failed: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

            ontology_url = f'/media/uploads/{file_uuid}/{file_uuid}_ontology.owl'
            map_url = f'/media/uploads/{file_uuid}/{file_uuid}_map.html'

            try:
                add_ontology_to_graphdb(ontology_file_path, file_uuid, ontology_url, map_url,metadata)
            except Exception as e:
                return Response({'error': f'Failed to add ontology to GraphDB: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
            data = {
                'uuid': file_uuid,
                'file_path': ontology_file_path,
                'map_path': map_file_path,
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
                'map_url': map_url
            }, status=status.HTTP_201_CREATED)

        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)