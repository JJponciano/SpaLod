# Django installation


```bash
python -m pip install django 
django-admin startproject spalod 
python spalod/manage.py runserver 8080
cd spalod/
```
# Tutorial
see: https://docs.djangoproject.com/en/4.1/intro/tutorial01/


Optionaly change `TIME_ZONE = 'Europe/Paris'` in settings and create your models in `geotime/models.py`

```
python manage.py migrate
```

intall the app by modifying settings:
```
INSTALLED_APPS = [
    'geotime.apps.GeotimeConfig',
     'django.contrib.admin',
    'django.contrib.auth',
    ...
```

then:
```
python manage.py makemigrations geotime
```
to check the databased:
```
 python manage.py sqlmigrate polls 0001
```

Now, run migrate again to create those model tables in your database:
```
python manage.py migrate
```

### The three-step guide to making model changes:

1. Change your models (in models.py).
2. Run `python manage.py makemigrations geotime` to create migrations for those changes
3. Run `python manage.py migrate` to apply those changes to the database.

# API
````
python manage.py shell
from geotime.models import *
User.objects.all()

user = User.objects.create_user(
    username="Claire",
    email="claire.ponciano@hs-mainz.de",
    password="i3mainzBKG"
)

profile = Profile(user=user, company="i3mainz", rights="admin")
profile.save()
````
Example of view:
````
u= User.objects.get(id=1)
u.email
u.delete()
````


Create admin:
````
python manage.py createsuperuser
python manage.py runserver
````
Then go to the site with `/admin` and login.
If you creates objects models and don't see them: you should specify that they have an admin interface. To do this, open the `geotime/admin.py` file, and edit it to look like this:

````
from django.contrib import admin

from .models import YOUR_MODEL

admin.site.register(YOUR_MODEL)
````

