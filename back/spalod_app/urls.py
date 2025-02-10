from django.urls import path
from django.conf import settings
from django.conf.urls.static import static

from .views.geo import GeoGetAllCatalogsView,GeoGetAllFeaturesOfDatasetView, GeoGetFeature,GeoGetCatalog,GeoGetDatasetOfCatalogView,GeoRemoveCatalog,GeoRemoveFeature,GeoWKT,GeoGetFeatureWKT
from .views.properties import PropertiesQueryView
from .views.sparql_query import SparqlQueryAPIView
from .views.upload import FileUploadView
from .views.ontology import UpdateOntologyView

urlpatterns = [
    path('sparql-query/', SparqlQueryAPIView.as_view(), name='sparql_query_api'),
    path('upload-file/', FileUploadView.as_view(), name='file_upload'),
    path('query-properties/', PropertiesQueryView.as_view(), name='query_properties'),
    path('update-ontology/', UpdateOntologyView.as_view(), name='update_ontology'),
    path('geo/all/catalog', GeoGetAllCatalogsView.as_view(), name='geo-get-all-catalog'),
    path('geo/all/dataset', GeoGetDatasetOfCatalogView.as_view(), name='geo-get-all-dataset'),
    # path('geo/all/feature', GeoGetAllFeaturesOfCatalogView.as_view(), name='geo-get-all-feature'),
    path('geo/all/feature', GeoGetAllFeaturesOfDatasetView.as_view(), name='geo-get-all-feature'),
    path('geo/feature', GeoGetFeature.as_view(), name='geo-get-feature'),
    path('geo/catalog', GeoGetCatalog.as_view(), name='geo-get-catalog'),
    path('geo/getwkt', GeoWKT.as_view(), name='geo-get-wkt'),
    path('geo/getfeaturewkt', GeoGetFeatureWKT.as_view(), name='geo-get-feature-wkt'),
    path('geo/catalog/delete', GeoRemoveCatalog.as_view(), name='geo-remove-catalog'),
    path('geo/feature/delete', GeoRemoveFeature.as_view(), name='geo-remove-feature'),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)