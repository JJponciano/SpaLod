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
import axios from 'axios';
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
            try {
            const response = await axios.post(`https://localhost:8081/register`, null, { params: {
                username:this.username,
                password:this.password
              }}).then((response) => {
                if(response.status==200)
                {
                    console.log("User successfully registered");
                    window.location.href='/';
                }
            });
            
            } 
            catch (error) {
              console.error(error);
            }
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