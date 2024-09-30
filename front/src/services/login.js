import { $fetch, $ajax } from "./api";

let logged = false;
let username = "";

export async function checkLogin() {
  if (window.location.pathname.startsWith("/login/gitlab")) {
    const urlParams = new URLSearchParams(window.location.search);
    $fetch(import.meta.env.VITE_APP_API_BASE_URL + "/auth/gitlab/", {
      method: "POST",
      body: JSON.stringify({ access_token: urlParams.get("access_token") }),
      headers: { "content-type": "application/json" },
    })
      .then(() => {
        window.location.href = "/admin";
      })
      .catch(() => {
        window.location.href = "/login";
      });
  } else {
    try {
      const res = await fetch(
        import.meta.env.VITE_APP_API_BASE_URL + "/auth/user/"
      );
      if (res.status !== 200) {
        logged = false;
        throw "forbidden";
      }
      logged = true;
      username = (await res.json()).username;
    } catch {
      if (
        window.location.pathname !== "/login" &&
        window.location.pathname !== "/register"
      ) {
        window.location.href = "/login";
      }
      logged = false;
    }
  }
}

export async function logout() {
  return new Promise((resolve, reject) => {
    $ajax({
      url: import.meta.env.VITE_APP_API_BASE_URL + "/auth/logout/",
      method: "POST",
      xhrFields: {
        withCredentials: true,
      },
      success: () => {
        logged = false;
        resolve();
      },
      error: (error) => {
        reject(error);
      },
    });
  });
}

export function isLogged() {
  return logged;
}

export function getUsername() {
  return username;
}

export function cookies() {
  return document.cookie
    .split(";")
    .map((v) => v.split("="))
    .reduce((acc, v) => {
      if (v.length === 2) {
        acc[decodeURIComponent(v[0].trim())] = decodeURIComponent(v[1].trim());
      }
      return acc;
    }, {});
}
