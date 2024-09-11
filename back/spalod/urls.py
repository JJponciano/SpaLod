"""
URL configuration for spalod project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/5.1/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path,include
from dj_rest_auth.registration.views import SocialLoginView
from allauth.socialaccount.providers.gitlab.views import GitLabOAuth2Adapter
from django.conf import settings
from django.conf.urls.static import static

class GitLabLogin(SocialLoginView):
    adapter_class = GitLabOAuth2Adapter

urlpatterns = [
    path('admin/', admin.site.urls),
    path('accounts/', include('allauth.urls')),  # Add this for allauth
    path('api/auth/', include('dj_rest_auth.urls')),  # REST auth
    path('api/auth/social/', include('allauth.socialaccount.urls')),  # For GitLab auth
    path('api/auth/gitlab/', GitLabLogin.as_view(), name='gitlab_login'),
    path('api/', include('spalod_app.urls')), 
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)