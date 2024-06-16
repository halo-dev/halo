import axios from "axios";
import {
  ApiConsoleHaloRunV1alpha1AttachmentApi,
  ApiConsoleHaloRunV1alpha1AuthProviderApi,
  ApiConsoleHaloRunV1alpha1CommentApi,
  ApiConsoleHaloRunV1alpha1IndicesApi,
  ApiConsoleHaloRunV1alpha1NotifierApi,
  ApiConsoleHaloRunV1alpha1PluginApi,
  ApiConsoleHaloRunV1alpha1PostApi,
  ApiConsoleHaloRunV1alpha1ReplyApi,
  ApiConsoleHaloRunV1alpha1SinglePageApi,
  ApiConsoleHaloRunV1alpha1StatsApi,
  ApiConsoleHaloRunV1alpha1SystemApi,
  ApiConsoleHaloRunV1alpha1TagApi,
  ApiConsoleHaloRunV1alpha1ThemeApi,
  ApiConsoleHaloRunV1alpha1UserApi,
  ApiConsoleMigrationHaloRunV1alpha1MigrationApi,
  ApiHaloRunV1alpha1UserApi,
  ApiNotificationHaloRunV1alpha1NotificationApi,
  ApiSecurityHaloRunV1alpha1AuthenticationTwoFactorApi,
  ApiSecurityHaloRunV1alpha1PersonalAccessTokenApi,
  AuthHaloRunV1alpha1AuthProviderApi,
  AuthHaloRunV1alpha1UserConnectionApi,
  ContentHaloRunV1alpha1CategoryApi,
  ContentHaloRunV1alpha1CommentApi,
  ContentHaloRunV1alpha1PostApi,
  ContentHaloRunV1alpha1ReplyApi,
  ContentHaloRunV1alpha1SinglePageApi,
  ContentHaloRunV1alpha1SnapshotApi,
  ContentHaloRunV1alpha1TagApi,
  LoginApi,
  MigrationHaloRunV1alpha1BackupApi,
  NotificationHaloRunV1alpha1NotifierDescriptorApi,
  PluginHaloRunV1alpha1PluginApi,
  PluginHaloRunV1alpha1ReverseProxyApi,
  SecurityHaloRunV1alpha1PersonalAccessTokenApi,
  StorageHaloRunV1alpha1AttachmentApi,
  StorageHaloRunV1alpha1GroupApi,
  StorageHaloRunV1alpha1PolicyApi,
  StorageHaloRunV1alpha1PolicyTemplateApi,
  ThemeHaloRunV1alpha1ThemeApi,
  UcApiContentHaloRunV1alpha1AttachmentApi,
  UcApiContentHaloRunV1alpha1PostApi,
  UcApiContentHaloRunV1alpha1SnapshotApi,
  V1alpha1AnnotationSettingApi,
  V1alpha1CacheApi,
  V1alpha1ConfigMapApi,
  V1alpha1MenuApi,
  V1alpha1MenuItemApi,
  V1alpha1RoleApi,
  V1alpha1RoleBindingApi,
  V1alpha1SettingApi,
  V1alpha1UserApi,
} from "../src";

const baseURL = "/";

const axiosInstance = axios.create({
  baseURL,
  withCredentials: true,
});

axiosInstance.defaults.headers.common["X-Requested-With"] = "XMLHttpRequest";

