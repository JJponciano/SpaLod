from rest_framework.views import APIView
from rest_framework.response import Response
from SPARQLWrapper import SPARQLWrapper, JSON
from rest_framework import status
from rdflib import Graph

from ..serializers import SparqlQuerySerializer
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
import os
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.conf import settings

class SparqlQueryAPIView(APIView):

    def post(self, request, *args, **kwargs):
        print("::::::: SparqlQueryAPIView :::::::")

        serializer = SparqlQuerySerializer(data=request.data)
        if serializer.is_valid():
            sparql_query = serializer.validated_data['query']
            graph_uri = serializer.validated_data.get('graph')

            if not graph_uri:
                return Response({'error': 'Graph URI is required.'}, status=status.HTTP_400_BAD_REQUEST)

            # Set up the SPARQL endpoint to query the graph and get the OWL file URI
            sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod")
            sparql.setQuery(f"""
            PREFIX spalod: <http://spalod/>
            SELECT ?owl_url WHERE {{ 
                GRAPH <{graph_uri}> {{ 
                    <{graph_uri}> spalod:hasOWL ?owl_url .
                }} 
            }}
            """)  # Dynamically use the graph URI from the request
            sparql.setReturnFormat(JSON)

            try:
                # Execute the SPARQL query to get the OWL URL
                result = sparql.query().convert()

                if result['results']['bindings']:
                    try:
                        # Extract the OWL URL from the result
                        owl_url = result['results']['bindings'][0]['owl_url']['value']
                        print(f"OWL URL from SPARQL query: {owl_url}")

                        # Strip the MEDIA_URL from the owl_url to get the relative path
                        owl_relative_path = owl_url.replace(settings.MEDIA_URL, '')
                        owl_path = os.path.join(settings.MEDIA_ROOT, owl_relative_path)

                        print(f"Full OWL file path: {owl_path}")

                        if not os.path.exists(owl_path):
                            return Response({'error': 'OWL file not found at the specified path.'}, status=status.HTTP_404_NOT_FOUND)

                        # Load the OWL file using rdflib
                        owl_graph = Graph()
                        try:
                            owl_graph.parse(owl_path, format='turtle')
                            print("OWL file parsed successfully.")
                        except Exception as parse_error:
                            return Response({'error': 'Error parsing the OWL file.'}, status=status.HTTP_400_BAD_REQUEST)

                        # Apply the SPARQL query on the loaded OWL file
                        owl_results = owl_graph.query(sparql_query)

                        # Process the SPARQLResult object
                        results = []
                        for row in owl_results:
                            result_dict = {}
                            for var in row.labels:
                                result_dict[var] = str(row[var])
                            results.append(result_dict)

                        return Response(results, status=status.HTTP_200_OK)

                    except Exception as e:
                        return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)

                else:
                    return Response({'error': 'No OWL file found for the given graph.'}, status=status.HTTP_404_NOT_FOUND)

            except Exception as e:
                return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)

        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
