<script>
import NavBar from "./components/NavBar.vue";
import PopUp from "./components/PopUp.vue";
import PopUpC from "./components/PopUpC.vue";
import { checkLogin } from "./services/login";

export default {
  components: {
    NavBar,
    PopUp,
    PopUpC,
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
  async beforeCreate() {
    await checkLogin();
  },
  methods: {
    onUnselectCSV() {
      this.chooseCSV = false;
    },
    onUnselectJson() {
      this.chooseJson = false;
    },
    onClosepopUp() {
      this.popup = false;
    },
    onClosepopUpC() {
      this.popupC = false;
    },
    goHome() {
      this.$router.push('/admin')
    },
    goToLogin() {
      this.$router.push('/login')
    },
    handleCatalogUpdate(data) {
      this.receivedData = data;
    },
    logUsername(data) {
      this.username = data;
    },
  },
};
</script>

<template>
  <div class="app">
    <div class="navbar">
      <NavBar @username-updated="logUsername"></NavBar>
    </div>
    <div class="main">
      <RouterView />
    </div>
  </div>
  <div class="popup">
    <PopUp :chooseCSV="chooseCSV" :chooseJson="chooseJson" :popup="popup" @CSVBack="onUnselectCSV"
      @JsonBack="onUnselectJson" @popupBack="onClosepopUp"></PopUp>
  </div>
  <div class="popupC">
    <PopUpC :popupC="popupC" @popupCBack="onClosepopUpC" @Catalog-data="handleCatalogUpdate"></PopUpC>
  </div>
  <notifications />
  <notifications group="login-success" @click="goHome()" />
  <notifications group="register-success" @click="goToLogin()" />
  <notifications group="notLoggedIn" @click="goToLogin()" />
</template>

<style scoped lang="scss">
.navbar {
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

.popup {
  position: fixed;
  top: 35%;
  left: 40%;
  width: fit-content;
  z-index: 100;
}

.popupC {
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

  .popup {
    top: 30vh;
    left: auto;
    right: 5vh;
  }

  .popupC {
    top: 30vh;
    left: auto;
    right: 5vh;
  }
}
</style>