const apiClient = {
  extension: {
    configMap: new V1alpha1ConfigMapApi(undefined, baseURL, axiosInstance),
    roleBinding: new V1alpha1RoleBindingApi(undefined, baseURL, axiosInstance),
    role: new V1alpha1RoleApi(undefined, baseURL, axiosInstance),
    setting: new V1alpha1SettingApi(undefined, baseURL, axiosInstance),
    reverseProxy: new PluginHaloRunV1alpha1ReverseProxyApi(
      undefined,
      baseURL,
      axiosInstance
    ),
    plugin: new PluginHaloRunV1alpha1PluginApi(undefined, baseURL, axiosInstance),
    user: new V1alpha1UserApi(undefined, baseURL, axiosInstance),
    theme: new ThemeHaloRunV1alpha1ThemeApi(undefined, baseURL, axiosInstance),
    menu: new V1alpha1MenuApi(undefined, baseURL, axiosInstance),
    menuItem: new V1alpha1MenuItemApi(undefined, baseURL, axiosInstance),
    post: new ContentHaloRunV1alpha1PostApi(undefined, baseURL, axiosInstance),
    singlePage: new ContentHaloRunV1alpha1SinglePageApi(
      undefined,
      baseURL,
      axiosInstance
    ),
    category: new ContentHaloRunV1alpha1CategoryApi(
      undefined,
      baseURL,
      axiosInstance
    ),
    tag: new ContentHaloRunV1alpha1TagApi(undefined, baseURL, axiosInstance),
    snapshot: new ContentHaloRunV1alpha1SnapshotApi(
      undefined,
      baseURL,
      axiosInstance
    ),
    comment: new ContentHaloRunV1alpha1CommentApi(undefined, baseURL, axiosInstance),
    reply: new ContentHaloRunV1alpha1ReplyApi(undefined, baseURL, axiosInstance),
    storage: {
      group: new StorageHaloRunV1alpha1GroupApi(undefined, baseURL, axiosInstance),
      attachment: new StorageHaloRunV1alpha1AttachmentApi(
        undefined,
        baseURL,
        axiosInstance
      ),
      policy: new StorageHaloRunV1alpha1PolicyApi(undefined, baseURL, axiosInstance),
      policyTemplate: new StorageHaloRunV1alpha1PolicyTemplateApi(
        undefined,
        baseURL,
        axiosInstance
      ),
    },
    annotationSetting: new V1alpha1AnnotationSettingApi(
      undefined,
      baseURL,
      axiosInstance
    ),
    authProvider: new AuthHaloRunV1alpha1AuthProviderApi(
      undefined,
      baseURL,
      axiosInstance
    ),
    userConnection: new AuthHaloRunV1alpha1UserConnectionApi(
      undefined,
      baseURL,
      axiosInstance
    ),
    backup: new MigrationHaloRunV1alpha1BackupApi(undefined, baseURL, axiosInstance),
    notifierDescriptors: new NotificationHaloRunV1alpha1NotifierDescriptorApi(
      undefined,
      baseURL,
      axiosInstance
    ),
    pat: new SecurityHaloRunV1alpha1PersonalAccessTokenApi(
      undefined,
      baseURL,
      axiosInstance
    ),
  },
  // custom endpoints
  user: new ApiConsoleHaloRunV1alpha1UserApi(undefined, baseURL, axiosInstance),
  plugin: new ApiConsoleHaloRunV1alpha1PluginApi(undefined, baseURL, axiosInstance),
  theme: new ApiConsoleHaloRunV1alpha1ThemeApi(undefined, baseURL, axiosInstance),
  post: new ApiConsoleHaloRunV1alpha1PostApi(undefined, baseURL, axiosInstance),
  tag: new ApiConsoleHaloRunV1alpha1TagApi(undefined, baseURL, axiosInstance),
  singlePage: new ApiConsoleHaloRunV1alpha1SinglePageApi(
    undefined,
    baseURL,
    axiosInstance
  ),
  comment: new ApiConsoleHaloRunV1alpha1CommentApi(undefined, baseURL, axiosInstance),
  reply: new ApiConsoleHaloRunV1alpha1ReplyApi(undefined, baseURL, axiosInstance),
  stats: new ApiConsoleHaloRunV1alpha1StatsApi(undefined, baseURL, axiosInstance),
  attachment: new ApiConsoleHaloRunV1alpha1AttachmentApi(
    undefined,
    baseURL,
    axiosInstance
  ),
  login: new LoginApi(undefined, baseURL, axiosInstance),
  indices: new ApiConsoleHaloRunV1alpha1IndicesApi(undefined, baseURL, axiosInstance),
  authProvider: new ApiConsoleHaloRunV1alpha1AuthProviderApi(
    undefined,
    baseURL,
    axiosInstance
  ),
  common: {
    user: new ApiHaloRunV1alpha1UserApi(undefined, baseURL, axiosInstance),
  },
  cache: new V1alpha1CacheApi(undefined, baseURL, axiosInstance),
  migration: new ApiConsoleMigrationHaloRunV1alpha1MigrationApi(
    undefined,
    baseURL,
    axiosInstance
  ),
  system: new ApiConsoleHaloRunV1alpha1SystemApi(undefined, baseURL, axiosInstance),
  notifier: new ApiConsoleHaloRunV1alpha1NotifierApi(
    undefined,
    baseURL,
    axiosInstance
  ),
  notification: new ApiNotificationHaloRunV1alpha1NotificationApi(
    undefined,
    baseURL,
    axiosInstance
  ),
  pat: new ApiSecurityHaloRunV1alpha1PersonalAccessTokenApi(
    undefined,
    baseURL,
    axiosInstance
  ),
  twoFactor: new ApiSecurityHaloRunV1alpha1AuthenticationTwoFactorApi(
    undefined,
    baseURL,
    axiosInstance
  ),
  uc: {
    post: new UcApiContentHaloRunV1alpha1PostApi(undefined, baseURL, axiosInstance),
    attachment: new UcApiContentHaloRunV1alpha1AttachmentApi(
      undefined,
      baseURL,
      axiosInstance
    ),
    snapshot: new UcApiContentHaloRunV1alpha1SnapshotApi(
      undefined,
      baseURL,
      axiosInstance
    ),
  },
}

export { apiClient, axiosInstance };
