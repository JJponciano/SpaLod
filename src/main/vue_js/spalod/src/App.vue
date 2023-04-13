<script setup>
import NavBar from './components/NavBar.vue';
import UserActions from './components/UserActions.vue';

</script>

<template>
  <div class="app">
    <NavBar></NavBar>
    <div class="main">
      <div class="user-actions-container">
        <UserActions></UserActions>
      </div>
    </div>
  </div>
</template>

<style scoped>
.app {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.main {
  flex: 1;
  display: flex;
  flex-direction: row;
}

.user-actions-container {
  flex: 1;
  padding: 20px;
}

@media (min-width: 1024px) {
  
}
</style>
