# Generated by Django 5.1 on 2024-09-11 12:15

from django.db import migrations, models


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='UploadedFile',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('file', models.FileField(upload_to='uploads/')),
                ('metadata', models.JSONField()),
                ('uploaded_at', models.DateTimeField(auto_now_add=True)),
            ],
        ),
    ]
