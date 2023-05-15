import { createApp } from 'vue';
import App from './App.vue';
import './assets/main.css';
import 'leaflet/dist/leaflet.css';
import Notifications from '@kyvg/vue3-notification'

let app = createApp(App);
app.use(Notifications)
app.mount('#app');
