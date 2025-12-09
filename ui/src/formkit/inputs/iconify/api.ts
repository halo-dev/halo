import axios from "axios";

export const ICONIFY_BASE_URL = "https://api.iconify.design";

export const iconifyClient = axios.create({
  baseURL: ICONIFY_BASE_URL,
});
