import { $fetch } from "./api";

export async function checkLogin() {
  if (window.location.pathname.startsWith("/login/gitlab")) {
    const urlParams = new URLSearchParams(window.location.search);
    try {
      await $fetch(import.meta.env.VITE_APP_API_BASE_URL + "/auth/gitlab/", {
        method: "POST",
        body: JSON.stringify({ access_token: urlParams.get("access_token") }),
        headers: { "content-type": "application/json" },
      });
      window.location.href = "/admin";
    } catch {
      window.location.href = "/login";
    }
  } else if (
    window.location.pathname !== "/login" &&
    window.location.pathname !== "/register"
  ) {
    try {
      const res = await fetch(
        import.meta.env.VITE_APP_API_BASE_URL + "/auth/user/"
      );
      if (res.status !== 200) {
        throw "forbidden";
      }
      console.log(await res.json());
    } catch {
      window.location.href = "/login";
    }
  }
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
