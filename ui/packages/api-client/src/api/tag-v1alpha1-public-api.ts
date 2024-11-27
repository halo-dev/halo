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
import { ListedPostVo } from '../models';
// @ts-ignore
import { TagVo } from '../models';
// @ts-ignore
import { TagVoList } from '../models';
/**
 * TagV1alpha1PublicApi - axios parameter creator
 * @export
 */
export const TagV1alpha1PublicApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Lists posts by tag name
         * @param {string} name Tag name
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        queryPostsByTagName: async (name: string, page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('queryPostsByTagName', 'name', name)
            const localVarPath = `/apis/api.content.halo.run/v1alpha1/tags/{name}/posts`
                .replace(`{${"name"}}`, encodeURIComponent(String(name)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'GET', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication basicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication bearerAuth required
            // http bearer authentication required
            await setBearerAuthToObject(localVarHeaderParameter, configuration)

            if (page !== undefined) {
                localVarQueryParameter['page'] = page;
            }

            if (size !== undefined) {
                localVarQueryParameter['size'] = size;
            }

            if (labelSelector) {
                localVarQueryParameter['labelSelector'] = labelSelector;
            }

            if (fieldSelector) {
                localVarQueryParameter['fieldSelector'] = fieldSelector;
            }

            if (sort) {
                localVarQueryParameter['sort'] = sort;
            }


    
            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

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
        queryTagByName: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('queryTagByName', 'name', name)
            const localVarPath = `/apis/api.content.halo.run/v1alpha1/tags/{name}`
                .replace(`{${"name"}}`, encodeURIComponent(String(name)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'GET', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication basicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication bearerAuth required
            // http bearer authentication required
            await setBearerAuthToObject(localVarHeaderParameter, configuration)


    
            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Lists tags
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        queryTags: async (page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/api.content.halo.run/v1alpha1/tags`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'GET', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication basicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication bearerAuth required
            // http bearer authentication required
            await setBearerAuthToObject(localVarHeaderParameter, configuration)

            if (page !== undefined) {
                localVarQueryParameter['page'] = page;
            }

            if (size !== undefined) {
                localVarQueryParameter['size'] = size;
            }

            if (labelSelector) {
                localVarQueryParameter['labelSelector'] = labelSelector;
            }

            if (fieldSelector) {
                localVarQueryParameter['fieldSelector'] = fieldSelector;
            }

            if (sort) {
                localVarQueryParameter['sort'] = sort;
            }


    
            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * TagV1alpha1PublicApi - functional programming interface
 * @export
 */
export const TagV1alpha1PublicApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = TagV1alpha1PublicApiAxiosParamCreator(configuration)
    return {
        /**
         * Lists posts by tag name
         * @param {string} name Tag name
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async queryPostsByTagName(name: string, page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ListedPostVo>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.queryPostsByTagName(name, page, size, labelSelector, fieldSelector, sort, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TagV1alpha1PublicApi.queryPostsByTagName']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Gets tag by name
         * @param {string} name Tag name
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async queryTagByName(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<TagVo>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.queryTagByName(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TagV1alpha1PublicApi.queryTagByName']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Lists tags
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async queryTags(page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<TagVoList>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.queryTags(page, size, labelSelector, fieldSelector, sort, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TagV1alpha1PublicApi.queryTags']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * TagV1alpha1PublicApi - factory interface
 * @export
 */
export const TagV1alpha1PublicApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = TagV1alpha1PublicApiFp(configuration)
    return {
        /**
         * Lists posts by tag name
         * @param {TagV1alpha1PublicApiQueryPostsByTagNameRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        queryPostsByTagName(requestParameters: TagV1alpha1PublicApiQueryPostsByTagNameRequest, options?: RawAxiosRequestConfig): AxiosPromise<ListedPostVo> {
            return localVarFp.queryPostsByTagName(requestParameters.name, requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(axios, basePath));
        },
        /**
         * Gets tag by name
         * @param {TagV1alpha1PublicApiQueryTagByNameRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        queryTagByName(requestParameters: TagV1alpha1PublicApiQueryTagByNameRequest, options?: RawAxiosRequestConfig): AxiosPromise<TagVo> {
            return localVarFp.queryTagByName(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * Lists tags
         * @param {TagV1alpha1PublicApiQueryTagsRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        queryTags(requestParameters: TagV1alpha1PublicApiQueryTagsRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<TagVoList> {
            return localVarFp.queryTags(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for queryPostsByTagName operation in TagV1alpha1PublicApi.
 * @export
 * @interface TagV1alpha1PublicApiQueryPostsByTagNameRequest
 */
export interface TagV1alpha1PublicApiQueryPostsByTagNameRequest {
    /**
     * Tag name
     * @type {string}
     * @memberof TagV1alpha1PublicApiQueryPostsByTagName
     */
    readonly name: string

    /**
     * Page number. Default is 0.
     * @type {number}
     * @memberof TagV1alpha1PublicApiQueryPostsByTagName
     */
    readonly page?: number

    /**
     * Size number. Default is 0.
     * @type {number}
     * @memberof TagV1alpha1PublicApiQueryPostsByTagName
     */
    readonly size?: number

    /**
     * Label selector. e.g.: hidden!&#x3D;true
     * @type {Array<string>}
     * @memberof TagV1alpha1PublicApiQueryPostsByTagName
     */
    readonly labelSelector?: Array<string>

    /**
     * Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
     * @type {Array<string>}
     * @memberof TagV1alpha1PublicApiQueryPostsByTagName
     */
    readonly fieldSelector?: Array<string>

    /**
     * Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
     * @type {Array<string>}
     * @memberof TagV1alpha1PublicApiQueryPostsByTagName
     */
    readonly sort?: Array<string>
}

/**
 * Request parameters for queryTagByName operation in TagV1alpha1PublicApi.
 * @export
 * @interface TagV1alpha1PublicApiQueryTagByNameRequest
 */
export interface TagV1alpha1PublicApiQueryTagByNameRequest {
    /**
     * Tag name
     * @type {string}
     * @memberof TagV1alpha1PublicApiQueryTagByName
     */
    readonly name: string
}

/**
 * Request parameters for queryTags operation in TagV1alpha1PublicApi.
 * @export
 * @interface TagV1alpha1PublicApiQueryTagsRequest
 */
export interface TagV1alpha1PublicApiQueryTagsRequest {
    /**
     * Page number. Default is 0.
     * @type {number}
     * @memberof TagV1alpha1PublicApiQueryTags
     */
    readonly page?: number

    /**
     * Size number. Default is 0.
     * @type {number}
     * @memberof TagV1alpha1PublicApiQueryTags
     */
    readonly size?: number

    /**
     * Label selector. e.g.: hidden!&#x3D;true
     * @type {Array<string>}
     * @memberof TagV1alpha1PublicApiQueryTags
     */
    readonly labelSelector?: Array<string>

    /**
     * Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
     * @type {Array<string>}
     * @memberof TagV1alpha1PublicApiQueryTags
     */
    readonly fieldSelector?: Array<string>

    /**
     * Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
     * @type {Array<string>}
     * @memberof TagV1alpha1PublicApiQueryTags
     */
    readonly sort?: Array<string>
}

/**
 * TagV1alpha1PublicApi - object-oriented interface
 * @export
 * @class TagV1alpha1PublicApi
 * @extends {BaseAPI}
 */
export class TagV1alpha1PublicApi extends BaseAPI {
    /**
     * Lists posts by tag name
     * @param {TagV1alpha1PublicApiQueryPostsByTagNameRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof TagV1alpha1PublicApi
     */
    public queryPostsByTagName(requestParameters: TagV1alpha1PublicApiQueryPostsByTagNameRequest, options?: RawAxiosRequestConfig) {
        return TagV1alpha1PublicApiFp(this.configuration).queryPostsByTagName(requestParameters.name, requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Gets tag by name
     * @param {TagV1alpha1PublicApiQueryTagByNameRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof TagV1alpha1PublicApi
     */
    public queryTagByName(requestParameters: TagV1alpha1PublicApiQueryTagByNameRequest, options?: RawAxiosRequestConfig) {
        return TagV1alpha1PublicApiFp(this.configuration).queryTagByName(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Lists tags
     * @param {TagV1alpha1PublicApiQueryTagsRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof TagV1alpha1PublicApi
     */
    public queryTags(requestParameters: TagV1alpha1PublicApiQueryTagsRequest = {}, options?: RawAxiosRequestConfig) {
        return TagV1alpha1PublicApiFp(this.configuration).queryTags(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(this.axios, this.basePath));
    }
}

