<template>
    <div class="register-container">
      <h2>Register</h2>
      <form @submit.prevent="submitForm">
        <div>
          <label for="username">Username</label>
          <input type="text" id="username" v-model="username" required>
        </div>
        <div>
          <label for="password">Password</label>
          <input type="password" id="password" v-model="password" required>
        </div>
        <div>
          <label for="confirm-password">Confirm Password</label>
          <input type="password" id="confirm-password" v-model="confirmPassword" required>
        </div>
        <button type="submit">Register</button>
      </form>
    </div>
</template>

<script>
import $ from "jquery";
export default {
data() {
    return {
    username:"",
    password: "",
    confirmPassword: "",
    };
},
methods: {
    async submitForm() {
        if(this.password==this.confirmPassword)
        {
          $.ajax({
            url: 'https://localhost:8081/register',
            method: 'POST',
            data: {
              username: this.username,
              password: this.password
            },
            xhrFields: {
              withCredentials: true
            },
            success: (response) => {
              console.log(response)
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

                $.ajax({
                  url: 'https://localhost:8081/saveInOnt',
                  method: 'GET',
                  data: {
                    username: this.username,
                  },
                  xhrFields: {
                    withCredentials: true
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
            error: (error) => {
              this.$notify({
                title: 'User already registered',
                text: 'Please chose an other username.',
                type: 'error',
                duration: 5000, // notification will disappear after 5 seconds
              });
              console.error(error);
            }
          })
        }
        else{
          this.$notify({
                title: 'Please make sure your passwords match. ',
                type: 'error',
                duration: 10000, // notification will disappear after 5 seconds
              });
        } 
    },
},
};
</script>
  

<style>

.register-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width:100vw;
}

.register-container > h2 {
  margin-bottom: 20px;
}

</style>