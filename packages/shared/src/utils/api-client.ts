import {
  PluginHaloRunV1alpha1PluginApi,
  V1alpha1UserApi,
  ApiHaloRunV1alpha1UserApi,
  V1alpha1ConfigMapApi,
  V1alpha1PersonalAccessTokenApi,
  V1alpha1RoleBindingApi,
  V1alpha1RoleApi,
  V1alpha1SettingApi,
  PluginHaloRunV1alpha1ReverseProxyApi,
  CoreHaloRunV1alpha1LinkApi,
  CoreHaloRunV1alpha1LinkGroupApi,
} from "@halo-dev/api-client";
import axios from "axios";

const baseUrl = "http://localhost:8090";

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

const apiClient = {
  extension: {
    configMap: new V1alpha1ConfigMapApi(undefined, baseUrl, axiosInstance),
    personalAccessToken: new V1alpha1PersonalAccessTokenApi(
      undefined,
      baseUrl,
      axiosInstance
    ),
    roleBinding: new V1alpha1RoleBindingApi(undefined, baseUrl, axiosInstance),
    role: new V1alpha1RoleApi(undefined, baseUrl, axiosInstance),
    setting: new V1alpha1SettingApi(undefined, baseUrl, axiosInstance),
    reverseProxy: new PluginHaloRunV1alpha1ReverseProxyApi(
      undefined,
      baseUrl,
      axiosInstance
    ),
    plugin: new PluginHaloRunV1alpha1PluginApi(
      undefined,
      baseUrl,
      axiosInstance
    ),
    user: new V1alpha1UserApi(undefined, baseUrl, axiosInstance),

    // TODO optional
    link: new CoreHaloRunV1alpha1LinkApi(undefined, baseUrl, axiosInstance),
    linkGroup: new CoreHaloRunV1alpha1LinkGroupApi(
      undefined,
      baseUrl,
      axiosInstance
    ),
  },
  user: new ApiHaloRunV1alpha1UserApi(undefined, baseUrl, axiosInstance),
};

export { apiClient };
