from rest_framework.views import APIView
from rest_framework.response import Response
from SPARQLWrapper import SPARQLWrapper, POST
from rest_framework import status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from datetime import datetime
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

class UpdateOntologyView(APIView):
    def post(self, request, *args, **kwargs):
        # Extract the mappings from the request body
        mappings = request.data.get("mappings", [])
        
        # SPARQL endpoint for update queries
        sparql = SPARQLWrapper("http://localhost:7200/repositories/Spalod/statements")
        sparql.setMethod(POST)

        try:
            # For each mapping, create SPARQL update queries
            for mapping in mappings:
                new_property = mapping["new_property"]
                old_property = mapping["old_property"]
                current_time = datetime.now().isoformat()

                # SPARQL query to insert data
                sparql_query = f"""
                    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                    INSERT DATA {{
                        <{new_property}> <https://schema.org/isSimilarTo> <{old_property}> .
                        <{new_property}> <http://spalod/hasBeenValidated> "{current_time}"^^xsd:dateTime .
                    }}
                """
                sparql.setQuery(sparql_query)

                # Execute the SPARQL query
                sparql.query()

            return Response({"message": "Ontology updated successfully"}, status=status.HTTP_200_OK)

        except Exception as e:
            return Response({"error": str(e)}, status=status.HTTP_400_BAD_REQUEST)

