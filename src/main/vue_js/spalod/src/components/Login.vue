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
  import axios from 'axios';
  import $ from "jquery";
  export default {
    name:'Login',
    data() {
      return {
        username: "",
        password: "",
      };
    },
    methods: {
        async submitForm() {
          $.ajax({
            url: 'https://localhost:8081/login',
            method: 'POST',
            data: {
              username: this.username,
              password: this.password
            },
            xhrFields: {
              withCredentials: true
            },
            success: (response) => {
              console.log(response.cookie)
              if (response.status === 200) {
                console.log('Successful authentication');
                this.$router.push('/');
              }
            },
            error: (error) => {
              console.error(error);
            }
            })
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