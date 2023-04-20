<template>
    <div class ="login-container">
      <h2>Login</h2>
      <form @submit.prevent="submitForm">
        <div>
          <label for="username">Username</label>
          <input type="username" id="username" v-model="username" required>
        </div>
        <div>
          <label for="password">Password</label>
          <input type="password" id="password" v-model="password" required>
        </div>
        <button type="submit">Login</button>
      </form>
    </div>
</template>
  
<script>
  export default {
    name:'Login',
    data() {
      return {
        username: "",
        password: "",
      };
    },
    methods: {
      submitForm() {
        const enteredUser=this.username;
        const enteredPassword=this.password;
        console.log(enteredUser+enteredPassword);
      },
    },
  };
</script>

<style>
.login-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width:100vw;
}

.login-container > h2 {
  margin-bottom: 20px;
}
</style>