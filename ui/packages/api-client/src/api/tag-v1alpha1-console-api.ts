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
import { TagList } from '../models';
/**
 * TagV1alpha1ConsoleApi - axios parameter creator
 * @export
 */
export const TagV1alpha1ConsoleApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * List Post Tags.
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {string} [keyword] Post tags filtered by keyword.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listPostTags: async (page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, keyword?: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/api.console.halo.run/v1alpha1/tags`;
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

            if (keyword !== undefined) {
                localVarQueryParameter['keyword'] = keyword;
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
 * TagV1alpha1ConsoleApi - functional programming interface
 * @export
 */
export const TagV1alpha1ConsoleApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = TagV1alpha1ConsoleApiAxiosParamCreator(configuration)
    return {
        /**
         * List Post Tags.
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {string} [keyword] Post tags filtered by keyword.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async listPostTags(page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, keyword?: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<TagList>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.listPostTags(page, size, labelSelector, fieldSelector, sort, keyword, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TagV1alpha1ConsoleApi.listPostTags']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * TagV1alpha1ConsoleApi - factory interface
 * @export
 */
export const TagV1alpha1ConsoleApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = TagV1alpha1ConsoleApiFp(configuration)
    return {
        /**
         * List Post Tags.
         * @param {TagV1alpha1ConsoleApiListPostTagsRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listPostTags(requestParameters: TagV1alpha1ConsoleApiListPostTagsRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<TagList> {
            return localVarFp.listPostTags(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, requestParameters.keyword, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for listPostTags operation in TagV1alpha1ConsoleApi.
 * @export
 * @interface TagV1alpha1ConsoleApiListPostTagsRequest
 */
export interface TagV1alpha1ConsoleApiListPostTagsRequest {
    /**
     * Page number. Default is 0.
     * @type {number}
     * @memberof TagV1alpha1ConsoleApiListPostTags
     */
    readonly page?: number

    /**
     * Size number. Default is 0.
     * @type {number}
     * @memberof TagV1alpha1ConsoleApiListPostTags
     */
    readonly size?: number

    /**
     * Label selector. e.g.: hidden!&#x3D;true
     * @type {Array<string>}
     * @memberof TagV1alpha1ConsoleApiListPostTags
     */
    readonly labelSelector?: Array<string>

    /**
     * Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
     * @type {Array<string>}
     * @memberof TagV1alpha1ConsoleApiListPostTags
     */
    readonly fieldSelector?: Array<string>

    /**
     * Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
     * @type {Array<string>}
     * @memberof TagV1alpha1ConsoleApiListPostTags
     */
    readonly sort?: Array<string>

    /**
     * Post tags filtered by keyword.
     * @type {string}
     * @memberof TagV1alpha1ConsoleApiListPostTags
     */
    readonly keyword?: string
}

/**
 * TagV1alpha1ConsoleApi - object-oriented interface
 * @export
 * @class TagV1alpha1ConsoleApi
 * @extends {BaseAPI}
 */
export class TagV1alpha1ConsoleApi extends BaseAPI {
    /**
     * List Post Tags.
     * @param {TagV1alpha1ConsoleApiListPostTagsRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof TagV1alpha1ConsoleApi
     */
    public listPostTags(requestParameters: TagV1alpha1ConsoleApiListPostTagsRequest = {}, options?: RawAxiosRequestConfig) {
        return TagV1alpha1ConsoleApiFp(this.configuration).listPostTags(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, requestParameters.keyword, options).then((request) => request(this.axios, this.basePath));
    }
}

