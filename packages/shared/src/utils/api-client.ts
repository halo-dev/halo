import {
  ApiHaloRunV1alpha1ContentApi,
  ApiHaloRunV1alpha1PluginApi,
  ApiHaloRunV1alpha1PostApi,
  ApiHaloRunV1alpha1ThemeApi,
  ApiHaloRunV1alpha1UserApi,
  ContentHaloRunV1alpha1CategoryApi,
  ContentHaloRunV1alpha1CommentApi,
  ContentHaloRunV1alpha1PostApi,
  ContentHaloRunV1alpha1ReplyApi,
  ContentHaloRunV1alpha1SnapshotApi,
  ContentHaloRunV1alpha1TagApi,
  PluginHaloRunV1alpha1PluginApi,
  PluginHaloRunV1alpha1ReverseProxyApi,
  StorageHaloRunV1alpha1AttachmentApi,
  StorageHaloRunV1alpha1GroupApi,
  StorageHaloRunV1alpha1PolicyApi,
  StorageHaloRunV1alpha1PolicyTemplateApi,
  ThemeHaloRunV1alpha1ThemeApi,
  V1alpha1ConfigMapApi,
  V1alpha1MenuApi,
  V1alpha1MenuItemApi,
  V1alpha1PersonalAccessTokenApi,
  V1alpha1RoleApi,
  V1alpha1RoleBindingApi,
  V1alpha1SettingApi,
  V1alpha1UserApi,
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
      post: new ContentHaloRunV1alpha1PostApi(undefined, apiUrl, axios),
      category: new ContentHaloRunV1alpha1CategoryApi(undefined, apiUrl, axios),
      tag: new ContentHaloRunV1alpha1TagApi(undefined, apiUrl, axios),
      snapshot: new ContentHaloRunV1alpha1SnapshotApi(undefined, apiUrl, axios),
      comment: new ContentHaloRunV1alpha1CommentApi(undefined, apiUrl, axios),
      reply: new ContentHaloRunV1alpha1ReplyApi(undefined, apiUrl, axios),
      storage: {
        group: new StorageHaloRunV1alpha1GroupApi(undefined, apiUrl, axios),
        attachment: new StorageHaloRunV1alpha1AttachmentApi(
          undefined,
          apiUrl,
          axios
        ),
        policy: new StorageHaloRunV1alpha1PolicyApi(undefined, apiUrl, axios),
        policyTemplate: new StorageHaloRunV1alpha1PolicyTemplateApi(
          undefined,
          apiUrl,
          axios
        ),
      },
    },
    // custom endpoints
    user: new ApiHaloRunV1alpha1UserApi(undefined, apiUrl, axios),
    plugin: new ApiHaloRunV1alpha1PluginApi(undefined, apiUrl, axios),
    theme: new ApiHaloRunV1alpha1ThemeApi(undefined, apiUrl, axios),
    post: new ApiHaloRunV1alpha1PostApi(undefined, apiUrl, axios),
    content: new ApiHaloRunV1alpha1ContentApi(undefined, apiUrl, axios),
  };
}

export { apiClient, setApiUrl };
