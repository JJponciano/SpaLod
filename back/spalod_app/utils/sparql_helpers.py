from SPARQLWrapper import SPARQLWrapper, POST
import rdflib

def add_ontology_to_graphdb(ontology_file_path, uuid, ontology_url, map_url, metadata):
    """
    Adds the ontology to GraphDB as a named graph and inserts the properties
    hasHTML, hasOWL, and the metadata associated with the graph.
    """
    sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod/statements")
    sparql.setMethod(POST)

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

    try:
        # Serialize the ontology as plain N-Triples, which can be inserted directly into the SPARQL query
        rdf_data = graph.serialize(format="nt")

        # Construct SPARQL query without Turtle prefixes
        insert_query = f"""
        INSERT DATA {{
            GRAPH <{graph_iri}> {{
                {rdf_data}
            }}
        }}
        """

        # Debug: Print the query before sending it
        print("SPARQL Query:", insert_query)  # Print query to debug
        sparql.setQuery(insert_query)
        sparql.query()

        # Insert the hasHTML, hasOWL properties
        sparql.setQuery(sparql_update_query)
        sparql.query()

        # Insert the metadata
        sparql.setQuery(metadata_query)
        sparql.query()

    except Exception as e:
        raise Exception(f"Failed to add ontology to GraphDB: {str(e)}")
