/* tslint:disable */
/* eslint-disable */
/**
 * Halo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.20.10-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


import type { Configuration } from '../configuration';
import type { AxiosPromise, AxiosInstance, RawAxiosRequestConfig } from 'axios';
import globalAxios from 'axios';
// Some imports not used depending on template conditions
// @ts-ignore
import { DUMMY_BASE_URL, assertParamExists, setApiKeyToObject, setBasicAuthToObject, setBearerAuthToObject, setOAuthToObject, setSearchParams, serializeDataIfNeeded, toPathString, createRequestFunction } from '../common';
// @ts-ignore
import { BASE_PATH, COLLECTION_FORMATS, RequestArgs, BaseAPI, RequiredError, operationServerMap } from '../base';
// @ts-ignore
import { CounterRequest } from '../models';
// @ts-ignore
import { VoteRequest } from '../models';
/**
 * MetricsV1alpha1PublicApi - axios parameter creator
 * @export
 */
export const MetricsV1alpha1PublicApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Count an extension resource visits.
         * @param {CounterRequest} counterRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        count: async (counterRequest: CounterRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'counterRequest' is not null or undefined
            assertParamExists('count', 'counterRequest', counterRequest)
            const localVarPath = `/apis/api.halo.run/v1alpha1/trackers/counter`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication basicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication bearerAuth required
            // http bearer authentication required
            await setBearerAuthToObject(localVarHeaderParameter, configuration)


    
            localVarHeaderParameter['Content-Type'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(counterRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Downvote an extension resource.
         * @param {VoteRequest} voteRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        downvote: async (voteRequest: VoteRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'voteRequest' is not null or undefined
            assertParamExists('downvote', 'voteRequest', voteRequest)
            const localVarPath = `/apis/api.halo.run/v1alpha1/trackers/downvote`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication basicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication bearerAuth required
            // http bearer authentication required
            await setBearerAuthToObject(localVarHeaderParameter, configuration)


    
            localVarHeaderParameter['Content-Type'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(voteRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Upvote an extension resource.
         * @param {VoteRequest} voteRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        upvote: async (voteRequest: VoteRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'voteRequest' is not null or undefined
            assertParamExists('upvote', 'voteRequest', voteRequest)
            const localVarPath = `/apis/api.halo.run/v1alpha1/trackers/upvote`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication basicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication bearerAuth required
            // http bearer authentication required
            await setBearerAuthToObject(localVarHeaderParameter, configuration)


    
            localVarHeaderParameter['Content-Type'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(voteRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * MetricsV1alpha1PublicApi - functional programming interface
 * @export
 */
export const MetricsV1alpha1PublicApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = MetricsV1alpha1PublicApiAxiosParamCreator(configuration)
    return {
        /**
         * Count an extension resource visits.
         * @param {CounterRequest} counterRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async count(counterRequest: CounterRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.count(counterRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['MetricsV1alpha1PublicApi.count']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Downvote an extension resource.
         * @param {VoteRequest} voteRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async downvote(voteRequest: VoteRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.downvote(voteRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['MetricsV1alpha1PublicApi.downvote']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Upvote an extension resource.
         * @param {VoteRequest} voteRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async upvote(voteRequest: VoteRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.upvote(voteRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['MetricsV1alpha1PublicApi.upvote']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * MetricsV1alpha1PublicApi - factory interface
 * @export
 */
export const MetricsV1alpha1PublicApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = MetricsV1alpha1PublicApiFp(configuration)
    return {
        /**
         * Count an extension resource visits.
         * @param {MetricsV1alpha1PublicApiCountRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        count(requestParameters: MetricsV1alpha1PublicApiCountRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.count(requestParameters.counterRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * Downvote an extension resource.
         * @param {MetricsV1alpha1PublicApiDownvoteRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        downvote(requestParameters: MetricsV1alpha1PublicApiDownvoteRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.downvote(requestParameters.voteRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * Upvote an extension resource.
         * @param {MetricsV1alpha1PublicApiUpvoteRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        upvote(requestParameters: MetricsV1alpha1PublicApiUpvoteRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.upvote(requestParameters.voteRequest, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for count operation in MetricsV1alpha1PublicApi.
 * @export
 * @interface MetricsV1alpha1PublicApiCountRequest
 */
export interface MetricsV1alpha1PublicApiCountRequest {
    /**
     * 
     * @type {CounterRequest}
     * @memberof MetricsV1alpha1PublicApiCount
     */
    readonly counterRequest: CounterRequest
}

/**
 * Request parameters for downvote operation in MetricsV1alpha1PublicApi.
 * @export
 * @interface MetricsV1alpha1PublicApiDownvoteRequest
 */
export interface MetricsV1alpha1PublicApiDownvoteRequest {
    /**
     * 
     * @type {VoteRequest}
     * @memberof MetricsV1alpha1PublicApiDownvote
     */
    readonly voteRequest: VoteRequest
}

/**
 * Request parameters for upvote operation in MetricsV1alpha1PublicApi.
 * @export
 * @interface MetricsV1alpha1PublicApiUpvoteRequest
 */
export interface MetricsV1alpha1PublicApiUpvoteRequest {
    /**
     * 
     * @type {VoteRequest}
     * @memberof MetricsV1alpha1PublicApiUpvote
     */
    readonly voteRequest: VoteRequest
}

/**
 * MetricsV1alpha1PublicApi - object-oriented interface
 * @export
 * @class MetricsV1alpha1PublicApi
 * @extends {BaseAPI}
 */
export class MetricsV1alpha1PublicApi extends BaseAPI {
    /**
     * Count an extension resource visits.
     * @param {MetricsV1alpha1PublicApiCountRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof MetricsV1alpha1PublicApi
     */
    public count(requestParameters: MetricsV1alpha1PublicApiCountRequest, options?: RawAxiosRequestConfig) {
        return MetricsV1alpha1PublicApiFp(this.configuration).count(requestParameters.counterRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Downvote an extension resource.
     * @param {MetricsV1alpha1PublicApiDownvoteRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof MetricsV1alpha1PublicApi
     */
    public downvote(requestParameters: MetricsV1alpha1PublicApiDownvoteRequest, options?: RawAxiosRequestConfig) {
        return MetricsV1alpha1PublicApiFp(this.configuration).downvote(requestParameters.voteRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Upvote an extension resource.
     * @param {MetricsV1alpha1PublicApiUpvoteRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof MetricsV1alpha1PublicApi
     */
    public upvote(requestParameters: MetricsV1alpha1PublicApiUpvoteRequest, options?: RawAxiosRequestConfig) {
        return MetricsV1alpha1PublicApiFp(this.configuration).upvote(requestParameters.voteRequest, options).then((request) => request(this.axios, this.basePath));
    }
}

