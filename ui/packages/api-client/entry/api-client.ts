import axios, { AxiosInstance } from "axios";
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
  ApiHaloRunV1alpha1CommentApi,
  ApiHaloRunV1alpha1MenuApi,
  ApiHaloRunV1alpha1PostApi,
  ApiHaloRunV1alpha1StatsApi,
  ApiHaloRunV1alpha1TrackerApi,
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
  MetricsHaloRunV1alpha1CounterApi,
  MigrationHaloRunV1alpha1BackupApi,
  NotificationHaloRunV1alpha1NotificationApi,
  NotificationHaloRunV1alpha1NotificationTemplateApi,
  NotificationHaloRunV1alpha1NotifierDescriptorApi,
  NotificationHaloRunV1alpha1ReasonApi,
  NotificationHaloRunV1alpha1ReasonTypeApi,
  NotificationHaloRunV1alpha1SubscriptionApi,
  PluginHaloRunV1alpha1ExtensionDefinitionApi,
  PluginHaloRunV1alpha1ExtensionPointDefinitionApi,
  PluginHaloRunV1alpha1PluginApi,
  PluginHaloRunV1alpha1ReverseProxyApi,
  PluginHaloRunV1alpha1SearchEngineApi,
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
  V1alpha1SecretApi,
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

/**
 * Create a core api client
 * 
 * This is the API client for the CRUD interface that is automatically generated for Halo's core extensions.
 * 
 * @see <https://github.com/halo-dev/rfcs/tree/main/extension>
 * @param baseURL Halo backend base URL
 * @param axiosInstance Axios instance
 * @returns Core api client
 * @example
 * const axiosInstance = axios.create({
 *  withCredentials: true,
 * })
 * 
 * axiosInstance.interceptors.request.use((config) => {
 *   // do something before request
 *   return config;
 * }, (error) => {
 *   // do something with request error
 *   return Promise.reject(error);
 * });
 * 
 * const coreApiClient = createCoreApiClient("https://demo.halo.run" , axiosInstance);
 * 
 * coreApiClient.content.post.listContentHaloRunV1alpha1Post().then(response => {
 *    // handle response
 * })
 */
function createCoreApiClient (baseURL:string, axiosInstance: AxiosInstance) {
  return {
    // core
    annotationSetting: new V1alpha1AnnotationSettingApi(undefined,baseURL,axiosInstance),
    menu: new V1alpha1MenuApi(undefined,baseURL,axiosInstance),
    menuItem: new V1alpha1MenuItemApi(undefined,baseURL,axiosInstance),
    setting: new V1alpha1SettingApi(undefined,baseURL,axiosInstance),
    configMap: new V1alpha1ConfigMapApi(undefined,baseURL,axiosInstance),
    secret: new V1alpha1SecretApi(undefined,baseURL,axiosInstance),
    user: new V1alpha1UserApi(undefined,baseURL,axiosInstance),
    role: new V1alpha1RoleApi(undefined,baseURL,axiosInstance),
    roleBinding: new V1alpha1RoleBindingApi(undefined,baseURL,axiosInstance),

    // content.halo.run
    content: {
      category: new ContentHaloRunV1alpha1CategoryApi(undefined,baseURL,axiosInstance),
      comment: new ContentHaloRunV1alpha1CommentApi(undefined,baseURL,axiosInstance),
      post: new ContentHaloRunV1alpha1PostApi(undefined,baseURL,axiosInstance),
      reply: new ContentHaloRunV1alpha1ReplyApi(undefined,baseURL,axiosInstance),
      singlePage: new ContentHaloRunV1alpha1SinglePageApi(undefined,baseURL,axiosInstance),
      snapshot: new ContentHaloRunV1alpha1SnapshotApi(undefined,baseURL,axiosInstance),
      tag: new ContentHaloRunV1alpha1TagApi(undefined,baseURL,axiosInstance),
    },

    // auth.halo.run
    auth: {
      authProvider: new AuthHaloRunV1alpha1AuthProviderApi(undefined,baseURL,axiosInstance),
      userConnection: new AuthHaloRunV1alpha1UserConnectionApi(undefined,baseURL,axiosInstance),
    },

    // storage.halo.run
    storage: {
      attachment: new StorageHaloRunV1alpha1AttachmentApi(undefined,baseURL,axiosInstance),
      group: new StorageHaloRunV1alpha1GroupApi(undefined,baseURL,axiosInstance),
      policy: new StorageHaloRunV1alpha1PolicyApi(undefined,baseURL,axiosInstance),
      policyTemplate: new StorageHaloRunV1alpha1PolicyTemplateApi(undefined,baseURL,axiosInstance),
    },

    // plugin.halo.run
    plugin: {
      extensionDefinition: new PluginHaloRunV1alpha1ExtensionDefinitionApi(undefined,baseURL,axiosInstance),
      extensionPointDefinition: new PluginHaloRunV1alpha1ExtensionPointDefinitionApi(undefined,baseURL,axiosInstance),
      plugin: new PluginHaloRunV1alpha1PluginApi(undefined,baseURL,axiosInstance),
      reverseProxy: new PluginHaloRunV1alpha1ReverseProxyApi(undefined,baseURL,axiosInstance),
      searchEngine: new PluginHaloRunV1alpha1SearchEngineApi(undefined,baseURL,axiosInstance),
    },

    // metrics.halo.run
    metrics: {
      counter: new MetricsHaloRunV1alpha1CounterApi(undefined,baseURL,axiosInstance),
    },

    // theme.halo.run
    theme: {
      theme: new ThemeHaloRunV1alpha1ThemeApi(undefined,baseURL,axiosInstance),
    },

    // notification.halo.run
    notification: {
      notification: new NotificationHaloRunV1alpha1NotificationApi(undefined,baseURL,axiosInstance),
      notificationTemplate: new NotificationHaloRunV1alpha1NotificationTemplateApi(undefined,baseURL,axiosInstance),
      notifierDescriptor: new NotificationHaloRunV1alpha1NotifierDescriptorApi(undefined,baseURL,axiosInstance),
      reason: new NotificationHaloRunV1alpha1ReasonApi(undefined,baseURL,axiosInstance),
      reasonType:new NotificationHaloRunV1alpha1ReasonTypeApi(undefined,baseURL,axiosInstance),
      subscription: new NotificationHaloRunV1alpha1SubscriptionApi(undefined,baseURL,axiosInstance),
    },

    // migration.halo.run
    migration: {
      backup: new MigrationHaloRunV1alpha1BackupApi(undefined,baseURL,axiosInstance),
    },

    // security.halo.run
    security: {
      personalAccessToken: new SecurityHaloRunV1alpha1PersonalAccessTokenApi(undefined,baseURL,axiosInstance),
    }
  }
}

