import axios from "axios";

const token =
"eyJhbGciOiJSUzUxMiJ9.eyJpc3MiOiJIYWxvIE93bmVyIiwic3ViIjoiYWRtaW4iLCJleHAiOjE2NTYxMzk1MzcsImlhdCI6MTY1NjA1MzEzNywic2NvcGUiOlsiUk9MRV9zdXBlci1yb2xlIl19.QUAe5lD2chY5NG_bXwqPxk2y4gxUifz-_H-dN_ZcggdQ0Nm2m5R168VUt33jJ4gCUW5HQx7hqJY_V60jDQ0nAP3VjrfeDNG7BvoaqLyoThg40f1oD-AUoB648b_TZga0oKtwkCyLYi_qkgmvF6UZumMsI9rTVHxef6O5vAbSrFFc2pZ90xNH9PN7ILJDimv_2I1IRpBtASJG1cM0yWYUmHWO-yW2UwUKGxJTf3TN_OvJqHcMRqD7Y5Fe1BVhZfzY8UX6KygG21eKd26hLNIQz6xx-O85HTj8HM5CJUR7jSp3Oo7rtQwIbEDTdGeTqmFM96ufL4nVYdpDuONm0zSxYQ"
const axiosInstance = axios.create({
  headers: {
    Authorization: `Bearer ${token}`,
  },
  baseURL: "http://localhost:8090",
});

export default axiosInstance;
