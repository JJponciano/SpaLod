# Spalod Setup Guide

## Installation

1. Install required dependencies:
   ```bash
   pip install django djangorestframework dj-rest-auth social-auth-app-django
   ```

2. Create a new Django project and app:
   ```bash
   django-admin startproject spalod
   cd spalod
   django-admin startapp spalod_app
   ```

## User Creation

1. Create a new user:
   ```bash
   python manage.py shell
   ```

   Inside the Django shell:
   ```python
   from django.contrib.auth.models import User
   # Create a user with username and password
   user = User.objects.create_user(username='testuser', password='testpassword')
   user.email = 'testuser@example.com'
   user.save()
   ```

## Migrations

1. Apply migrations:
   ```bash
   python manage.py makemigrations spalod_app
   python manage.py migrate
   ```

2. Run the development server:
   ```bash
   python manage.py runserver
   ```

## GraphDB

Ensure GraphDB is running and accessible at:
   ```
   http://localhost:7200/repositories/Spalod
   ```

## Authentication and SPARQL Query

1. Log in to get an authentication token:
   ```bash
   curl -X POST http://127.0.0.1:8000/api/auth/login/ -d "username=Falk&password=GNybRXbC563"
   ```

2. Run a SPARQL query:
   ```bash
   curl -X POST http://127.0.0.1:8000/api/sparql-query/ \
   -H "Content-Type: application/json" \
   -H "Authorization: Token 1a2926e119b4560da1faa48d3aead3a2ce1a5f78" \
   -d '{"query": "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10"}'
   ```