import axios from "axios";

const token =
  "eyJhbGciOiJSUzUxMiJ9.eyJpc3MiOiJIYWxvIE93bmVyIiwic3ViIjoiYWRtaW4iLCJleHAiOjE2NTU4OTQxNTQsImlhdCI6MTY1NTgwNzc1NCwic2NvcGUiOlsiUk9MRV9zdXBlci1yb2xlIl19.Gnj0rM8DU2bP1KcgKBUVaKf6zs1pDqGxYvii9zxG4lFv4rVZ_uNGXyfhi9V10vRK0GM4v4NEuMtX9-DYnqAV0wR2JcoFevPrJnHHWsvnFrOQm32qeMpew3PsZ5-YAwi9n8Y9GpAcQz_6aWsEuRwm9w5CC3A67CrYPfCK5qwuR5FFLfiMRqPAqNNuZ4r2IfoSZUvXy4HxhUS-01J2BCqP3-hbdN_-tFHCDxtIO637a51EsCmRItY5wSVNmwYPaPOYV7lbHxzBIKXw5RNXg6SrQCSLTVaaJCXsZjwIirk02RQACr6oqTHPbriBVuu-SIgPXS5PJ9i4VaMCn-z8t-oZlQ";

const axiosInstance = axios.create({
  headers: {
    Authorization: `Bearer ${token}`,
  },
  baseURL: "http://localhost:8090",
});

export default axiosInstance;
