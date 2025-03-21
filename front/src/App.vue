<script>
import NavBar from "./components/NavBar.vue";
import { checkLogin } from "./services/login";

export default {
  components: {
    NavBar,
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
      this.$router.push("/admin");
    },
    goToLogin() {
      this.$router.push("/login");
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
  <notifications />
  <notifications group="login-success" @click="goHome()" />
  <notifications group="register-success" @click="goToLogin()" />
  <notifications group="notLoggedIn" @click="goToLogin()" />
</template>

<style lang="scss">
button {
  border: none;
  cursor: pointer;
  background-color: #ef4444;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  padding: 5px 10px;
  border-radius: 5px;
}

button:hover {
  background-color: lighten($color: #ef4444, $amount: 10%);
  transition: background-color 0.5s ease;
}
.p-autocomplete-overlay {
  background-color: white !important;
  color: black !important;
  box-shadow: 0px 10px 15px 2px #0005 !important;
  min-width: 270px !important;
  transform: translateX(-10px) !important;

  .p-autocomplete-list-container {
    ul {
      li {
        padding: 5px 10px;
      }
    }
  }
}
.p-autocomplete-input {
  border: none !important;
  border-radius: 5px !important;
  padding: 11px !important;
  margin: 0px 15px !important;
  width: 250px !important;
  font-size: 14px !important;
  font-family: revert !important;

  &::placeholder {
    opacity: 0.7;
  }
}
</style>

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
  flex: 1;
  display: flex;
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
