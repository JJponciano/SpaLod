<template>
    <nav class="navbar">
      <div class="navbar-brand">
        <a href="#">Mon Site</a>
      </div>
      <button class="navbar-toggler" @click="toggleMenu">
        <span></span>
        <span></span>
        <span></span>
      </button>
      <div class="navbar-menu" :class="{ active: menuOpen }">
        <ul class="navbar-nav">
          <li class="nav-item active">
            <a class="nav-link" href="#">Accueil</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#">À propos</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#">Services</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#">Contact</a>
          </li>
        </ul>
      </div>
    </nav>
  </template>
  
  <script>
  export default {
    name: "Navbar",
    data() {
      return {
        menuOpen: false,
      };
    },
    methods: {
      toggleMenu() {
        this.menuOpen = !this.menuOpen;
      },
      closeMenu() {
        this.menuOpen = false;
      },
    },
    mounted() {
      window.addEventListener("resize", this.closeMenu);
    },
    beforeDestroy() {
      window.removeEventListener("resize", this.closeMenu);
    },
  };
  </script>
  
  <style>
  .navbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    background-color: #eee;
    position: relative;
  }
  
  .navbar-brand {
    font-weight: bold;
    font-size: 1.5rem;
  }
  
  .navbar-menu {
    display: flex;
    flex-direction: column;
    position: absolute;
    top: 100%;
    left: 0;
    width: 100%;
    background-color: #eee;
    z-index: 1;
    overflow: hidden;
    max-height: 0;
    transition: max-height 0.3s ease-in-out;
  }
  
  .navbar-menu.active {
    max-height: 50rem;
  }
  
  .navbar-nav {
    display: flex;
    flex-direction: column;
    padding: 0;
    margin: 0;
    list-style: none;
  }
  
  .nav-item {
    margin: 0.5rem 0;
  }
  
  .nav-link {
    display: block;
    padding: 0.5rem;
    color: #333;
    text-decoration: none;
    transition: background-color 0.3s ease-in-out;
  }
  
  .nav-link:hover {
    background-color: #ddd;
  }
  
  @media screen and (min-width: 768px) {
    .navbar {
      flex-direction: row;
      align-items: center;
    }
  
    .navbar-menu {
      position: static;
      flex-direction: row;
      max-height: none;
      overflow: visible;
    }
  
    .navbar-nav {
      flex-direction: row;
      margin-left: auto;
    }
  
    .nav-item {
      margin: 0 0.5rem;
    }
  
    .navbar-toggler {
      display: none;
    }
  }
  </style>