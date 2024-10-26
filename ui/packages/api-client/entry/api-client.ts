import axios, { AxiosInstance } from "axios";
import QueryString from "qs";
import {
  AnnotationSettingV1alpha1Api,
  AttachmentV1alpha1Api,
  AttachmentV1alpha1ConsoleApi,
  AttachmentV1alpha1UcApi,
  AuthProviderV1alpha1Api,
  AuthProviderV1alpha1ConsoleApi,
  BackupV1alpha1Api,
  CategoryV1alpha1Api,
  CommentV1alpha1Api,
  CommentV1alpha1ConsoleApi,
  CommentV1alpha1PublicApi,
  ConfigMapV1alpha1Api,
  CounterV1alpha1Api,
  DeviceV1alpha1UcApi,
  ExtensionDefinitionV1alpha1Api,
  ExtensionPointDefinitionV1alpha1Api,
  GroupV1alpha1Api,
  IndicesV1alpha1ConsoleApi,
  MenuItemV1alpha1Api,
  MenuV1alpha1Api,
  MenuV1alpha1PublicApi,
  MetricsV1alpha1PublicApi,
  MigrationV1alpha1ConsoleApi,
  NotificationTemplateV1alpha1Api,
  NotificationV1alpha1Api,
  NotificationV1alpha1PublicApi,
  NotificationV1alpha1UcApi,
  NotifierDescriptorV1alpha1Api,
  NotifierV1alpha1ConsoleApi,
  PersonalAccessTokenV1alpha1Api,
  PersonalAccessTokenV1alpha1UcApi,
  PluginV1alpha1Api,
  PluginV1alpha1ConsoleApi,
  PolicyTemplateV1alpha1Api,
  PolicyV1alpha1Api,
  PostV1alpha1Api,
  PostV1alpha1ConsoleApi,
  PostV1alpha1PublicApi,
  PostV1alpha1UcApi,
  ReasonTypeV1alpha1Api,
  ReasonV1alpha1Api,
  ReplyV1alpha1Api,
  ReplyV1alpha1ConsoleApi,
  ReverseProxyV1alpha1Api,
  RoleBindingV1alpha1Api,
  RoleV1alpha1Api,
  SearchEngineV1alpha1Api,
  SecretV1alpha1Api,
  SettingV1alpha1Api,
  SinglePageV1alpha1Api,
  SinglePageV1alpha1ConsoleApi,
  SnapshotV1alpha1Api,
  SnapshotV1alpha1UcApi,
  SubscriptionV1alpha1Api,
  SystemConfigV1alpha1ConsoleApi,
  SystemV1alpha1ConsoleApi,
  SystemV1alpha1PublicApi,
  TagV1alpha1Api,
  TagV1alpha1ConsoleApi,
  ThemeV1alpha1Api,
  ThemeV1alpha1ConsoleApi,
  TwoFactorAuthV1alpha1UcApi,
  UserConnectionV1alpha1Api,
  UserV1alpha1Api,
  UserV1alpha1ConsoleApi,
} from "../src";

const defaultAxiosInstance = axios.create({
  baseURL: "",
  withCredentials: true,
  paramsSerializer: (params) => {
    return QueryString.stringify(params, { arrayFormat: "repeat" });
  },
});

defaultAxiosInstance.defaults.headers.common["X-Requested-With"] =
  "XMLHttpRequest";

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
 *  baseURL: "https://demo.halo.run",
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
 * const coreApiClient = createCoreApiClient(axiosInstance);
 *
 * coreApiClient.content.post.listContentHaloRunV1alpha1Post().then(response => {
 *    // handle response
 * })
 */
