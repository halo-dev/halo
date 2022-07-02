import axios from "axios";

const axiosInstance = axios.create({
  baseURL: "http://localhost:8090",
  withCredentials: true,
});

axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    console.log("error", error);
    if (error.response.status === 401) {
      window.location.href = "/#/login";
    }
    return Promise.reject(error);
  }
);

export { axiosInstance };
