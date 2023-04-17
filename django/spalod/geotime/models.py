from django.db import models
from django.contrib.auth.models import User

class Profile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    company = models.CharField(max_length=255)
    rights = models.CharField(max_length=255)
    
    def __str__(self):
        return self.user.email