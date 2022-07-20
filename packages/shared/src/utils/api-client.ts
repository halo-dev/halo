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
import type { AxiosInstance } from "axios";

let apiUrl: string | undefined;
const axiosInstance = axios.create({
  withCredentials: true,
});

let apiClient = setupApiClient(axiosInstance);

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

const setApiUrl = (url: string) => {
  axiosInstance.defaults.baseURL = url;
  apiUrl = url;
  apiClient = setupApiClient(axiosInstance);
};

function setupApiClient(axios: AxiosInstance) {
  return {
    extension: {
      configMap: new V1alpha1ConfigMapApi(undefined, apiUrl, axios),
      personalAccessToken: new V1alpha1PersonalAccessTokenApi(
        undefined,
        apiUrl,
        axios
      ),
      roleBinding: new V1alpha1RoleBindingApi(undefined, apiUrl, axios),
      role: new V1alpha1RoleApi(undefined, apiUrl, axios),
      setting: new V1alpha1SettingApi(undefined, apiUrl, axios),
      reverseProxy: new PluginHaloRunV1alpha1ReverseProxyApi(
        undefined,
        apiUrl,
        axios
      ),
      plugin: new PluginHaloRunV1alpha1PluginApi(undefined, apiUrl, axios),
      user: new V1alpha1UserApi(undefined, apiUrl, axios),

      // TODO optional
      link: new CoreHaloRunV1alpha1LinkApi(undefined, apiUrl, axios),
      linkGroup: new CoreHaloRunV1alpha1LinkGroupApi(undefined, apiUrl, axios),
    },
    user: new ApiHaloRunV1alpha1UserApi(undefined, apiUrl, axios),
  };
}

export { apiClient, setApiUrl };
