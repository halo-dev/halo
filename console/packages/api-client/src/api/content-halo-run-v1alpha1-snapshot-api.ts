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
import { Snapshot } from "../models";
// @ts-ignore
import { SnapshotList } from "../models";
/**
 * ContentHaloRunV1alpha1SnapshotApi - axios parameter creator
 * @export
 */
export const ContentHaloRunV1alpha1SnapshotApiAxiosParamCreator = function (
  configuration?: Configuration
) {
  return {
    /**
     * Create content.halo.run/v1alpha1/Snapshot
     * @param {Snapshot} [snapshot] Fresh snapshot
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createcontentHaloRunV1alpha1Snapshot: async (
      snapshot?: Snapshot,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      const localVarPath = `/apis/content.halo.run/v1alpha1/snapshots`;
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
        snapshot,
        localVarRequestOptions,
        configuration
      );

      return {
        url: toPathString(localVarUrlObj),
        options: localVarRequestOptions,
      };
    },
    /**
     * Delete content.halo.run/v1alpha1/Snapshot
     * @param {string} name Name of snapshot
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deletecontentHaloRunV1alpha1Snapshot: async (
      name: string,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("deletecontentHaloRunV1alpha1Snapshot", "name", name);
      const localVarPath =
        `/apis/content.halo.run/v1alpha1/snapshots/{name}`.replace(
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
     * Get content.halo.run/v1alpha1/Snapshot
     * @param {string} name Name of snapshot
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getcontentHaloRunV1alpha1Snapshot: async (
      name: string,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("getcontentHaloRunV1alpha1Snapshot", "name", name);
      const localVarPath =
        `/apis/content.halo.run/v1alpha1/snapshots/{name}`.replace(
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
     * List content.halo.run/v1alpha1/Snapshot
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listcontentHaloRunV1alpha1Snapshot: async (
      size?: number,
      labelSelector?: Array<string>,
      fieldSelector?: Array<string>,
      page?: number,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      const localVarPath = `/apis/content.halo.run/v1alpha1/snapshots`;
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
     * Update content.halo.run/v1alpha1/Snapshot
     * @param {string} name Name of snapshot
     * @param {Snapshot} [snapshot] Updated snapshot
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updatecontentHaloRunV1alpha1Snapshot: async (
      name: string,
      snapshot?: Snapshot,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("updatecontentHaloRunV1alpha1Snapshot", "name", name);
      const localVarPath =
        `/apis/content.halo.run/v1alpha1/snapshots/{name}`.replace(
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
        snapshot,
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
 * ContentHaloRunV1alpha1SnapshotApi - functional programming interface
 * @export
 */
export const ContentHaloRunV1alpha1SnapshotApiFp = function (
  configuration?: Configuration
) {
  const localVarAxiosParamCreator =
    ContentHaloRunV1alpha1SnapshotApiAxiosParamCreator(configuration);
  return {
    /**
     * Create content.halo.run/v1alpha1/Snapshot
     * @param {Snapshot} [snapshot] Fresh snapshot
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async createcontentHaloRunV1alpha1Snapshot(
      snapshot?: Snapshot,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<Snapshot>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.createcontentHaloRunV1alpha1Snapshot(
          snapshot,
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
     * Delete content.halo.run/v1alpha1/Snapshot
     * @param {string} name Name of snapshot
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async deletecontentHaloRunV1alpha1Snapshot(
      name: string,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.deletecontentHaloRunV1alpha1Snapshot(
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
     * Get content.halo.run/v1alpha1/Snapshot
     * @param {string} name Name of snapshot
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async getcontentHaloRunV1alpha1Snapshot(
      name: string,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<Snapshot>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.getcontentHaloRunV1alpha1Snapshot(
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
     * List content.halo.run/v1alpha1/Snapshot
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async listcontentHaloRunV1alpha1Snapshot(
      size?: number,
      labelSelector?: Array<string>,
      fieldSelector?: Array<string>,
      page?: number,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<SnapshotList>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.listcontentHaloRunV1alpha1Snapshot(
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
     * Update content.halo.run/v1alpha1/Snapshot
     * @param {string} name Name of snapshot
     * @param {Snapshot} [snapshot] Updated snapshot
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async updatecontentHaloRunV1alpha1Snapshot(
      name: string,
      snapshot?: Snapshot,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<Snapshot>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.updatecontentHaloRunV1alpha1Snapshot(
          name,
          snapshot,
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
 * ContentHaloRunV1alpha1SnapshotApi - factory interface
 * @export
 */
export const ContentHaloRunV1alpha1SnapshotApiFactory = function (
  configuration?: Configuration,
  basePath?: string,
  axios?: AxiosInstance
) {
  const localVarFp = ContentHaloRunV1alpha1SnapshotApiFp(configuration);
  return {
    /**
     * Create content.halo.run/v1alpha1/Snapshot
     * @param {ContentHaloRunV1alpha1SnapshotApiCreatecontentHaloRunV1alpha1SnapshotRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createcontentHaloRunV1alpha1Snapshot(
      requestParameters: ContentHaloRunV1alpha1SnapshotApiCreatecontentHaloRunV1alpha1SnapshotRequest = {},
      options?: AxiosRequestConfig
    ): AxiosPromise<Snapshot> {
      return localVarFp
        .createcontentHaloRunV1alpha1Snapshot(
          requestParameters.snapshot,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * Delete content.halo.run/v1alpha1/Snapshot
     * @param {ContentHaloRunV1alpha1SnapshotApiDeletecontentHaloRunV1alpha1SnapshotRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deletecontentHaloRunV1alpha1Snapshot(
      requestParameters: ContentHaloRunV1alpha1SnapshotApiDeletecontentHaloRunV1alpha1SnapshotRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<void> {
      return localVarFp
        .deletecontentHaloRunV1alpha1Snapshot(requestParameters.name, options)
        .then((request) => request(axios, basePath));
    },
    /**
     * Get content.halo.run/v1alpha1/Snapshot
     * @param {ContentHaloRunV1alpha1SnapshotApiGetcontentHaloRunV1alpha1SnapshotRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getcontentHaloRunV1alpha1Snapshot(
      requestParameters: ContentHaloRunV1alpha1SnapshotApiGetcontentHaloRunV1alpha1SnapshotRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<Snapshot> {
      return localVarFp
        .getcontentHaloRunV1alpha1Snapshot(requestParameters.name, options)
        .then((request) => request(axios, basePath));
    },
    /**
     * List content.halo.run/v1alpha1/Snapshot
     * @param {ContentHaloRunV1alpha1SnapshotApiListcontentHaloRunV1alpha1SnapshotRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listcontentHaloRunV1alpha1Snapshot(
      requestParameters: ContentHaloRunV1alpha1SnapshotApiListcontentHaloRunV1alpha1SnapshotRequest = {},
      options?: AxiosRequestConfig
    ): AxiosPromise<SnapshotList> {
      return localVarFp
        .listcontentHaloRunV1alpha1Snapshot(
          requestParameters.size,
          requestParameters.labelSelector,
          requestParameters.fieldSelector,
          requestParameters.page,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * Update content.halo.run/v1alpha1/Snapshot
     * @param {ContentHaloRunV1alpha1SnapshotApiUpdatecontentHaloRunV1alpha1SnapshotRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updatecontentHaloRunV1alpha1Snapshot(
      requestParameters: ContentHaloRunV1alpha1SnapshotApiUpdatecontentHaloRunV1alpha1SnapshotRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<Snapshot> {
      return localVarFp
        .updatecontentHaloRunV1alpha1Snapshot(
          requestParameters.name,
          requestParameters.snapshot,
          options
        )
        .then((request) => request(axios, basePath));
    },
  };
};

/**
 * Request parameters for createcontentHaloRunV1alpha1Snapshot operation in ContentHaloRunV1alpha1SnapshotApi.
 * @export
 * @interface ContentHaloRunV1alpha1SnapshotApiCreatecontentHaloRunV1alpha1SnapshotRequest
 */
export interface ContentHaloRunV1alpha1SnapshotApiCreatecontentHaloRunV1alpha1SnapshotRequest {
  /**
   * Fresh snapshot
   * @type {Snapshot}
   * @memberof ContentHaloRunV1alpha1SnapshotApiCreatecontentHaloRunV1alpha1Snapshot
   */
  readonly snapshot?: Snapshot;
}

/**
 * Request parameters for deletecontentHaloRunV1alpha1Snapshot operation in ContentHaloRunV1alpha1SnapshotApi.
 * @export
 * @interface ContentHaloRunV1alpha1SnapshotApiDeletecontentHaloRunV1alpha1SnapshotRequest
 */
export interface ContentHaloRunV1alpha1SnapshotApiDeletecontentHaloRunV1alpha1SnapshotRequest {
  /**
   * Name of snapshot
   * @type {string}
   * @memberof ContentHaloRunV1alpha1SnapshotApiDeletecontentHaloRunV1alpha1Snapshot
   */
  readonly name: string;
}

/**
 * Request parameters for getcontentHaloRunV1alpha1Snapshot operation in ContentHaloRunV1alpha1SnapshotApi.
 * @export
 * @interface ContentHaloRunV1alpha1SnapshotApiGetcontentHaloRunV1alpha1SnapshotRequest
 */
export interface ContentHaloRunV1alpha1SnapshotApiGetcontentHaloRunV1alpha1SnapshotRequest {
  /**
   * Name of snapshot
   * @type {string}
   * @memberof ContentHaloRunV1alpha1SnapshotApiGetcontentHaloRunV1alpha1Snapshot
   */
  readonly name: string;
}

/**
 * Request parameters for listcontentHaloRunV1alpha1Snapshot operation in ContentHaloRunV1alpha1SnapshotApi.
 * @export
 * @interface ContentHaloRunV1alpha1SnapshotApiListcontentHaloRunV1alpha1SnapshotRequest
 */
export interface ContentHaloRunV1alpha1SnapshotApiListcontentHaloRunV1alpha1SnapshotRequest {
  /**
   * Size of one page. Zero indicates no limit.
   * @type {number}
   * @memberof ContentHaloRunV1alpha1SnapshotApiListcontentHaloRunV1alpha1Snapshot
   */
  readonly size?: number;

  /**
   * Label selector for filtering.
   * @type {Array<string>}
   * @memberof ContentHaloRunV1alpha1SnapshotApiListcontentHaloRunV1alpha1Snapshot
   */
  readonly labelSelector?: Array<string>;

  /**
   * Field selector for filtering.
   * @type {Array<string>}
   * @memberof ContentHaloRunV1alpha1SnapshotApiListcontentHaloRunV1alpha1Snapshot
   */
  readonly fieldSelector?: Array<string>;

  /**
   * The page number. Zero indicates no page.
   * @type {number}
   * @memberof ContentHaloRunV1alpha1SnapshotApiListcontentHaloRunV1alpha1Snapshot
   */
  readonly page?: number;
}

/**
 * Request parameters for updatecontentHaloRunV1alpha1Snapshot operation in ContentHaloRunV1alpha1SnapshotApi.
 * @export
 * @interface ContentHaloRunV1alpha1SnapshotApiUpdatecontentHaloRunV1alpha1SnapshotRequest
 */
export interface ContentHaloRunV1alpha1SnapshotApiUpdatecontentHaloRunV1alpha1SnapshotRequest {
  /**
   * Name of snapshot
   * @type {string}
   * @memberof ContentHaloRunV1alpha1SnapshotApiUpdatecontentHaloRunV1alpha1Snapshot
   */
  readonly name: string;

  /**
   * Updated snapshot
   * @type {Snapshot}
   * @memberof ContentHaloRunV1alpha1SnapshotApiUpdatecontentHaloRunV1alpha1Snapshot
   */
  readonly snapshot?: Snapshot;
}

/**
 * ContentHaloRunV1alpha1SnapshotApi - object-oriented interface
 * @export
 * @class ContentHaloRunV1alpha1SnapshotApi
 * @extends {BaseAPI}
 */
export class ContentHaloRunV1alpha1SnapshotApi extends BaseAPI {
  /**
   * Create content.halo.run/v1alpha1/Snapshot
   * @param {ContentHaloRunV1alpha1SnapshotApiCreatecontentHaloRunV1alpha1SnapshotRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ContentHaloRunV1alpha1SnapshotApi
   */
  public createcontentHaloRunV1alpha1Snapshot(
    requestParameters: ContentHaloRunV1alpha1SnapshotApiCreatecontentHaloRunV1alpha1SnapshotRequest = {},
    options?: AxiosRequestConfig
  ) {
    return ContentHaloRunV1alpha1SnapshotApiFp(this.configuration)
      .createcontentHaloRunV1alpha1Snapshot(requestParameters.snapshot, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Delete content.halo.run/v1alpha1/Snapshot
   * @param {ContentHaloRunV1alpha1SnapshotApiDeletecontentHaloRunV1alpha1SnapshotRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ContentHaloRunV1alpha1SnapshotApi
   */
  public deletecontentHaloRunV1alpha1Snapshot(
    requestParameters: ContentHaloRunV1alpha1SnapshotApiDeletecontentHaloRunV1alpha1SnapshotRequest,
    options?: AxiosRequestConfig
  ) {
    return ContentHaloRunV1alpha1SnapshotApiFp(this.configuration)
      .deletecontentHaloRunV1alpha1Snapshot(requestParameters.name, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Get content.halo.run/v1alpha1/Snapshot
   * @param {ContentHaloRunV1alpha1SnapshotApiGetcontentHaloRunV1alpha1SnapshotRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ContentHaloRunV1alpha1SnapshotApi
   */
  public getcontentHaloRunV1alpha1Snapshot(
    requestParameters: ContentHaloRunV1alpha1SnapshotApiGetcontentHaloRunV1alpha1SnapshotRequest,
    options?: AxiosRequestConfig
  ) {
    return ContentHaloRunV1alpha1SnapshotApiFp(this.configuration)
      .getcontentHaloRunV1alpha1Snapshot(requestParameters.name, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * List content.halo.run/v1alpha1/Snapshot
   * @param {ContentHaloRunV1alpha1SnapshotApiListcontentHaloRunV1alpha1SnapshotRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ContentHaloRunV1alpha1SnapshotApi
   */
  public listcontentHaloRunV1alpha1Snapshot(
    requestParameters: ContentHaloRunV1alpha1SnapshotApiListcontentHaloRunV1alpha1SnapshotRequest = {},
    options?: AxiosRequestConfig
  ) {
    return ContentHaloRunV1alpha1SnapshotApiFp(this.configuration)
      .listcontentHaloRunV1alpha1Snapshot(
        requestParameters.size,
        requestParameters.labelSelector,
        requestParameters.fieldSelector,
        requestParameters.page,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Update content.halo.run/v1alpha1/Snapshot
   * @param {ContentHaloRunV1alpha1SnapshotApiUpdatecontentHaloRunV1alpha1SnapshotRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ContentHaloRunV1alpha1SnapshotApi
   */
  public updatecontentHaloRunV1alpha1Snapshot(
    requestParameters: ContentHaloRunV1alpha1SnapshotApiUpdatecontentHaloRunV1alpha1SnapshotRequest,
    options?: AxiosRequestConfig
  ) {
    return ContentHaloRunV1alpha1SnapshotApiFp(this.configuration)
      .updatecontentHaloRunV1alpha1Snapshot(
        requestParameters.name,
        requestParameters.snapshot,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }
}
