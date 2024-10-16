from rest_framework.views import APIView
from rest_framework.response import Response
from SPARQLWrapper import SPARQLWrapper, JSON
from rest_framework import status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status


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



