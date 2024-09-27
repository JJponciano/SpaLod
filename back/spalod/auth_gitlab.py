from rest_framework.views import APIView
from rest_framework import status
import json
from rest_framework.decorators import permission_classes
from rest_framework.permissions import AllowAny
from django.http import JsonResponse
from django.shortcuts import redirect
from django.conf import settings
import requests

@permission_classes([AllowAny])
class Authorize(APIView):
    def get(self, request, *args, **kwargs):
        print("auth")
        base = settings.SOCIAL_AUTH_GITLAB_API_URL
        client_id = settings.SOCIAL_AUTH_GITLAB_KEY
        redirect_uri = "http://localhost:8000/auth/gitlab/redirect/"
        
        return redirect(f"{base}/oauth/authorize?client_id={client_id}&redirect_uri={redirect_uri}&response_type=code")
    
@permission_classes([AllowAny])
class Redirect(APIView):
    def get(self, request, *args, **kwargs):
        base = settings.SOCIAL_AUTH_GITLAB_API_URL
        client_id = settings.SOCIAL_AUTH_GITLAB_KEY
        client_secret = settings.SOCIAL_AUTH_GITLAB_SECRET
        code = request.query_params['code']
        
        x = requests.post(f"{base}/oauth/token", params = {
            "client_id": client_id,
            "client_secret": client_secret,
            "code": code,
            "grant_type": "authorization_code",
            "redirect_uri": "http://localhost:8000/auth/gitlab/redirect/"
        })
        
        print(x.json())
        access_token = x.json().get("access_token")
        refresh_token = x.json().get("refresh_token")
        
        return redirect(f"http://localhost:8080/login/gitlab/?access_token={access_token}&refresh_token={refresh_token}")