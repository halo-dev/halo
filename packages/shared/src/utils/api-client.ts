import {
  ApiHaloRunV1alpha1PluginApi,
  ApiHaloRunV1alpha1UserApi,
  PluginHaloRunV1alpha1PluginApi,
  PluginHaloRunV1alpha1ReverseProxyApi,
  V1alpha1ConfigMapApi,
  V1alpha1PersonalAccessTokenApi,
  V1alpha1RoleApi,
  V1alpha1RoleBindingApi,
  V1alpha1SettingApi,
  V1alpha1UserApi,
  V1alpha1MenuApi,
  V1alpha1MenuItemApi,
  ThemeHaloRunV1alpha1ThemeApi,
  ApiHaloRunV1alpha1ThemeApi,
} from "@halo-dev/api-client";
import type { AxiosInstance } from "axios";
import axios from "axios";

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
      theme: new ThemeHaloRunV1alpha1ThemeApi(undefined, apiUrl, axios),
      menu: new V1alpha1MenuApi(undefined, apiUrl, axios),
      menuItem: new V1alpha1MenuItemApi(undefined, apiUrl, axios),
    },
    user: new ApiHaloRunV1alpha1UserApi(undefined, apiUrl, axios),
    plugin: new ApiHaloRunV1alpha1PluginApi(undefined, apiUrl, axios),
    theme: new ApiHaloRunV1alpha1ThemeApi(undefined, apiUrl, axios),
  };
}

export { apiClient, setApiUrl };
