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
from ..utils.GraphDBManager import GraphDBManager
 
class SparqlQueryAPIView(APIView):

    def post(self, request, *args, **kwargs):
        print("::::::: SparqlQueryAPIView :::::::")
        serializer = SparqlQuerySerializer(data=request.data)
        if serializer.is_valid():
            sparql_query = serializer.validated_data['query']
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            
            try:
                results = graph_manager.query_graphdb(sparql_query)
                return Response(results, status=status.HTTP_200_OK)
            except Exception as e:
                return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)


        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
