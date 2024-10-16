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


class GeoGetAllView(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: PropertiesQueryView :::::::")
        sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod")
        sparql.setQuery("""     
            PREFIX geo: <http://www.opengis.net/ont/geosparql#> 
            SELECT ?feature ?wkt 
            WHERE { 
                ?feature a geo:Feature ; geo:hasGeometry ?geom . 
                ?geom geo:asWKT ?wkt . 
            }
        """)
        sparql.setReturnFormat(JSON)
        
        try:
            results = sparql.query().convert()
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)


class GeoGetFeature(APIView):
    def get(self, request, *args, **kwargs):
        id = request.query_params.get('id')
        sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod")
        sparql.setQuery(f"""     
            select ?key ?value
            where {{
                <{id}> ?key ?value .
            }}
        """)
        sparql.setReturnFormat(JSON)
        
        try:
            results = sparql.query().convert()
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
