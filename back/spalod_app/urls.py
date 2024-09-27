from django.urls import path
from .views import PropertiesQueryView,UpdateOntologyView,FileUploadView,SparqlQueryAPIView,User
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    path('sparql-query/', SparqlQueryAPIView.as_view(), name='sparql_query_api'),
    path('upload-file/', FileUploadView.as_view(), name='file-upload'),
    path('query-properties/', PropertiesQueryView.as_view(), name='query_properties'),
    path('update-ontology/', UpdateOntologyView.as_view(), name='update_ontology'),
    path('user/', User.as_view(), name='update_ontology'),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)