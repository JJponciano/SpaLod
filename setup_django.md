pip install django djangorestframework dj-rest-auth social-auth-app-django django-allauth
django-admin startproject spalod
cd spalod
django-admin startapp spalod_app



## User creation
python manage.py shell


from django.contrib.auth.models import User
# Create a user with a username and password
user = User.objects.create_user(username='testuser', password='testpassword')
# Optionally, you can add an email
user.email = 'testuser@example.com'
user.save()

python manage.py makemigrations spalod_app
python manage.py migrate
python manage.py runserver  
## GraphDB
 http://localhost:7200/repositories/Spalod

curl -X POST http://127.0.0.1:8000/api/auth/login/ -d "username=Falk&password=GNybRXbC563"     


 curl -X POST http://127.0.0.1:8000/api/sparql-query/ \
-H "Content-Type: application/json" \
-H "Authorization: Token 1a2926e119b4560da1faa48d3aead3a2ce1a5f78" \
-d '{"query": "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10"}'