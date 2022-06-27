import axios from "axios";

const token =
  "eyJhbGciOiJSUzUxMiJ9.eyJpc3MiOiJIYWxvIE93bmVyIiwic3ViIjoiYWRtaW4iLCJleHAiOjE2NTYzODYyODYsImlhdCI6MTY1NjI5OTg4Niwic2NvcGUiOlsiUk9MRV9zdXBlci1yb2xlIl19.uiBxplxctL3a8G_pNWJzEKEMD-a11VPygHh-yUmm3jaZadt5KWIonkuMl3bSASC96NJn3Lmo1OtSagWIkRWUrWqM4r4_sj3NDOiDpcjxy2msrpe8v9sp_BA4yGjAoaoMBEvdjZ3sQ7L9gJKHhL9bTNsXiSkjS5b7OJLujuF5VFJMp-fmzUDz8-EC5u43wz8MUF88bMk4gUg3nOJYnSTRlrU6759IggEM_0SjZu9wewcLxxa4Vhson_mN6hTObsOyWYjMjTbMlSBQiDfN9IXZ2cRZTnE2me0pT6J0AyP33qBp69B-K0cTaUO2Tcx5_BuWHGUCFUAFwE5FKchGgr6NuA"
const axiosInstance = axios.create({
  headers: {
    Authorization: `Bearer ${token}`,
  },
  baseURL: "http://localhost:8090",
});

export { axiosInstance };
