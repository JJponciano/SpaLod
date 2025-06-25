from django.urls import path
from django.conf import settings
from django.conf.urls.static import static

from .views.geo import *
from .views.properties import PropertiesQueryView
from .views.sparql_query import SparqlQueryAPIView
from .views.upload import FileUploadView
from .views.ontology import UpdateOntologyView

urlpatterns = [
    path('sparql-query/', SparqlQueryAPIView.as_view(), name='sparql_query_api'),
    path('upload-file/', FileUploadView.as_view(), name='file_upload'),
    path('query-properties/', PropertiesQueryView.as_view(), name='query_properties'),
    path('update-ontology/', UpdateOntologyView.as_view(), name='update_ontology'),
    path('geo/catalog', GeoGetCatalog.as_view(), name='geo-catalog'),
    path('geo/catalog/all', GeoGetAllCatalogsView.as_view(), name='geo-catalog-all'),
    path('geo/catalog/all/wkt', GeoCatalogWKT.as_view(), name='geo-catalog-all-wkt'),
    path('geo/catalog/delete', GeoRemoveID.as_view(), name='geo-catalog-delete'),
    path('geo/dataset', GeoGetItem.as_view(), name='geo-dataset'),
    path('geo/dataset/filter', GeoFilterDatasetByMetadata.as_view(), name='geo-dataset-filter'),
    path('geo/dataset/all', GeoGetDatasetOfCatalogView.as_view(), name='geo-dataset-all'),
    path('geo/dataset/all/wkt', GeoDatasetWKT.as_view(), name='geo-dataset-all-wkt'),
    path('geo/dataset/delete', GeoRemoveID.as_view(), name='geo-dataset-remove'),
    path('geo/feature', GeoGetItem.as_view(), name='geo-feature'),
    path('geo/feature/all', GeoGetAllFeaturesOfDatasetView.as_view(), name='geo-feature-all'),
    path('geo/feature/wkt', GeoGetFeatureWKT.as_view(), name='geo-feature-wkt'),
    path('geo/feature/delete', GeoRemoveID.as_view(), name='geo-feature-delete'),
    path('geo/delete', GeoRemoveID.as_view(), name='geo-delete'),
    path('geo/feature/update', GeoUpdateFeatureItem.as_view(), name='geo-feature-update'),
    path('geo/feature/insert', GeoInsertFeatureItem.as_view(), name='geo-feature-insert'),
    path('geo/generic/delete', GeoGenericDelete.as_view(), name='geo-feature-insert'),
    path('geo/feature/add/file', GeoFeatureAddFile.as_view(), name='geo-feature-add-file'),
    path('geo/feature/new', GeoFeatureNew.as_view(), name='geo-feature-new'),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)