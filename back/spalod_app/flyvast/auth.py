from django.conf import settings
import requests
import hashlib
import os

auth_token = ""

def authenticate_flyvast():
    fv_api_url = settings.FLYVAST_API_URL
    
    try:
        fv_user = settings.FLYVAST_USER
    except:
        fv_user = os.environ['FLYVAST_USER']
        
    try:
        fv_password = settings.FLYVAST_PASSWORD
    except:
        fv_password = os.environ['FLYVAST_PASSWORD']
    
    r = requests.post(f'{fv_api_url}/Login.php', json={
        "email": fv_user,
        "password": hashlib.sha512(fv_password.encode('utf-8')).hexdigest()
    })
    
    global auth_token
    if r.json().get("result") != None:
        auth_token = r.json().get("result")
    else:
        raise
    
def auth_request(func):
    def new_function(*args, **kwargs):
        if kwargs.get('headers') == None:
            kwargs['headers'] = {}
        
        headers = kwargs.get('headers')
        headers['Authorization'] = f'Bearer {auth_token}'
        
        ret = func(*args, **kwargs)
        
        result = ret.json()
        if result.get('error'):
            raise BaseException('bad credentials')
        
        return ret
    return new_function
        
def with_flyvast_auth(func):
    def new_function(*args, **kwargs):
        try:
            ret = func(*args, **kwargs)
        except:
            authenticate_flyvast()
            ret = func(*args, **kwargs)
        return ret
    return new_function