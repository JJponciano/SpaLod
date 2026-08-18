from django.urls import path
from django.conf import settings
from django.conf.urls.static import static

from .views.geo import *
from .views.properties import PropertiesQueryView
from .views.sparql_query import SparqlQueryAPIView
from .views.upload import FileUploadView
from .views.ontology import UpdateOntologyView
from .views.matching import MatchingDatasetsView, MatchingMappingsView, MatchingPropertiesView, SchemaOrgTerms, ExternalVocabularyTerms , UserVocabularies
from .views.metadata_api import EvaluateMetadataView
from .views.metadata_parser_view import ParseMetadataView

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
    path('matching/datasets/', MatchingDatasetsView.as_view(), name='matching_datasets'),
    path("matching/properties/", MatchingPropertiesView.as_view(), name="matching_properties"),
    path("matching/mappings/",   MatchingMappingsView.as_view(),   name="matching_mappings"),
    path('vocabulary/uskb/', SchemaOrgTerms.as_view(), name='vocabulary_uskb'),
    path('geo/dataset/ld', GeoDatasetGeoJsonLD.as_view(), name='geo-dataset-ld'),
    path('vocabulary/external/', ExternalVocabularyTerms.as_view(), name='vocabulary_external'),
    path("vocabulary/user/", UserVocabularies.as_view(), name="user_vocabularies"),
    path('metadata/evaluate/', EvaluateMetadataView.as_view(), name='evaluate_metadata'),
    path('geo/dataset/replace-metadata', GeoDatasetReplaceMetadata.as_view(), name='geo-dataset-replace-metadata'),
    path('metadata/parse/', ParseMetadataView.as_view(), name='parse_metadata'),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)