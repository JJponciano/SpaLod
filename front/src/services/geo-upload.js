import { ref } from "vue";
import { $ajax } from "./api";
import { init as initGeo } from "./geo";

const progress = ref("");

export function getProcess() {
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
        const percentage = Math.floor((event.loaded / event.total) * 100);

        if (percentage === 100) {
          progress.value = "Processing";
        } else {
          progress.value = `Upload: ${percentage}%`;
        }
      };

      return xhr;
    },
    url: "/api/upload-file/",
    type: "POST",
    data: formData,
    processData: false,
    contentType: false,
    success: () => {
      progress.value = "";
      initGeo();
    },
    error: () => {
      alert("Error while checking ontology!");
    },
  });
}
