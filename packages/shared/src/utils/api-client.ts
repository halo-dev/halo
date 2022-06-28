import axios from "axios";

const token =
  "eyJhbGciOiJSUzUxMiJ9.eyJpc3MiOiJIYWxvIE93bmVyIiwic3ViIjoiYWRtaW4iLCJleHAiOjE2NTY0NzM3NTMsImlhdCI6MTY1NjM4NzM1Mywic2NvcGUiOlsiUk9MRV9zdXBlci1yb2xlIl19.vJ7l6jo3CJv7h4XTDvqjY70qngpaiiYhJL7_vXNPRY2Rz2NdmosMzy-BIRYPRuJnhU33uQ5LjXY5K2YwyQinrIsT66JsfckuYi6slAQY2rUC3929wC3gBcMJp9Z--VGRA701vDoecGWnGh68XR-RCK_uGcMnNhC4A1bCsb-nrn4TYdUndM0Aa2OsGP6G0IjzuyvXwwoAGB2JojBdGzXVz2KujHsaELziAZ2Kx78wsEIREN0pZGXnapB1-0nqgjLQ9VuGl62bRAkyzirwbasgB6Tk8njz-TR_uIL-smyWt_LUwX5I97lrM5VCtMmBlT999_Zi8MgzWTHeEUobzbI4ng";
const axiosInstance = axios.create({
  headers: {
    Authorization: `Bearer ${token}`,
  },
  baseURL: "http://localhost:8090",
});

export { axiosInstance };
