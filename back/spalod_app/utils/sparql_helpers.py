from SPARQLWrapper import SPARQLWrapper, POST
import rdflib
from rdflib import Graph
import os
from django.conf import settings
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

    
