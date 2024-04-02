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
import { ExtensionDefinition } from "../models";
// @ts-ignore
import { ExtensionDefinitionList } from "../models";
/**
 * PluginHaloRunV1alpha1ExtensionDefinitionApi - axios parameter creator
 * @export
 */
export const PluginHaloRunV1alpha1ExtensionDefinitionApiAxiosParamCreator =
  function (configuration?: Configuration) {
    return {
      /**
       * Create plugin.halo.run/v1alpha1/ExtensionDefinition
       * @param {ExtensionDefinition} [extensionDefinition] Fresh extensiondefinition
       * @param {*} [options] Override http request option.
       * @throws {RequiredError}
       */
      createpluginHaloRunV1alpha1ExtensionDefinition: async (
        extensionDefinition?: ExtensionDefinition,
        options: AxiosRequestConfig = {}
      ): Promise<RequestArgs> => {
        const localVarPath = `/apis/plugin.halo.run/v1alpha1/extensiondefinitions`;
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
          extensionDefinition,
          localVarRequestOptions,
          configuration
        );

        return {
          url: toPathString(localVarUrlObj),
          options: localVarRequestOptions,
        };
      },
      /**
       * Delete plugin.halo.run/v1alpha1/ExtensionDefinition
       * @param {string} name Name of extensiondefinition
       * @param {*} [options] Override http request option.
       * @throws {RequiredError}
       */
      deletepluginHaloRunV1alpha1ExtensionDefinition: async (
        name: string,
        options: AxiosRequestConfig = {}
      ): Promise<RequestArgs> => {
        // verify required parameter 'name' is not null or undefined
        assertParamExists(
          "deletepluginHaloRunV1alpha1ExtensionDefinition",
          "name",
          name
        );
        const localVarPath =
          `/apis/plugin.halo.run/v1alpha1/extensiondefinitions/{name}`.replace(
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
       * Get plugin.halo.run/v1alpha1/ExtensionDefinition
       * @param {string} name Name of extensiondefinition
       * @param {*} [options] Override http request option.
       * @throws {RequiredError}
       */
      getpluginHaloRunV1alpha1ExtensionDefinition: async (
        name: string,
        options: AxiosRequestConfig = {}
      ): Promise<RequestArgs> => {
        // verify required parameter 'name' is not null or undefined
        assertParamExists(
          "getpluginHaloRunV1alpha1ExtensionDefinition",
          "name",
          name
        );
        const localVarPath =
          `/apis/plugin.halo.run/v1alpha1/extensiondefinitions/{name}`.replace(
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
       * List plugin.halo.run/v1alpha1/ExtensionDefinition
       * @param {Array<string>} [fieldSelector] Field selector for filtering.
       * @param {Array<string>} [labelSelector] Label selector for filtering.
       * @param {number} [page] The page number. Zero indicates no page.
       * @param {number} [size] Size of one page. Zero indicates no limit.
       * @param {Array} [sort]
       * @param {*} [options] Override http request option.
       * @throws {RequiredError}
       */
      listpluginHaloRunV1alpha1ExtensionDefinition: async (
        fieldSelector?: Array<string>,
        labelSelector?: Array<string>,
        page?: number,
        size?: number,
        sort?: Array,
        options: AxiosRequestConfig = {}
      ): Promise<RequestArgs> => {
        const localVarPath = `/apis/plugin.halo.run/v1alpha1/extensiondefinitions`;
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

        if (fieldSelector) {
          localVarQueryParameter["fieldSelector"] = fieldSelector;
        }

        if (labelSelector) {
          localVarQueryParameter["labelSelector"] = labelSelector;
        }

        if (page !== undefined) {
          localVarQueryParameter["page"] = page;
        }

        if (size !== undefined) {
          localVarQueryParameter["size"] = size;
        }

        if (sort !== undefined) {
          localVarQueryParameter["sort"] = sort;
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
       * Update plugin.halo.run/v1alpha1/ExtensionDefinition
       * @param {string} name Name of extensiondefinition
       * @param {ExtensionDefinition} [extensionDefinition] Updated extensiondefinition
       * @param {*} [options] Override http request option.
       * @throws {RequiredError}
       */
      updatepluginHaloRunV1alpha1ExtensionDefinition: async (
        name: string,
        extensionDefinition?: ExtensionDefinition,
        options: AxiosRequestConfig = {}
      ): Promise<RequestArgs> => {
        // verify required parameter 'name' is not null or undefined
        assertParamExists(
          "updatepluginHaloRunV1alpha1ExtensionDefinition",
          "name",
          name
        );
        const localVarPath =
          `/apis/plugin.halo.run/v1alpha1/extensiondefinitions/{name}`.replace(
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
          extensionDefinition,
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
 * PluginHaloRunV1alpha1ExtensionDefinitionApi - functional programming interface
 * @export
 */
export const PluginHaloRunV1alpha1ExtensionDefinitionApiFp = function (
  configuration?: Configuration
) {
  const localVarAxiosParamCreator =
    PluginHaloRunV1alpha1ExtensionDefinitionApiAxiosParamCreator(configuration);
  return {
    /**
     * Create plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {ExtensionDefinition} [extensionDefinition] Fresh extensiondefinition
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async createpluginHaloRunV1alpha1ExtensionDefinition(
      extensionDefinition?: ExtensionDefinition,
      options?: AxiosRequestConfig
    ): Promise<
      (
        axios?: AxiosInstance,
        basePath?: string
      ) => AxiosPromise<ExtensionDefinition>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.createpluginHaloRunV1alpha1ExtensionDefinition(
          extensionDefinition,
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
     * Delete plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {string} name Name of extensiondefinition
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async deletepluginHaloRunV1alpha1ExtensionDefinition(
      name: string,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.deletepluginHaloRunV1alpha1ExtensionDefinition(
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
     * Get plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {string} name Name of extensiondefinition
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async getpluginHaloRunV1alpha1ExtensionDefinition(
      name: string,
      options?: AxiosRequestConfig
    ): Promise<
      (
        axios?: AxiosInstance,
        basePath?: string
      ) => AxiosPromise<ExtensionDefinition>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.getpluginHaloRunV1alpha1ExtensionDefinition(
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
     * List plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array} [sort]
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async listpluginHaloRunV1alpha1ExtensionDefinition(
      fieldSelector?: Array<string>,
      labelSelector?: Array<string>,
      page?: number,
      size?: number,
      sort?: Array,
      options?: AxiosRequestConfig
    ): Promise<
      (
        axios?: AxiosInstance,
        basePath?: string
      ) => AxiosPromise<ExtensionDefinitionList>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.listpluginHaloRunV1alpha1ExtensionDefinition(
          fieldSelector,
          labelSelector,
          page,
          size,
          sort,
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
     * Update plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {string} name Name of extensiondefinition
     * @param {ExtensionDefinition} [extensionDefinition] Updated extensiondefinition
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async updatepluginHaloRunV1alpha1ExtensionDefinition(
      name: string,
      extensionDefinition?: ExtensionDefinition,
      options?: AxiosRequestConfig
    ): Promise<
      (
        axios?: AxiosInstance,
        basePath?: string
      ) => AxiosPromise<ExtensionDefinition>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.updatepluginHaloRunV1alpha1ExtensionDefinition(
          name,
          extensionDefinition,
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
 * PluginHaloRunV1alpha1ExtensionDefinitionApi - factory interface
 * @export
 */
export const PluginHaloRunV1alpha1ExtensionDefinitionApiFactory = function (
  configuration?: Configuration,
  basePath?: string,
  axios?: AxiosInstance
) {
  const localVarFp =
    PluginHaloRunV1alpha1ExtensionDefinitionApiFp(configuration);
  return {
    /**
     * Create plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiCreatepluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createpluginHaloRunV1alpha1ExtensionDefinition(
      requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiCreatepluginHaloRunV1alpha1ExtensionDefinitionRequest = {},
      options?: AxiosRequestConfig
    ): AxiosPromise<ExtensionDefinition> {
      return localVarFp
        .createpluginHaloRunV1alpha1ExtensionDefinition(
          requestParameters.extensionDefinition,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * Delete plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiDeletepluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deletepluginHaloRunV1alpha1ExtensionDefinition(
      requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiDeletepluginHaloRunV1alpha1ExtensionDefinitionRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<void> {
      return localVarFp
        .deletepluginHaloRunV1alpha1ExtensionDefinition(
          requestParameters.name,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * Get plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiGetpluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getpluginHaloRunV1alpha1ExtensionDefinition(
      requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiGetpluginHaloRunV1alpha1ExtensionDefinitionRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<ExtensionDefinition> {
      return localVarFp
        .getpluginHaloRunV1alpha1ExtensionDefinition(
          requestParameters.name,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * List plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiListpluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listpluginHaloRunV1alpha1ExtensionDefinition(
      requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiListpluginHaloRunV1alpha1ExtensionDefinitionRequest = {},
      options?: AxiosRequestConfig
    ): AxiosPromise<ExtensionDefinitionList> {
      return localVarFp
        .listpluginHaloRunV1alpha1ExtensionDefinition(
          requestParameters.fieldSelector,
          requestParameters.labelSelector,
          requestParameters.page,
          requestParameters.size,
          requestParameters.sort,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * Update plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatepluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updatepluginHaloRunV1alpha1ExtensionDefinition(
      requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatepluginHaloRunV1alpha1ExtensionDefinitionRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<ExtensionDefinition> {
      return localVarFp
        .updatepluginHaloRunV1alpha1ExtensionDefinition(
          requestParameters.name,
          requestParameters.extensionDefinition,
          options
        )
        .then((request) => request(axios, basePath));
    },
  };
};

/**
 * Request parameters for createpluginHaloRunV1alpha1ExtensionDefinition operation in PluginHaloRunV1alpha1ExtensionDefinitionApi.
 * @export
 * @interface PluginHaloRunV1alpha1ExtensionDefinitionApiCreatepluginHaloRunV1alpha1ExtensionDefinitionRequest
 */
export interface PluginHaloRunV1alpha1ExtensionDefinitionApiCreatepluginHaloRunV1alpha1ExtensionDefinitionRequest {
  /**
   * Fresh extensiondefinition
   * @type {ExtensionDefinition}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiCreatepluginHaloRunV1alpha1ExtensionDefinition
   */
  readonly extensionDefinition?: ExtensionDefinition;
}

/**
 * Request parameters for deletepluginHaloRunV1alpha1ExtensionDefinition operation in PluginHaloRunV1alpha1ExtensionDefinitionApi.
 * @export
 * @interface PluginHaloRunV1alpha1ExtensionDefinitionApiDeletepluginHaloRunV1alpha1ExtensionDefinitionRequest
 */
export interface PluginHaloRunV1alpha1ExtensionDefinitionApiDeletepluginHaloRunV1alpha1ExtensionDefinitionRequest {
  /**
   * Name of extensiondefinition
   * @type {string}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiDeletepluginHaloRunV1alpha1ExtensionDefinition
   */
  readonly name: string;
}

/**
 * Request parameters for getpluginHaloRunV1alpha1ExtensionDefinition operation in PluginHaloRunV1alpha1ExtensionDefinitionApi.
 * @export
 * @interface PluginHaloRunV1alpha1ExtensionDefinitionApiGetpluginHaloRunV1alpha1ExtensionDefinitionRequest
 */
export interface PluginHaloRunV1alpha1ExtensionDefinitionApiGetpluginHaloRunV1alpha1ExtensionDefinitionRequest {
  /**
   * Name of extensiondefinition
   * @type {string}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiGetpluginHaloRunV1alpha1ExtensionDefinition
   */
  readonly name: string;
}

/**
 * Request parameters for listpluginHaloRunV1alpha1ExtensionDefinition operation in PluginHaloRunV1alpha1ExtensionDefinitionApi.
 * @export
 * @interface PluginHaloRunV1alpha1ExtensionDefinitionApiListpluginHaloRunV1alpha1ExtensionDefinitionRequest
 */
export interface PluginHaloRunV1alpha1ExtensionDefinitionApiListpluginHaloRunV1alpha1ExtensionDefinitionRequest {
  /**
   * Field selector for filtering.
   * @type {Array<string>}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiListpluginHaloRunV1alpha1ExtensionDefinition
   */
  readonly fieldSelector?: Array<string>;

  /**
   * Label selector for filtering.
   * @type {Array<string>}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiListpluginHaloRunV1alpha1ExtensionDefinition
   */
  readonly labelSelector?: Array<string>;

  /**
   * The page number. Zero indicates no page.
   * @type {number}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiListpluginHaloRunV1alpha1ExtensionDefinition
   */
  readonly page?: number;

  /**
   * Size of one page. Zero indicates no limit.
   * @type {number}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiListpluginHaloRunV1alpha1ExtensionDefinition
   */
  readonly size?: number;

  /**
   *
   * @type {Array}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiListpluginHaloRunV1alpha1ExtensionDefinition
   */
  readonly sort?: Array;
}

/**
 * Request parameters for updatepluginHaloRunV1alpha1ExtensionDefinition operation in PluginHaloRunV1alpha1ExtensionDefinitionApi.
 * @export
 * @interface PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatepluginHaloRunV1alpha1ExtensionDefinitionRequest
 */
export interface PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatepluginHaloRunV1alpha1ExtensionDefinitionRequest {
  /**
   * Name of extensiondefinition
   * @type {string}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatepluginHaloRunV1alpha1ExtensionDefinition
   */
  readonly name: string;

  /**
   * Updated extensiondefinition
   * @type {ExtensionDefinition}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatepluginHaloRunV1alpha1ExtensionDefinition
   */
  readonly extensionDefinition?: ExtensionDefinition;
}

/**
 * PluginHaloRunV1alpha1ExtensionDefinitionApi - object-oriented interface
 * @export
 * @class PluginHaloRunV1alpha1ExtensionDefinitionApi
 * @extends {BaseAPI}
 */
export class PluginHaloRunV1alpha1ExtensionDefinitionApi extends BaseAPI {
  /**
   * Create plugin.halo.run/v1alpha1/ExtensionDefinition
   * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiCreatepluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApi
   */
  public createpluginHaloRunV1alpha1ExtensionDefinition(
    requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiCreatepluginHaloRunV1alpha1ExtensionDefinitionRequest = {},
    options?: AxiosRequestConfig
  ) {
    return PluginHaloRunV1alpha1ExtensionDefinitionApiFp(this.configuration)
      .createpluginHaloRunV1alpha1ExtensionDefinition(
        requestParameters.extensionDefinition,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Delete plugin.halo.run/v1alpha1/ExtensionDefinition
   * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiDeletepluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApi
   */
  public deletepluginHaloRunV1alpha1ExtensionDefinition(
    requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiDeletepluginHaloRunV1alpha1ExtensionDefinitionRequest,
    options?: AxiosRequestConfig
  ) {
    return PluginHaloRunV1alpha1ExtensionDefinitionApiFp(this.configuration)
      .deletepluginHaloRunV1alpha1ExtensionDefinition(
        requestParameters.name,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Get plugin.halo.run/v1alpha1/ExtensionDefinition
   * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiGetpluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApi
   */
  public getpluginHaloRunV1alpha1ExtensionDefinition(
    requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiGetpluginHaloRunV1alpha1ExtensionDefinitionRequest,
    options?: AxiosRequestConfig
  ) {
    return PluginHaloRunV1alpha1ExtensionDefinitionApiFp(this.configuration)
      .getpluginHaloRunV1alpha1ExtensionDefinition(
        requestParameters.name,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * List plugin.halo.run/v1alpha1/ExtensionDefinition
   * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiListpluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApi
   */
  public listpluginHaloRunV1alpha1ExtensionDefinition(
    requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiListpluginHaloRunV1alpha1ExtensionDefinitionRequest = {},
    options?: AxiosRequestConfig
  ) {
    return PluginHaloRunV1alpha1ExtensionDefinitionApiFp(this.configuration)
      .listpluginHaloRunV1alpha1ExtensionDefinition(
        requestParameters.fieldSelector,
        requestParameters.labelSelector,
        requestParameters.page,
        requestParameters.size,
        requestParameters.sort,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Update plugin.halo.run/v1alpha1/ExtensionDefinition
   * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatepluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApi
   */
  public updatepluginHaloRunV1alpha1ExtensionDefinition(
    requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatepluginHaloRunV1alpha1ExtensionDefinitionRequest,
    options?: AxiosRequestConfig
  ) {
    return PluginHaloRunV1alpha1ExtensionDefinitionApiFp(this.configuration)
      .updatepluginHaloRunV1alpha1ExtensionDefinition(
        requestParameters.name,
        requestParameters.extensionDefinition,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }
}
