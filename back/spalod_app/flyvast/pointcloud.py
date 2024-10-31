from django.conf import settings
from .auth import with_flyvast_auth, auth_request
import requests

@with_flyvast_auth
def create_flyvast_pointcloud(name, size):
    fv_api_url = settings.FLYVAST_API_URL
    
    r = auth_request(requests.post)(
        f'{fv_api_url}/Pointcloud.php?method=createPointcloud', 
        json={
            "useSegmentor": False,
            "size": size,
            "name": name,
            "fileName": name
        }
    )
    
    pointcloud_id = r.json().get("result").get("pointcloudId")
    uuid = r.json().get("result").get("uuid")
    upload_url = r.json().get("result").get("uploadUrl")
    treatment_url = r.json().get("result").get("treatmentUrl")
    
    auth_request(requests.put)(f"{fv_api_url}/Pointcloud.php?method=changePointcloudPublic", json={"pointcloudId": pointcloud_id})
    
    return {
        "pointcloud_id": pointcloud_id,
        "pointcloud_uuid": uuid,
        "upload_url": upload_url,
        "treatment_url": treatment_url
    }