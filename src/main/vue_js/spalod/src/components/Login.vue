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
            try {
            const response = await axios.post(`http://localhost:8081/login`, null, { params: {
                username:this.username,
                password:this.password
            }}).then((response) => {
                if(response.status==200)
                {
                    console.log("Successful authentication");
                    this.$router.push('/');
                }
            });
            
            

            } catch (error) {
            console.error(error);
            }
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