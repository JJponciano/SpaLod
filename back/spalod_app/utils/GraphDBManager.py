from SPARQLWrapper import SPARQLWrapper, POST
import rdflib
from rdflib import Graph
import os
from django.conf import settings
from tqdm import tqdm
from SPARQLWrapper import SPARQLWrapper, JSON, POST, GET,POSTDIRECTLY,SPARQLExceptions
from rdflib import URIRef, Literal, RDF,Namespace
from rdflib.namespace import  RDFS, OWL

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
            print(f"Data uploaded successfully to Graph: {self.graph_iri}")
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

        if self.graph_iri is not None and query_type not in ["ask", "select"]:
            # Only wrap if it's a raw pattern (e.g., `?s ?p ?o`) — not a full ASK/SELECT
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
            print(f"✅ Data uploaded successfully to Graph: {self.graph_iri}")
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
            print(f"✅ Data uploaded successfully to Graph: {self.graph_iri}")
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
            #     (dataset_uri, NS['SPALOD'].hasOWL, URIRef(f"https://spalod.geovast3d.com{self.ontology_url}")),  # Ensure it's a URI
            #     (dataset_uri, NS['SPALOD'].hasFile, URIRef(f"https://spalod.geovast3d.com{self.file_url}"))  # Ensure it's a URI
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
            file_url = f"https://spalod.geovast3d.com{file_url}"  # Ensure it's an absolute URI

        triple = (dataset_uri, NS["SPALOD"].hasFile, URIRef(file_url))
        try:
            self.upload_to_graphdb([triple])
            print(f"[INFO] File {file_url} added to dataset <{dataset_uri}>.")
        except Exception as e:
            print(f"[ERROR] Failed to add file to dataset: {e}")
            raise
