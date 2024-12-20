from rest_framework.views import APIView
from rest_framework.response import Response
from SPARQLWrapper import SPARQLWrapper, JSON
from rest_framework import status
from rdflib import Graph, URIRef, Literal, Namespace, XSD

from ..serializers import SparqlQuerySerializer
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
import os
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.conf import settings
from ..utils.sparql_helpers import process_owl_file

class SparqlQueryAPIView(APIView):

    def post(self, request, *args, **kwargs):
        print("::::::: SparqlQueryAPIView :::::::")
        serializer = SparqlQuerySerializer(data=request.data)
        if serializer.is_valid():
            sparql_query = serializer.validated_data['query']
            try:
                catalog_id = serializer.validated_data['catalog_id']
            except:
                catalog_id = None
            # graph_uri = serializer.validated_data.get('graph')
            # Set up the SPARQL endpoint to query the graph and get the OWL file URI
            sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod")
            self.spalod = Namespace("http://spalod/")
            self.geo = Namespace("http://www.opengis.net/ont/geosparql#")
            self.ex =  Namespace("https://registry.gdi-de.org/id/hamburg/")
            self.gdi = Namespace("https://registry.gdi-de.org/")
            self.flyvast = Namespace("https://flyvast.com/")
            self.spalod = Namespace("http://spalod/")
            prefixes = f"""
                PREFIX spalod: <{self.spalod}>
                PREFIX ex: <{self.ex}>
                PREFIX geo: <{self.geo}>
                PREFIX gdi: <{self.gdi}>
                PREFIX flyvast: <{self.flyvast}>
            """
            # Combine the prefixes with your query
            sparql_query = prefixes + sparql_query
            graph_general = self.spalod.General

            if catalog_id:
                print(f" Search only in {catalog_id}")
                sparql.setQuery(f"""
                PREFIX spalod: <http://spalod/>
                SELECT ?catalog ?owl_url WHERE {{ 
                    GRAPH <{graph_general}> {{ 
                        <{catalog_id}> spalod:hasOWL ?owl_url .
                    }} 
                }}
                """) 
            else:
                sparql.setQuery(f"""
                PREFIX spalod: <http://spalod/>
                SELECT ?catalog ?owl_url WHERE {{ 
                    GRAPH <{graph_general}> {{ 
                        ?catalog spalod:hasOWL ?owl_url .
                    }} 
                }}
                """) 
            
            sparql.setReturnFormat(JSON)

            try:
                # Execute the SPARQL query to get the OWL URLs
                result = sparql.query().convert()
                # Process the SPARQLResult object
                
                if result['results']['bindings']:
                    try:
                        results = []
                        # Extract all OWL URLs from the result
                        for binding in result['results']['bindings']:
                            owl_url = binding['owl_url']['value']
                            results.append(process_owl_file(owl_url, sparql_query))
                        flattened_array = [item for sublist in results for item in sublist]

                        return Response(flattened_array, status=status.HTTP_200_OK)

                    except Exception as e:
                        return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)

                else:
                    return Response({'error': 'No OWL files found for the given graph.'}, status=status.HTTP_404_NOT_FOUND)

            except Exception as e:
                return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)


        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
