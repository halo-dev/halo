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
import { ListedPostVo } from "../models";
// @ts-ignore
import { TagVo } from "../models";
// @ts-ignore
import { TagVoList } from "../models";
/**
 * ApiContentHaloRunV1alpha1TagApi - axios parameter creator
 * @export
 */
export const ApiContentHaloRunV1alpha1TagApiAxiosParamCreator = function (
  configuration?: Configuration
) {
  return {
    /**
     * Lists posts by tag name
     * @param {string} name Tag name
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array<string>} [sort] Sort property and direction of the list result. Support sorting based on attribute name path.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    queryPostsByTagName: async (
      name: string,
      fieldSelector?: Array<string>,
      labelSelector?: Array<string>,
      page?: number,
      size?: number,
      sort?: Array<string>,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("queryPostsByTagName", "name", name);
      const localVarPath =
        `/apis/api.content.halo.run/v1alpha1/tags/{name}/posts`.replace(
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

      if (sort) {
        localVarQueryParameter["sort"] = Array.from(sort);
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
     * Gets tag by name
     * @param {string} name Tag name
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    queryTagByName: async (
      name: string,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("queryTagByName", "name", name);
      const localVarPath =
        `/apis/api.content.halo.run/v1alpha1/tags/{name}`.replace(
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
     * Lists tags
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array<string>} [sort] Sort property and direction of the list result. Support sorting based on attribute name path.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    queryTags: async (
      fieldSelector?: Array<string>,
      labelSelector?: Array<string>,
      page?: number,
      size?: number,
      sort?: Array<string>,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      const localVarPath = `/apis/api.content.halo.run/v1alpha1/tags`;
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

      if (sort) {
        localVarQueryParameter["sort"] = Array.from(sort);
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
  };
};

/**
 * ApiContentHaloRunV1alpha1TagApi - functional programming interface
 * @export
 */
export const ApiContentHaloRunV1alpha1TagApiFp = function (
  configuration?: Configuration
) {
  const localVarAxiosParamCreator =
    ApiContentHaloRunV1alpha1TagApiAxiosParamCreator(configuration);
  return {
    /**
     * Lists posts by tag name
     * @param {string} name Tag name
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array<string>} [sort] Sort property and direction of the list result. Support sorting based on attribute name path.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async queryPostsByTagName(
      name: string,
      fieldSelector?: Array<string>,
      labelSelector?: Array<string>,
      page?: number,
      size?: number,
      sort?: Array<string>,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<ListedPostVo>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.queryPostsByTagName(
          name,
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
     * Gets tag by name
     * @param {string} name Tag name
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async queryTagByName(
      name: string,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<TagVo>
    > {
      const localVarAxiosArgs = await localVarAxiosParamCreator.queryTagByName(
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
     * Lists tags
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array<string>} [sort] Sort property and direction of the list result. Support sorting based on attribute name path.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async queryTags(
      fieldSelector?: Array<string>,
      labelSelector?: Array<string>,
      page?: number,
      size?: number,
      sort?: Array<string>,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<TagVoList>
    > {
      const localVarAxiosArgs = await localVarAxiosParamCreator.queryTags(
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
  };
};

/**
 * ApiContentHaloRunV1alpha1TagApi - factory interface
 * @export
 */
export const ApiContentHaloRunV1alpha1TagApiFactory = function (
  configuration?: Configuration,
  basePath?: string,
  axios?: AxiosInstance
) {
  const localVarFp = ApiContentHaloRunV1alpha1TagApiFp(configuration);
  return {
    /**
     * Lists posts by tag name
     * @param {ApiContentHaloRunV1alpha1TagApiQueryPostsByTagNameRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    queryPostsByTagName(
      requestParameters: ApiContentHaloRunV1alpha1TagApiQueryPostsByTagNameRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<ListedPostVo> {
      return localVarFp
        .queryPostsByTagName(
          requestParameters.name,
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
     * Gets tag by name
     * @param {ApiContentHaloRunV1alpha1TagApiQueryTagByNameRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    queryTagByName(
      requestParameters: ApiContentHaloRunV1alpha1TagApiQueryTagByNameRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<TagVo> {
      return localVarFp
        .queryTagByName(requestParameters.name, options)
        .then((request) => request(axios, basePath));
    },
    /**
     * Lists tags
     * @param {ApiContentHaloRunV1alpha1TagApiQueryTagsRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    queryTags(
      requestParameters: ApiContentHaloRunV1alpha1TagApiQueryTagsRequest = {},
      options?: AxiosRequestConfig
    ): AxiosPromise<TagVoList> {
      return localVarFp
        .queryTags(
          requestParameters.fieldSelector,
          requestParameters.labelSelector,
          requestParameters.page,
          requestParameters.size,
          requestParameters.sort,
          options
        )
        .then((request) => request(axios, basePath));
    },
  };
};

/**
 * Request parameters for queryPostsByTagName operation in ApiContentHaloRunV1alpha1TagApi.
 * @export
 * @interface ApiContentHaloRunV1alpha1TagApiQueryPostsByTagNameRequest
 */
export interface ApiContentHaloRunV1alpha1TagApiQueryPostsByTagNameRequest {
  /**
   * Tag name
   * @type {string}
   * @memberof ApiContentHaloRunV1alpha1TagApiQueryPostsByTagName
   */
  readonly name: string;

  /**
   * Field selector for filtering.
   * @type {Array<string>}
   * @memberof ApiContentHaloRunV1alpha1TagApiQueryPostsByTagName
   */
  readonly fieldSelector?: Array<string>;

  /**
   * Label selector for filtering.
   * @type {Array<string>}
   * @memberof ApiContentHaloRunV1alpha1TagApiQueryPostsByTagName
   */
  readonly labelSelector?: Array<string>;

  /**
   * The page number. Zero indicates no page.
   * @type {number}
   * @memberof ApiContentHaloRunV1alpha1TagApiQueryPostsByTagName
   */
  readonly page?: number;

  /**
   * Size of one page. Zero indicates no limit.
   * @type {number}
   * @memberof ApiContentHaloRunV1alpha1TagApiQueryPostsByTagName
   */
  readonly size?: number;

  /**
   * Sort property and direction of the list result. Support sorting based on attribute name path.
   * @type {Array<string>}
   * @memberof ApiContentHaloRunV1alpha1TagApiQueryPostsByTagName
   */
  readonly sort?: Array<string>;
}

/**
 * Request parameters for queryTagByName operation in ApiContentHaloRunV1alpha1TagApi.
 * @export
 * @interface ApiContentHaloRunV1alpha1TagApiQueryTagByNameRequest
 */
export interface ApiContentHaloRunV1alpha1TagApiQueryTagByNameRequest {
  /**
   * Tag name
   * @type {string}
   * @memberof ApiContentHaloRunV1alpha1TagApiQueryTagByName
   */
  readonly name: string;
}

/**
 * Request parameters for queryTags operation in ApiContentHaloRunV1alpha1TagApi.
 * @export
 * @interface ApiContentHaloRunV1alpha1TagApiQueryTagsRequest
 */
export interface ApiContentHaloRunV1alpha1TagApiQueryTagsRequest {
  /**
   * Field selector for filtering.
   * @type {Array<string>}
   * @memberof ApiContentHaloRunV1alpha1TagApiQueryTags
   */
  readonly fieldSelector?: Array<string>;

  /**
   * Label selector for filtering.
   * @type {Array<string>}
   * @memberof ApiContentHaloRunV1alpha1TagApiQueryTags
   */
  readonly labelSelector?: Array<string>;

  /**
   * The page number. Zero indicates no page.
   * @type {number}
   * @memberof ApiContentHaloRunV1alpha1TagApiQueryTags
   */
  readonly page?: number;

  /**
   * Size of one page. Zero indicates no limit.
   * @type {number}
   * @memberof ApiContentHaloRunV1alpha1TagApiQueryTags
   */
  readonly size?: number;

  /**
   * Sort property and direction of the list result. Support sorting based on attribute name path.
   * @type {Array<string>}
   * @memberof ApiContentHaloRunV1alpha1TagApiQueryTags
   */
  readonly sort?: Array<string>;
}

/**
 * ApiContentHaloRunV1alpha1TagApi - object-oriented interface
 * @export
 * @class ApiContentHaloRunV1alpha1TagApi
 * @extends {BaseAPI}
 */
export class ApiContentHaloRunV1alpha1TagApi extends BaseAPI {
  /**
   * Lists posts by tag name
   * @param {ApiContentHaloRunV1alpha1TagApiQueryPostsByTagNameRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ApiContentHaloRunV1alpha1TagApi
   */
  public queryPostsByTagName(
    requestParameters: ApiContentHaloRunV1alpha1TagApiQueryPostsByTagNameRequest,
    options?: AxiosRequestConfig
  ) {
    return ApiContentHaloRunV1alpha1TagApiFp(this.configuration)
      .queryPostsByTagName(
        requestParameters.name,
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
   * Gets tag by name
   * @param {ApiContentHaloRunV1alpha1TagApiQueryTagByNameRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ApiContentHaloRunV1alpha1TagApi
   */
  public queryTagByName(
    requestParameters: ApiContentHaloRunV1alpha1TagApiQueryTagByNameRequest,
    options?: AxiosRequestConfig
  ) {
    return ApiContentHaloRunV1alpha1TagApiFp(this.configuration)
      .queryTagByName(requestParameters.name, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Lists tags
   * @param {ApiContentHaloRunV1alpha1TagApiQueryTagsRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ApiContentHaloRunV1alpha1TagApi
   */
  public queryTags(
    requestParameters: ApiContentHaloRunV1alpha1TagApiQueryTagsRequest = {},
    options?: AxiosRequestConfig
  ) {
    return ApiContentHaloRunV1alpha1TagApiFp(this.configuration)
      .queryTags(
        requestParameters.fieldSelector,
        requestParameters.labelSelector,
        requestParameters.page,
        requestParameters.size,
        requestParameters.sort,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }
}
