import { ref } from "vue";
import { $ajax } from "./api";
import { refreshGeoData } from "./geo";

const progress = ref(-1);

export function getProgress() {
  return progress.value;
}

export function uploadGeo(file, metadata) {
  let formData = new FormData();
  formData.append("file", file);
  formData.append("metadata", JSON.stringify(metadata));

  $ajax({
    xhr: () => {
      const xhr = new XMLHttpRequest();

      xhr.upload.onprogress = (event) => {
        progress.value = Math.floor((event.loaded / event.total) * 100);
      };

      return xhr;
    },
    url: "/api/upload-file/",
    type: "POST",
    data: formData,
    processData: false,
    contentType: false,
    success: async () => {
      progress.value = -1;
      await refreshGeoData();
    },
    error: (error) => {
      console.error(error);
      alert(error.responseJSON.error);
      progress.value = -1;
    },
  });
}
