import axios from "axios";

const iconifyClient = axios.create({
  baseURL: "https://api.iconify.design",
});

export default iconifyClient;
