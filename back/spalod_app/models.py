from django.db import models

class UploadedFile(models.Model):
    file = models.FileField(upload_to='uploads/')
    metadata = models.JSONField()
    uploaded_at = models.DateTimeField(auto_now_add=True)
