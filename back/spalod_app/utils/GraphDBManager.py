from SPARQLWrapper import SPARQLWrapper, POST
import rdflib
from rdflib import XSD, Graph
import os
from django.conf import settings
from tqdm import tqdm
from SPARQLWrapper import SPARQLWrapper, JSON, POST, GET,POSTDIRECTLY,SPARQLExceptions
from rdflib.namespace import  RDFS, OWL
import pyproj
import laspy
import pydeck as pdk
from rdflib import URIRef, Literal, Graph, RDF,Namespace
from rdflib.namespace import RDF
from shapely.geometry import Polygon
from shapely.ops import transform
import numpy as np
import json, re, uuid
from ..utils.env import get_env_settings

BATCH_SIZE = 5000  # Adjust batch size to balance speed and memory usage
import time
        # Define namespaces, including standard RDF, RDFS, OWL, etc.
NS = {
            "RDF": Namespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
            "RDFS": Namespace("http://www.w3.org/2000/01/rdf-schema#"),
            "OWL": Namespace("http://www.w3.org/2002/07/owl#"),
            "XSD": Namespace("http://www.w3.org/2001/XMLSchema#"),
            "FOAF": Namespace("http://xmlns.com/foaf/0.1/"),
            "SKOS": Namespace("http://www.w3.org/2004/02/skos/core#"),
            "GEOSPARQL": Namespace("http://www.opengis.net/ont/geosparql#"),
            "SPALOD": Namespace("https://geovast3d.com/ontologies/spalod#"),
            "DCAT": Namespace("http://www.w3.org/ns/dcat#"),
            "DCTERMS": Namespace("http://purl.org/dc/terms/"),
            "GDI": Namespace("https://registry.gdi-de.org/")
        }
def delete_ontology_entry(owl_url, sparql_delete_query):
    """
    Process an OWL file from a given URL, load it into an RDF graph, and apply a SPARQL DELETE query.

    Args:
        owl_url (str): The URL of the OWL file to be processed.
        sparql_delete_query (str): The SPARQL DELETE query to be applied to the OWL file.

    Returns:
        str: A message indicating the success of the operation.

    Raises:
        FileNotFoundError: If the OWL file is not found at the specified path.
        ValueError: If there is an error parsing the OWL file or executing the SPARQL query.
    """
    # Strip the MEDIA_URL from the owl_url to get the relative path
    owl_relative_path = owl_url.replace(settings.MEDIA_URL, '')
    owl_path = os.path.join(settings.MEDIA_ROOT, owl_relative_path)

    if not os.path.exists(owl_path):
        raise FileNotFoundError(f'OWL file not found at the specified path: {owl_path}')

    # Load the OWL file using rdflib
    owl_graph = Graph()
    try:
        owl_graph.parse(owl_path, format='turtle')
    except Exception as parse_error:
        raise ValueError(f'Error parsing the OWL file: {parse_error}')

    # Apply the SPARQL DELETE query on the loaded OWL file
    try:
        owl_graph.update(sparql_delete_query)
    except Exception as query_error:
        raise ValueError(f'Error executing SPARQL DELETE query: {query_error}')

    # Save the updated graph back to the file
    try:
        owl_graph.serialize(destination=owl_path, format='turtle')
    except Exception as save_error:
        raise ValueError(f'Error saving the updated OWL file: {save_error}')

    return f'Ontology entry has been successfully deleted from {owl_path}.'

def process_owl_file(owl_url, sparql_query):
    """
    Process an OWL file from a given URL, load it into an RDF graph, and apply a SPARQL query.

    Args:
        owl_url (str): The URL of the OWL file to be processed.
        sparql_query (str): The SPARQL query to be applied to the OWL file.

    Returns:
        list: A list of dictionaries containing the query results.

    Raises:
        FileNotFoundError: If the OWL file is not found at the specified path.
        ValueError: If there is an error parsing the OWL file or executing the SPARQL query.
    """
    # Strip the MEDIA_URL from the owl_url to get the relative path
    owl_relative_path = owl_url.replace(settings.MEDIA_URL, '')
    owl_path = os.path.join(settings.MEDIA_ROOT, owl_relative_path)

    if not os.path.exists(owl_path):
        raise FileNotFoundError(f'OWL file not found at the specified path: {owl_path}')

    # Load the OWL file using rdflib
    owl_graph = Graph()
    try:
        owl_graph.parse(owl_path, format='turtle')
    except Exception as parse_error:
        raise ValueError(f'Error parsing the OWL file: {parse_error}')

    # Apply the SPARQL query on the loaded OWL file
    try:
        owl_results = owl_graph.query(sparql_query)
    except Exception as query_error:
        raise ValueError(f'Error executing SPARQL query: {query_error}')

    results = []
    for row in owl_results:
        result_dict = {var: str(row[var]) for var in row.labels}
        results.append(result_dict)

    return results


