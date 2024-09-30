import { createApp } from "vue";
import App from "./App.vue";
import "./assets/main.css";
import "leaflet/dist/leaflet.css";
import Notifications from "@kyvg/vue3-notification";
import { createWebHistory, createRouter } from "vue-router";

import Admin from "./views/Admin.vue";
import Docs from "./views/Docs.vue";
import ExternalLinks from "./views/ExternalLinks.vue";
import Loading from "./views/Loading.vue";
import Login from "./views/Login.vue";
import OgcApi from "./views/OgcApi.vue";
import Register from "./views/Register.vue";

import { checkLogin, isLogged } from "./services/login";

const routes = [
  { path: "/admin", component: Admin },
  { path: "/docs", component: Docs },
  { path: "/external-links", component: ExternalLinks },
  { path: "/login/gitlab/", component: Loading },
  { path: "/login", component: Login },
  { path: "/ogc-api", component: OgcApi },
  { path: "/register", component: Register },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from) => {
  if (
    !isLogged() &&
    to.path !== "/login" &&
    to.path !== "/register" &&
    to.path !== "/login/gitlab/"
  ) {
    return { path: "/login" };
  }
});

checkLogin().then(() => {
  createApp(App).use(Notifications).use(router).mount("#app");
});
