import axios from "axios";

const token =
  "eyJhbGciOiJSUzUxMiJ9.eyJpc3MiOiJIYWxvIE93bmVyIiwic3ViIjoiYWRtaW4iLCJleHAiOjE2NTYwNDM3NTYsImlhdCI6MTY1NTk1NzM1Niwic2NvcGUiOlsiUk9MRV9zdXBlci1yb2xlIl19.XHmq5q9-HWkWQsPdcuvldeiKOQxbKHEd9qP33ZWaLSFVgj5D-8QvfLjuLreMWUBLvZXvsqBuDHpib70gO6V2c4VtUbnAQnzr8oQx4E5ypMnWH4Gdbs8UlSpMGjTPzSk-QNFKB48nMo8wgTcq2oyhBsMEIArKFm7v2pa5dSX1LbWTRRpNfJpHPVwrAPzaNkOs_qasS8QzSTHU1C3wCf_A4lEILVhbrHq_mv9yeMQZL0enD-gpbGXEQzHE59zwxFC7kfgb_YhzYZfzuXAv2BIKn4TU14W9aW4HySymsqM0ItO5RT3GmJgurbX9USHhIKfGdTFEG1cfgZ0ZJNNOOLEndA";

const axiosInstance = axios.create({
  headers: {
    Authorization: `Bearer ${token}`,
  },
  baseURL: "http://localhost:8090",
});

export default axiosInstance;
