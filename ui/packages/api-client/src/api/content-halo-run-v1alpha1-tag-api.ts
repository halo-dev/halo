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


import type { Configuration } from '../configuration';
import type { AxiosPromise, AxiosInstance, RawAxiosRequestConfig } from 'axios';
import globalAxios from 'axios';
// Some imports not used depending on template conditions
// @ts-ignore
import { DUMMY_BASE_URL, assertParamExists, setApiKeyToObject, setBasicAuthToObject, setBearerAuthToObject, setOAuthToObject, setSearchParams, serializeDataIfNeeded, toPathString, createRequestFunction } from '../common';
// @ts-ignore
import { BASE_PATH, COLLECTION_FORMATS, RequestArgs, BaseAPI, RequiredError, operationServerMap } from '../base';
// @ts-ignore
import { Tag } from '../models';
// @ts-ignore
import { TagList } from '../models';
/**
 * ContentHaloRunV1alpha1TagApi - axios parameter creator
 * @export
 */
export const ContentHaloRunV1alpha1TagApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Create content.halo.run/v1alpha1/Tag
         * @param {Tag} [tag] Fresh tag
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createcontentHaloRunV1alpha1Tag: async (tag?: Tag, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/content.halo.run/v1alpha1/tags`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication BasicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication BearerAuth required
            // http bearer authentication required
            await setBearerAuthToObject(localVarHeaderParameter, configuration)


    
            localVarHeaderParameter['Content-Type'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(tag, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Delete content.halo.run/v1alpha1/Tag
         * @param {string} name Name of tag
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deletecontentHaloRunV1alpha1Tag: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('deletecontentHaloRunV1alpha1Tag', 'name', name)
            const localVarPath = `/apis/content.halo.run/v1alpha1/tags/{name}`
                .replace(`{${"name"}}`, encodeURIComponent(String(name)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'DELETE', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication BasicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication BearerAuth required
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
         * Get content.halo.run/v1alpha1/Tag
         * @param {string} name Name of tag
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getcontentHaloRunV1alpha1Tag: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('getcontentHaloRunV1alpha1Tag', 'name', name)
            const localVarPath = `/apis/content.halo.run/v1alpha1/tags/{name}`
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

            // authentication BasicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication BearerAuth required
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
         * List content.halo.run/v1alpha1/Tag
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listcontentHaloRunV1alpha1Tag: async (page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/content.halo.run/v1alpha1/tags`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'GET', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication BasicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication BearerAuth required
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
         * Update content.halo.run/v1alpha1/Tag
         * @param {string} name Name of tag
         * @param {Tag} [tag] Updated tag
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updatecontentHaloRunV1alpha1Tag: async (name: string, tag?: Tag, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('updatecontentHaloRunV1alpha1Tag', 'name', name)
            const localVarPath = `/apis/content.halo.run/v1alpha1/tags/{name}`
                .replace(`{${"name"}}`, encodeURIComponent(String(name)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'PUT', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication BasicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication BearerAuth required
            // http bearer authentication required
            await setBearerAuthToObject(localVarHeaderParameter, configuration)


    
            localVarHeaderParameter['Content-Type'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(tag, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * ContentHaloRunV1alpha1TagApi - functional programming interface
 * @export
 */
export const ContentHaloRunV1alpha1TagApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = ContentHaloRunV1alpha1TagApiAxiosParamCreator(configuration)
    return {
        /**
         * Create content.halo.run/v1alpha1/Tag
         * @param {Tag} [tag] Fresh tag
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async createcontentHaloRunV1alpha1Tag(tag?: Tag, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Tag>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.createcontentHaloRunV1alpha1Tag(tag, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['ContentHaloRunV1alpha1TagApi.createcontentHaloRunV1alpha1Tag']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Delete content.halo.run/v1alpha1/Tag
         * @param {string} name Name of tag
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async deletecontentHaloRunV1alpha1Tag(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.deletecontentHaloRunV1alpha1Tag(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['ContentHaloRunV1alpha1TagApi.deletecontentHaloRunV1alpha1Tag']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Get content.halo.run/v1alpha1/Tag
         * @param {string} name Name of tag
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getcontentHaloRunV1alpha1Tag(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Tag>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getcontentHaloRunV1alpha1Tag(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['ContentHaloRunV1alpha1TagApi.getcontentHaloRunV1alpha1Tag']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * List content.halo.run/v1alpha1/Tag
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async listcontentHaloRunV1alpha1Tag(page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<TagList>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.listcontentHaloRunV1alpha1Tag(page, size, labelSelector, fieldSelector, sort, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['ContentHaloRunV1alpha1TagApi.listcontentHaloRunV1alpha1Tag']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Update content.halo.run/v1alpha1/Tag
         * @param {string} name Name of tag
         * @param {Tag} [tag] Updated tag
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async updatecontentHaloRunV1alpha1Tag(name: string, tag?: Tag, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Tag>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.updatecontentHaloRunV1alpha1Tag(name, tag, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['ContentHaloRunV1alpha1TagApi.updatecontentHaloRunV1alpha1Tag']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * ContentHaloRunV1alpha1TagApi - factory interface
 * @export
 */
export const ContentHaloRunV1alpha1TagApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = ContentHaloRunV1alpha1TagApiFp(configuration)
    return {
        /**
         * Create content.halo.run/v1alpha1/Tag
         * @param {ContentHaloRunV1alpha1TagApiCreatecontentHaloRunV1alpha1TagRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createcontentHaloRunV1alpha1Tag(requestParameters: ContentHaloRunV1alpha1TagApiCreatecontentHaloRunV1alpha1TagRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<Tag> {
            return localVarFp.createcontentHaloRunV1alpha1Tag(requestParameters.tag, options).then((request) => request(axios, basePath));
        },
        /**
         * Delete content.halo.run/v1alpha1/Tag
         * @param {ContentHaloRunV1alpha1TagApiDeletecontentHaloRunV1alpha1TagRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deletecontentHaloRunV1alpha1Tag(requestParameters: ContentHaloRunV1alpha1TagApiDeletecontentHaloRunV1alpha1TagRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.deletecontentHaloRunV1alpha1Tag(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * Get content.halo.run/v1alpha1/Tag
         * @param {ContentHaloRunV1alpha1TagApiGetcontentHaloRunV1alpha1TagRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getcontentHaloRunV1alpha1Tag(requestParameters: ContentHaloRunV1alpha1TagApiGetcontentHaloRunV1alpha1TagRequest, options?: RawAxiosRequestConfig): AxiosPromise<Tag> {
            return localVarFp.getcontentHaloRunV1alpha1Tag(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * List content.halo.run/v1alpha1/Tag
         * @param {ContentHaloRunV1alpha1TagApiListcontentHaloRunV1alpha1TagRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listcontentHaloRunV1alpha1Tag(requestParameters: ContentHaloRunV1alpha1TagApiListcontentHaloRunV1alpha1TagRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<TagList> {
            return localVarFp.listcontentHaloRunV1alpha1Tag(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(axios, basePath));
        },
        /**
         * Update content.halo.run/v1alpha1/Tag
         * @param {ContentHaloRunV1alpha1TagApiUpdatecontentHaloRunV1alpha1TagRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updatecontentHaloRunV1alpha1Tag(requestParameters: ContentHaloRunV1alpha1TagApiUpdatecontentHaloRunV1alpha1TagRequest, options?: RawAxiosRequestConfig): AxiosPromise<Tag> {
            return localVarFp.updatecontentHaloRunV1alpha1Tag(requestParameters.name, requestParameters.tag, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for createcontentHaloRunV1alpha1Tag operation in ContentHaloRunV1alpha1TagApi.
 * @export
 * @interface ContentHaloRunV1alpha1TagApiCreatecontentHaloRunV1alpha1TagRequest
 */
export interface ContentHaloRunV1alpha1TagApiCreatecontentHaloRunV1alpha1TagRequest {
    /**
     * Fresh tag
     * @type {Tag}
     * @memberof ContentHaloRunV1alpha1TagApiCreatecontentHaloRunV1alpha1Tag
     */
    readonly tag?: Tag
}

/**
 * Request parameters for deletecontentHaloRunV1alpha1Tag operation in ContentHaloRunV1alpha1TagApi.
 * @export
 * @interface ContentHaloRunV1alpha1TagApiDeletecontentHaloRunV1alpha1TagRequest
 */
export interface ContentHaloRunV1alpha1TagApiDeletecontentHaloRunV1alpha1TagRequest {
    /**
     * Name of tag
     * @type {string}
     * @memberof ContentHaloRunV1alpha1TagApiDeletecontentHaloRunV1alpha1Tag
     */
    readonly name: string
}

/**
 * Request parameters for getcontentHaloRunV1alpha1Tag operation in ContentHaloRunV1alpha1TagApi.
 * @export
 * @interface ContentHaloRunV1alpha1TagApiGetcontentHaloRunV1alpha1TagRequest
 */
export interface ContentHaloRunV1alpha1TagApiGetcontentHaloRunV1alpha1TagRequest {
    /**
     * Name of tag
     * @type {string}
     * @memberof ContentHaloRunV1alpha1TagApiGetcontentHaloRunV1alpha1Tag
     */
    readonly name: string
}

/**
 * Request parameters for listcontentHaloRunV1alpha1Tag operation in ContentHaloRunV1alpha1TagApi.
 * @export
 * @interface ContentHaloRunV1alpha1TagApiListcontentHaloRunV1alpha1TagRequest
 */
export interface ContentHaloRunV1alpha1TagApiListcontentHaloRunV1alpha1TagRequest {
    /**
     * Page number. Default is 0.
     * @type {number}
     * @memberof ContentHaloRunV1alpha1TagApiListcontentHaloRunV1alpha1Tag
     */
    readonly page?: number

    /**
     * Size number. Default is 0.
     * @type {number}
     * @memberof ContentHaloRunV1alpha1TagApiListcontentHaloRunV1alpha1Tag
     */
    readonly size?: number

    /**
     * Label selector. e.g.: hidden!&#x3D;true
     * @type {Array<string>}
     * @memberof ContentHaloRunV1alpha1TagApiListcontentHaloRunV1alpha1Tag
     */
    readonly labelSelector?: Array<string>

    /**
     * Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
     * @type {Array<string>}
     * @memberof ContentHaloRunV1alpha1TagApiListcontentHaloRunV1alpha1Tag
     */
    readonly fieldSelector?: Array<string>

    /**
     * Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
     * @type {Array<string>}
     * @memberof ContentHaloRunV1alpha1TagApiListcontentHaloRunV1alpha1Tag
     */
    readonly sort?: Array<string>
}

/**
 * Request parameters for updatecontentHaloRunV1alpha1Tag operation in ContentHaloRunV1alpha1TagApi.
 * @export
 * @interface ContentHaloRunV1alpha1TagApiUpdatecontentHaloRunV1alpha1TagRequest
 */
export interface ContentHaloRunV1alpha1TagApiUpdatecontentHaloRunV1alpha1TagRequest {
    /**
     * Name of tag
     * @type {string}
     * @memberof ContentHaloRunV1alpha1TagApiUpdatecontentHaloRunV1alpha1Tag
     */
    readonly name: string

    /**
     * Updated tag
     * @type {Tag}
     * @memberof ContentHaloRunV1alpha1TagApiUpdatecontentHaloRunV1alpha1Tag
     */
    readonly tag?: Tag
}

/**
 * ContentHaloRunV1alpha1TagApi - object-oriented interface
 * @export
 * @class ContentHaloRunV1alpha1TagApi
 * @extends {BaseAPI}
 */
export class ContentHaloRunV1alpha1TagApi extends BaseAPI {
    /**
     * Create content.halo.run/v1alpha1/Tag
     * @param {ContentHaloRunV1alpha1TagApiCreatecontentHaloRunV1alpha1TagRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof ContentHaloRunV1alpha1TagApi
     */
    public createcontentHaloRunV1alpha1Tag(requestParameters: ContentHaloRunV1alpha1TagApiCreatecontentHaloRunV1alpha1TagRequest = {}, options?: RawAxiosRequestConfig) {
        return ContentHaloRunV1alpha1TagApiFp(this.configuration).createcontentHaloRunV1alpha1Tag(requestParameters.tag, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Delete content.halo.run/v1alpha1/Tag
     * @param {ContentHaloRunV1alpha1TagApiDeletecontentHaloRunV1alpha1TagRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof ContentHaloRunV1alpha1TagApi
     */
    public deletecontentHaloRunV1alpha1Tag(requestParameters: ContentHaloRunV1alpha1TagApiDeletecontentHaloRunV1alpha1TagRequest, options?: RawAxiosRequestConfig) {
        return ContentHaloRunV1alpha1TagApiFp(this.configuration).deletecontentHaloRunV1alpha1Tag(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Get content.halo.run/v1alpha1/Tag
     * @param {ContentHaloRunV1alpha1TagApiGetcontentHaloRunV1alpha1TagRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof ContentHaloRunV1alpha1TagApi
     */
    public getcontentHaloRunV1alpha1Tag(requestParameters: ContentHaloRunV1alpha1TagApiGetcontentHaloRunV1alpha1TagRequest, options?: RawAxiosRequestConfig) {
        return ContentHaloRunV1alpha1TagApiFp(this.configuration).getcontentHaloRunV1alpha1Tag(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * List content.halo.run/v1alpha1/Tag
     * @param {ContentHaloRunV1alpha1TagApiListcontentHaloRunV1alpha1TagRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof ContentHaloRunV1alpha1TagApi
     */
    public listcontentHaloRunV1alpha1Tag(requestParameters: ContentHaloRunV1alpha1TagApiListcontentHaloRunV1alpha1TagRequest = {}, options?: RawAxiosRequestConfig) {
        return ContentHaloRunV1alpha1TagApiFp(this.configuration).listcontentHaloRunV1alpha1Tag(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Update content.halo.run/v1alpha1/Tag
     * @param {ContentHaloRunV1alpha1TagApiUpdatecontentHaloRunV1alpha1TagRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof ContentHaloRunV1alpha1TagApi
     */
    public updatecontentHaloRunV1alpha1Tag(requestParameters: ContentHaloRunV1alpha1TagApiUpdatecontentHaloRunV1alpha1TagRequest, options?: RawAxiosRequestConfig) {
        return ContentHaloRunV1alpha1TagApiFp(this.configuration).updatecontentHaloRunV1alpha1Tag(requestParameters.name, requestParameters.tag, options).then((request) => request(this.axios, this.basePath));
    }
}

