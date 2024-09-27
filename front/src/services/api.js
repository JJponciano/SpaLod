import $ from "jquery";
import { cookies } from "../services/login";

export function $ajax(args) {
  if (!args.headers) {
    args.headers = {};
  }
  args.headers["X-CSRFToken"] = cookies()["csrftoken"];

  return $.ajax(args);
}

export function $fetch(...args) {
  if (!args[1]) {
    args[1] = {};
  }
  if (!args[1].headers) {
    args[1].headers = {};
  }
  args[1].headers["X-CSRFToken"] = cookies()["csrftoken"];

  return fetch(...args);
}
