from rest_framework import serializers
from .models import UploadedFile

class SparqlQuerySerializer(serializers.Serializer):
    query = serializers.CharField(required=True)
    catalog_id = serializers.CharField(required=False)  # Add this line to accept the graph field



class UploadedFileSerializer(serializers.ModelSerializer):
    class Meta:
        model = UploadedFile
        fields = ['file', 'metadata', 'uploaded_at']