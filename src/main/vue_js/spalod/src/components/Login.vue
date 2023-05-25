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
      <p> Login with OAuth 2.0</p>
      <button @click="oauthLogin()">Github</button>
    </div>
</template>
  
<script>
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
              console.log(response);
              $.ajax({
                url: 'https://localhost:8081/uuid',
                method: 'GET',
                data: {
                  username: this.username,
                },
                xhrFields: {
                  withCredentials: true
                },
                success: (response) => {
                  console.log(response)
                  this.$notify({
                    title: 'Login successful',
                    text: 'You have successfully logged in! Click here to go back to default page.',
                    type: 'success',
                    group: 'login-success',
                    duration: 50000, 
                    });
                  localStorage.setItem('username', this.username);
                  localStorage.setItem('UUID',response);
                }
              })
              },
            error: (error) => {
              this.$notify({
                title: 'Login failed',
                text: 'Please check your credentials and try again.',
                type: 'error',
                duration: 5000 // notification will disappear after 5 seconds
              });
              console.error(error);
            }
            })
      },
      oauthLogin(){
        window.location.href="https://localhost:8081/oauth2/authorization/github";
      }
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