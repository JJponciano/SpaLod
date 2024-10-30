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
from rdflib import Graph, URIRef, Literal, Namespace, XSD
from ..utils.sparql_helpers import process_owl_file
from  .sparql_query import SparqlQueryAPIView
from ..serializers import SparqlQuerySerializer

class GeoGetAllView(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetAllView :::::::")

        sparql_query = """     
            PREFIX geo: <http://www.opengis.net/ont/geosparql#> 
            SELECT ?catalog ?feature ?wkt 
            WHERE { 
                ?feature a geo:Feature ; geo:hasGeometry ?geom . 
                ?geom geo:asWKT ?wkt . 
            }
        """

        # Add the SPARQL query to request data for use in SparqlQueryAPIView
        request.data['query'] = sparql_query

        # Instantiate SparqlQueryAPIView and directly call its `post` method
        sparql_view = SparqlQueryAPIView()
        return sparql_view.post(request, *args, **kwargs)
        

class GeoGetCatalog(APIView):

    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetCatalog :::::::")
        id = request.query_params.get('id')
        sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod")
        self.spalod = Namespace("http://spalod/")

        graph_general = self.spalod.General

        sparql.setQuery(f"""     
            SELECT ?key ?value
            WHERE {{
                GRAPH <{graph_general}> {{ 
                    <{id}> ?key ?value .
                }}
            }}
        """)
        sparql.setReturnFormat(JSON)
        
        try:
            results = sparql.query().convert()
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)

class GeoGetFeature(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetFeature :::::::")
        id = request.query_params.get('id')
        sparql_query=f"""     
            select ?key ?value
            where {{
                <{id}> ?key ?value .
            }}
        """
        # Add the SPARQL query to request data for use in SparqlQueryAPIView
        request.data['query'] = sparql_query

        # Instantiate SparqlQueryAPIView and directly call its `post` method
        sparql_view = SparqlQueryAPIView()
        return sparql_view.post(request, *args, **kwargs)