def add_ontology_to_graphdb(ontology_file_path, uuid, ontology_url, map_url, metadata):
    """
    Adds the ontology to GraphDB as a named graph and inserts the properties
    hasHTML, hasOWL, and the metadata associated with the graph.
    """
    sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod/statements")
    sparql.setMethod(POST)
    sparql.setRequestMethod("postdirectly")  # SPARQLWrapper uses this method for SPARQL update requests
    sparql.addCustomHttpHeader("Content-Type", "application/sparql-update")

    graph = rdflib.Graph()
    graph.parse(ontology_file_path, format="turtle")

    graph_iri = f"http://spalod/{uuid}"

    # Add hasHTML and hasOWL properties (as strings)
    sparql_update_query = f"""
    PREFIX ex: <https://registry.gdi-de.org/id/hamburg/>
    INSERT DATA {{
        GRAPH <{graph_iri}> {{
            <{graph_iri}> <http://spalod/hasHTML> "{map_url}"^^xsd:string .
            <{graph_iri}> <http://spalod/hasOWL> "{ontology_url}"^^xsd:string .
        }}
    }}
    """

    # Prepare metadata triples
    metadata_triples = ""
    for key, value in metadata.items():
        metadata_triples += f'<{graph_iri}> <http://spalod/{key}> "{value}"^^xsd:string .\n'

    metadata_query = f"""
    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
    INSERT DATA {{
        GRAPH <{graph_iri}> {{
            {metadata_triples}
        }}
    }}
    """

 
    # Serialize the ontology as plain N-Triples, which can be inserted directly into the SPARQL query
    rdf_data = graph.serialize(format="nt")
    
    # Construct SPARQL query without Turtle prefixes
    try:
        try:
            insert_query = f"""
            INSERT DATA {{
                GRAPH <{graph_iri}> {{
                    {rdf_data}
                }}
            }}
            """
    
            # Debug: Print the query before sending it
            print("SPARQL Query:", insert_query[:200])  # Print query to debug
            sparql.setQuery(insert_query)
            sparql.query()
        except Exception as e:
            print(f"Failed to add ontology to GraphDB: {str(e)}")
            # Split the serialized RDF data into individual triples
            rdf_triples = rdf_data.splitlines()

            # Remove any double periods and ensure each triple ends with a single period
            rdf_triples_cleaned = [
                triple.strip().rstrip('.') + ' .' for triple in rdf_triples[:100]
            ]

            # Join the triples back into a string
            rdf_triples_limited = "\n".join(rdf_triples_cleaned)

            # Construct the SPARQL INSERT DATA query
            insert_query = f"""
                INSERT DATA {{
                    GRAPH <{graph_iri}> {{
                        {rdf_triples_limited}
                    }}
                }}
            """

            sparql.setQuery(insert_query)
            sparql.query()
        # Insert the hasHTML, hasOWL properties
        print("Insert the hasHTML, hasOWL properties")
        sparql.setQuery(sparql_update_query)
        sparql.query()

        # Insert the metadata
        print("Insert the metadata")
        sparql.setQuery(metadata_query)
        sparql.query()
    except Exception as e:
        raise Exception(f"Failed to add ontology to GraphDB: {str(e)}")
    

def validate_geojson_file(file_path):
    """
    Validates that the file is a proper GeoJSON file:
    - Checks the extension (.json or .geojson)
    - Checks that content has 'type': 'FeatureCollection' and a 'features' list

    Args:
        file_path (str): Path to the file to validate.

    Returns:
        dict: {
            'valid': bool,
            'message': str
        }
    """
    valid_extensions = ['.json', '.geojson']
    ext = os.path.splitext(file_path)[1].lower()

    if ext not in valid_extensions:
        return {'valid': False, 'message': f"❌ Invalid file extension '{ext}'. Expected .json or .geojson."}

    try:
        with open(file_path, 'r') as f:
            content = json.load(f)

        if not isinstance(content, dict):
            return {'valid': False, 'message': "❌ File content is not a valid JSON object."}

        if content.get("type") != "FeatureCollection":
            return {'valid': False, 'message': "❌ JSON is not a valid GeoJSON: missing 'type': 'FeatureCollection'."}

        if not isinstance(content.get("features"), list):
            return {'valid': False, 'message': "❌ JSON is not a valid GeoJSON: 'features' field is missing or not a list."}

        return {'valid': True, 'message': "✅ Valid GeoJSON file."}

    except Exception as e:
        return {'valid': False, 'message': f"❌ Error reading file: {str(e)}"}
    
