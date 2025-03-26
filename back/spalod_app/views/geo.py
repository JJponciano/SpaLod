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
import re

class GeoGetAllCatalogsView(APIView):
    def get(self, request, *args, **kwargs):

        print("::::::: GeoGetAllCatalogsView :::::::")
        user_id = request.user.id
        graph_manager = GraphDBManager(user_id)
        sparql_query="""
            SELECT ?catalog ?label
            WHERE { 
                ?catalog a dcat:Catalog . 
                OPTIONAL { ?catalog rdfs:label ?label  } 
            }
        """
        try:
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
    
class GeoGetDatasetOfCatalogView(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetDatasetOfCatalogView :::::::")
        catalog_id = request.query_params.get('catalog_id')
        user_id = request.user.id
        graph_manager = GraphDBManager(user_id)
        sparql_query=f"""
            SELECT ?dataset ?label
            WHERE {{ 
                <{catalog_id}>  dcat:dataset ?dataset.
                OPTIONAL {{ ?dataset rdfs:label ?label }} 
            }}
        """
        try:
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
class GeoGetAllFeaturesOfDatasetView(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetAllFeaturesOfDatasetView :::::::")
        dataset_id = request.query_params.get('dataset_id')
        user_id = request.user.id
        graph_manager = GraphDBManager(user_id)
        sparql_query=f"""
            SELECT ?feature ?label
            WHERE {{ 
                <{dataset_id}> geosparql:hasFeatureCollection ?fc.
                ?fc  geosparql:hasFeature ?feature . 
                OPTIONAL {{ ?feature rdfs:label ?label  }} 
            }}
        """
        try:
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        


# class GeoGetAllFeaturesOfCatalogView(APIView):
#     def get(self, request, *args, **kwargs):
#         print("::::::: GeoGeGeoWKTbyCatalogtAllView :::::::")
#         catalog_id = request.query_params.get('catalog_id')
        
#         sparql_query = """     
#             PREFIX geosparql: <http://www.opengis.net/ont/geosparql#> 
#             PREFIX spalod: <http://spalod/> 
#             SELECT ?feature
#             WHERE { 
#                 ?feature a geosparql:Feature . 
#             }
#         """

#         # Add the SPARQL query to request data for use in SparqlQueryAPIView
#         request.data['query'] = sparql_query
#         request.data['catalog_id'] = catalog_id

#         # Instantiate SparqlQueryAPIView and directly call its `post` method
#         sparql_view = SparqlQueryAPIView()
#         return sparql_view.post(request, *args, **kwargs)
        

class GeoGetCatalog(APIView):

    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetCatalog :::::::")
        id = request.query_params.get('id')
        return Response({'error':"Not yet implemented"}, status=status.HTTP_501_NOT_IMPLEMENTED)

        # sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod")
        # self.spalod = Namespace("http://spalod/")

        # graph_general = self.spalod.General

        # sparql.setQuery(f"""     
        #     SELECT ?key ?value
        #     WHERE {{
        #         GRAPH <{graph_general}> {{ 
        #             <{id}> ?key ?value .
        #         }}
        #     }}
        # """)
        # sparql.setReturnFormat(JSON)
        
        # try:
        #     results = sparql.query().convert()
        #     return Response(results, status=status.HTTP_200_OK)
        # except Exception as e:
        #     return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)

class GeoDatasetWKT(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoWKT :::::::")
        dataset_id = request.query_params.get('dataset_id')
        sparql_query = f"""     
             SELECT ?feature ?label ?wkt WHERE {{
                <{dataset_id}>  geosparql:hasFeatureCollection ?fc. 
                ?fc  geosparql:hasFeature ?feature .
                ?feature geosparql:hasGeometry ?geom . 
                ?geom geosparql:asWKT ?wkt . 
                OPTIONAL {{ ?feature rdfs:label ?label  }} 
            }}
            
        """
        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        
class GeoCatalogWKT(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoWKT :::::::")
        catalog_id = request.query_params.get('catalog_id')
        sparql_query = f"""     
             SELECT ?feature ?label ?wkt ?dataset WHERE {{
                <{catalog_id}> dcat:dataset ?dataset. 
                ?dataset geosparql:hasFeatureCollection ?fc. 
                ?fc  geosparql:hasFeature ?feature .
                ?feature geosparql:hasGeometry ?geom . 
                ?geom geosparql:asWKT ?wkt . 
                OPTIONAL {{ ?feature rdfs:label ?label  }} 
            }}
            
        """
        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
    
class GeoGetFeatureWKT(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetFeature :::::::")
        id = request.query_params.get('id')
        sparql_query = f"""     
             SELECT ?wkt WHERE {{
                <{id}> geosparql:hasGeometry ?geom . 
                ?geom geosparql:asWKT ?wkt . 
            }}
        """
        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        

        # user_id = request.user.id
        # graph_manager = GraphDBManager(user_id)
        # # Add the SPARQL query to request data for use in SparqlQueryAPIView
        # request.data['query'] = sparql_query
        # request.data['catalog_id'] = catalog_id

        # # Instantiate SparqlQueryAPIView and directly call its `post` method
        # sparql_view = SparqlQueryAPIView()
        # return sparql_view.post(request, *args, **kwargs)
    
class GeoGetFeature(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoGetFeature :::::::")
        
        id = request.query_params.get('id')
        
        sparql_query = f"""     
             SELECT ?key ?value ?label WHERE {{
                <{id}> ?key ?value . 
                OPTIONAL {{ <{id}> rdfs:label ?label  }} 

            }}
        """
        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            results = graph_manager.query_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        
class GeoUpdateFeatureItem(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoUpdateFeatureItem :::::::")
        
        id = request.query_params.get('id')
        key = request.query_params.get('key')
        value = request.query_params.get('value')
        
        sparql_query = f"""     
            DELETE {{
                <{id}> <{key}> ?value .
            }}
            INSERT {{
                <{id}> <{key}> "{value}" .
            }}
            WHERE {{
                <{id}> <{key}> ?value .
            }}
        """
        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            results = graph_manager.update_graphdb(sparql_query)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)

class GeoInsertFeatureItem(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoInsertFeatureItem :::::::")
        
        id = request.query_params.get('id')
        key = request.query_params.get('key')
        value = request.query_params.get('value')
        
        try:
            user_id = request.user.id
            graph_manager = GraphDBManager(user_id)
            results = graph_manager.insert_graphdb(id, key, value)
            return Response(results, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
        
    
# class GeoGetFeature_graphdb_only(APIView):
#     def get(self, request, *args, **kwargs):
#         print("::::::: GeoGetFeature :::::::")
#         id = request.query_params.get('id')
#         catalog_id = request.query_params.get('catalog_id')

#         if catalog_id:
#             # If catalog_id is provided, query within the specific graph.
#             sparql_query = f"""     
#                 select ?key ?value
#                 where {{
#                     GRAPH <{catalog_id}> {{ 
#                         <{id}> ?key ?value .
#                     }}
#                 }}
#             """
#         else:
#             # If catalog_id is not provided, query across all graphs.
#             sparql_query = f"""     
#                 select ?key ?value
#                 where {{
#                     <{id}> ?key ?value .
#                 }}
#             """

#         sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod")
#         self.spalod = Namespace("http://spalod/")
#         sparql.setQuery(sparql_query)
#         sparql.setReturnFormat(JSON)
        
#         try:
#             results = sparql.query().convert()
#             print(results)
#             return Response(results, status=status.HTTP_200_OK)
#         except Exception as e:
#             return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
class GeoRemoveID(APIView):
    def get(self, request, *args, **kwargs):
        print("::::::: GeoRemoveID :::::::")
        id = request.query_params.get('id')
        user_id = request.user.id
        graph_manager = GraphDBManager(user_id)
        try:
            response=graph_manager.delete_all(id)
            print(response)
            return Response({'message': f'Elements with ID {id} has been successfully deleted.'}, status=status.HTTP_200_OK)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)
