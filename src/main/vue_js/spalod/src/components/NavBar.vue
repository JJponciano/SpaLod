<template>
    <div class="navbar" :class="{ dark: isDarkMode }">
      <div class="navbar-links">
        <button @click="navigateTo('public')" :class="{ active: activeTab === 'public' }">Public</button>
        <button @click="navigateTo('doc')" :class="{ active: activeTab === 'doc' }">Doc</button>
        <button @click="navigateTo('external')" :class="{ active: activeTab === 'external' }">External Links</button>
      </div>
      <button v-if="isAdmin" @click="navigateTo('admin')" class="navbar-title">Admin</button>
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        activeTab: 'admin',
        isDarkMode: false,
        isAdmin: true,
      };
    },
    mounted() {
      window.matchMedia('(prefers-color-scheme: dark)').addListener(event => {
        this.isDarkMode = event.matches;
      });
    },
    methods: {
      navigateTo(page) {
        this.activeTab = page;
      },
    },
  };
  </script>
  
  <style scoped>
  .navbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
  }
  
  .navbar.dark {
    background-color: #1A202C;
    color: #fff;
  }
  
  .navbar.light {
    background-color: #fff;
    color: #1A202C;
  }
  
  .navbar-links button {
    background-color: transparent;
    color: inherit;
    border: none;
    margin: 0 10px;
    cursor: pointer;
    font-size: 18px;
    font-weight: bold;
    padding: 5px 10px;
    border-radius: 5px;
    transition: background-color 0.2s ease-in-out;
  }
  
  .navbar-links button:hover, .navbar-links button.active {
    background-color: #4A5568;
    color: #fff;
  }
  
  .navbar-title {
    border: none;
    cursor: pointer;
    background-color: #EF4444;
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    padding: 5px 10px;
    border-radius: 5px;
  }
  </style>
  