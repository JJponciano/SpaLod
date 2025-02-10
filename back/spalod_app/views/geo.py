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
from ..utils.GraphDBManager import process_owl_file,delete_ontology_entry,GraphDBManager
from  .sparql_query import SparqlQueryAPIView
from ..serializers import SparqlQuerySerializer

class GeoGetAllCatalogsView(APIView):
    def get(self, request, *args, **kwargs):

        print("::::::: GeoGetAllCatalogsView :::::::")
        user_id = request.user.id
        graph_manager = GraphDBManager(user_id)
        sparql_query="""
            SELECT ?catalog ?label WHERE { ?catalog a dcat:Catalog . OPTIONAL { ?catalog rdfs:label ?label  } }
        """
        try:
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
    
class GeoGetDatasetOfCatalogView(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGeGeoWKTbyCatalogtAllView :::::::")
        catalog_id = request.query_params.get('catalog_id')
        user_id = request.user.id
        graph_manager = GraphDBManager(user_id)
        sparql_query=f"""
            SELECT ?dataset ?label WHERE {{ spalod:{catalog_id}  dcat:dataset ?dataset. OPTIONAL {{ ?dataset rdfs:label ?label  }} }}
        """
        try:
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        
class GeoGetAllFeaturesOfCatalogView(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGeGeoWKTbyCatalogtAllView :::::::")
        catalog_id = request.query_params.get('catalog_id')
        
        sparql_query = """     
            PREFIX geo: <http://www.opengis.net/ont/geosparql#> 
            PREFIX spalod: <http://spalod/> 
            SELECT ?feature
            WHERE { 
                ?feature a geo:Feature . 
            }
        """

        # Add the SPARQL query to request data for use in SparqlQueryAPIView
        request.data['query'] = sparql_query
        request.data['catalog_id'] = catalog_id

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

class GeoWKT(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGeGeoWKTbyCatalogtAllView :::::::")
        catalog_id = request.query_params.get('catalog_id')

        sparql_query = """     
            PREFIX geo: <http://www.opengis.net/ont/geosparql#> 
            PREFIX spalod: <http://spalod/> 
            SELECT  ?feature ?wkt 
            WHERE { 
                ?feature a geo:Feature ; geo:hasGeometry ?geom . 
                ?geom geo:asWKT ?wkt . 
            }
        """

        # Add the SPARQL query to request data for use in SparqlQueryAPIView
        request.data['query'] = sparql_query
        request.data['catalog_id'] = catalog_id

        # Instantiate SparqlQueryAPIView and directly call its `post` method
        sparql_view = SparqlQueryAPIView()
        return sparql_view.post(request, *args, **kwargs)
    
class GeoGetFeatureWKT(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetFeature :::::::")
        id = request.query_params.get('id')
        catalog_id = request.query_params.get('catalog_id')
        print(f"id: {id}, catalog_id: {catalog_id}")

        sparql_query=f"""     
            select ?key ?value ?wkt
            where {{
                <{id}> a geo:Feature ; geo:hasGeometry ?geom . 
                ?geom geo:asWKT ?wkt . 
            }}
        """
        # Add the SPARQL query to request data for use in SparqlQueryAPIView
        request.data['query'] = sparql_query
        request.data['catalog_id'] = catalog_id

        # Instantiate SparqlQueryAPIView and directly call its `post` method
        sparql_view = SparqlQueryAPIView()
        return sparql_view.post(request, *args, **kwargs)
    
class GeoGetFeature(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetFeature :::::::")
        id = request.query_params.get('id')
        catalog_id = request.query_params.get('catalog_id')
        print(f"id: {id}, catalog_id: {catalog_id}")

        sparql_query=f"""     
            select ?key ?value
            where {{
                <{id}> ?key ?value .
            }}
        """
        # Add the SPARQL query to request data for use in SparqlQueryAPIView
        request.data['query'] = sparql_query
        request.data['catalog_id'] = catalog_id

        # Instantiate SparqlQueryAPIView and directly call its `post` method
        sparql_view = SparqlQueryAPIView()
        return sparql_view.post(request, *args, **kwargs)
    
class GeoGetFeature_graphdb_only(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetFeature :::::::")
        id = request.query_params.get('id')
        catalog_id = request.query_params.get('catalog_id')

        if catalog_id:
            # If catalog_id is provided, query within the specific graph.
            sparql_query = f"""     
                select ?key ?value
                where {{
                    GRAPH <{catalog_id}> {{ 
                        <{id}> ?key ?value .
                    }}
                }}
            """
        else:
            # If catalog_id is not provided, query across all graphs.
            sparql_query = f"""     
                select ?key ?value
                where {{
                    <{id}> ?key ?value .
                }}
            """

        sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod")
        self.spalod = Namespace("http://spalod/")
        sparql.setQuery(sparql_query)
        sparql.setReturnFormat(JSON)
        
        try:
            results = sparql.query().convert()
            print(results)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
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

                