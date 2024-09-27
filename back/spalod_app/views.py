from rest_framework.views import APIView
from rest_framework.response import Response
from SPARQLWrapper import SPARQLWrapper, JSON, POST, URLENCODED
from rest_framework import status
from .serializers import SparqlQuerySerializer ,UploadedFileSerializer
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from datetime import datetime
import os
import json
import uuid
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from .serializers import UploadedFileSerializer
from django.conf import settings
from rdflib.namespace import Namespace
# from .utils.ontology_processing import OntologyProcessor
# from .utils.sparql_helpers import add_ontology_to_graphdb

class UpdateOntologyView(APIView):
    def post(self, request, *args, **kwargs):
        # Extract the mappings from the request body
        mappings = request.data.get("mappings", [])
        
        # SPARQL endpoint for update queries
        sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod/statements")
        sparql.setMethod(POST)

        try:
            # For each mapping, create SPARQL update queries
            for mapping in mappings:
                new_property = mapping["new_property"]
                old_property = mapping["old_property"]
                current_time = datetime.now().isoformat()

                # SPARQL query to insert data
                sparql_query = f"""
                    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                    INSERT DATA {{
                        <{new_property}> <https://schema.org/isSimilarTo> <{old_property}> .
                        <{new_property}> <http://spalod/hasBeenValidated> "{current_time}"^^xsd:dateTime .
                    }}
                """
                sparql.setQuery(sparql_query)

                # Execute the SPARQL query
                sparql.query()

            return Response({"message": "Ontology updated successfully"}, status=status.HTTP_200_OK)

        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_400_BAD_REQUEST)


class PropertiesQueryView(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: PropertiesQueryView :::::::")
        sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod")
        sparql.setQuery("""
            SELECT ?property
            WHERE {
              {
                ?property <http://spalod/hasBeenValidated> "none" .
              }
            }
        """)
        sparql.setReturnFormat(JSON)
        
        try:
            results = sparql.query().convert()
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)



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

        if not file.name.endswith('json'):
            return Response({'error': 'Only GeoJSON files are accepted.'}, status=status.HTTP_400_BAD_REQUEST)

        file_uuid = str(uuid.uuid4())

        upload_dir = os.path.join(settings.MEDIA_ROOT, 'uploads', file_uuid)
        os.makedirs(upload_dir, exist_ok=True)

        file_path = os.path.join(upload_dir, file.name)
        with open(file_path, 'wb+') as temp_file:
            for chunk in file.chunks():
                temp_file.write(chunk)

        try:
            geo = Namespace("http://www.opengis.net/ont/geosparql#")
            ex = Namespace("https://registry.gdi-de.org/id/hamburg/")
            gdi = Namespace("https://registry.gdi-de.org/id/de.bund.balm.radnetz/")

            ontology_file_path = os.path.join(upload_dir, f'{file_uuid}_ontology.owl')
            map_file_path = os.path.join(upload_dir, f'{file_uuid}_map.html')

            try:
                processor = OntologyProcessor(ontology_file_path, geo, ex, gdi)
                processor.process_geojson(file_path, map_file_path)
            except Exception as e:
                return Response({'error': f'Ontology processing failed: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

            ontology_url = f'/media/uploads/{file_uuid}/{file_uuid}_ontology.owl'
            map_url = f'/media/uploads/{file_uuid}/{file_uuid}_map.html'

            try:
                add_ontology_to_graphdb(ontology_file_path, file_uuid, ontology_url, map_url)
            except Exception as e:
                return Response({'error': f'Failed to add ontology to GraphDB: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
            data = {
                'uuid': file_uuid,
                'file_path': ontology_file_path,
                'map_path': map_file_path,
                'metadata': metadata
            }
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

class SparqlQueryAPIView(APIView):
    def post(self, request, *args, **kwargs):
        print("::::::: SparqlQueryAPIView :::::::")

        serializer = SparqlQuerySerializer(data=request.data)
        if serializer.is_valid():
            sparql_query = serializer.validated_data['query']

            # Set up the SPARQL endpoint
            sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod")
            sparql.setQuery(sparql_query)
            sparql.setReturnFormat(JSON)

            try:
                # Execute the SPARQL query and return the result
                results = sparql.query().convert()
                return Response(results, status=status.HTTP_200_OK)
            except Exception as e:
                return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class User(APIView):
    def get(self, request, *args, **kwargs):
        user = request.user
        return Response({
            'username': user.username,
            'email': user.email,
        }, status=status.HTTP_200_OK)
    