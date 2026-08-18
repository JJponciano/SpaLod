from rest_framework import status
from rest_framework.parsers import MultiPartParser
from rest_framework.response import Response
from rest_framework.views import APIView

from spalod_app.utils.metadata_parser import parse_metadata_xml


class ParseMetadataView(APIView):
    parser_classes = [MultiPartParser]

    def post(self, request, *args, **kwargs):
        uploaded_file = request.FILES.get("file")

        if not uploaded_file:
            return Response(
                {"error": "Field 'file' is required."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        try:
            xml_bytes = uploaded_file.read()
        except Exception as exc:
            return Response(
                {"error": f"Failed to read file: {exc}"},
                status=status.HTTP_400_BAD_REQUEST,
            )

        try:
            result = parse_metadata_xml(xml_bytes)
        except Exception as exc:
            return Response(
                {"error": f"Failed to parse metadata: {exc}"},
                status=status.HTTP_400_BAD_REQUEST,
            )

        return Response({"result": result}, status=status.HTTP_200_OK)
