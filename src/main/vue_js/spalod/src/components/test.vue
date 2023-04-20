<template>
  <div class="menu-container">
    <button class="hamburger-button" @click="toggleMenu">
      <span></span>
      <span></span>
      <span></span>
    </button>
    <ul :class="{'menu-open': isMenuOpen}">
      <li><a href="#">Menu Item 1</a></li>
      <li><a href="#">Menu Item 2</a></li>
      <li><a href="#">Menu Item 3</a></li>
      <li><a href="#">Menu Item 4</a></li>
    </ul>
  </div>
</template>

<script>
export default {
  data() {
    return {
      isMenuOpen: false
    }
  },
  methods: {
    toggleMenu() {
      this.isMenuOpen = !this.isMenuOpen;
    }
  }
}
</script>

<style>
.menu-container {
  position: relative;
  display: inline-block;
}

.hamburger-button {
  position: relative;
  display: inline-block;
  padding: 10px 15px;
  background-color: #fff;
  border: 1px solid #000;
  border-radius: 5px;
}

.hamburger-button span {
  position: absolute;
  display: block;
  width: 25px;
  height: 3px;
  background-color: #000;
  border-radius: 3px;
  transition: all 0.3s ease-in-out;
}

.hamburger-button span:nth-child(1) {
  top: 10px;
}

.hamburger-button span:nth-child(2) {
  top: 50%;
  transform: translateY(-50%);
}

.hamburger-button span:nth-child(3) {
  bottom: 10px;
}

.menu-open {
  display: block;
}

ul {
  position: absolute;
  top: 100%;
  left: 0;
  display: none;
  padding: 10px;
  background-color: #fff;
  border: 1px solid #000;
  border-radius: 5px;
  box-shadow: 0px 2px 5px rgba(0,0,0,0.3);
}

ul li {
  margin: 10px 0;
}

ul li a {
  color: #000;
  text-decoration: none;
}
</style>