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
import { AnnotationSetting } from "../models";
// @ts-ignore
import { AnnotationSettingList } from "../models";
/**
 * V1alpha1AnnotationSettingApi - axios parameter creator
 * @export
 */
export const V1alpha1AnnotationSettingApiAxiosParamCreator = function (
  configuration?: Configuration
) {
  return {
    /**
     * Create v1alpha1/AnnotationSetting
     * @param {AnnotationSetting} [annotationSetting] Fresh annotationsetting
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createv1alpha1AnnotationSetting: async (
      annotationSetting?: AnnotationSetting,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      const localVarPath = `/api/v1alpha1/annotationsettings`;
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
        annotationSetting,
        localVarRequestOptions,
        configuration
      );

      return {
        url: toPathString(localVarUrlObj),
        options: localVarRequestOptions,
      };
    },
    /**
     * Delete v1alpha1/AnnotationSetting
     * @param {string} name Name of annotationsetting
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deletev1alpha1AnnotationSetting: async (
      name: string,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("deletev1alpha1AnnotationSetting", "name", name);
      const localVarPath = `/api/v1alpha1/annotationsettings/{name}`.replace(
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
     * Get v1alpha1/AnnotationSetting
     * @param {string} name Name of annotationsetting
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getv1alpha1AnnotationSetting: async (
      name: string,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("getv1alpha1AnnotationSetting", "name", name);
      const localVarPath = `/api/v1alpha1/annotationsettings/{name}`.replace(
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
     * List v1alpha1/AnnotationSetting
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listv1alpha1AnnotationSetting: async (
      size?: number,
      labelSelector?: Array<string>,
      fieldSelector?: Array<string>,
      page?: number,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      const localVarPath = `/api/v1alpha1/annotationsettings`;
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

      if (size !== undefined) {
        localVarQueryParameter["size"] = size;
      }

      if (labelSelector) {
        localVarQueryParameter["labelSelector"] = labelSelector;
      }

      if (fieldSelector) {
        localVarQueryParameter["fieldSelector"] = fieldSelector;
      }

      if (page !== undefined) {
        localVarQueryParameter["page"] = page;
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
     * Update v1alpha1/AnnotationSetting
     * @param {string} name Name of annotationsetting
     * @param {AnnotationSetting} [annotationSetting] Updated annotationsetting
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updatev1alpha1AnnotationSetting: async (
      name: string,
      annotationSetting?: AnnotationSetting,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("updatev1alpha1AnnotationSetting", "name", name);
      const localVarPath = `/api/v1alpha1/annotationsettings/{name}`.replace(
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
        annotationSetting,
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
 * V1alpha1AnnotationSettingApi - functional programming interface
 * @export
 */
export const V1alpha1AnnotationSettingApiFp = function (
  configuration?: Configuration
) {
  const localVarAxiosParamCreator =
    V1alpha1AnnotationSettingApiAxiosParamCreator(configuration);
  return {
    /**
     * Create v1alpha1/AnnotationSetting
     * @param {AnnotationSetting} [annotationSetting] Fresh annotationsetting
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async createv1alpha1AnnotationSetting(
      annotationSetting?: AnnotationSetting,
      options?: AxiosRequestConfig
    ): Promise<
      (
        axios?: AxiosInstance,
        basePath?: string
      ) => AxiosPromise<AnnotationSetting>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.createv1alpha1AnnotationSetting(
          annotationSetting,
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
     * Delete v1alpha1/AnnotationSetting
     * @param {string} name Name of annotationsetting
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async deletev1alpha1AnnotationSetting(
      name: string,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.deletev1alpha1AnnotationSetting(
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
     * Get v1alpha1/AnnotationSetting
     * @param {string} name Name of annotationsetting
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async getv1alpha1AnnotationSetting(
      name: string,
      options?: AxiosRequestConfig
    ): Promise<
      (
        axios?: AxiosInstance,
        basePath?: string
      ) => AxiosPromise<AnnotationSetting>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.getv1alpha1AnnotationSetting(
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
     * List v1alpha1/AnnotationSetting
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async listv1alpha1AnnotationSetting(
      size?: number,
      labelSelector?: Array<string>,
      fieldSelector?: Array<string>,
      page?: number,
      options?: AxiosRequestConfig
    ): Promise<
      (
        axios?: AxiosInstance,
        basePath?: string
      ) => AxiosPromise<AnnotationSettingList>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.listv1alpha1AnnotationSetting(
          size,
          labelSelector,
          fieldSelector,
          page,
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
     * Update v1alpha1/AnnotationSetting
     * @param {string} name Name of annotationsetting
     * @param {AnnotationSetting} [annotationSetting] Updated annotationsetting
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async updatev1alpha1AnnotationSetting(
      name: string,
      annotationSetting?: AnnotationSetting,
      options?: AxiosRequestConfig
    ): Promise<
      (
        axios?: AxiosInstance,
        basePath?: string
      ) => AxiosPromise<AnnotationSetting>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.updatev1alpha1AnnotationSetting(
          name,
          annotationSetting,
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
 * V1alpha1AnnotationSettingApi - factory interface
 * @export
 */
export const V1alpha1AnnotationSettingApiFactory = function (
  configuration?: Configuration,
  basePath?: string,
  axios?: AxiosInstance
) {
  const localVarFp = V1alpha1AnnotationSettingApiFp(configuration);
  return {
    /**
     * Create v1alpha1/AnnotationSetting
     * @param {V1alpha1AnnotationSettingApiCreatev1alpha1AnnotationSettingRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createv1alpha1AnnotationSetting(
      requestParameters: V1alpha1AnnotationSettingApiCreatev1alpha1AnnotationSettingRequest = {},
      options?: AxiosRequestConfig
    ): AxiosPromise<AnnotationSetting> {
      return localVarFp
        .createv1alpha1AnnotationSetting(
          requestParameters.annotationSetting,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * Delete v1alpha1/AnnotationSetting
     * @param {V1alpha1AnnotationSettingApiDeletev1alpha1AnnotationSettingRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deletev1alpha1AnnotationSetting(
      requestParameters: V1alpha1AnnotationSettingApiDeletev1alpha1AnnotationSettingRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<void> {
      return localVarFp
        .deletev1alpha1AnnotationSetting(requestParameters.name, options)
        .then((request) => request(axios, basePath));
    },
    /**
     * Get v1alpha1/AnnotationSetting
     * @param {V1alpha1AnnotationSettingApiGetv1alpha1AnnotationSettingRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getv1alpha1AnnotationSetting(
      requestParameters: V1alpha1AnnotationSettingApiGetv1alpha1AnnotationSettingRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<AnnotationSetting> {
      return localVarFp
        .getv1alpha1AnnotationSetting(requestParameters.name, options)
        .then((request) => request(axios, basePath));
    },
    /**
     * List v1alpha1/AnnotationSetting
     * @param {V1alpha1AnnotationSettingApiListv1alpha1AnnotationSettingRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listv1alpha1AnnotationSetting(
      requestParameters: V1alpha1AnnotationSettingApiListv1alpha1AnnotationSettingRequest = {},
      options?: AxiosRequestConfig
    ): AxiosPromise<AnnotationSettingList> {
      return localVarFp
        .listv1alpha1AnnotationSetting(
          requestParameters.size,
          requestParameters.labelSelector,
          requestParameters.fieldSelector,
          requestParameters.page,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * Update v1alpha1/AnnotationSetting
     * @param {V1alpha1AnnotationSettingApiUpdatev1alpha1AnnotationSettingRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updatev1alpha1AnnotationSetting(
      requestParameters: V1alpha1AnnotationSettingApiUpdatev1alpha1AnnotationSettingRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<AnnotationSetting> {
      return localVarFp
        .updatev1alpha1AnnotationSetting(
          requestParameters.name,
          requestParameters.annotationSetting,
          options
        )
        .then((request) => request(axios, basePath));
    },
  };
};

/**
 * Request parameters for createv1alpha1AnnotationSetting operation in V1alpha1AnnotationSettingApi.
 * @export
 * @interface V1alpha1AnnotationSettingApiCreatev1alpha1AnnotationSettingRequest
 */
export interface V1alpha1AnnotationSettingApiCreatev1alpha1AnnotationSettingRequest {
  /**
   * Fresh annotationsetting
   * @type {AnnotationSetting}
   * @memberof V1alpha1AnnotationSettingApiCreatev1alpha1AnnotationSetting
   */
  readonly annotationSetting?: AnnotationSetting;
}

/**
 * Request parameters for deletev1alpha1AnnotationSetting operation in V1alpha1AnnotationSettingApi.
 * @export
 * @interface V1alpha1AnnotationSettingApiDeletev1alpha1AnnotationSettingRequest
 */
export interface V1alpha1AnnotationSettingApiDeletev1alpha1AnnotationSettingRequest {
  /**
   * Name of annotationsetting
   * @type {string}
   * @memberof V1alpha1AnnotationSettingApiDeletev1alpha1AnnotationSetting
   */
  readonly name: string;
}

/**
 * Request parameters for getv1alpha1AnnotationSetting operation in V1alpha1AnnotationSettingApi.
 * @export
 * @interface V1alpha1AnnotationSettingApiGetv1alpha1AnnotationSettingRequest
 */
export interface V1alpha1AnnotationSettingApiGetv1alpha1AnnotationSettingRequest {
  /**
   * Name of annotationsetting
   * @type {string}
   * @memberof V1alpha1AnnotationSettingApiGetv1alpha1AnnotationSetting
   */
  readonly name: string;
}

/**
 * Request parameters for listv1alpha1AnnotationSetting operation in V1alpha1AnnotationSettingApi.
 * @export
 * @interface V1alpha1AnnotationSettingApiListv1alpha1AnnotationSettingRequest
 */
export interface V1alpha1AnnotationSettingApiListv1alpha1AnnotationSettingRequest {
  /**
   * Size of one page. Zero indicates no limit.
   * @type {number}
   * @memberof V1alpha1AnnotationSettingApiListv1alpha1AnnotationSetting
   */
  readonly size?: number;

  /**
   * Label selector for filtering.
   * @type {Array<string>}
   * @memberof V1alpha1AnnotationSettingApiListv1alpha1AnnotationSetting
   */
  readonly labelSelector?: Array<string>;

  /**
   * Field selector for filtering.
   * @type {Array<string>}
   * @memberof V1alpha1AnnotationSettingApiListv1alpha1AnnotationSetting
   */
  readonly fieldSelector?: Array<string>;

  /**
   * The page number. Zero indicates no page.
   * @type {number}
   * @memberof V1alpha1AnnotationSettingApiListv1alpha1AnnotationSetting
   */
  readonly page?: number;
}

/**
 * Request parameters for updatev1alpha1AnnotationSetting operation in V1alpha1AnnotationSettingApi.
 * @export
 * @interface V1alpha1AnnotationSettingApiUpdatev1alpha1AnnotationSettingRequest
 */
export interface V1alpha1AnnotationSettingApiUpdatev1alpha1AnnotationSettingRequest {
  /**
   * Name of annotationsetting
   * @type {string}
   * @memberof V1alpha1AnnotationSettingApiUpdatev1alpha1AnnotationSetting
   */
  readonly name: string;

  /**
   * Updated annotationsetting
   * @type {AnnotationSetting}
   * @memberof V1alpha1AnnotationSettingApiUpdatev1alpha1AnnotationSetting
   */
  readonly annotationSetting?: AnnotationSetting;
}

/**
 * V1alpha1AnnotationSettingApi - object-oriented interface
 * @export
 * @class V1alpha1AnnotationSettingApi
 * @extends {BaseAPI}
 */
export class V1alpha1AnnotationSettingApi extends BaseAPI {
  /**
   * Create v1alpha1/AnnotationSetting
   * @param {V1alpha1AnnotationSettingApiCreatev1alpha1AnnotationSettingRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof V1alpha1AnnotationSettingApi
   */
  public createv1alpha1AnnotationSetting(
    requestParameters: V1alpha1AnnotationSettingApiCreatev1alpha1AnnotationSettingRequest = {},
    options?: AxiosRequestConfig
  ) {
    return V1alpha1AnnotationSettingApiFp(this.configuration)
      .createv1alpha1AnnotationSetting(
        requestParameters.annotationSetting,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Delete v1alpha1/AnnotationSetting
   * @param {V1alpha1AnnotationSettingApiDeletev1alpha1AnnotationSettingRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof V1alpha1AnnotationSettingApi
   */
  public deletev1alpha1AnnotationSetting(
    requestParameters: V1alpha1AnnotationSettingApiDeletev1alpha1AnnotationSettingRequest,
    options?: AxiosRequestConfig
  ) {
    return V1alpha1AnnotationSettingApiFp(this.configuration)
      .deletev1alpha1AnnotationSetting(requestParameters.name, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Get v1alpha1/AnnotationSetting
   * @param {V1alpha1AnnotationSettingApiGetv1alpha1AnnotationSettingRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof V1alpha1AnnotationSettingApi
   */
  public getv1alpha1AnnotationSetting(
    requestParameters: V1alpha1AnnotationSettingApiGetv1alpha1AnnotationSettingRequest,
    options?: AxiosRequestConfig
  ) {
    return V1alpha1AnnotationSettingApiFp(this.configuration)
      .getv1alpha1AnnotationSetting(requestParameters.name, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * List v1alpha1/AnnotationSetting
   * @param {V1alpha1AnnotationSettingApiListv1alpha1AnnotationSettingRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof V1alpha1AnnotationSettingApi
   */
  public listv1alpha1AnnotationSetting(
    requestParameters: V1alpha1AnnotationSettingApiListv1alpha1AnnotationSettingRequest = {},
    options?: AxiosRequestConfig
  ) {
    return V1alpha1AnnotationSettingApiFp(this.configuration)
      .listv1alpha1AnnotationSetting(
        requestParameters.size,
        requestParameters.labelSelector,
        requestParameters.fieldSelector,
        requestParameters.page,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Update v1alpha1/AnnotationSetting
   * @param {V1alpha1AnnotationSettingApiUpdatev1alpha1AnnotationSettingRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof V1alpha1AnnotationSettingApi
   */
  public updatev1alpha1AnnotationSetting(
    requestParameters: V1alpha1AnnotationSettingApiUpdatev1alpha1AnnotationSettingRequest,
    options?: AxiosRequestConfig
  ) {
    return V1alpha1AnnotationSettingApiFp(this.configuration)
      .updatev1alpha1AnnotationSetting(
        requestParameters.name,
        requestParameters.annotationSetting,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }
}