/**
 * Create a console api client
 * 
 * Console api client is used for console related operations
 * 
 * @param baseURL Halo backend base URL
 * @param axiosInstance Axios instance
 * @returns Console api client
 * @example
 * const axiosInstance = axios.create({
 *  withCredentials: true,
 * })
 * 
 * axiosInstance.interceptors.request.use((config) => {
 *   // do something before request
 *   return config;
 * }, (error) => {
 *   // do something with request error
 *   return Promise.reject(error);
 * });
 * 
 * const consoleApiClient = createConsoleApiClient("https://demo.halo.run" , axiosInstance);
 * 
 * consoleApiClient.content.post.listPosts().then(response => {
 *    // handle response
 * })
 */
function createConsoleApiClient(baseURL: string,axiosInstance: AxiosInstance) {
  return {
    user: new ApiConsoleHaloRunV1alpha1UserApi(undefined, baseURL, axiosInstance),
    stats: new ApiConsoleHaloRunV1alpha1StatsApi(undefined, baseURL, axiosInstance),
    system: new ApiConsoleHaloRunV1alpha1SystemApi(undefined, baseURL, axiosInstance),
    migration: new ApiConsoleMigrationHaloRunV1alpha1MigrationApi(undefined, baseURL, axiosInstance),
    cache: new V1alpha1CacheApi(undefined, baseURL, axiosInstance),
    login: new LoginApi(undefined, baseURL, axiosInstance),
    storage: {
      attachment: new ApiConsoleHaloRunV1alpha1AttachmentApi(undefined, baseURL, axiosInstance),
    },
    auth: {
      authProvider: new ApiConsoleHaloRunV1alpha1AuthProviderApi(undefined, baseURL, axiosInstance),
    },
    content: {
      comment: new ApiConsoleHaloRunV1alpha1CommentApi(undefined, baseURL, axiosInstance),
      reply: new ApiConsoleHaloRunV1alpha1ReplyApi(undefined, baseURL, axiosInstance),
      indices: new ApiConsoleHaloRunV1alpha1IndicesApi(undefined, baseURL, axiosInstance),
      post: new ApiConsoleHaloRunV1alpha1PostApi(undefined, baseURL, axiosInstance),
      singlePage: new ApiConsoleHaloRunV1alpha1SinglePageApi(undefined, baseURL, axiosInstance),
      tag: new ApiConsoleHaloRunV1alpha1TagApi(undefined, baseURL, axiosInstance),
    },
    notification: {
      notifier: new ApiConsoleHaloRunV1alpha1NotifierApi(undefined, baseURL, axiosInstance),
      notification: new ApiNotificationHaloRunV1alpha1NotificationApi(undefined, baseURL, axiosInstance),
    },
    plugin: {
      plugin: new ApiConsoleHaloRunV1alpha1PluginApi(undefined, baseURL, axiosInstance),
    },
    theme: {
      theme: new ApiConsoleHaloRunV1alpha1ThemeApi(undefined, baseURL, axiosInstance),
    }
  }
}

