import axios from "axios";

const token =
  "eyJhbGciOiJSUzUxMiJ9.eyJpc3MiOiJIYWxvIE93bmVyIiwic3ViIjoiYWRtaW4iLCJleHAiOjE2NTY1NjAzMjQsImlhdCI6MTY1NjQ3MzkyNCwic2NvcGUiOlsiUk9MRV9zdXBlci1yb2xlIl19.CloSZG47VmzUd7uLUHb1q-0mmu0SE3zoRVHVAFeN4P9izXc-Sy2ZZc_3QKQPb4HyfWV1HoFDr2GeOKudbkAJIBvvJ1rr-NLh1szj0le06aeK1U2RqANgR0oI4HxSWrPu9Mnz-r04L1D-KrrmnG7votQ6ekeTPwlKIyEI6zl-0NlCqGdv0g3FV37dDbu8-9QrgtTOvRcpymLYGJ-vY2CqGm1z7vNbgMSneIhbRxe6lXfwkbvjtCp3P3Mkj5_14cAt_FQEUHGYnZZH_dNtdU2NZBpMXIgZYE0CaTBes4ciH1N62cwIT05iBTN_tdmDwEsg-BYjvI5IqVmrt2CwT_IcBg"
const axiosInstance = axios.create({
  headers: {
    Authorization: `Bearer ${token}`,
  },
  baseURL: "http://localhost:8090",
});

export { axiosInstance };
