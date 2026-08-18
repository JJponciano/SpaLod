<template>
  <div class="user-actions-container">
    <UserActions 
      @file-selected="onFileSelected" 
      @evaluate-dataset="onEvaluateDataset">
    </UserActions>
  </div>
  <div class="map-container">
    <MapView @add-feature="onAddFeature"></MapView>
    <div
      v-if="showEvaluateModal"
      class="evaluate-modal-overlay"
      @click="showEvaluateModal = false"
    >
      <div class="evaluate-modal" @click.stop>
        <h2>Metadata Evaluation</h2>

        <div v-if="evaluationResult">
          <p><strong>Letters:</strong> {{ evaluationResult.letters || "-" }}</p>
          <p><strong>Stars:</strong> {{ evaluationResult.stars || "-" }}</p>

          <hr />

          <div v-if="evaluationResult.details">
            <div
              v-for="(item, key) in evaluationResult.details"
              :key="key"
              class="evaluation-row"
            >
              <strong>{{ key }}:</strong>
              {{ item.passed ? "PASS" : "FAIL" }} - {{ item.reason }}
            </div>
          </div>
        </div>

        <button class="close-btn" @click="showEvaluateModal = false">
          Close
        </button>
      </div>
    </div>
  </div>
  <div class="metadatas-container" v-show="file || latlng">
    <Metadatas
      :file="file"
      :latlng="latlng"
      @close="(file = null), (latlng = null)"
    ></Metadatas>
  </div>
</template>

<script>
import UserActions from "../components/UserActions.vue";
import MapView from "../components/MapView.vue";
import Metadatas from "../components/Metadatas.vue";
import { getDataset } from "../services/api-geo";

export default {
  components: {
    UserActions,
    MapView,
    Metadatas,
  },
  data() {
    return {
      file: null,
      latlng: null,
      chooseCSV: false,
      chooseJson: false,
      popup: false,
      popupC: false,
      receivedData: null,
      properties_unknown: null,
      username: "",
      showEvaluateModal: false,
      evaluationResult: null,
    };
  },
  methods: {
    onFileSelected(file) {
      this.file = file;
    },
    onAddFeature({ lat, lng }) {
      this.latlng = { lat, lng };
    },
    async onEvaluateDataset(datasetId) {
      try {
        const res = await getDataset(datasetId);

        const metadataUrl = res
          .map((x) => x.metadatas)
          .find((x) => x.displayKey === "metadata_file_url")?.value;

        if (!metadataUrl) {
          alert("No metadata XML found for this dataset.");
          return;
        }

        const response = await fetch(
          `/api/metadata/evaluate/?metadata_file_url=${encodeURIComponent(metadataUrl)}`
        );

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }

        const result = await response.json();

        this.evaluationResult = result;
        this.showEvaluateModal = true;
      } catch (error) {
        console.error("Metadata evaluation failed:", error);
        alert("Metadata evaluation failed.");
      }
    },
  },
};
</script>

<style scoped>
:host {
  display: flex;
}

.map-container {
  flex: 1;
  position: relative;
}

.rdf-data-container {
  position: absolute;
  right: 0px;
  bottom: 0px;
  z-index: 2;
  padding: 10px;
}

.metadatas-container {
  position: absolute;
  top: 0px;
  left: 0px;
  width: 100%;
  height: 100%;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.5);
}
.evaluate-modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  padding: 24px;
}

.evaluate-modal {
  background: #1a1a1a;
  color: #fff;
  width: clamp(500px, 65vw, 1000px);
  max-height: 85vh;
  overflow: auto;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.35);
}

.close-btn {
  margin-top: 16px;
  width: auto;
}

.evaluation-row {
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .right-container {
    flex: 1;
    padding: 10px;
  }

  .user-actions-container {
    padding: 10px;
    height: fit-content;
    position: absolute;
  }
}
</style>
