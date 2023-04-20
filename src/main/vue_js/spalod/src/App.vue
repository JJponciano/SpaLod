<script>
import NavBar from './components/NavBar.vue';
import UserActions from './components/UserActions.vue';
import MapView from './components/MapView.vue';
import RDFData from './components/RDFData.vue';

export default {
  components: {
    NavBar,
    UserActions,
    MapView,
    RDFData
  }
}

</script>

<template>
  <div class="app">
    <div class="main">
      <div class="user-actions-container" v-if="$route.path !== '/login'">
        <UserActions></UserActions>
      </div>
      <div class="right-container" v-if="$route.path !== '/login'">
        <div class="map-container">
          <MapView></MapView>
        </div>
        <div class="rdf-data-container">
          <RDFData></RDFData>
        </div>
      </div>
      <div class="navbar">
        <NavBar></NavBar>
      </div>
      <router-view></router-view>
    </div>
  </div>
</template>


<style scoped>

.navbar{
  z-index: 10;
}

.app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  z-index: 1;
}
.main {
  display: flex;
  flex-direction: row;
  flex: 1;
  align-items: stretch;
  justify-content: space-between;
}

.user-actions-container {
  flex: 1;
  padding: 75px 25px 0 25px;
  z-index: 3;
}

.right-container {
  display: flex;
  flex-direction: column;
  flex: 2 0 auto;
  align-items: stretch;
  padding: 75px 25px 0 0;
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
  .main {
    flex-direction: column;
    display: flex;
  }

  .right-container {
    flex: 1;
    padding: 10px 0px 10px;
  }
  .user-actions-container{
    padding: 70px 125px 0px;
    height: fit-content;
    position: absolute;
  }
  .map-container{
    margin-top: 120px;
  }
}
</style>