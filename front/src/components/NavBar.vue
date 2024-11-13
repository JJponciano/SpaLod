<template>
  <div class="navbar dark">
    <div class="telefon">
      <button class="hamburger-button" @click="togglemenu">
        <span></span>
        <span></span>
        <span></span>
      </button>
      <ul
        :class="['menuopen', menuAnimationClass]"
        @transitionend="onTransitionend"
      >
        <li>
          <button
            v-if="isLogged()"
            @click="navigateTo('ogc-api')"
            :class="{ active: activeTab === 'ogc-api' }"
          >
            SpaLod API
          </button>
        </li>
        <li>
          <button
            @click="navigateTo('login')"
            :class="{ active: activeTab === 'login' }"
          >
            Login
          </button>
        </li>
        <li>
          <button
            @click="navigateTo('register')"
            :class="{ active: activeTab === 'register' }"
          >
            Register
          </button>
        </li>
        <li>
          <button
            v-if="isLogged()"
            @click="navigateTo('admin')"
            :class="{ active: activeTab === 'admin' }"
          >
            Admin
          </button>
        </li>
        <li>
          <button
            @click="navigateTo('docs')"
            :class="{ active: activeTab === 'docs' }"
          >
            Doc
          </button>
        </li>
        <li>
          <button
            @click="navigateTo('external-links')"
            :class="{ active: activeTab === 'external-links' }"
          >
            External Links
          </button>
        </li>
      </ul>
    </div>
    <button v-if="isLogged()" @click="logout()" class="navbar-title">
      Logout
    </button>
    <button
      v-if="!isLogged()"
      @click="navigateTo('login')"
      :class="{ active: activeTab === 'login' }"
    >
      Login
    </button>
    <div class="computer">
      <button
        v-if="isLogged()"
        @click="navigateTo('ogc-api')"
        :class="{ active: activeTab === 'ogc-api' }"
      >
        SpaLod API
      </button>
      <button
        @click="navigateTo('register')"
        :class="{ active: activeTab === 'register' }"
      >
        Register
      </button>
      <button
        v-if="isLogged()"
        @click="navigateTo('admin')"
        :class="{ active: activeTab === 'admin' }"
      >
        Admin
      </button>
      <button
        v-if="isLogged()"
        @click="navigateTo('docs')"
        :class="{ active: activeTab === 'docs' }"
      >
        Docs
      </button>
      <button
        v-if="isLogged()"
        @click="navigateTo('external-links')"
        :class="{ active: activeTab === 'external-links' }"
      >
        External Links
      </button>
    </div>
    <div>
      {{ getUsername() }}
    </div>
  </div>
  <div></div>
</template>

<script>
import { $ajax } from "../services/api";
import { logout, getUsername } from "../services/login";
import { isLogged } from "../services/login";

export default {
  data() {
    return {
      activeTab: "admin",
      isDarkMode: false,
      menuOpen: "menu-closed",
      isAdmin: false,
      menuAnimationClass: "",
      isLoggedIn: false,
      username: "",
    };
  },
  mounted() {},
  methods: {
    isLogged,
    getUsername,
    togglemenu() {
      this.menuOpen =
        this.menuOpen === "menu-closed" ? "menu-open" : "menu-closed";
      this.menuAnimationClass =
        this.menuOpen === "menu-open" ? "slide-in-left" : "slide-out-left";
    },
    closeMenu() {
      this.menuOpen = false;
    },
    navigateTo(page) {
      this.activeTab = page;
      this.$router.push(`/${page}`);
    },
    checkRole() {
      $ajax({
        url: "/admin",
        method: "GET",
        xhrFields: {
          withCredentials: true,
        },
        success: (response) => {
          this.isAdmin = true;
        },
        error: (error) => {
          //console.error(error);
          console.log(error);
        },
      });
    },
    checkLoggedIn() {
      $ajax({
        url: "/status",
        method: "GET",
        xhrFields: {
          withCredentials: true,
        },
        success: (response) => {
          this.isLoggedIn = true;
          this.isAdmin = true;
        },
        error: (error) => {
          console.log(error);
        },
      });
    },
    async logout() {
      await logout();
      window.location.href = "/login";
    },
  },
};
</script>

<style scoped>
.navbar {
  display: flex;
  background-color: rgb(241, 241, 241);
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
}

.navbar.dark {
  background-color: #1a202c;
  color: #fff;
}

.navbar.light {
  background-color: #fff;
  color: #1a202c;
}

.navbar-title {
  border: none;
  cursor: pointer;
  background-color: #ef4444;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  padding: 5px 10px;
  border-radius: 5px;
}

.computer {
  display: block;
  width: 100%;
  text-align: center;
}

.computer button {
  border: none;
  cursor: pointer;
  background-color: transparent;
  font-size: 18px;
  color: #1a202c;
  font-weight: 750;
  padding: 5px 10px;
  border-radius: 5px;
  margin-left: 20px;
  transition: background-color 0.3s ease-in-out;
}

.navbar.dark .computer button {
  color: white;
}

.computer button:hover {
  background-color: #dee1e6;
}

.navbar.dark .computer button:hover {
  background-color: #4a5568;
  color: white;
}

.telefon {
  display: none;
}

@media screen and (max-width: 768px) {
  .navbar {
    position: fixed;
  }

  .telefon {
    display: block;
  }

  .computer {
    display: none;
  }

  .hamburger-button {
    position: relative;
    display: inline-block;
    cursor: pointer;
    padding: 30px 42px 10px 10px;
    border: 1px solid #000;
    border-radius: 5px;
    background-color: transparent;
    transition: background-color 0.2s ease-in-out;
  }

  .navbar.dark .hamburger-button {
    background-color: transparent;
    border: 1px solid white;
  }

  .hamburger-button span {
    position: absolute;
    display: block;
    width: 31px;
    height: 3px;
    background-color: #000;
    border-radius: 5px;
    transition: all 0.3s ease-in-out;
  }

  .navbar.dark .hamburger-button span {
    background-color: whitesmoke;
  }

  .hamburger-button span:nth-child(1) {
    top: 25%;
  }

  .hamburger-button span:nth-child(2) {
    top: 50%;
  }

  .hamburger-button span:nth-child(3) {
    top: 75%;
  }

  .slide-in-left {
    animation: slide-in-left 0.7s forwards;
  }

  .slide-out-left {
    animation: slide-out-left 0.7s forwards;
  }

  @keyframes slide-in-left {
    from {
      transform: translateX(0%);
    }

    to {
      transform: translateX(+100%);
    }
  }

  @keyframes slide-out-left {
    from {
      transform: translateX(100%);
    }

    to {
      transform: translateX(0%);
    }
  }

  ul {
    position: absolute;
    bottom: -90vh;
    top: 125%;
    left: -225px;
    padding: 10px;
    background-color: #fff;
    border: 1px solid #000;
    border-radius: 5px;
    box-shadow: 0px 2px 5px rgba(0, 0, 0, 0.3);
  }

  ul button {
    padding: 10px 20px;
    border-radius: 5px;
    border: none;
    background-color: transparent;
    color: inherit;
    cursor: pointer;
    transition: background-color 0.2s ease-in-out;
    font-size: 18px;
    font-weight: bold;
    margin-top: 10px;
    width: 100%;
    text-align: left;
  }

  .navbar.dark ul {
    background-color: #4a5568;
  }

  .navbar.light ul {
    background-color: aliceblue;
  }

  ul li {
    margin: 20px 10px 20px 20px;
  }
}
</style>
