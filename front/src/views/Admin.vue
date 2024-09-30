<template>
  <div class="user-actions-container">
    <UserActions @properties_unknown="onProperties_unknown" @file-selected="onFileSelected" @JsonSelected="onChooseJson"
      @CSVSelected="onChooseCSV" @popupShow="onShowpopup" @popupCShow="onShowpopupC">
    </UserActions>
  </div>
  <div class="right-container">
    <div class="map-container">
      <MapView :file="file"></MapView>
    </div>
    <div class="rdf-data-container">
      <RDFData @update="onFileSelected" @popupCShow="onShowpopupC" :file="file" :receivedData="receivedData"
        :username="username" :properties_unknown="properties_unknown">
      </RDFData>
    </div>
  </div>
</template>

<script>
import UserActions from "../components/UserActions.vue";
import MapView from "../components/MapView.vue";
import RDFData from "../components/RDFData.vue";

export default {
  components: {
    UserActions,
    MapView,
    RDFData
  },
  data() {
    return {
      file: null,
      chooseCSV: false,
      chooseJson: false,
      popup: false,
      popupC: false,
      receivedData: null,
      properties_unknown: null,
      username: "",
    }
  },
  methods: {
    onFileSelected(file) {
      this.file = file;
    },
    onProperties_unknown(properties_unknown) {
      this.properties_unknown = properties_unknown;
    },
    onChooseCSV() {
      this.chooseCSV = true;
    },
    onChooseJson() {
      this.chooseJson = true;
    },
    onShowpopup() {
      this.popup = true;
    },
    onShowpopupC() {
      this.popupC = true;
    },
  },
}
</script>

<style>
.user-actions-container {
  flex: 0;
  padding: 25px;
  z-index: 3;
}

.right-container {
  display: flex;
  flex-direction: column;
  flex: 2 0 auto;
  align-items: stretch;
  padding: 25px;
  padding-left: 0px;
  z-index: 2;
}

.map-container {
  flex: 1;
  margin: 0 0 20px 0;
}

.rdf-data-container {
  flex: 1;
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

  .map-container {
    margin-top: 120px;
  }
}
</style>