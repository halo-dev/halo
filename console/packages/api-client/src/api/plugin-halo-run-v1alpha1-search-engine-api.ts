/* tslint:disable */
/* eslint-disable */
/**
 * Halo Next API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.0.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import type { Configuration } from "../configuration";
import type { AxiosPromise, AxiosInstance, AxiosRequestConfig } from "axios";
import globalAxios from "axios";
// Some imports not used depending on template conditions
// @ts-ignore
import {
  DUMMY_BASE_URL,
  assertParamExists,
  setApiKeyToObject,
  setBasicAuthToObject,
  setBearerAuthToObject,
  setOAuthToObject,
  setSearchParams,
  serializeDataIfNeeded,
  toPathString,
  createRequestFunction,
} from "../common";
// @ts-ignore
import {
  BASE_PATH,
  COLLECTION_FORMATS,
  RequestArgs,
  BaseAPI,
  RequiredError,
} from "../base";
// @ts-ignore
import { SearchEngine } from "../models";
// @ts-ignore
import { SearchEngineList } from "../models";
/**
 * PluginHaloRunV1alpha1SearchEngineApi - axios parameter creator
 * @export
 */
export const PluginHaloRunV1alpha1SearchEngineApiAxiosParamCreator = function (
  configuration?: Configuration
) {
  return {
    /**
     * Create plugin.halo.run/v1alpha1/SearchEngine
     * @param {SearchEngine} [searchEngine] Fresh searchengine
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createpluginHaloRunV1alpha1SearchEngine: async (
      searchEngine?: SearchEngine,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      const localVarPath = `/apis/plugin.halo.run/v1alpha1/searchengines`;
      // use dummy base URL string because the URL constructor only accepts absolute URLs.
      const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
      let baseOptions;
      if (configuration) {
        baseOptions = configuration.baseOptions;
      }

      const localVarRequestOptions = {
        method: "POST",
        ...baseOptions,
        ...options,
      };
      const localVarHeaderParameter = {} as any;
      const localVarQueryParameter = {} as any;

      // authentication BasicAuth required
      // http basic authentication required
      setBasicAuthToObject(localVarRequestOptions, configuration);

      // authentication BearerAuth required
      // http bearer authentication required
      await setBearerAuthToObject(localVarHeaderParameter, configuration);

      localVarHeaderParameter["Content-Type"] = "application/json";

      setSearchParams(localVarUrlObj, localVarQueryParameter);
      let headersFromBaseOptions =
        baseOptions && baseOptions.headers ? baseOptions.headers : {};
      localVarRequestOptions.headers = {
        ...localVarHeaderParameter,
        ...headersFromBaseOptions,
        ...options.headers,
      };
      localVarRequestOptions.data = serializeDataIfNeeded(
        searchEngine,
        localVarRequestOptions,
        configuration
      );

      return {
        url: toPathString(localVarUrlObj),
        options: localVarRequestOptions,
      };
    },
    /**
     * Delete plugin.halo.run/v1alpha1/SearchEngine
     * @param {string} name Name of searchengine
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deletepluginHaloRunV1alpha1SearchEngine: async (
      name: string,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists(
        "deletepluginHaloRunV1alpha1SearchEngine",
        "name",
        name
      );
      const localVarPath =
        `/apis/plugin.halo.run/v1alpha1/searchengines/{name}`.replace(
          `{${"name"}}`,
          encodeURIComponent(String(name))
        );
      // use dummy base URL string because the URL constructor only accepts absolute URLs.
      const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
      let baseOptions;
      if (configuration) {
        baseOptions = configuration.baseOptions;
      }

      const localVarRequestOptions = {
        method: "DELETE",
        ...baseOptions,
        ...options,
      };
      const localVarHeaderParameter = {} as any;
      const localVarQueryParameter = {} as any;

      // authentication BasicAuth required
      // http basic authentication required
      setBasicAuthToObject(localVarRequestOptions, configuration);

      // authentication BearerAuth required
      // http bearer authentication required
      await setBearerAuthToObject(localVarHeaderParameter, configuration);

      setSearchParams(localVarUrlObj, localVarQueryParameter);
      let headersFromBaseOptions =
        baseOptions && baseOptions.headers ? baseOptions.headers : {};
      localVarRequestOptions.headers = {
        ...localVarHeaderParameter,
        ...headersFromBaseOptions,
        ...options.headers,
      };

      return {
        url: toPathString(localVarUrlObj),
        options: localVarRequestOptions,
      };
    },
    /**
     * Get plugin.halo.run/v1alpha1/SearchEngine
     * @param {string} name Name of searchengine
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getpluginHaloRunV1alpha1SearchEngine: async (
      name: string,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("getpluginHaloRunV1alpha1SearchEngine", "name", name);
      const localVarPath =
        `/apis/plugin.halo.run/v1alpha1/searchengines/{name}`.replace(
          `{${"name"}}`,
          encodeURIComponent(String(name))
        );
      // use dummy base URL string because the URL constructor only accepts absolute URLs.
      const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
      let baseOptions;
      if (configuration) {
        baseOptions = configuration.baseOptions;
      }

      const localVarRequestOptions = {
        method: "GET",
        ...baseOptions,
        ...options,
      };
      const localVarHeaderParameter = {} as any;
      const localVarQueryParameter = {} as any;

      // authentication BasicAuth required
      // http basic authentication required
      setBasicAuthToObject(localVarRequestOptions, configuration);

      // authentication BearerAuth required
      // http bearer authentication required
      await setBearerAuthToObject(localVarHeaderParameter, configuration);

      setSearchParams(localVarUrlObj, localVarQueryParameter);
      let headersFromBaseOptions =
        baseOptions && baseOptions.headers ? baseOptions.headers : {};
      localVarRequestOptions.headers = {
        ...localVarHeaderParameter,
        ...headersFromBaseOptions,
        ...options.headers,
      };

      return {
        url: toPathString(localVarUrlObj),
        options: localVarRequestOptions,
      };
    },
    /**
     * List plugin.halo.run/v1alpha1/SearchEngine
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listpluginHaloRunV1alpha1SearchEngine: async (
      page?: number,
      size?: number,
      labelSelector?: Array<string>,
      fieldSelector?: Array<string>,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      const localVarPath = `/apis/plugin.halo.run/v1alpha1/searchengines`;
      // use dummy base URL string because the URL constructor only accepts absolute URLs.
      const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
      let baseOptions;
      if (configuration) {
        baseOptions = configuration.baseOptions;
      }

      const localVarRequestOptions = {
        method: "GET",
        ...baseOptions,
        ...options,
      };
      const localVarHeaderParameter = {} as any;
      const localVarQueryParameter = {} as any;

      // authentication BasicAuth required
      // http basic authentication required
      setBasicAuthToObject(localVarRequestOptions, configuration);

      // authentication BearerAuth required
      // http bearer authentication required
      await setBearerAuthToObject(localVarHeaderParameter, configuration);

      if (page !== undefined) {
        localVarQueryParameter["page"] = page;
      }

      if (size !== undefined) {
        localVarQueryParameter["size"] = size;
      }

      if (labelSelector) {
        localVarQueryParameter["labelSelector"] = labelSelector;
      }

      if (fieldSelector) {
        localVarQueryParameter["fieldSelector"] = fieldSelector;
      }

      setSearchParams(localVarUrlObj, localVarQueryParameter);
      let headersFromBaseOptions =
        baseOptions && baseOptions.headers ? baseOptions.headers : {};
      localVarRequestOptions.headers = {
        ...localVarHeaderParameter,
        ...headersFromBaseOptions,
        ...options.headers,
      };

      return {
        url: toPathString(localVarUrlObj),
        options: localVarRequestOptions,
      };
    },
    /**
     * Update plugin.halo.run/v1alpha1/SearchEngine
     * @param {string} name Name of searchengine
     * @param {SearchEngine} [searchEngine] Updated searchengine
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updatepluginHaloRunV1alpha1SearchEngine: async (
      name: string,
      searchEngine?: SearchEngine,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists(
        "updatepluginHaloRunV1alpha1SearchEngine",
        "name",
        name
      );
      const localVarPath =
        `/apis/plugin.halo.run/v1alpha1/searchengines/{name}`.replace(
          `{${"name"}}`,
          encodeURIComponent(String(name))
        );
      // use dummy base URL string because the URL constructor only accepts absolute URLs.
      const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
      let baseOptions;
      if (configuration) {
        baseOptions = configuration.baseOptions;
      }

      const localVarRequestOptions = {
        method: "PUT",
        ...baseOptions,
        ...options,
      };
      const localVarHeaderParameter = {} as any;
      const localVarQueryParameter = {} as any;

      // authentication BasicAuth required
      // http basic authentication required
      setBasicAuthToObject(localVarRequestOptions, configuration);

      // authentication BearerAuth required
      // http bearer authentication required
      await setBearerAuthToObject(localVarHeaderParameter, configuration);

      localVarHeaderParameter["Content-Type"] = "application/json";

      setSearchParams(localVarUrlObj, localVarQueryParameter);
      let headersFromBaseOptions =
        baseOptions && baseOptions.headers ? baseOptions.headers : {};
      localVarRequestOptions.headers = {
        ...localVarHeaderParameter,
        ...headersFromBaseOptions,
        ...options.headers,
      };
      localVarRequestOptions.data = serializeDataIfNeeded(
        searchEngine,
        localVarRequestOptions,
        configuration
      );

      return {
        url: toPathString(localVarUrlObj),
        options: localVarRequestOptions,
      };
    },
  };
};

/**
 * PluginHaloRunV1alpha1SearchEngineApi - functional programming interface
 * @export
 */
export const PluginHaloRunV1alpha1SearchEngineApiFp = function (
  configuration?: Configuration
) {
  const localVarAxiosParamCreator =
    PluginHaloRunV1alpha1SearchEngineApiAxiosParamCreator(configuration);
  return {
    /**
     * Create plugin.halo.run/v1alpha1/SearchEngine
     * @param {SearchEngine} [searchEngine] Fresh searchengine
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async createpluginHaloRunV1alpha1SearchEngine(
      searchEngine?: SearchEngine,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<SearchEngine>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.createpluginHaloRunV1alpha1SearchEngine(
          searchEngine,
          options
        );
      return createRequestFunction(
        localVarAxiosArgs,
        globalAxios,
        BASE_PATH,
        configuration
      );
    },
    /**
     * Delete plugin.halo.run/v1alpha1/SearchEngine
     * @param {string} name Name of searchengine
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async deletepluginHaloRunV1alpha1SearchEngine(
      name: string,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.deletepluginHaloRunV1alpha1SearchEngine(
          name,
          options
        );
      return createRequestFunction(
        localVarAxiosArgs,
        globalAxios,
        BASE_PATH,
        configuration
      );
    },
    /**
     * Get plugin.halo.run/v1alpha1/SearchEngine
     * @param {string} name Name of searchengine
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async getpluginHaloRunV1alpha1SearchEngine(
      name: string,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<SearchEngine>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.getpluginHaloRunV1alpha1SearchEngine(
          name,
          options
        );
      return createRequestFunction(
        localVarAxiosArgs,
        globalAxios,
        BASE_PATH,
        configuration
      );
    },
    /**
     * List plugin.halo.run/v1alpha1/SearchEngine
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async listpluginHaloRunV1alpha1SearchEngine(
      page?: number,
      size?: number,
      labelSelector?: Array<string>,
      fieldSelector?: Array<string>,
      options?: AxiosRequestConfig
    ): Promise<
      (
        axios?: AxiosInstance,
        basePath?: string
      ) => AxiosPromise<SearchEngineList>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.listpluginHaloRunV1alpha1SearchEngine(
          page,
          size,
          labelSelector,
          fieldSelector,
          options
        );
      return createRequestFunction(
        localVarAxiosArgs,
        globalAxios,
        BASE_PATH,
        configuration
      );
    },
    /**
     * Update plugin.halo.run/v1alpha1/SearchEngine
     * @param {string} name Name of searchengine
     * @param {SearchEngine} [searchEngine] Updated searchengine
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async updatepluginHaloRunV1alpha1SearchEngine(
      name: string,
      searchEngine?: SearchEngine,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<SearchEngine>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.updatepluginHaloRunV1alpha1SearchEngine(
          name,
          searchEngine,
          options
        );
      return createRequestFunction(
        localVarAxiosArgs,
        globalAxios,
        BASE_PATH,
        configuration
      );
    },
  };
};

/**
 * PluginHaloRunV1alpha1SearchEngineApi - factory interface
 * @export
 */
export const PluginHaloRunV1alpha1SearchEngineApiFactory = function (
  configuration?: Configuration,
  basePath?: string,
  axios?: AxiosInstance
) {
  const localVarFp = PluginHaloRunV1alpha1SearchEngineApiFp(configuration);
  return {
    /**
     * Create plugin.halo.run/v1alpha1/SearchEngine
     * @param {PluginHaloRunV1alpha1SearchEngineApiCreatepluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createpluginHaloRunV1alpha1SearchEngine(
      requestParameters: PluginHaloRunV1alpha1SearchEngineApiCreatepluginHaloRunV1alpha1SearchEngineRequest = {},
      options?: AxiosRequestConfig
    ): AxiosPromise<SearchEngine> {
      return localVarFp
        .createpluginHaloRunV1alpha1SearchEngine(
          requestParameters.searchEngine,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * Delete plugin.halo.run/v1alpha1/SearchEngine
     * @param {PluginHaloRunV1alpha1SearchEngineApiDeletepluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deletepluginHaloRunV1alpha1SearchEngine(
      requestParameters: PluginHaloRunV1alpha1SearchEngineApiDeletepluginHaloRunV1alpha1SearchEngineRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<void> {
      return localVarFp
        .deletepluginHaloRunV1alpha1SearchEngine(
          requestParameters.name,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * Get plugin.halo.run/v1alpha1/SearchEngine
     * @param {PluginHaloRunV1alpha1SearchEngineApiGetpluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getpluginHaloRunV1alpha1SearchEngine(
      requestParameters: PluginHaloRunV1alpha1SearchEngineApiGetpluginHaloRunV1alpha1SearchEngineRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<SearchEngine> {
      return localVarFp
        .getpluginHaloRunV1alpha1SearchEngine(requestParameters.name, options)
        .then((request) => request(axios, basePath));
    },
    /**
     * List plugin.halo.run/v1alpha1/SearchEngine
     * @param {PluginHaloRunV1alpha1SearchEngineApiListpluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listpluginHaloRunV1alpha1SearchEngine(
      requestParameters: PluginHaloRunV1alpha1SearchEngineApiListpluginHaloRunV1alpha1SearchEngineRequest = {},
      options?: AxiosRequestConfig
    ): AxiosPromise<SearchEngineList> {
      return localVarFp
        .listpluginHaloRunV1alpha1SearchEngine(
          requestParameters.page,
          requestParameters.size,
          requestParameters.labelSelector,
          requestParameters.fieldSelector,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * Update plugin.halo.run/v1alpha1/SearchEngine
     * @param {PluginHaloRunV1alpha1SearchEngineApiUpdatepluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updatepluginHaloRunV1alpha1SearchEngine(
      requestParameters: PluginHaloRunV1alpha1SearchEngineApiUpdatepluginHaloRunV1alpha1SearchEngineRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<SearchEngine> {
      return localVarFp
        .updatepluginHaloRunV1alpha1SearchEngine(
          requestParameters.name,
          requestParameters.searchEngine,
          options
        )
        .then((request) => request(axios, basePath));
    },
  };
};

/**
 * Request parameters for createpluginHaloRunV1alpha1SearchEngine operation in PluginHaloRunV1alpha1SearchEngineApi.
 * @export
 * @interface PluginHaloRunV1alpha1SearchEngineApiCreatepluginHaloRunV1alpha1SearchEngineRequest
 */
export interface PluginHaloRunV1alpha1SearchEngineApiCreatepluginHaloRunV1alpha1SearchEngineRequest {
  /**
   * Fresh searchengine
   * @type {SearchEngine}
   * @memberof PluginHaloRunV1alpha1SearchEngineApiCreatepluginHaloRunV1alpha1SearchEngine
   */
  readonly searchEngine?: SearchEngine;
}

/**
 * Request parameters for deletepluginHaloRunV1alpha1SearchEngine operation in PluginHaloRunV1alpha1SearchEngineApi.
 * @export
 * @interface PluginHaloRunV1alpha1SearchEngineApiDeletepluginHaloRunV1alpha1SearchEngineRequest
 */
export interface PluginHaloRunV1alpha1SearchEngineApiDeletepluginHaloRunV1alpha1SearchEngineRequest {
  /**
   * Name of searchengine
   * @type {string}
   * @memberof PluginHaloRunV1alpha1SearchEngineApiDeletepluginHaloRunV1alpha1SearchEngine
   */
  readonly name: string;
}

/**
 * Request parameters for getpluginHaloRunV1alpha1SearchEngine operation in PluginHaloRunV1alpha1SearchEngineApi.
 * @export
 * @interface PluginHaloRunV1alpha1SearchEngineApiGetpluginHaloRunV1alpha1SearchEngineRequest
 */
export interface PluginHaloRunV1alpha1SearchEngineApiGetpluginHaloRunV1alpha1SearchEngineRequest {
  /**
   * Name of searchengine
   * @type {string}
   * @memberof PluginHaloRunV1alpha1SearchEngineApiGetpluginHaloRunV1alpha1SearchEngine
   */
  readonly name: string;
}

/**
 * Request parameters for listpluginHaloRunV1alpha1SearchEngine operation in PluginHaloRunV1alpha1SearchEngineApi.
 * @export
 * @interface PluginHaloRunV1alpha1SearchEngineApiListpluginHaloRunV1alpha1SearchEngineRequest
 */
export interface PluginHaloRunV1alpha1SearchEngineApiListpluginHaloRunV1alpha1SearchEngineRequest {
  /**
   * The page number. Zero indicates no page.
   * @type {number}
   * @memberof PluginHaloRunV1alpha1SearchEngineApiListpluginHaloRunV1alpha1SearchEngine
   */
  readonly page?: number;

  /**
   * Size of one page. Zero indicates no limit.
   * @type {number}
   * @memberof PluginHaloRunV1alpha1SearchEngineApiListpluginHaloRunV1alpha1SearchEngine
   */
  readonly size?: number;

  /**
   * Label selector for filtering.
   * @type {Array<string>}
   * @memberof PluginHaloRunV1alpha1SearchEngineApiListpluginHaloRunV1alpha1SearchEngine
   */
  readonly labelSelector?: Array<string>;

  /**
   * Field selector for filtering.
   * @type {Array<string>}
   * @memberof PluginHaloRunV1alpha1SearchEngineApiListpluginHaloRunV1alpha1SearchEngine
   */
  readonly fieldSelector?: Array<string>;
}

/**
 * Request parameters for updatepluginHaloRunV1alpha1SearchEngine operation in PluginHaloRunV1alpha1SearchEngineApi.
 * @export
 * @interface PluginHaloRunV1alpha1SearchEngineApiUpdatepluginHaloRunV1alpha1SearchEngineRequest
 */
export interface PluginHaloRunV1alpha1SearchEngineApiUpdatepluginHaloRunV1alpha1SearchEngineRequest {
  /**
   * Name of searchengine
   * @type {string}
   * @memberof PluginHaloRunV1alpha1SearchEngineApiUpdatepluginHaloRunV1alpha1SearchEngine
   */
  readonly name: string;

  /**
   * Updated searchengine
   * @type {SearchEngine}
   * @memberof PluginHaloRunV1alpha1SearchEngineApiUpdatepluginHaloRunV1alpha1SearchEngine
   */
  readonly searchEngine?: SearchEngine;
}

/**
 * PluginHaloRunV1alpha1SearchEngineApi - object-oriented interface
 * @export
 * @class PluginHaloRunV1alpha1SearchEngineApi
 * @extends {BaseAPI}
 */
export class PluginHaloRunV1alpha1SearchEngineApi extends BaseAPI {
  /**
   * Create plugin.halo.run/v1alpha1/SearchEngine
   * @param {PluginHaloRunV1alpha1SearchEngineApiCreatepluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof PluginHaloRunV1alpha1SearchEngineApi
   */
  public createpluginHaloRunV1alpha1SearchEngine(
    requestParameters: PluginHaloRunV1alpha1SearchEngineApiCreatepluginHaloRunV1alpha1SearchEngineRequest = {},
    options?: AxiosRequestConfig
  ) {
    return PluginHaloRunV1alpha1SearchEngineApiFp(this.configuration)
      .createpluginHaloRunV1alpha1SearchEngine(
        requestParameters.searchEngine,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Delete plugin.halo.run/v1alpha1/SearchEngine
   * @param {PluginHaloRunV1alpha1SearchEngineApiDeletepluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof PluginHaloRunV1alpha1SearchEngineApi
   */
  public deletepluginHaloRunV1alpha1SearchEngine(
    requestParameters: PluginHaloRunV1alpha1SearchEngineApiDeletepluginHaloRunV1alpha1SearchEngineRequest,
    options?: AxiosRequestConfig
  ) {
    return PluginHaloRunV1alpha1SearchEngineApiFp(this.configuration)
      .deletepluginHaloRunV1alpha1SearchEngine(requestParameters.name, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Get plugin.halo.run/v1alpha1/SearchEngine
   * @param {PluginHaloRunV1alpha1SearchEngineApiGetpluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof PluginHaloRunV1alpha1SearchEngineApi
   */
  public getpluginHaloRunV1alpha1SearchEngine(
    requestParameters: PluginHaloRunV1alpha1SearchEngineApiGetpluginHaloRunV1alpha1SearchEngineRequest,
    options?: AxiosRequestConfig
  ) {
    return PluginHaloRunV1alpha1SearchEngineApiFp(this.configuration)
      .getpluginHaloRunV1alpha1SearchEngine(requestParameters.name, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * List plugin.halo.run/v1alpha1/SearchEngine
   * @param {PluginHaloRunV1alpha1SearchEngineApiListpluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof PluginHaloRunV1alpha1SearchEngineApi
   */
  public listpluginHaloRunV1alpha1SearchEngine(
    requestParameters: PluginHaloRunV1alpha1SearchEngineApiListpluginHaloRunV1alpha1SearchEngineRequest = {},
    options?: AxiosRequestConfig
  ) {
    return PluginHaloRunV1alpha1SearchEngineApiFp(this.configuration)
      .listpluginHaloRunV1alpha1SearchEngine(
        requestParameters.page,
        requestParameters.size,
        requestParameters.labelSelector,
        requestParameters.fieldSelector,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Update plugin.halo.run/v1alpha1/SearchEngine
   * @param {PluginHaloRunV1alpha1SearchEngineApiUpdatepluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof PluginHaloRunV1alpha1SearchEngineApi
   */
  public updatepluginHaloRunV1alpha1SearchEngine(
    requestParameters: PluginHaloRunV1alpha1SearchEngineApiUpdatepluginHaloRunV1alpha1SearchEngineRequest,
    options?: AxiosRequestConfig
  ) {
    return PluginHaloRunV1alpha1SearchEngineApiFp(this.configuration)
      .updatepluginHaloRunV1alpha1SearchEngine(
        requestParameters.name,
        requestParameters.searchEngine,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }
}