class GraphDBManager:
    def __init__(self, user_id):
        """Initialize GraphDBManager with a user-specific graph if user_id is provided."""
        self.sparql = SPARQLWrapper(settings.GRAPH_DB)  # Query endpoint
        self.sparql_statements = SPARQLWrapper(settings.GRAPH_DB_STATEMENTS)  # Update endpoint
        


        # Auto-generate SPARQL PREFIX declarations
        self.prefixes = "\n".join([
            f"PREFIX {key.lower()}: <{uri}>"
            for key, uri in NS.items()
        ]) + "\n"
        # Set the graph IRI only if user_id is provided
        self.graph_iri = f"https://geovast3d.com/ontologies/spalod#graph_{user_id}" if user_id is not None else None

    def upload_to_graphdb(self, triples):
        """Uploads triples to a user-specific named graph in GraphDB."""
        
        if self.graph_iri is None:
            raise ValueError("User ID is required to determine the target graph.")

        # Prepare SPARQL Update query
        update_query = f"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n INSERT DATA {{ GRAPH <{self.graph_iri}> {{"
        for s, p, o in triples:
            if isinstance(o, URIRef):
                update_query += f"<{s}> <{p}> <{o}> . "
            else:
                update_query += f"<{s}> <{p}> \"{o}\" . "
        update_query += "}}}"

        # Set up SPARQLWrapper for updates
        self.sparql_statements.setMethod(POST)
        self.sparql_statements.setQuery(update_query.encode("utf-8"))
        self.sparql_statements.setReturnFormat(JSON)

        try:
            self.sparql_statements.query()
        except Exception as e:
            print(f"SPARQL update failed: {e}")
            raise

    def query_graphdb_depreciated(self, select_query):
        """Executes a SPARQL SELECT query only within the user's named graph (if user_id is set)."""
        
        if self.graph_iri is not None:
            select_query=self.prefixes+f"""
            SELECT * WHERE {{
                GRAPH <{self.graph_iri}> {{
                    {select_query}
                }}
            }}
            """

        self.sparql.setMethod(GET)
        self.sparql.setQuery(select_query)
        self.sparql.setReturnFormat(JSON)

        try:
            response = self.sparql.query().convert()
            return response.get("results", {}).get("bindings", [])
        except Exception as e:
            print(f"SPARQL SELECT query failed: {e}")
            print("QUERY:\n")
            print(select_query)
            raise
    def query_graphdb(self, select_query):
        """Executes a SPARQL SELECT or ASK query within the user's named graph (if user_id is set)."""
        
        # Normalize query (strip leading/trailing spaces and lowercase the start)
        query_type = select_query.strip().lower().split()[0]

        if self.graph_iri is not None and query_type not in ["ask"]:
            ## Only wrap if it's a raw pattern (e.g., `?s ?p ?o`) — not a full ASK/SELECT
        
            select_query = f"""
            SELECT * WHERE {{
                GRAPH <{self.graph_iri}> {{
                    {select_query}
                }}
            }}
            """

        # Add prefixes
        select_query = self.prefixes + select_query

        self.sparql.setMethod(GET)
        self.sparql.setQuery(select_query)
        self.sparql.setReturnFormat(JSON)

        try:
            response = self.sparql.query().convert()
            return response.get("results", {}).get("bindings", []) if query_type == "select" else response
        except Exception as e:
            print(f"SPARQL SELECT/ASK query failed: {e}")
            print("QUERY:\n", select_query)
            raise
    
    def upload_to_graphdb(self, triples):
        """Uploads RDFLib triples to GraphDB using INSERT DATA with SPARQL-safe serialization."""
        
        if self.graph_iri is None:
            raise ValueError("User ID is required to determine the target graph.")

        # Build a temporary RDFLib Graph to serialize safely
        g = Graph()
        for s, p, o in triples:
            g.add((s, p, o))

        # Serialize in N-Triples format (SPARQL safe) inside the named graph
        nt_data = g.serialize(format='nt')

        update_query = f"""
        INSERT DATA {{
            GRAPH <{self.graph_iri}> {{
                {nt_data}
            }}
        }}
        """

        self.sparql_statements.setMethod(POST)
        self.sparql_statements.setQuery(update_query.encode("utf-8"))
        self.sparql_statements.setReturnFormat(JSON)

        try:
            self.sparql_statements.query()
        except Exception as e:
            print(f"❌ SPARQL update failed: {e}")
            print("SPARQL QUERY:\n", update_query)
            raise

    def upload_to_graphdb_depreciated(self, triples):
        """Uploads RDFLib triples to GraphDB using INSERT DATA with SPARQL-safe serialization."""
        
        if self.graph_iri is None:
            raise ValueError("User ID is required to determine the target graph.")

        # Build a temporary RDFLib Graph to serialize safely
        g = Graph()
        for s, p, o in triples:
            g.add((s, p, o))

        # Serialize in N-Triples format (SPARQL safe) inside the named graph
        nt_data = g.serialize(format='nt').decode('utf-8')

        update_query = f"""
        INSERT DATA {{
            GRAPH <{self.graph_iri}> {{
                {nt_data}
            }}
        }}
        """

        self.sparql_statements.setMethod(POST)
        self.sparql_statements.setQuery(update_query.encode("utf-8"))
        self.sparql_statements.setReturnFormat(JSON)

        try:
            self.sparql_statements.query()
        except Exception as e:
            print(f"❌ SPARQL update failed: {e}")
            print("SPARQL QUERY:\n", update_query)
            raise

    def update_graphdb(self, select_query):
        """Executes a SPARQL SELECT query only within the user's named graph (if user_id is set)."""
        
        if self.graph_iri is not None:
            select_query=self.prefixes+f"""
                WITH <{self.graph_iri}>
                {select_query}
            """

        self.sparql_statements.setMethod(POST)
        self.sparql_statements.setQuery(select_query.encode("utf-8"))
        self.sparql_statements.setReturnFormat(JSON)

        try:
            response = self.sparql_statements.query()
            return response
        except Exception as e:
            print(f"SPARQL SELECT query failed: {e}")
            print("QUERY:\n")
            print(select_query)
            raise
        
    def insert_graphdb(self, s, p, o):
        """Executes a SPARQL SELECT query only within the user's named graph (if user_id is set)."""
        
        if self.graph_iri is not None:
            insert_query = f"""
                INSERT DATA {{
                    GRAPH <{self.graph_iri}> {{
                        <{s}> <{p}> "{o}" .
                    }}
                }}
            """
        else:
            insert_query = f"""
                INSERT DATA {{
                    <{s}> <{p}> "{o}" .
                }}
            """

        self.sparql_statements.setMethod(POST)
        self.sparql_statements.setQuery(insert_query)
        self.sparql_statements.setReturnFormat(JSON)

        try:
            response = self.sparql_statements.query()
            return response
        except Exception as e:
            print(f"SPARQL SELECT query failed: {e}")
            print("QUERY:\n")
            print(insert_query)
            raise
    
    def delete_all(self, target_uri):
        print(f"[INFO] Preparing to delete dataset and related data: {target_uri}")
        target_uri = f"<{target_uri}>"
        graph_clause = f"GRAPH <{self.graph_iri}>" if self.graph_iri else ""
        
        select_query = f"""
        SELECT DISTINCT ?type WHERE {{
            {target_uri} a ?type .
            FILTER (?type IN (
                <{NS["DCAT"].Dataset}>,
                <{NS["DCAT"].Catalog}>,
                <{NS["GEOSPARQL"].Feature}>,
                <{NS["GEOSPARQL"].FeatureCollection}>
            ))
        }}
        """
        try:
            self.sparql.setQuery(select_query)
            self.sparql.setReturnFormat(JSON)
            response = self.sparql.query().convert()
            bindings = response.get("results", {}).get("bindings", [])
            if not bindings:
                print("⚠️ No type found for the target URI.")
                return

            res_type = bindings[0].get("type", {}).get("value", None)
            print(res_type)
            print(res_type)
            # FOR A DATASET
            queries=[
                f"""
        DELETE {{?g ?gp ?go}} WHERE {{
            {graph_clause} {{
                {target_uri} 
                    <{NS["GEOSPARQL"].hasFeatureCollection}>/
                    <{NS["GEOSPARQL"].hasFeature}>/ 
                    <{NS["GEOSPARQL"].hasGeometry}> ?g.
                    ?g ?gp ?go .
            }}
        }}
        """,f"""
        DELETE {{?g ?gp ?go}} WHERE {{
            {graph_clause} {{
                {target_uri} 
                    <{NS["GEOSPARQL"].hasFeatureCollection}>/
                    <{NS["GEOSPARQL"].hasFeature}> ?g.
                    ?g ?gp ?go .
            }}
        }}
        """,f"""
        DELETE {{?g ?gp ?go}} WHERE {{
            {graph_clause} {{
                {target_uri} 
                    <{NS["GEOSPARQL"].hasFeatureCollection}> ?g.
                    ?g ?gp ?go .
            }}
        }}
        """,
        #For a Feature Collection
        f"""
        DELETE {{?g ?gp ?go}} WHERE {{
            {graph_clause} {{
                {target_uri} 
                    <{NS["GEOSPARQL"].hasFeature}>/ 
                    <{NS["GEOSPARQL"].hasGeometry}> ?g.
                    ?g ?gp ?go .
            }}
        }}
        """,f"""
        DELETE {{?g ?gp ?go}} WHERE {{
            {graph_clause} {{
                {target_uri} 
                    <{NS["GEOSPARQL"].hasFeature}> ?g.
                    ?g ?gp ?go .
            }}
        }}
        """, #For a Feature
        f"""
        DELETE {{?g ?gp ?go}} WHERE {{
            {graph_clause} {{
                {target_uri} 
                    <{NS["GEOSPARQL"].hasGeometry}> ?g.
                    ?g ?gp ?go .
            }}
        }}
        """,
        #LAST 2 QUERIES->
        f"""
        DELETE WHERE {{
            {graph_clause} {{
                {target_uri} ?p ?o .
            }}
        }}
        """,f"""
        DELETE WHERE {{
            {graph_clause} {{
                ?s ?p  {target_uri} .
            }}
        }}
        """]

            for query in tqdm(queries):
                delete_query =query
                self.sparql_statements.setMethod(POST)
                self.sparql_statements.setReturnFormat(JSON)
                self.sparql_statements.setQuery(delete_query.encode("utf-8"))
                self.sparql_statements.query()
            print("✅ All related triples deleted.")
        except Exception as e:
            print(f"❌ Deletion failed: {e}")
            raise

       # def delete_all(self,id):
    #    # Set up SPARQLWrapper for updates
    #    # If the user has a specific graph
    #     if self.graph_iri is not None:
    #         exec_query1 = self.prefixes + f"""
    #         DELETE WHERE {{
    #             GRAPH <{self.graph_iri}> {{
    #                 <{id}> ?key ?value .
    #             }}
    #         }}
    #         """
    #         exec_query2 = self.prefixes + f"""
    #         DELETE WHERE {{
    #             GRAPH <{self.graph_iri}> {{
    #                 ?key ?value <{id}> .
    #             }}
    #         }}
    #         """
    #     else:
    #         exec_query1  = self.prefixes + f"""
    #         DELETE WHERE {{
    #                 <{id}> ?key ?value .
    #             }}
    #         }}
    #         """
    #         exec_query2  = self.prefixes + f"""
    #         DELETE WHERE {{
    #                 ?key ?value <{id}> .
    #             }}
    #         }}
    #         """
            
    #     self.sparql_statements.setMethod(POST)
    #     self.sparql_statements.setReturnFormat(JSON)

    #     try:
    #         self.sparql_statements.setQuery(exec_query1.encode("utf-8"))
    #         self.sparql_statements.query()
            
    #         self.sparql_statements.setQuery(exec_query2.encode("utf-8"))
    #         response = self.sparql_statements.query()
    #         return response
    #     except Exception as e:
    #         print(f"SPARQL update failed: {e}")
    #         raise

    def construct_graphdb(self, construct_query):
        """Executes a SPARQL CONSTRUCT query only within the user's named graph (if user_id is set)."""
        
        if self.graph_iri is not None:
            construct_query =self.prefixes+ f"""
            CONSTRUCT {{
                GRAPH <{self.graph_iri}> {{
                    {construct_query}
                }}
            }}
            """

        self.sparql.setMethod(GET)
        self.sparql.setQuery(construct_query)
        self.sparql.setReturnFormat("text/turtle")  # Return RDF in Turtle format

        try:
            response = self.sparql.query().convert()
            return response  # Returns RDF triples in Turtle format
        except Exception as e:
            print(f"SPARQL CONSTRUCT query failed: {e}")
            return None
    def catalog_exists(self, catalog_name):
        catalog_uri = URIRef(f"{NS['SPALOD']}{catalog_name}")
       
        query = f"""
        ASK {{
            <{catalog_uri}> a <{NS["DCAT"].Catalog}> .
        }}
        """
        self.sparql.setQuery(query)
        self.sparql.setReturnFormat(JSON)
        try:
            result = self.sparql.query().convert()
            return result.get("boolean", False)
        except SPARQLExceptions.SPARQLWrapperException as e:
            print(f"SPARQL Query Error: {e}")
            return False
    def dataset_exists(self,dataset_name):
        """
        Check if the dataset already exists in the catalog.
        """
        dataset_uri = URIRef(f"{NS['SPALOD']}{dataset_name}")
        query = f"""
        ASK {{
            ?x <{NS["DCAT"].dataset}> <{dataset_uri}> .
        }}
        """
        self.sparql.setQuery(self.prefixes + query)
        self.sparql.setReturnFormat(JSON)
        try:
            result = self.sparql.query().convert()
            return result.get("boolean", False)
        except Exception as e:
            print(f"[ERROR] SPARQL dataset_exists query failed: {e}")
            return False

    def create_catalog(self, catalog_name):
        catalog_uri = URIRef(f"{NS['SPALOD']}{catalog_name}")

        catalog_data = [
            (catalog_uri, RDF.type, NS["DCAT"].Catalog),
            (catalog_uri, RDFS.label, Literal(catalog_name)),
        ]
        self.upload_to_graphdb(catalog_data)
        return catalog_uri
    def add_graph(self, graph):
        """
        Uploads all triples from an RDFLib Graph object to the user's named graph in GraphDB.
        
        Args:
            graph (rdflib.Graph): The RDFLib graph containing the triples to insert.
        """
        if not isinstance(graph, Graph):
            raise TypeError("Input must be an instance of rdflib.Graph")

        # Extract triples and pass to the upload method
        triples = list(graph.triples((None, None, None)))
        if not triples:
            print("[INFO] No triples to upload.")
            return

        try:
            self.upload_to_graphdb(triples)
            print(f"[INFO] Uploaded {len(triples)} triples to graph <{self.graph_iri}>.")
        except Exception as e:
            print(f"[ERROR] Failed to upload RDF graph: {e}")
            raise

    def create_dataset(self, name,catalog_uri):
            dataset_uri = URIRef(f"{NS['SPALOD']}{name}")
            dataset_data = [
                (dataset_uri, RDF.type, NS["DCAT"].Dataset),
                (dataset_uri, RDFS.label, Literal(name)),
                (catalog_uri, NS["DCAT"].dataset, dataset_uri),
            ]
            self.upload_to_graphdb(dataset_data)
            return dataset_uri
    
    def add_file_to_dataset_or_feature(self, dataset_uri, file_url):
        """
        Adds a file to the dataset using the `spalod:hasFile` property.

        Args:
            dataset_uri (rdflib.URIRef): The URI of the dataset to update.
            file_url (str): The relative or full URL of the file to attach.
        """
        if not file_url.startswith("http"):
            spalod_url = get_env_settings("SPALOD_URL")
            file_url = f"{spalod_url}{file_url}"  # Ensure it's an absolute URI

        triple = (dataset_uri, NS["SPALOD"].hasFile, URIRef(file_url))
        try:
            self.upload_to_graphdb([triple])
            print(f"[INFO] File {file_url} added to dataset <{dataset_uri}>.")
        except Exception as e:
            print(f"[ERROR] Failed to add file to dataset: {e}")
            raise


    def initialize_dataset_structure(self, catalog_name, dataset_name):
        """
        Initializes catalog, dataset, and feature collection in the triple store
        if they do not already exist. Returns their URIs.

        Args:
            catalog_name (str): The name of the catalog.
            dataset_name (str): The name of the dataset.

        Returns:
            tuple: (catalog_uri, dataset_uri, feature_collection_uri)
        """

        # Create or retrieve catalog
        if not self.catalog_exists(catalog_name):
            catalog_uri = self.create_catalog(catalog_name)
        else:
            catalog_uri = URIRef(f"{NS['SPALOD']}{catalog_name}")

        # Create or retrieve dataset
        if not self.dataset_exists(dataset_name):
            dataset_uri = self.create_dataset(dataset_name, catalog_uri)
        else:
            dataset_uri = URIRef(f"{NS['SPALOD']}{dataset_name}")

        # Ensure dataset is linked to catalog
        check_dataset_query = f"""
        ASK {{
            <{catalog_uri}> <{NS["DCAT"].dataset}> <{dataset_uri}> .
        }}
        """
        if not self.query_graphdb(check_dataset_query)['boolean']:
            dataset_graph = Graph()
            dataset_graph.add((catalog_uri, NS["DCAT"].dataset, dataset_uri))
            dataset_graph.add((dataset_uri, RDF.type, NS["DCAT"].Dataset))
            dataset_graph.add((dataset_uri, NS["RDFS"].label, Literal(dataset_name)))
            self.add_graph(dataset_graph)


        return catalog_uri, dataset_uri
    def get_or_create_feature_collection_uri(self, dataset_uri):
        """
        Ensures that the given dataset has a linked FeatureCollection.
        If not, creates the triple and returns the FeatureCollection URI.

        Args:
            user_id (str): The ID used to initialize the GraphDBManager.
            dataset_uri (str or URIRef): The URI of the dataset.

        Returns:
            URIRef: The URI of the associated FeatureCollection.
        """
        dataset_uri = URIRef(dataset_uri)
        feature_collection_uri = URIRef(f"{dataset_uri}/collection")

        # Check if dataset → FeatureCollection link exists
        check_fc_query = f"""
        ASK {{
            <{dataset_uri}> <{NS["GEOSPARQL"].hasFeatureCollection}> <{feature_collection_uri}> .
        }}
        """
        if not self.query_graphdb(check_fc_query)['boolean']:
            fc_graph = Graph()
            fc_graph.add((dataset_uri, NS["GEOSPARQL"].hasFeatureCollection, feature_collection_uri))
            fc_graph.add((feature_collection_uri, RDF.type, NS["GEOSPARQL"].FeatureCollection))
            self.add_graph(fc_graph)

        return feature_collection_uri

    def create_feature_with_geometry(self, feature_collection_uri, label, wkt, metadata=None):
        """
        Creates a new feature with geometry and optional metadata inside a feature collection.

        Args:
            feature_collection_uri (str or URIRef): URI of the FeatureCollection.
            label (str): Label for the new feature.
            wkt (str): Geometry as WKT (e.g., POINT, POLYGON, LINESTRING).
            metadata (dict, optional): Additional metadata as {predicate URI: literal value}.

        Returns:
            dict: {
                'feature_uri': str,
                'geometry_uri': str
            }

        Raises:
            RuntimeError: if GraphDBManager initialization fails.
        """

        # Generate URIs
        feature_id = str(uuid.uuid4())
        feature_uri = URIRef(f"{feature_collection_uri}/feature/{feature_id}")
        geometry_uri = URIRef(f"{feature_uri}/geom")

        # Create triples
        feature_graph = Graph()
        feature_graph.add((URIRef(feature_collection_uri), NS["GEOSPARQL"].hasFeature, feature_uri))
        feature_graph.add((feature_uri, RDF.type, NS["GEOSPARQL"].Feature))
        feature_graph.add((feature_uri, NS["RDFS"].label, Literal(label)))
        feature_graph.add((feature_uri, NS["GEOSPARQL"].hasGeometry, geometry_uri))
        feature_graph.add((geometry_uri, RDF.type, NS["GEOSPARQL"].Geometry))
        feature_graph.add((geometry_uri, NS["GEOSPARQL"].asWKT, Literal(wkt, datatype=NS["GEOSPARQL"].wktLiteral)))

        # Optional metadata
        DCTERMS = NS["DCTERMS"]
        if isinstance(metadata, dict):
            for key, value in metadata.items():
                # Ensure the key is a full URI
                if not key.startswith(("http://", "https://", "urn:")):
                    full_key = DCTERMS + key
                else:
                    full_key = key
                feature_graph.add((feature_uri, URIRef(full_key), Literal(value)))

        # Upload to GraphDB
        self.add_graph(feature_graph)

        return {
            'feature_uri': str(feature_uri),
            'geometry_uri': str(geometry_uri)
        }

    def add_dcterms_metadata_to_dataset(self, dataset_uri, metadata, excluded_keys=None):
        """
        Initializes GraphDBManager and adds DCTERMS metadata triples to a dataset,
        excluding specified keys.

        Args:
            dataset_uri (str or URIRef): The URI of the dataset.
            metadata (dict): Metadata dictionary with DCTERMS keys and literal values.
            excluded_keys (list, optional): List of keys to exclude (default: ["catalog", "title"]).

        Returns:
            list: List of inserted RDF triples.
        
        Raises:
            RuntimeError: If GraphDBManager initialization fails.
        """
        if excluded_keys is None:
            excluded_keys = ["catalog", "title"]
        dataset_uri = URIRef(dataset_uri)
        dataset_data = []

        for key, value in metadata.items():
            if key not in excluded_keys:
                predicate = NS["DCTERMS"][key]
                dataset_data.append((dataset_uri, predicate, Literal(value)))
        graph = rdflib.Graph()

        for triple in dataset_data:
            graph.add(triple)

        self.upload_to_graphdb(dataset_data)
        return dataset_data


    def add_pointcloud_to_dataset(self, dataset_uri, file_path, file_url, pointcloud_id, pointcloud_uuid):
        """
        Adds a point cloud as a feature to the dataset's FeatureCollection,
        generates its geometry, links metadata, and registers the file URL.

        Args:
            dataset_uri (str or URIRef): The dataset to which the point cloud belongs.
            file_path (str): Local path to the file used to compute WKT geometry.
            file_url (str): Public URL of the uploaded file (e.g., "/uploads/pointclouds/model.ply").
            pointcloud_id (str): Human-readable ID for the point cloud.
            pointcloud_uuid (str): UUID used to create the point cloud URI.

        Returns:
            dict: {
                'feature_uri': str,
                'pointcloud_uri': str,
                'geometry_uri': str
            }

        Raises:
            RuntimeError: If GraphDBManager or WKT generation fails.
        """
        # Ensure feature collection exists
        feature_collection_uri = self.get_or_create_feature_collection_uri( dataset_uri)

        # Generate URIs
        random_uuid = uuid.uuid4()
        feature_uri = URIRef(f"{feature_collection_uri}/feature/{random_uuid}")
        geometry_uri = URIRef(f"{feature_uri}/geom")
        pointcloud_uri = URIRef(f"{NS['SPALOD']}{pointcloud_uuid}")
        dataset_uri = URIRef(dataset_uri)
        
        spalod_url = get_env_settings("SPALOD_URL")
        file_uri = URIRef(f"{spalod_url}{file_url}")  # Ensure it’s a proper URI

        # Compute WKT geometry
        try:
            wkt_string = self.get_wkt_polygon(file_path)
        except Exception as e:
            raise RuntimeError(f"Failed to generate WKT from file '{file_path}': {str(e)}")

        wkt_literal = Literal(wkt_string, datatype=NS["GEOSPARQL"].wktLiteral)

        # Build RDF triples
        triples = [
            # Pointcloud metadata
            (pointcloud_uri, RDF.type, NS['SPALOD'].Pointcloud),
            (pointcloud_uri, NS['SPALOD'].pointcloud_id, Literal(pointcloud_id, datatype=XSD.string)),
            (pointcloud_uri, NS['SPALOD'].pointcloud_uuid, Literal(pointcloud_uuid, datatype=XSD.string)),

            # Link feature to pointcloud
            (feature_uri, NS['SPALOD'].hasPointcloud, pointcloud_uri),
            (feature_uri, RDF.type, NS["GEOSPARQL"].Feature),
            (feature_uri, NS["GEOSPARQL"].hasGeometry, geometry_uri),

            # Link feature to feature collection
            (feature_collection_uri, NS["GEOSPARQL"].hasFeature, feature_uri),

            # Geometry
            (geometry_uri, RDF.type, NS["GEOSPARQL"].Geometry),
            (geometry_uri, NS["GEOSPARQL"].asWKT, wkt_literal),

            # Link dataset to downloadable file
            (dataset_uri, NS['SPALOD'].hasFile, file_uri)
        ]
        graph= Graph()
        for triple in triples:
            graph.add(triple)

        self.upload_to_graphdb(triples)

        return {
            'feature_uri': str(feature_uri),
            'pointcloud_uri': str(pointcloud_uri),
            'geometry_uri': str(geometry_uri)
        }


    def get_wkt_polygon(self,file_path, utm_zone=32, northern_hemisphere=True):
            """Generates a WKT polygon from a LAS file, transforming coordinates to WGS84."""
            with laspy.open(file_path) as las:
                point_data = las.read()

            x_coords = point_data.x
            y_coords = point_data.y

            # Create a convex hull
            points = np.vstack((x_coords, y_coords)).T
            hull = Polygon(points).convex_hull

            # Transform to WGS84
            utm_crs = f"epsg:326{utm_zone}" if northern_hemisphere else f"epsg:327{utm_zone}"
            project = pyproj.Transformer.from_crs(utm_crs, "epsg:4326", always_xy=True).transform
            hull_latlon = transform(project, hull)

            return hull_latlon.wkt

    def add_geojson_to_dataset(self, dataset_uri, file_path, file_url):
        """
        Parses a GeoJSON file, adds its features to a FeatureCollection,
        and links the file to the dataset via SPALOD.hasFile.

        Args:
            dataset_uri (str): URI of the dataset the GeoJSON belongs to.
            file_path (str): Local path to the uploaded GeoJSON file.
            file_url (str): Public URL where the file is accessible.

        Returns:
            dict: {
                'feature_collection_uri': str,
                'file_uri': str
            }

        Raises:
            RuntimeError: On failure to initialize or parse/upload.
        """

        print(f"📦 Processing GeoJSON: {file_path}")
        start_time = time.time()
        batched_triples = []

        with open(file_path, 'r') as f:
            data = json.load(f)

        if not isinstance(data, dict) or data.get("type") != "FeatureCollection":
            raise ValueError("GeoJSON must have type: 'FeatureCollection'.")

        if "name" not in data:
            data["name"] = str(uuid.uuid4())

        feature_collection_uri = self.get_or_create_feature_collection_uri(dataset_uri)
        batched_triples.extend([
            (feature_collection_uri, RDF.type, NS["GEOSPARQL"].FeatureCollection),
            (feature_collection_uri, RDFS.label, Literal(data["name"])),
            (URIRef(dataset_uri), NS["GEOSPARQL"].hasFeatureCollection, feature_collection_uri)
        ])

        # 🔗 Link the file to the dataset
        spalod_url = get_env_settings("SPALOD_URL")
        file_uri = URIRef(f"{spalod_url}{file_url}")
        batched_triples.append((URIRef(dataset_uri), NS['SPALOD'].hasFile, file_uri))

        total_features = len(data.get("features", []))
        print(f"📍 Found {total_features} features in '{data['name']}'.")

        for i, feature in enumerate(data["features"]):
            try:
                if not isinstance(feature, dict) or feature.get("type") != "Feature":
                    raise ValueError(f"Feature {i} is malformed.")

                properties = feature.get("properties", {})
                geometry = feature.get("geometry", {})

                feature_type = geometry.get("type")
                coordinates = geometry.get("coordinates")

                if not feature_type or not coordinates:
                    raise ValueError(f"Feature {i} is missing geometry.")

                # Generate URIs
                random_uuid = uuid.uuid4()
                feature_uri = URIRef(f"{feature_collection_uri}/feature/{random_uuid}")
                geom_uri = URIRef(f"{feature_uri}/geom")

                wkt_string = self.convert_coordinates_to_wkt(feature_type, coordinates)
                geom_literal = Literal(wkt_string, datatype=NS["GEOSPARQL"].wktLiteral)

                # Feature triples
                batched_triples.extend([
                    (feature_uri, RDF.type, NS["GEOSPARQL"].Feature),
                    (feature_collection_uri, NS["GEOSPARQL"].hasFeature, feature_uri),
                    (feature_uri, NS["GEOSPARQL"].hasGeometry, geom_uri),
                    (geom_uri, RDF.type, NS["GEOSPARQL"].Geometry),
                    (geom_uri, NS["GEOSPARQL"].asWKT, geom_literal)
                ])

                # Optional label
                for label_key in ["label", "name", "item"]:
                    if label_key in properties:
                        batched_triples.append((feature_uri, RDFS.label, Literal(properties[label_key])))
                        break

                # Add other properties
                for prop, value in properties.items():
                    batched_triples.append((feature_uri, URIRef(NS["SPALOD"] + prop), Literal(value)))

                if len(batched_triples) >= BATCH_SIZE:
                    self.upload_to_graphdb(batched_triples)
                    batched_triples.clear()

            except Exception as fe:
                print(f"⚠️ Feature {i} error: {fe}")

        # Final upload
        if batched_triples:
            self.upload_to_graphdb(batched_triples)

        print(f"✅ GeoJSON processing completed in {int(time.time() - start_time)}s.")

        return {
            'feature_collection_uri': str(feature_collection_uri),
            'file_uri': str(file_uri)
        }


    def convert_coordinates_to_wkt(self, feature_type, coordinates):
            """Converts coordinates to a valid WKT format, dynamically handling 2D and 3D coordinates."""
            def format_coords(coord):
                """Format individual coordinates based on 2D or 3D structure."""
                if len(coord) == 3:  # Handle 3D coordinates dynamically
                    return f"{coord[0]} {coord[1]} {coord[2]}"
                else:  # Handle 2D coordinates
                    return f"{coord[0]} {coord[1]}"

            # Handling different geometry types based on the feature type
            if feature_type.lower() == "point":
                # WKT for Point: POINT (x y)
                formatted_coords = format_coords(coordinates)
                wkt_string = f"POINT ({formatted_coords})"

            elif feature_type.lower() == "linestring":
                # WKT for LineString: LINESTRING (x1 y1, x2 y2, ...)
                formatted_coords = ", ".join([format_coords(coord) for coord in coordinates])
                wkt_string = f"LINESTRING ({formatted_coords})"

            elif feature_type.lower() == "polygon":
                # WKT for Polygon: POLYGON ((x1 y1, x2 y2, ...))
                formatted_coords = ", ".join([format_coords(coord) for coord in coordinates])
                wkt_string = f"POLYGON (({formatted_coords}))"

            elif feature_type.lower() == "multilinestring":
                # WKT for MultiLineString: MULTILINESTRING ((x1 y1, x2 y2), (x3 y3, x4 y4), ...)
                formatted_lines = ", ".join([f"({', '.join([format_coords(coord) for coord in line])})" for line in coordinates])
                wkt_string = f"MULTILINESTRING ({formatted_lines})"

            elif feature_type.lower() == "multipolygon":
                # WKT for MultiPolygon: MULTIPOLYGON (((x1 y1, x2 y2, ...)), ((x3 y3, x4 y4, ...)), ...)
                formatted_polygons = ", ".join([f"(({', '.join([format_coords(coord) for coord in polygon])}))" for polygon in coordinates])
                wkt_string = f"MULTIPOLYGON ({formatted_polygons})"

            else:
                raise ValueError("Unsupported geometry type")

            # Check for square brackets and raise an error or print a message if found
            if "[" in wkt_string or "]" in wkt_string:
                print(f"Error: WKT string contains square brackets: {wkt_string}")

            return wkt_string