/**
 * Create a uc api client
 * 
 * Uc api client is used for user content related operations
 * 
 * @param baseURL Halo backend base URL
 * @param axiosInstance Axios instance
 * @returns Uc api client
 * @example
 * const axiosInstance = axios.create({
 *  withCredentials: true,
 * })
 * 
 * axiosInstance.interceptors.request.use((config) => {
 *   // do something before request
 *   return config;
 * }, (error) => {
 *   // do something with request error
 *   return Promise.reject(error);
 * });
 * 
 * const ucApiClient = createUcApiClient("https://demo.halo.run" , axiosInstance);
 * 
 * ucApiClient.content.post.listMyPosts().then(response => {
 *    // handle response
 * })
 */
function createUcApiClient(baseURL: string,axiosInstance: AxiosInstance) {
  return {
    storage: {
      attachment: new UcApiContentHaloRunV1alpha1AttachmentApi(undefined, baseURL, axiosInstance),
    },
    content: {
      post: new UcApiContentHaloRunV1alpha1PostApi(undefined, baseURL, axiosInstance),
      snapshot: new UcApiContentHaloRunV1alpha1SnapshotApi(undefined, baseURL, axiosInstance),
    },
    security: {
      twoFactor: new ApiSecurityHaloRunV1alpha1AuthenticationTwoFactorApi(undefined, baseURL, axiosInstance),
      personalAccessToken: new ApiSecurityHaloRunV1alpha1PersonalAccessTokenApi(undefined, baseURL, axiosInstance),
    }
  }
}

/**
 * Create a public api client
 * 
 * Public api client is used for public content related operations, no need authentication
 * 
 * @param baseURL Halo backend base URL
 * @param axiosInstance Axios instance
 * @returns Public api client
 * @example 
 * const axiosInstance = axios.create({
 *   withCredentials: true,
 * })
 * 
 * axiosInstance.interceptors.request.use((config) => {
 *   // do something before request
 *   return config;
 * }, (error) => {
 *   // do something with request error
 *   return Promise.reject(error);
 * });
 * 
 * const publicApiClient = createPublicApiClient("https://demo.halo.run" , axiosInstance);
 * 
 * publicApiClient.content.post.searchPost({ keyword: "foo" }).then(response => {
 *    // handle response
 * })
 */
function createPublicApiClient(baseURL: string,axiosInstance: AxiosInstance) {
  return {
    menu: new ApiHaloRunV1alpha1MenuApi(undefined, baseURL, axiosInstance),
    stats: new ApiHaloRunV1alpha1StatsApi(undefined, baseURL, axiosInstance),
    user: new ApiHaloRunV1alpha1UserApi(undefined, baseURL, axiosInstance),
    content: {
      post: new ApiHaloRunV1alpha1PostApi(undefined, baseURL, axiosInstance),
      comment: new ApiHaloRunV1alpha1CommentApi(undefined, baseURL, axiosInstance),
    },
    metrics: {
      tracker: new ApiHaloRunV1alpha1TrackerApi(undefined, baseURL, axiosInstance),
    }
  }
}

const defaultCoreApiClient = createCoreApiClient(baseURL, axiosInstance);
const defaultConsoleApiClient = createConsoleApiClient(baseURL, axiosInstance);
const defaultUcApiClient = createUcApiClient(baseURL,axiosInstance);
const defaultPublicApiClient = createPublicApiClient(baseURL,axiosInstance);

export { apiClient, axiosInstance, defaultConsoleApiClient as consoleApiClient, defaultCoreApiClient as coreApiClient, createConsoleApiClient, createCoreApiClient, createPublicApiClient, createUcApiClient, defaultPublicApiClient as publicApiClient, defaultUcApiClient as ucApiClient };

