import os
from django.conf import settings


def get_env_settings(key):
    try:
        value = os.environ.get(key)
        
        if value is None:
            value = getattr(settings, key, None)
    except:
        value = getattr(settings, key, None)
        
    return value
        