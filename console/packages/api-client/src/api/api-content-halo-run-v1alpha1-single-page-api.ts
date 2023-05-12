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
import { ListedSinglePageVoList } from "../models";
// @ts-ignore
import { SinglePageVo } from "../models";
/**
 * ApiContentHaloRunV1alpha1SinglePageApi - axios parameter creator
 * @export
 */
export const ApiContentHaloRunV1alpha1SinglePageApiAxiosParamCreator =
  function (configuration?: Configuration) {
    return {
      /**
       * Gets single page by name
       * @param {string} name SinglePage name
       * @param {*} [options] Override http request option.
       * @throws {RequiredError}
       */
      querySinglePageByName: async (
        name: string,
        options: AxiosRequestConfig = {}
      ): Promise<RequestArgs> => {
        // verify required parameter 'name' is not null or undefined
        assertParamExists("querySinglePageByName", "name", name);
        const localVarPath =
          `/apis/api.content.halo.run/v1alpha1/singlepages/{name}`.replace(
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
       * Lists single pages
       * @param {Array<string>} [sort] Sort property and direction of the list result. Support sorting based on attribute name path.
       * @param {number} [size] Size of one page. Zero indicates no limit.
       * @param {Array<string>} [labelSelector] Label selector for filtering.
       * @param {Array<string>} [fieldSelector] Field selector for filtering.
       * @param {number} [page] The page number. Zero indicates no page.
       * @param {*} [options] Override http request option.
       * @throws {RequiredError}
       */
      querySinglePages: async (
        sort?: Array<string>,
        size?: number,
        labelSelector?: Array<string>,
        fieldSelector?: Array<string>,
        page?: number,
        options: AxiosRequestConfig = {}
      ): Promise<RequestArgs> => {
        const localVarPath = `/apis/api.content.halo.run/v1alpha1/singlepages`;
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

        if (sort) {
          localVarQueryParameter["sort"] = Array.from(sort);
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
    };
  };

/**
 * ApiContentHaloRunV1alpha1SinglePageApi - functional programming interface
 * @export
 */
export const ApiContentHaloRunV1alpha1SinglePageApiFp = function (
  configuration?: Configuration
) {
  const localVarAxiosParamCreator =
    ApiContentHaloRunV1alpha1SinglePageApiAxiosParamCreator(configuration);
  return {
    /**
     * Gets single page by name
     * @param {string} name SinglePage name
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async querySinglePageByName(
      name: string,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<SinglePageVo>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.querySinglePageByName(name, options);
      return createRequestFunction(
        localVarAxiosArgs,
        globalAxios,
        BASE_PATH,
        configuration
      );
    },
    /**
     * Lists single pages
     * @param {Array<string>} [sort] Sort property and direction of the list result. Support sorting based on attribute name path.
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {Array<string>} [labelSelector] Label selector for filtering.
     * @param {Array<string>} [fieldSelector] Field selector for filtering.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async querySinglePages(
      sort?: Array<string>,
      size?: number,
      labelSelector?: Array<string>,
      fieldSelector?: Array<string>,
      page?: number,
      options?: AxiosRequestConfig
    ): Promise<
      (
        axios?: AxiosInstance,
        basePath?: string
      ) => AxiosPromise<ListedSinglePageVoList>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.querySinglePages(
          sort,
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
  };
};

/**
 * ApiContentHaloRunV1alpha1SinglePageApi - factory interface
 * @export
 */
export const ApiContentHaloRunV1alpha1SinglePageApiFactory = function (
  configuration?: Configuration,
  basePath?: string,
  axios?: AxiosInstance
) {
  const localVarFp = ApiContentHaloRunV1alpha1SinglePageApiFp(configuration);
  return {
    /**
     * Gets single page by name
     * @param {ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePageByNameRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    querySinglePageByName(
      requestParameters: ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePageByNameRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<SinglePageVo> {
      return localVarFp
        .querySinglePageByName(requestParameters.name, options)
        .then((request) => request(axios, basePath));
    },
    /**
     * Lists single pages
     * @param {ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePagesRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    querySinglePages(
      requestParameters: ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePagesRequest = {},
      options?: AxiosRequestConfig
    ): AxiosPromise<ListedSinglePageVoList> {
      return localVarFp
        .querySinglePages(
          requestParameters.sort,
          requestParameters.size,
          requestParameters.labelSelector,
          requestParameters.fieldSelector,
          requestParameters.page,
          options
        )
        .then((request) => request(axios, basePath));
    },
  };
};

/**
 * Request parameters for querySinglePageByName operation in ApiContentHaloRunV1alpha1SinglePageApi.
 * @export
 * @interface ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePageByNameRequest
 */
export interface ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePageByNameRequest {
  /**
   * SinglePage name
   * @type {string}
   * @memberof ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePageByName
   */
  readonly name: string;
}

/**
 * Request parameters for querySinglePages operation in ApiContentHaloRunV1alpha1SinglePageApi.
 * @export
 * @interface ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePagesRequest
 */
export interface ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePagesRequest {
  /**
   * Sort property and direction of the list result. Support sorting based on attribute name path.
   * @type {Array<string>}
   * @memberof ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePages
   */
  readonly sort?: Array<string>;

  /**
   * Size of one page. Zero indicates no limit.
   * @type {number}
   * @memberof ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePages
   */
  readonly size?: number;

  /**
   * Label selector for filtering.
   * @type {Array<string>}
   * @memberof ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePages
   */
  readonly labelSelector?: Array<string>;

  /**
   * Field selector for filtering.
   * @type {Array<string>}
   * @memberof ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePages
   */
  readonly fieldSelector?: Array<string>;

  /**
   * The page number. Zero indicates no page.
   * @type {number}
   * @memberof ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePages
   */
  readonly page?: number;
}

/**
 * ApiContentHaloRunV1alpha1SinglePageApi - object-oriented interface
 * @export
 * @class ApiContentHaloRunV1alpha1SinglePageApi
 * @extends {BaseAPI}
 */
export class ApiContentHaloRunV1alpha1SinglePageApi extends BaseAPI {
  /**
   * Gets single page by name
   * @param {ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePageByNameRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ApiContentHaloRunV1alpha1SinglePageApi
   */
  public querySinglePageByName(
    requestParameters: ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePageByNameRequest,
    options?: AxiosRequestConfig
  ) {
    return ApiContentHaloRunV1alpha1SinglePageApiFp(this.configuration)
      .querySinglePageByName(requestParameters.name, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Lists single pages
   * @param {ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePagesRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ApiContentHaloRunV1alpha1SinglePageApi
   */
  public querySinglePages(
    requestParameters: ApiContentHaloRunV1alpha1SinglePageApiQuerySinglePagesRequest = {},
    options?: AxiosRequestConfig
  ) {
    return ApiContentHaloRunV1alpha1SinglePageApiFp(this.configuration)
      .querySinglePages(
        requestParameters.sort,
        requestParameters.size,
        requestParameters.labelSelector,
        requestParameters.fieldSelector,
        requestParameters.page,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }
}
