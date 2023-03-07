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
import { Post } from "../models";
// @ts-ignore
import { PostList } from "../models";
/**
 * ContentHaloRunV1alpha1PostApi - axios parameter creator
 * @export
 */
export const ContentHaloRunV1alpha1PostApiAxiosParamCreator = function (
  configuration?: Configuration
) {
  return {
    /**
     * Create content.halo.run/v1alpha1/Post
     * @param {Post} [post] Fresh post
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createcontentHaloRunV1alpha1Post: async (
      post?: Post,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      const localVarPath = `/apis/content.halo.run/v1alpha1/posts`;
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
        post,
        localVarRequestOptions,
        configuration
      );

      return {
        url: toPathString(localVarUrlObj),
        options: localVarRequestOptions,
      };
    },
    /**
     * Delete content.halo.run/v1alpha1/Post
     * @param {string} name Name of post
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deletecontentHaloRunV1alpha1Post: async (
      name: string,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("deletecontentHaloRunV1alpha1Post", "name", name);
      const localVarPath =
        `/apis/content.halo.run/v1alpha1/posts/{name}`.replace(
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
     * Get content.halo.run/v1alpha1/Post
     * @param {string} name Name of post
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getcontentHaloRunV1alpha1Post: async (
      name: string,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("getcontentHaloRunV1alpha1Post", "name", name);
      const localVarPath =
        `/apis/content.halo.run/v1alpha1/posts/{name}`.replace(
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
     * List content.halo.run/v1alpha1/Post
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listcontentHaloRunV1alpha1Post: async (
      page?: number,
      size?: number,
      labelSelector?: Array<string>,
      fieldSelector?: Array<string>,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      const localVarPath = `/apis/content.halo.run/v1alpha1/posts`;
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
     * Update content.halo.run/v1alpha1/Post
     * @param {string} name Name of post
     * @param {Post} [post] Updated post
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updatecontentHaloRunV1alpha1Post: async (
      name: string,
      post?: Post,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("updatecontentHaloRunV1alpha1Post", "name", name);
      const localVarPath =
        `/apis/content.halo.run/v1alpha1/posts/{name}`.replace(
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
        post,
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
 * ContentHaloRunV1alpha1PostApi - functional programming interface
 * @export
 */
export const ContentHaloRunV1alpha1PostApiFp = function (
  configuration?: Configuration
) {
  const localVarAxiosParamCreator =
    ContentHaloRunV1alpha1PostApiAxiosParamCreator(configuration);
  return {
    /**
     * Create content.halo.run/v1alpha1/Post
     * @param {Post} [post] Fresh post
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async createcontentHaloRunV1alpha1Post(
      post?: Post,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<Post>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.createcontentHaloRunV1alpha1Post(
          post,
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
     * Delete content.halo.run/v1alpha1/Post
     * @param {string} name Name of post
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async deletecontentHaloRunV1alpha1Post(
      name: string,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.deletecontentHaloRunV1alpha1Post(
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
     * Get content.halo.run/v1alpha1/Post
     * @param {string} name Name of post
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async getcontentHaloRunV1alpha1Post(
      name: string,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<Post>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.getcontentHaloRunV1alpha1Post(
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
     * List content.halo.run/v1alpha1/Post
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async listcontentHaloRunV1alpha1Post(
      page?: number,
      size?: number,
      labelSelector?: Array<string>,
      fieldSelector?: Array<string>,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<PostList>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.listcontentHaloRunV1alpha1Post(
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
     * Update content.halo.run/v1alpha1/Post
     * @param {string} name Name of post
     * @param {Post} [post] Updated post
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async updatecontentHaloRunV1alpha1Post(
      name: string,
      post?: Post,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<Post>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.updatecontentHaloRunV1alpha1Post(
          name,
          post,
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
 * ContentHaloRunV1alpha1PostApi - factory interface
 * @export
 */
export const ContentHaloRunV1alpha1PostApiFactory = function (
  configuration?: Configuration,
  basePath?: string,
  axios?: AxiosInstance
) {
  const localVarFp = ContentHaloRunV1alpha1PostApiFp(configuration);
  return {
    /**
     * Create content.halo.run/v1alpha1/Post
     * @param {ContentHaloRunV1alpha1PostApiCreatecontentHaloRunV1alpha1PostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createcontentHaloRunV1alpha1Post(
      requestParameters: ContentHaloRunV1alpha1PostApiCreatecontentHaloRunV1alpha1PostRequest = {},
      options?: AxiosRequestConfig
    ): AxiosPromise<Post> {
      return localVarFp
        .createcontentHaloRunV1alpha1Post(requestParameters.post, options)
        .then((request) => request(axios, basePath));
    },
    /**
     * Delete content.halo.run/v1alpha1/Post
     * @param {ContentHaloRunV1alpha1PostApiDeletecontentHaloRunV1alpha1PostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    deletecontentHaloRunV1alpha1Post(
      requestParameters: ContentHaloRunV1alpha1PostApiDeletecontentHaloRunV1alpha1PostRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<void> {
      return localVarFp
        .deletecontentHaloRunV1alpha1Post(requestParameters.name, options)
        .then((request) => request(axios, basePath));
    },
    /**
     * Get content.halo.run/v1alpha1/Post
     * @param {ContentHaloRunV1alpha1PostApiGetcontentHaloRunV1alpha1PostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getcontentHaloRunV1alpha1Post(
      requestParameters: ContentHaloRunV1alpha1PostApiGetcontentHaloRunV1alpha1PostRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<Post> {
      return localVarFp
        .getcontentHaloRunV1alpha1Post(requestParameters.name, options)
        .then((request) => request(axios, basePath));
    },
    /**
     * List content.halo.run/v1alpha1/Post
     * @param {ContentHaloRunV1alpha1PostApiListcontentHaloRunV1alpha1PostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listcontentHaloRunV1alpha1Post(
      requestParameters: ContentHaloRunV1alpha1PostApiListcontentHaloRunV1alpha1PostRequest = {},
      options?: AxiosRequestConfig
    ): AxiosPromise<PostList> {
      return localVarFp
        .listcontentHaloRunV1alpha1Post(
          requestParameters.page,
          requestParameters.size,
          requestParameters.labelSelector,
          requestParameters.fieldSelector,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * Update content.halo.run/v1alpha1/Post
     * @param {ContentHaloRunV1alpha1PostApiUpdatecontentHaloRunV1alpha1PostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    updatecontentHaloRunV1alpha1Post(
      requestParameters: ContentHaloRunV1alpha1PostApiUpdatecontentHaloRunV1alpha1PostRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<Post> {
      return localVarFp
        .updatecontentHaloRunV1alpha1Post(
          requestParameters.name,
          requestParameters.post,
          options
        )
        .then((request) => request(axios, basePath));
    },
  };
};

/**
 * Request parameters for createcontentHaloRunV1alpha1Post operation in ContentHaloRunV1alpha1PostApi.
 * @export
 * @interface ContentHaloRunV1alpha1PostApiCreatecontentHaloRunV1alpha1PostRequest
 */
export interface ContentHaloRunV1alpha1PostApiCreatecontentHaloRunV1alpha1PostRequest {
  /**
   * Fresh post
   * @type {Post}
   * @memberof ContentHaloRunV1alpha1PostApiCreatecontentHaloRunV1alpha1Post
   */
  readonly post?: Post;
}

/**
 * Request parameters for deletecontentHaloRunV1alpha1Post operation in ContentHaloRunV1alpha1PostApi.
 * @export
 * @interface ContentHaloRunV1alpha1PostApiDeletecontentHaloRunV1alpha1PostRequest
 */
export interface ContentHaloRunV1alpha1PostApiDeletecontentHaloRunV1alpha1PostRequest {
  /**
   * Name of post
   * @type {string}
   * @memberof ContentHaloRunV1alpha1PostApiDeletecontentHaloRunV1alpha1Post
   */
  readonly name: string;
}

/**
 * Request parameters for getcontentHaloRunV1alpha1Post operation in ContentHaloRunV1alpha1PostApi.
 * @export
 * @interface ContentHaloRunV1alpha1PostApiGetcontentHaloRunV1alpha1PostRequest
 */
export interface ContentHaloRunV1alpha1PostApiGetcontentHaloRunV1alpha1PostRequest {
  /**
   * Name of post
   * @type {string}
   * @memberof ContentHaloRunV1alpha1PostApiGetcontentHaloRunV1alpha1Post
   */
  readonly name: string;
}

/**
 * Request parameters for listcontentHaloRunV1alpha1Post operation in ContentHaloRunV1alpha1PostApi.
 * @export
 * @interface ContentHaloRunV1alpha1PostApiListcontentHaloRunV1alpha1PostRequest
 */
export interface ContentHaloRunV1alpha1PostApiListcontentHaloRunV1alpha1PostRequest {
  /**
   * The page number. Zero indicates no page.
   * @type {number}
   * @memberof ContentHaloRunV1alpha1PostApiListcontentHaloRunV1alpha1Post
   */
  readonly page?: number;

  /**
   * Size of one page. Zero indicates no limit.
   * @type {number}
   * @memberof ContentHaloRunV1alpha1PostApiListcontentHaloRunV1alpha1Post
   */
  readonly size?: number;

  /**
   * Label selector for filtering.
   * @type {Array<string>}
   * @memberof ContentHaloRunV1alpha1PostApiListcontentHaloRunV1alpha1Post
   */
  readonly labelSelector?: Array<string>;

  /**
   * Field selector for filtering.
   * @type {Array<string>}
   * @memberof ContentHaloRunV1alpha1PostApiListcontentHaloRunV1alpha1Post
   */
  readonly fieldSelector?: Array<string>;
}

/**
 * Request parameters for updatecontentHaloRunV1alpha1Post operation in ContentHaloRunV1alpha1PostApi.
 * @export
 * @interface ContentHaloRunV1alpha1PostApiUpdatecontentHaloRunV1alpha1PostRequest
 */
export interface ContentHaloRunV1alpha1PostApiUpdatecontentHaloRunV1alpha1PostRequest {
  /**
   * Name of post
   * @type {string}
   * @memberof ContentHaloRunV1alpha1PostApiUpdatecontentHaloRunV1alpha1Post
   */
  readonly name: string;

  /**
   * Updated post
   * @type {Post}
   * @memberof ContentHaloRunV1alpha1PostApiUpdatecontentHaloRunV1alpha1Post
   */
  readonly post?: Post;
}

/**
 * ContentHaloRunV1alpha1PostApi - object-oriented interface
 * @export
 * @class ContentHaloRunV1alpha1PostApi
 * @extends {BaseAPI}
 */
export class ContentHaloRunV1alpha1PostApi extends BaseAPI {
  /**
   * Create content.halo.run/v1alpha1/Post
   * @param {ContentHaloRunV1alpha1PostApiCreatecontentHaloRunV1alpha1PostRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ContentHaloRunV1alpha1PostApi
   */
  public createcontentHaloRunV1alpha1Post(
    requestParameters: ContentHaloRunV1alpha1PostApiCreatecontentHaloRunV1alpha1PostRequest = {},
    options?: AxiosRequestConfig
  ) {
    return ContentHaloRunV1alpha1PostApiFp(this.configuration)
      .createcontentHaloRunV1alpha1Post(requestParameters.post, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Delete content.halo.run/v1alpha1/Post
   * @param {ContentHaloRunV1alpha1PostApiDeletecontentHaloRunV1alpha1PostRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ContentHaloRunV1alpha1PostApi
   */
  public deletecontentHaloRunV1alpha1Post(
    requestParameters: ContentHaloRunV1alpha1PostApiDeletecontentHaloRunV1alpha1PostRequest,
    options?: AxiosRequestConfig
  ) {
    return ContentHaloRunV1alpha1PostApiFp(this.configuration)
      .deletecontentHaloRunV1alpha1Post(requestParameters.name, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Get content.halo.run/v1alpha1/Post
   * @param {ContentHaloRunV1alpha1PostApiGetcontentHaloRunV1alpha1PostRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ContentHaloRunV1alpha1PostApi
   */
  public getcontentHaloRunV1alpha1Post(
    requestParameters: ContentHaloRunV1alpha1PostApiGetcontentHaloRunV1alpha1PostRequest,
    options?: AxiosRequestConfig
  ) {
    return ContentHaloRunV1alpha1PostApiFp(this.configuration)
      .getcontentHaloRunV1alpha1Post(requestParameters.name, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * List content.halo.run/v1alpha1/Post
   * @param {ContentHaloRunV1alpha1PostApiListcontentHaloRunV1alpha1PostRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ContentHaloRunV1alpha1PostApi
   */
  public listcontentHaloRunV1alpha1Post(
    requestParameters: ContentHaloRunV1alpha1PostApiListcontentHaloRunV1alpha1PostRequest = {},
    options?: AxiosRequestConfig
  ) {
    return ContentHaloRunV1alpha1PostApiFp(this.configuration)
      .listcontentHaloRunV1alpha1Post(
        requestParameters.page,
        requestParameters.size,
        requestParameters.labelSelector,
        requestParameters.fieldSelector,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Update content.halo.run/v1alpha1/Post
   * @param {ContentHaloRunV1alpha1PostApiUpdatecontentHaloRunV1alpha1PostRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ContentHaloRunV1alpha1PostApi
   */
  public updatecontentHaloRunV1alpha1Post(
    requestParameters: ContentHaloRunV1alpha1PostApiUpdatecontentHaloRunV1alpha1PostRequest,
    options?: AxiosRequestConfig
  ) {
    return ContentHaloRunV1alpha1PostApiFp(this.configuration)
      .updatecontentHaloRunV1alpha1Post(
        requestParameters.name,
        requestParameters.post,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }
}
