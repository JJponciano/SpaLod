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
from allauth.socialaccount.providers.facebook.views import FacebookOAuth2Adapter
from django.conf import settings
from django.conf.urls.static import static
from .auth_gitlab import Authorize, Redirect

class GitLabLogin(SocialLoginView):
    adapter_class = GitLabOAuth2Adapter

class FacebookLogin(SocialLoginView):
    adapter_class = FacebookOAuth2Adapter

urlpatterns = [
    path('admin/', admin.site.urls),
    path('accounts/', include('allauth.urls')),  # Add this for allauth
    path('auth/', include('dj_rest_auth.urls')),  # REST auth
    path('auth/registration/', include('dj_rest_auth.registration.urls')),
    path('auth/social/', include('allauth.socialaccount.urls')),  # For GitLab auth
    path('auth/gitlab/', GitLabLogin.as_view(), name='gitlab_login'),
    path('auth/facebook/', FacebookLogin.as_view(), name='fb_login'),
    path('api/', include('spalod_app.urls')),
    path('auth/use/gitlab/', Authorize.as_view(), name='use_gitlab_login'),
    path('auth/gitlab/redirect/', Redirect.as_view(), name='gitlab_redirect'),
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)