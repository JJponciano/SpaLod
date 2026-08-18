import os

from django.conf import settings
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from spalod_app.utils.metadata_evaluator import evaluate_metadata_xml


class EvaluateMetadataView(APIView):
    def get(self, request, *args, **kwargs):
        metadata_file_url = request.GET.get("metadata_file_url")

        if not metadata_file_url:
            return Response(
                {"error": "metadata_file_url is required."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        xml_file_path = os.path.join(
            settings.MEDIA_ROOT,
            metadata_file_url.replace("/media/", "")
        )

        if not os.path.exists(xml_file_path):
            return Response(
                {"error": "Metadata XML file does not exist on server."},
                status=status.HTTP_404_NOT_FOUND,
            )

        result = evaluate_metadata_xml(xml_file_path)

        return Response(result, status=status.HTTP_200_OK)