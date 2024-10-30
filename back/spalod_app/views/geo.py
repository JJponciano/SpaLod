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
from ..utils.sparql_helpers import process_owl_file,delete_ontology_entry
from  .sparql_query import SparqlQueryAPIView
from ..serializers import SparqlQuerySerializer

class GeoGetAllView(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetAllView :::::::")
        sparql_query = """     
            PREFIX geo: <http://www.opengis.net/ont/geosparql#> 
            PREFIX spalod: <http://spalod/> 
            SELECT ?catalog ?feature ?wkt 
            WHERE { 
                ?catalog spalod:hasFeature ?feature . 
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
        
class GeoRemoveCatalog(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoDeleteCatalog :::::::")
        id = request.query_params.get('id')
        sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod/statements")  # GraphDB endpoint for updates
        self.spalod = Namespace("http://spalod/")

        graph_general = self.spalod.General

        sparql.setQuery(f"""
            DELETE WHERE {{
                GRAPH <{graph_general}> {{ 
                    <{id}> ?key ?value .
                }}
            }}
        """)
        # sparql.setMethod(POST)
        # sparql.setReturnFormat(JSON)

        try:
            # Execute the SPARQL update to delete the catalog
            sparql.query()
            return Response({'message': f'Catalog with ID {id} has been successfully deleted.'}, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)


class GeoRemoveFeature(APIView):
    def get(self, request, *args, **kwargs):
            id = request.query_params.get('id')
            print("::::::: GeoRemoveFeature :::::::")
            sparql_delete_query = f"""
                DELETE WHERE {{
                    ?s ?p <{id}> .
                }};
                DELETE WHERE {{
                    <{id}> ?p ?o .
                }}
            """
            # graph_uri = serializer.validated_data.get('graph')
            # Set up the SPARQL endpoint to query the graph and get the OWL file URI
            sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod")
            self.spalod = Namespace("http://spalod/")

            graph_general = self.spalod.General

            sparql.setQuery(f"""
            PREFIX spalod: <http://spalod/>
            SELECT ?catalog ?owl_url WHERE {{ 
                GRAPH <{graph_general}> {{ 
                    ?catalog spalod:hasOWL ?owl_url .
                }} 
            }}
            """) 
            
            sparql.setReturnFormat(JSON)

            # Execute the SPARQL query to get the OWL URLs
            result = sparql.query().convert()
            # Process the SPARQLResult object
            
            if result['results']['bindings']:
                try:
                    # Extract all OWL URLs from the result
                    for binding in result['results']['bindings']:
                        owl_url = binding['owl_url']['value']
                        delete_ontology_entry(owl_url, sparql_delete_query)
                except Exception as e:
                    return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)                    
            print(sparql_delete_query)
            # Now we work on statements
            sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod/statements")  # GraphDB endpoint for updates
            sparql.setQuery(sparql_delete_query)
            try:
                # Execute the SPARQL update to delete the catalog
                sparql.query()
                return Response({'message': f'Feature with ID {id} has been successfully deleted.'}, status=status.HTTP_200_OK)
            except Exception as e:
                return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)  

                