from django.urls import path
from .views import SparqlQueryAPIView
from .views import FileUploadView
from .views import PropertiesQueryView

urlpatterns = [
    path('sparql-query/', SparqlQueryAPIView.as_view(), name='sparql_query_api'),
    path('upload-file/', FileUploadView.as_view(), name='file-upload'),
    path('query-properties/', PropertiesQueryView.as_view(), name='query_properties'),

]
