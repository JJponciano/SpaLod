<template>
  <div class="user-actions-container">
    <UserActions @file-selected="onFileSelected"> </UserActions>
  </div>
  <div class="map-container">
    <MapView></MapView>
  </div>
  <div class="metadatas-container" v-show="file">
    <Metadatas :file="file" @close="file = null"></Metadatas>
  </div>
</template>

<script>
import UserActions from "../components/UserActions.vue";
import MapView from "../components/MapView.vue";
import Metadatas from "../components/Metadatas.vue";

export default {
  components: {
    UserActions,
    MapView,
    Metadatas,
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
    };
  },
  methods: {
    onFileSelected(file) {
      this.file = file;
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
