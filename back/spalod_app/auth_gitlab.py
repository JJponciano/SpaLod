from rest_framework.views import APIView
from rest_framework.decorators import permission_classes
from rest_framework.permissions import AllowAny
from django.shortcuts import redirect
from django.conf import settings
import requests

from .utils.env import get_env_settings

@permission_classes([AllowAny])
class Authorize(APIView):
    def get(self, request, *args, **kwargs):
        base = get_env_settings("SOCIAL_AUTH_GITLAB_API_URL")
        client_id = get_env_settings("SOCIAL_AUTH_GITLAB_KEY")
        redirect_uri = get_env_settings("SOCIAL_AUTH_GITLAB_REDIRECT_URI")
        
        return redirect(f"{base}/oauth/authorize?client_id={client_id}&redirect_uri={redirect_uri}&response_type=code")
    
@permission_classes([AllowAny])
class Redirect(APIView):
    def get(self, request, *args, **kwargs):
        base = get_env_settings("SOCIAL_AUTH_GITLAB_API_URL")
        client_id = get_env_settings("SOCIAL_AUTH_GITLAB_KEY")
        client_secret = get_env_settings("SOCIAL_AUTH_GITLAB_SECRET")
        redirect_uri = get_env_settings("SOCIAL_AUTH_GITLAB_REDIRECT_URI")
        front_url = get_env_settings("SOCIAL_AUTH_GITLAB_FRONT_URL")
        code = request.query_params['code']
        
        x = requests.post(f"{base}/oauth/token", params = {
            "client_id": client_id,
            "client_secret": client_secret,
            "code": code,
            "grant_type": "authorization_code",
            "redirect_uri": redirect_uri
        })
        
        print(x.json())
        access_token = x.json().get("access_token")
        refresh_token = x.json().get("refresh_token")
        
        return redirect(f"{front_url}/login/gitlab/?access_token={access_token}&refresh_token={refresh_token}")