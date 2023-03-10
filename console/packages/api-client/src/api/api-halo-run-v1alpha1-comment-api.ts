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
import { Comment } from "../models";
// @ts-ignore
import { CommentRequest } from "../models";
// @ts-ignore
import { CommentVoList } from "../models";
// @ts-ignore
import { Reply } from "../models";
// @ts-ignore
import { ReplyRequest } from "../models";
// @ts-ignore
import { ReplyVoList } from "../models";
/**
 * ApiHaloRunV1alpha1CommentApi - axios parameter creator
 * @export
 */
export const ApiHaloRunV1alpha1CommentApiAxiosParamCreator = function (
  configuration?: Configuration
) {
  return {
    /**
     * Create a comment.
     * @param {CommentRequest} commentRequest
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createComment1: async (
      commentRequest: CommentRequest,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'commentRequest' is not null or undefined
      assertParamExists("createComment1", "commentRequest", commentRequest);
      const localVarPath = `/apis/api.halo.run/v1alpha1/comments`;
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
        commentRequest,
        localVarRequestOptions,
        configuration
      );

      return {
        url: toPathString(localVarUrlObj),
        options: localVarRequestOptions,
      };
    },
    /**
     * Create a reply.
     * @param {string} name
     * @param {ReplyRequest} replyRequest
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createReply1: async (
      name: string,
      replyRequest: ReplyRequest,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("createReply1", "name", name);
      // verify required parameter 'replyRequest' is not null or undefined
      assertParamExists("createReply1", "replyRequest", replyRequest);
      const localVarPath =
        `/apis/api.halo.run/v1alpha1/comments/{name}/reply`.replace(
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
        replyRequest,
        localVarRequestOptions,
        configuration
      );

      return {
        url: toPathString(localVarUrlObj),
        options: localVarRequestOptions,
      };
    },
    /**
     * Get a comment.
     * @param {string} name
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getComment: async (
      name: string,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("getComment", "name", name);
      const localVarPath =
        `/apis/api.halo.run/v1alpha1/comments/{name}`.replace(
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
     * List comment replies.
     * @param {string} name
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listCommentReplies: async (
      name: string,
      size?: number,
      page?: number,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("listCommentReplies", "name", name);
      const localVarPath =
        `/apis/api.halo.run/v1alpha1/comments/{name}/reply`.replace(
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

      if (size !== undefined) {
        localVarQueryParameter["size"] = size;
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
     * List comments.
     * @param {string} name The comment subject name.
     * @param {string} version The comment subject version.
     * @param {string} kind The comment subject kind.
     * @param {string} [group] The comment subject group.
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listComments1: async (
      name: string,
      version: string,
      kind: string,
      group?: string,
      size?: number,
      page?: number,
      options: AxiosRequestConfig = {}
    ): Promise<RequestArgs> => {
      // verify required parameter 'name' is not null or undefined
      assertParamExists("listComments1", "name", name);
      // verify required parameter 'version' is not null or undefined
      assertParamExists("listComments1", "version", version);
      // verify required parameter 'kind' is not null or undefined
      assertParamExists("listComments1", "kind", kind);
      const localVarPath = `/apis/api.halo.run/v1alpha1/comments`;
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

      if (name !== undefined) {
        localVarQueryParameter["name"] = name;
      }

      if (version !== undefined) {
        localVarQueryParameter["version"] = version;
      }

      if (group !== undefined) {
        localVarQueryParameter["group"] = group;
      }

      if (kind !== undefined) {
        localVarQueryParameter["kind"] = kind;
      }

      if (size !== undefined) {
        localVarQueryParameter["size"] = size;
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
 * ApiHaloRunV1alpha1CommentApi - functional programming interface
 * @export
 */
export const ApiHaloRunV1alpha1CommentApiFp = function (
  configuration?: Configuration
) {
  const localVarAxiosParamCreator =
    ApiHaloRunV1alpha1CommentApiAxiosParamCreator(configuration);
  return {
    /**
     * Create a comment.
     * @param {CommentRequest} commentRequest
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async createComment1(
      commentRequest: CommentRequest,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<Comment>
    > {
      const localVarAxiosArgs = await localVarAxiosParamCreator.createComment1(
        commentRequest,
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
     * Create a reply.
     * @param {string} name
     * @param {ReplyRequest} replyRequest
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async createReply1(
      name: string,
      replyRequest: ReplyRequest,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<Reply>
    > {
      const localVarAxiosArgs = await localVarAxiosParamCreator.createReply1(
        name,
        replyRequest,
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
     * Get a comment.
     * @param {string} name
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async getComment(
      name: string,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<CommentVoList>
    > {
      const localVarAxiosArgs = await localVarAxiosParamCreator.getComment(
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
     * List comment replies.
     * @param {string} name
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async listCommentReplies(
      name: string,
      size?: number,
      page?: number,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<ReplyVoList>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.listCommentReplies(
          name,
          size,
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
     * List comments.
     * @param {string} name The comment subject name.
     * @param {string} version The comment subject version.
     * @param {string} kind The comment subject kind.
     * @param {string} [group] The comment subject group.
     * @param {number} [size] Size of one page. Zero indicates no limit.
     * @param {number} [page] The page number. Zero indicates no page.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async listComments1(
      name: string,
      version: string,
      kind: string,
      group?: string,
      size?: number,
      page?: number,
      options?: AxiosRequestConfig
    ): Promise<
      (axios?: AxiosInstance, basePath?: string) => AxiosPromise<CommentVoList>
    > {
      const localVarAxiosArgs = await localVarAxiosParamCreator.listComments1(
        name,
        version,
        kind,
        group,
        size,
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
 * ApiHaloRunV1alpha1CommentApi - factory interface
 * @export
 */
export const ApiHaloRunV1alpha1CommentApiFactory = function (
  configuration?: Configuration,
  basePath?: string,
  axios?: AxiosInstance
) {
  const localVarFp = ApiHaloRunV1alpha1CommentApiFp(configuration);
  return {
    /**
     * Create a comment.
     * @param {ApiHaloRunV1alpha1CommentApiCreateComment1Request} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createComment1(
      requestParameters: ApiHaloRunV1alpha1CommentApiCreateComment1Request,
      options?: AxiosRequestConfig
    ): AxiosPromise<Comment> {
      return localVarFp
        .createComment1(requestParameters.commentRequest, options)
        .then((request) => request(axios, basePath));
    },
    /**
     * Create a reply.
     * @param {ApiHaloRunV1alpha1CommentApiCreateReply1Request} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    createReply1(
      requestParameters: ApiHaloRunV1alpha1CommentApiCreateReply1Request,
      options?: AxiosRequestConfig
    ): AxiosPromise<Reply> {
      return localVarFp
        .createReply1(
          requestParameters.name,
          requestParameters.replyRequest,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * Get a comment.
     * @param {ApiHaloRunV1alpha1CommentApiGetCommentRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    getComment(
      requestParameters: ApiHaloRunV1alpha1CommentApiGetCommentRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<CommentVoList> {
      return localVarFp
        .getComment(requestParameters.name, options)
        .then((request) => request(axios, basePath));
    },
    /**
     * List comment replies.
     * @param {ApiHaloRunV1alpha1CommentApiListCommentRepliesRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listCommentReplies(
      requestParameters: ApiHaloRunV1alpha1CommentApiListCommentRepliesRequest,
      options?: AxiosRequestConfig
    ): AxiosPromise<ReplyVoList> {
      return localVarFp
        .listCommentReplies(
          requestParameters.name,
          requestParameters.size,
          requestParameters.page,
          options
        )
        .then((request) => request(axios, basePath));
    },
    /**
     * List comments.
     * @param {ApiHaloRunV1alpha1CommentApiListComments1Request} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listComments1(
      requestParameters: ApiHaloRunV1alpha1CommentApiListComments1Request,
      options?: AxiosRequestConfig
    ): AxiosPromise<CommentVoList> {
      return localVarFp
        .listComments1(
          requestParameters.name,
          requestParameters.version,
          requestParameters.kind,
          requestParameters.group,
          requestParameters.size,
          requestParameters.page,
          options
        )
        .then((request) => request(axios, basePath));
    },
  };
};

/**
 * Request parameters for createComment1 operation in ApiHaloRunV1alpha1CommentApi.
 * @export
 * @interface ApiHaloRunV1alpha1CommentApiCreateComment1Request
 */
export interface ApiHaloRunV1alpha1CommentApiCreateComment1Request {
  /**
   *
   * @type {CommentRequest}
   * @memberof ApiHaloRunV1alpha1CommentApiCreateComment1
   */
  readonly commentRequest: CommentRequest;
}

/**
 * Request parameters for createReply1 operation in ApiHaloRunV1alpha1CommentApi.
 * @export
 * @interface ApiHaloRunV1alpha1CommentApiCreateReply1Request
 */
export interface ApiHaloRunV1alpha1CommentApiCreateReply1Request {
  /**
   *
   * @type {string}
   * @memberof ApiHaloRunV1alpha1CommentApiCreateReply1
   */
  readonly name: string;

  /**
   *
   * @type {ReplyRequest}
   * @memberof ApiHaloRunV1alpha1CommentApiCreateReply1
   */
  readonly replyRequest: ReplyRequest;
}

/**
 * Request parameters for getComment operation in ApiHaloRunV1alpha1CommentApi.
 * @export
 * @interface ApiHaloRunV1alpha1CommentApiGetCommentRequest
 */
export interface ApiHaloRunV1alpha1CommentApiGetCommentRequest {
  /**
   *
   * @type {string}
   * @memberof ApiHaloRunV1alpha1CommentApiGetComment
   */
  readonly name: string;
}

/**
 * Request parameters for listCommentReplies operation in ApiHaloRunV1alpha1CommentApi.
 * @export
 * @interface ApiHaloRunV1alpha1CommentApiListCommentRepliesRequest
 */
export interface ApiHaloRunV1alpha1CommentApiListCommentRepliesRequest {
  /**
   *
   * @type {string}
   * @memberof ApiHaloRunV1alpha1CommentApiListCommentReplies
   */
  readonly name: string;

  /**
   * Size of one page. Zero indicates no limit.
   * @type {number}
   * @memberof ApiHaloRunV1alpha1CommentApiListCommentReplies
   */
  readonly size?: number;

  /**
   * The page number. Zero indicates no page.
   * @type {number}
   * @memberof ApiHaloRunV1alpha1CommentApiListCommentReplies
   */
  readonly page?: number;
}

/**
 * Request parameters for listComments1 operation in ApiHaloRunV1alpha1CommentApi.
 * @export
 * @interface ApiHaloRunV1alpha1CommentApiListComments1Request
 */
export interface ApiHaloRunV1alpha1CommentApiListComments1Request {
  /**
   * The comment subject name.
   * @type {string}
   * @memberof ApiHaloRunV1alpha1CommentApiListComments1
   */
  readonly name: string;

  /**
   * The comment subject version.
   * @type {string}
   * @memberof ApiHaloRunV1alpha1CommentApiListComments1
   */
  readonly version: string;

  /**
   * The comment subject kind.
   * @type {string}
   * @memberof ApiHaloRunV1alpha1CommentApiListComments1
   */
  readonly kind: string;

  /**
   * The comment subject group.
   * @type {string}
   * @memberof ApiHaloRunV1alpha1CommentApiListComments1
   */
  readonly group?: string;

  /**
   * Size of one page. Zero indicates no limit.
   * @type {number}
   * @memberof ApiHaloRunV1alpha1CommentApiListComments1
   */
  readonly size?: number;

  /**
   * The page number. Zero indicates no page.
   * @type {number}
   * @memberof ApiHaloRunV1alpha1CommentApiListComments1
   */
  readonly page?: number;
}

/**
 * ApiHaloRunV1alpha1CommentApi - object-oriented interface
 * @export
 * @class ApiHaloRunV1alpha1CommentApi
 * @extends {BaseAPI}
 */
export class ApiHaloRunV1alpha1CommentApi extends BaseAPI {
  /**
   * Create a comment.
   * @param {ApiHaloRunV1alpha1CommentApiCreateComment1Request} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ApiHaloRunV1alpha1CommentApi
   */
  public createComment1(
    requestParameters: ApiHaloRunV1alpha1CommentApiCreateComment1Request,
    options?: AxiosRequestConfig
  ) {
    return ApiHaloRunV1alpha1CommentApiFp(this.configuration)
      .createComment1(requestParameters.commentRequest, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Create a reply.
   * @param {ApiHaloRunV1alpha1CommentApiCreateReply1Request} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ApiHaloRunV1alpha1CommentApi
   */
  public createReply1(
    requestParameters: ApiHaloRunV1alpha1CommentApiCreateReply1Request,
    options?: AxiosRequestConfig
  ) {
    return ApiHaloRunV1alpha1CommentApiFp(this.configuration)
      .createReply1(
        requestParameters.name,
        requestParameters.replyRequest,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * Get a comment.
   * @param {ApiHaloRunV1alpha1CommentApiGetCommentRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ApiHaloRunV1alpha1CommentApi
   */
  public getComment(
    requestParameters: ApiHaloRunV1alpha1CommentApiGetCommentRequest,
    options?: AxiosRequestConfig
  ) {
    return ApiHaloRunV1alpha1CommentApiFp(this.configuration)
      .getComment(requestParameters.name, options)
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * List comment replies.
   * @param {ApiHaloRunV1alpha1CommentApiListCommentRepliesRequest} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ApiHaloRunV1alpha1CommentApi
   */
  public listCommentReplies(
    requestParameters: ApiHaloRunV1alpha1CommentApiListCommentRepliesRequest,
    options?: AxiosRequestConfig
  ) {
    return ApiHaloRunV1alpha1CommentApiFp(this.configuration)
      .listCommentReplies(
        requestParameters.name,
        requestParameters.size,
        requestParameters.page,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }

  /**
   * List comments.
   * @param {ApiHaloRunV1alpha1CommentApiListComments1Request} requestParameters Request parameters.
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ApiHaloRunV1alpha1CommentApi
   */
  public listComments1(
    requestParameters: ApiHaloRunV1alpha1CommentApiListComments1Request,
    options?: AxiosRequestConfig
  ) {
    return ApiHaloRunV1alpha1CommentApiFp(this.configuration)
      .listComments1(
        requestParameters.name,
        requestParameters.version,
        requestParameters.kind,
        requestParameters.group,
        requestParameters.size,
        requestParameters.page,
        options
      )
      .then((request) => request(this.axios, this.basePath));
  }
}
