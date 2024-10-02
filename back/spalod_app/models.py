from django.db import models
from django.contrib.auth.models import User

class UploadedFile(models.Model):
    file = models.FileField(upload_to='uploads/')
    metadata = models.JSONField()
    uploaded_at = models.DateTimeField(auto_now_add=True)

class FlyvastPointcloud(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    pc_id = models.CharField(max_length=512)