function createCoreApiClient(axiosInstance: AxiosInstance) {
  const baseURL = axiosInstance.defaults.baseURL;

  return {
    // core
    annotationSetting: new AnnotationSettingV1alpha1Api(
      undefined,
      baseURL,
      axiosInstance
    ),
    menu: new MenuV1alpha1Api(undefined, baseURL, axiosInstance),
    menuItem: new MenuItemV1alpha1Api(undefined, baseURL, axiosInstance),
    setting: new SettingV1alpha1Api(undefined, baseURL, axiosInstance),
    configMap: new ConfigMapV1alpha1Api(undefined, baseURL, axiosInstance),
    secret: new SecretV1alpha1Api(undefined, baseURL, axiosInstance),
    user: new UserV1alpha1Api(undefined, baseURL, axiosInstance),
    role: new RoleV1alpha1Api(undefined, baseURL, axiosInstance),
    roleBinding: new RoleBindingV1alpha1Api(undefined, baseURL, axiosInstance),

    // content.halo.run
    content: {
      category: new CategoryV1alpha1Api(undefined, baseURL, axiosInstance),
      comment: new CommentV1alpha1Api(undefined, baseURL, axiosInstance),
      post: new PostV1alpha1Api(undefined, baseURL, axiosInstance),
      reply: new ReplyV1alpha1Api(undefined, baseURL, axiosInstance),
      singlePage: new SinglePageV1alpha1Api(undefined, baseURL, axiosInstance),
      snapshot: new SnapshotV1alpha1Api(undefined, baseURL, axiosInstance),
      tag: new TagV1alpha1Api(undefined, baseURL, axiosInstance),
    },

    // auth.halo.run
    auth: {
      authProvider: new AuthProviderV1alpha1Api(
        undefined,
        baseURL,
        axiosInstance
      ),
      userConnection: new UserConnectionV1alpha1Api(
        undefined,
        baseURL,
        axiosInstance
      ),
    },

    // storage.halo.run
    storage: {
      attachment: new AttachmentV1alpha1Api(undefined, baseURL, axiosInstance),
      group: new GroupV1alpha1Api(undefined, baseURL, axiosInstance),
      policy: new PolicyV1alpha1Api(undefined, baseURL, axiosInstance),
      policyTemplate: new PolicyTemplateV1alpha1Api(
        undefined,
        baseURL,
        axiosInstance
      ),
    },

    // plugin.halo.run
    plugin: {
      extensionDefinition: new ExtensionDefinitionV1alpha1Api(
        undefined,
        baseURL,
        axiosInstance
      ),
      extensionPointDefinition: new ExtensionPointDefinitionV1alpha1Api(
        undefined,
        baseURL,
        axiosInstance
      ),
      plugin: new PluginV1alpha1Api(undefined, baseURL, axiosInstance),
      reverseProxy: new ReverseProxyV1alpha1Api(
        undefined,
        baseURL,
        axiosInstance
      ),
      searchEngine: new SearchEngineV1alpha1Api(
        undefined,
        baseURL,
        axiosInstance
      ),
    },

    // metrics.halo.run
    metrics: {
      counter: new CounterV1alpha1Api(undefined, baseURL, axiosInstance),
    },

    // theme.halo.run
    theme: {
      theme: new ThemeV1alpha1Api(undefined, baseURL, axiosInstance),
    },

    // notification.halo.run
    notification: {
      notification: new NotificationV1alpha1Api(
        undefined,
        baseURL,
        axiosInstance
      ),
      notificationTemplate: new NotificationTemplateV1alpha1Api(
        undefined,
        baseURL,
        axiosInstance
      ),
      notifierDescriptor: new NotifierDescriptorV1alpha1Api(
        undefined,
        baseURL,
        axiosInstance
      ),
      reason: new ReasonV1alpha1Api(undefined, baseURL, axiosInstance),
      reasonType: new ReasonTypeV1alpha1Api(undefined, baseURL, axiosInstance),
      subscription: new SubscriptionV1alpha1Api(
        undefined,
        baseURL,
        axiosInstance
      ),
    },

    // migration.halo.run
    migration: {
      backup: new BackupV1alpha1Api(undefined, baseURL, axiosInstance),
    },

    // security.halo.run
    security: {
      personalAccessToken: new PersonalAccessTokenV1alpha1Api(
        undefined,
        baseURL,
        axiosInstance
      ),
    },
  };
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
 *  baseURL: "https://demo.halo.run",
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
 * const consoleApiClient = createConsoleApiClient(axiosInstance);
 *
 * consoleApiClient.content.post.listPosts().then(response => {
 *    // handle response
 * })
 */
function createConsoleApiClient(axiosInstance: AxiosInstance) {
  const baseURL = axiosInstance.defaults.baseURL;

  return {
    user: new UserV1alpha1ConsoleApi(undefined, baseURL, axiosInstance),
    system: new SystemV1alpha1ConsoleApi(undefined, baseURL, axiosInstance),
    migration: new MigrationV1alpha1ConsoleApi(
      undefined,
      baseURL,
      axiosInstance
    ),
    storage: {
      attachment: new AttachmentV1alpha1ConsoleApi(
        undefined,
        baseURL,
        axiosInstance
      ),
    },
    auth: {
      authProvider: new AuthProviderV1alpha1ConsoleApi(
        undefined,
        baseURL,
        axiosInstance
      ),
    },
    content: {
      comment: new CommentV1alpha1ConsoleApi(undefined, baseURL, axiosInstance),
      reply: new ReplyV1alpha1ConsoleApi(undefined, baseURL, axiosInstance),
      indices: new IndicesV1alpha1ConsoleApi(undefined, baseURL, axiosInstance),
      post: new PostV1alpha1ConsoleApi(undefined, baseURL, axiosInstance),
      singlePage: new SinglePageV1alpha1ConsoleApi(
        undefined,
        baseURL,
        axiosInstance
      ),
      tag: new TagV1alpha1ConsoleApi(undefined, baseURL, axiosInstance),
    },
    notification: {
      notifier: new NotifierV1alpha1ConsoleApi(
        undefined,
        baseURL,
        axiosInstance
      ),
    },
    plugin: {
      plugin: new PluginV1alpha1ConsoleApi(undefined, baseURL, axiosInstance),
    },
    theme: {
      theme: new ThemeV1alpha1ConsoleApi(undefined, baseURL, axiosInstance),
    },
    configMap: {
      system: new SystemConfigV1alpha1ConsoleApi(
        undefined,
        baseURL,
        axiosInstance
      ),
    },
  };
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
 *  baseURL: "https://demo.halo.run",
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
 * const ucApiClient = createUcApiClient(axiosInstance);
 *
 * ucApiClient.content.post.listMyPosts().then(response => {
 *    // handle response
 * })
 */
function createUcApiClient(axiosInstance: AxiosInstance) {
  const baseURL = axiosInstance.defaults.baseURL;

  return {
    storage: {
      attachment: new AttachmentV1alpha1UcApi(
        undefined,
        baseURL,
        axiosInstance
      ),
    },
    content: {
      post: new PostV1alpha1UcApi(undefined, baseURL, axiosInstance),
      snapshot: new SnapshotV1alpha1UcApi(undefined, baseURL, axiosInstance),
    },
    security: {
      twoFactor: new TwoFactorAuthV1alpha1UcApi(
        undefined,
        baseURL,
        axiosInstance
      ),
      personalAccessToken: new PersonalAccessTokenV1alpha1UcApi(
        undefined,
        baseURL,
        axiosInstance
      ),
      device: new DeviceV1alpha1UcApi(undefined, baseURL, axiosInstance),
    },
    notification: {
      notification: new NotificationV1alpha1UcApi(
        undefined,
        baseURL,
        axiosInstance
      ),
    },
  };
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
 *   baseURL: "https://demo.halo.run",
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
 * const publicApiClient = createPublicApiClient(axiosInstance);
 *
 * publicApiClient.content.post.searchPost({ keyword: "foo" }).then(response => {
 *    // handle response
 * })
 */
function createPublicApiClient(axiosInstance: AxiosInstance) {
  const baseURL = axiosInstance.defaults.baseURL;

  return {
    menu: new MenuV1alpha1PublicApi(undefined, baseURL, axiosInstance),
    stats: new SystemV1alpha1PublicApi(undefined, baseURL, axiosInstance),
    content: {
      post: new PostV1alpha1PublicApi(undefined, baseURL, axiosInstance),
      comment: new CommentV1alpha1PublicApi(undefined, baseURL, axiosInstance),
    },
    metrics: {
      metrics: new MetricsV1alpha1PublicApi(undefined, baseURL, axiosInstance),
    },
    notification: new NotificationV1alpha1PublicApi(
      undefined,
      baseURL,
      axiosInstance
    ),
  };
}

const defaultCoreApiClient = createCoreApiClient(defaultAxiosInstance);
const defaultConsoleApiClient = createConsoleApiClient(defaultAxiosInstance);
const defaultUcApiClient = createUcApiClient(defaultAxiosInstance);
const defaultPublicApiClient = createPublicApiClient(defaultAxiosInstance);

export {
  defaultAxiosInstance as axiosInstance,
  defaultConsoleApiClient as consoleApiClient,
  defaultCoreApiClient as coreApiClient,
  createConsoleApiClient,
  createCoreApiClient,
  createPublicApiClient,
  createUcApiClient,
  defaultPublicApiClient as publicApiClient,
  defaultUcApiClient as ucApiClient,
};
