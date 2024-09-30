<template>
  <div class="user-actions-container">
    <UserActions @properties_unknown="onProperties_unknown" @file-selected="onFileSelected" @JsonSelected="onChooseJson"
      @CSVSelected="onChooseCSV" @popupShow="onShowpopup" @popupCShow="onShowpopupC">
    </UserActions>
  </div>
  <div class="map-container">
    <MapView :file="file"></MapView>
  </div>
  <div class="rdf-data-container">
    <RDFData @update="onFileSelected" @popupCShow="onShowpopupC" :file="file" :receivedData="receivedData"
      :username="username" :properties_unknown="properties_unknown">
    </RDFData>
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
  position: absolute;
  left: 0px;
  bottom: 0px;
  z-index: 2;
  padding: 10px;
}

.map-container {
  position: absolute;
  top: 0px;
  left: 0px;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.rdf-data-container {
  position: absolute;
  right: 0px;
  bottom: 0px;
  z-index: 2;
  padding: 10px;
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