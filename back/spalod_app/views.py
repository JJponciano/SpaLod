from rest_framework.views import APIView
from rest_framework.response import Response
from SPARQLWrapper import SPARQLWrapper, JSON, POST, URLENCODED

from rest_framework import status
from .serializers import SparqlQuerySerializer ,UploadedFileSerializer
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
import json
import json
from datetime import datetime

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
        # You need to make sure that the request contains both the file and metadata
        file = request.FILES.get('file')  # Access the file
        metadata = request.data.get('metadata')  # Access the metadata as JSON

        if not file or not metadata:
            return Response({'error': 'File and metadata are required.'}, status=status.HTTP_400_BAD_REQUEST)

        # Parse the metadata to ensure it's valid JSON
        try:
            metadata = json.loads(metadata)
        except ValueError as e:
            return Response({'error': 'Invalid JSON for metadata.'}, status=status.HTTP_400_BAD_REQUEST)

        # Create the serializer
        data = {'file': file, 'metadata': metadata}
        serializer = UploadedFileSerializer(data=data)

        if serializer.is_valid():
            serializer.save()  # Save the file and metadata to the database
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class SparqlQueryAPIView(APIView):
    def post(self, request, *args, **kwargs):
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

