<script>
import NavBar from './components/NavBar.vue';
import UserActions from './components/UserActions.vue';
import MapView from './components/MapView.vue';
import RDFData from './components/RDFData.vue';
import Login from './components/Login.vue';
import Register from './components/Register.vue';
import PopUp from './components/PopUp.vue';
import PopUpC from './components/PopUpC.vue';
import OgcApi from './components/OgcApi.vue';


export default {
  components: {
    NavBar,
    UserActions,
    MapView,
    RDFData,
    PopUp,
    PopUpC,
    Login,
    Register,
    OgcApi,
  },
  data() {
    return {
      file: null,
      chooseCSV:false,
      chooseJson:false,
      popup:false,
      popupC:false,
      currentPath: window.location.pathname,
      receivedData: null,
      username:""
    };
  },
  computed: {
    currentView() {
      if (this.currentPath === '/' || this.currentPath === '/admin' ) {
        return 'main';
      } else if (this.currentPath === '/login') {
        return 'login';
      } else if (this.currentPath === '/register') {
        return 'register';
      } else if (this.currentPath.startsWith('/spalodWFS')) {
        return 'spalodWFS';
      } else {
        return 'main';
      }
    }
  },
  mounted() {
    window.addEventListener('popstate', () => {
      this.currentPath = window.location.pathname;
    });
  },
  methods: {
    onFileSelected(file) {
      this.file = file;
    },
    onChooseCSV(){
      this.chooseCSV=true;
    },
    onChooseJson(){
      this.chooseJson=true;
    },
    onUnselectCSV(){
      this.chooseCSV=false;
    },
    onUnselectJson(){
      this.chooseJson=false;
    },
    onShowpopup(){
      this.popup=true;
    },
    onShowpopupC(){
      this.popupC=true;
    },
    onClosepopUp(){
      this.popup=false;
    },
    onClosepopUpC(){
      this.popupC=false;
    },
    goHome(){
      window.location.href="/";
    },
    goToLogin(){
      window.location.href="/login";
    },
    handleCatalogUpdate(data){
      this.receivedData=data;
    },
    logUsername(data){
      this.username=data;
    }
  }
};
</script>

<template>
  <div class="app">
    <div class="navbar">
        <NavBar @username-updated="logUsername"></NavBar>
    </div>
    <div class="main" v-if="currentView === 'main'">
      <div class="user-actions-container">
        <UserActions @file-selected="onFileSelected" @JsonSelected="onChooseJson" @CSVSelected="onChooseCSV" @popupShow="onShowpopup" @popupCShow="onShowpopupC"></UserActions>
      </div>
      <div class="right-container" >
        <div class="map-container">
          <MapView :file="file"></MapView>
        </div>
        <div class="rdf-data-container">
          <RDFData @update="onFileSelected" @popupCShow="onShowpopupC" :file="file" :receivedData="receivedData" :username="username"></RDFData>
        </div>
      </div>
    </div>
    <div class="main" v-if="currentView === 'login'">
      <Login></Login>
    </div>
    <div class="main" v-if="currentView === 'register'">
      <Register></Register>
    </div>
    <div class="main" v-if="currentView === 'spalodWFS'">
      <OgcApi></OgcApi>
    </div>
  </div>
  <div class="popup">
    <PopUp :chooseCSV="chooseCSV" :chooseJson="chooseJson" :popup="popup" @CSVBack="onUnselectCSV" @JsonBack="onUnselectJson" @popupBack="onClosepopUp"></PopUp>
  </div>
  <div class="popupC">
    <PopUpC :popupC="popupC" @popupCBack="onClosepopUpC" @Catalog-data="handleCatalogUpdate"></PopUpC>
  </div>
  <notifications/>
  <notifications group="login-success" @click="goHome()" />
  <notifications group="register-success" @click="goToLogin()" />
  <notifications group="notLoggedIn" @click="goToLogin()"/>
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
.popup{
  position: fixed;
  top: 35%;
  left: 40%;
  width: fit-content;
  z-index: 100;
}
.popupC{
  position: fixed;
  top: 35%;
  left: 40%;
  width: fit-content;
  z-index: 100;
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
  .popup{
    top:30vh;
    left:auto;
    right:5vh;
  }
  .popupC{
    top: 30vh;
    left: auto;
    right: 5vh;
  }
}
</style>