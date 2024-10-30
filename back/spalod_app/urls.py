from django.urls import path
from django.conf import settings
from django.conf.urls.static import static

from .views.geo import GeoGetAllView, GeoGetFeature,GeoGetCatalog
from .views.properties import PropertiesQueryView
from .views.sparql_query import SparqlQueryAPIView
from .views.upload import FileUploadView
from .views.ontology import UpdateOntologyView

urlpatterns = [
    path('sparql-query/', SparqlQueryAPIView.as_view(), name='sparql_query_api'),
    path('upload-file/', FileUploadView.as_view(), name='file_upload'),
    path('query-properties/', PropertiesQueryView.as_view(), name='query_properties'),
    path('update-ontology/', UpdateOntologyView.as_view(), name='update_ontology'),
    path('geo/all', GeoGetAllView.as_view(), name='geo-get-all'),
    path('geo/feature', GeoGetFeature.as_view(), name='geo-get-feature'),
    path('geo/catalog', GeoGetCatalog.as_view(), name='geo-get-catalog'),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)