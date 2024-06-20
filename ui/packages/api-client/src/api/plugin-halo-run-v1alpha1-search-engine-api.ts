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
import { JsonPatchInner } from '../models';
// @ts-ignore
import { SearchEngine } from '../models';
// @ts-ignore
import { SearchEngineList } from '../models';
/**
 * PluginHaloRunV1alpha1SearchEngineApi - axios parameter creator
 * @export
 */
export const PluginHaloRunV1alpha1SearchEngineApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Create plugin.halo.run/v1alpha1/SearchEngine
         * @param {SearchEngine} [searchEngine] Fresh searchengine
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createPluginHaloRunV1alpha1SearchEngine: async (searchEngine?: SearchEngine, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/plugin.halo.run/v1alpha1/searchengines`;
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
            localVarRequestOptions.data = serializeDataIfNeeded(searchEngine, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Delete plugin.halo.run/v1alpha1/SearchEngine
         * @param {string} name Name of searchengine
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deletePluginHaloRunV1alpha1SearchEngine: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('deletePluginHaloRunV1alpha1SearchEngine', 'name', name)
            const localVarPath = `/apis/plugin.halo.run/v1alpha1/searchengines/{name}`
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
         * Get plugin.halo.run/v1alpha1/SearchEngine
         * @param {string} name Name of searchengine
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getPluginHaloRunV1alpha1SearchEngine: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('getPluginHaloRunV1alpha1SearchEngine', 'name', name)
            const localVarPath = `/apis/plugin.halo.run/v1alpha1/searchengines/{name}`
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
         * List plugin.halo.run/v1alpha1/SearchEngine
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listPluginHaloRunV1alpha1SearchEngine: async (page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/plugin.halo.run/v1alpha1/searchengines`;
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
         * Patch plugin.halo.run/v1alpha1/SearchEngine
         * @param {string} name Name of searchengine
         * @param {Array<JsonPatchInner>} [jsonPatchInner] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        patchPluginHaloRunV1alpha1SearchEngine: async (name: string, jsonPatchInner?: Array<JsonPatchInner>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('patchPluginHaloRunV1alpha1SearchEngine', 'name', name)
            const localVarPath = `/apis/plugin.halo.run/v1alpha1/searchengines/{name}`
                .replace(`{${"name"}}`, encodeURIComponent(String(name)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'PATCH', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication BasicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication BearerAuth required
            // http bearer authentication required
            await setBearerAuthToObject(localVarHeaderParameter, configuration)


    
            localVarHeaderParameter['Content-Type'] = 'application/json-patch+json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(jsonPatchInner, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Update plugin.halo.run/v1alpha1/SearchEngine
         * @param {string} name Name of searchengine
         * @param {SearchEngine} [searchEngine] Updated searchengine
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updatePluginHaloRunV1alpha1SearchEngine: async (name: string, searchEngine?: SearchEngine, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('updatePluginHaloRunV1alpha1SearchEngine', 'name', name)
            const localVarPath = `/apis/plugin.halo.run/v1alpha1/searchengines/{name}`
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
            localVarRequestOptions.data = serializeDataIfNeeded(searchEngine, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * PluginHaloRunV1alpha1SearchEngineApi - functional programming interface
 * @export
 */
export const PluginHaloRunV1alpha1SearchEngineApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = PluginHaloRunV1alpha1SearchEngineApiAxiosParamCreator(configuration)
    return {
        /**
         * Create plugin.halo.run/v1alpha1/SearchEngine
         * @param {SearchEngine} [searchEngine] Fresh searchengine
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async createPluginHaloRunV1alpha1SearchEngine(searchEngine?: SearchEngine, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<SearchEngine>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.createPluginHaloRunV1alpha1SearchEngine(searchEngine, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PluginHaloRunV1alpha1SearchEngineApi.createPluginHaloRunV1alpha1SearchEngine']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Delete plugin.halo.run/v1alpha1/SearchEngine
         * @param {string} name Name of searchengine
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async deletePluginHaloRunV1alpha1SearchEngine(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.deletePluginHaloRunV1alpha1SearchEngine(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PluginHaloRunV1alpha1SearchEngineApi.deletePluginHaloRunV1alpha1SearchEngine']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Get plugin.halo.run/v1alpha1/SearchEngine
         * @param {string} name Name of searchengine
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getPluginHaloRunV1alpha1SearchEngine(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<SearchEngine>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getPluginHaloRunV1alpha1SearchEngine(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PluginHaloRunV1alpha1SearchEngineApi.getPluginHaloRunV1alpha1SearchEngine']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * List plugin.halo.run/v1alpha1/SearchEngine
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async listPluginHaloRunV1alpha1SearchEngine(page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<SearchEngineList>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.listPluginHaloRunV1alpha1SearchEngine(page, size, labelSelector, fieldSelector, sort, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PluginHaloRunV1alpha1SearchEngineApi.listPluginHaloRunV1alpha1SearchEngine']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Patch plugin.halo.run/v1alpha1/SearchEngine
         * @param {string} name Name of searchengine
         * @param {Array<JsonPatchInner>} [jsonPatchInner] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async patchPluginHaloRunV1alpha1SearchEngine(name: string, jsonPatchInner?: Array<JsonPatchInner>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<SearchEngine>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.patchPluginHaloRunV1alpha1SearchEngine(name, jsonPatchInner, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PluginHaloRunV1alpha1SearchEngineApi.patchPluginHaloRunV1alpha1SearchEngine']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Update plugin.halo.run/v1alpha1/SearchEngine
         * @param {string} name Name of searchengine
         * @param {SearchEngine} [searchEngine] Updated searchengine
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async updatePluginHaloRunV1alpha1SearchEngine(name: string, searchEngine?: SearchEngine, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<SearchEngine>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.updatePluginHaloRunV1alpha1SearchEngine(name, searchEngine, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PluginHaloRunV1alpha1SearchEngineApi.updatePluginHaloRunV1alpha1SearchEngine']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * PluginHaloRunV1alpha1SearchEngineApi - factory interface
 * @export
 */
export const PluginHaloRunV1alpha1SearchEngineApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = PluginHaloRunV1alpha1SearchEngineApiFp(configuration)
    return {
        /**
         * Create plugin.halo.run/v1alpha1/SearchEngine
         * @param {PluginHaloRunV1alpha1SearchEngineApiCreatePluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createPluginHaloRunV1alpha1SearchEngine(requestParameters: PluginHaloRunV1alpha1SearchEngineApiCreatePluginHaloRunV1alpha1SearchEngineRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<SearchEngine> {
            return localVarFp.createPluginHaloRunV1alpha1SearchEngine(requestParameters.searchEngine, options).then((request) => request(axios, basePath));
        },
        /**
         * Delete plugin.halo.run/v1alpha1/SearchEngine
         * @param {PluginHaloRunV1alpha1SearchEngineApiDeletePluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deletePluginHaloRunV1alpha1SearchEngine(requestParameters: PluginHaloRunV1alpha1SearchEngineApiDeletePluginHaloRunV1alpha1SearchEngineRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.deletePluginHaloRunV1alpha1SearchEngine(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * Get plugin.halo.run/v1alpha1/SearchEngine
         * @param {PluginHaloRunV1alpha1SearchEngineApiGetPluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getPluginHaloRunV1alpha1SearchEngine(requestParameters: PluginHaloRunV1alpha1SearchEngineApiGetPluginHaloRunV1alpha1SearchEngineRequest, options?: RawAxiosRequestConfig): AxiosPromise<SearchEngine> {
            return localVarFp.getPluginHaloRunV1alpha1SearchEngine(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * List plugin.halo.run/v1alpha1/SearchEngine
         * @param {PluginHaloRunV1alpha1SearchEngineApiListPluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listPluginHaloRunV1alpha1SearchEngine(requestParameters: PluginHaloRunV1alpha1SearchEngineApiListPluginHaloRunV1alpha1SearchEngineRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<SearchEngineList> {
            return localVarFp.listPluginHaloRunV1alpha1SearchEngine(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(axios, basePath));
        },
        /**
         * Patch plugin.halo.run/v1alpha1/SearchEngine
         * @param {PluginHaloRunV1alpha1SearchEngineApiPatchPluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        patchPluginHaloRunV1alpha1SearchEngine(requestParameters: PluginHaloRunV1alpha1SearchEngineApiPatchPluginHaloRunV1alpha1SearchEngineRequest, options?: RawAxiosRequestConfig): AxiosPromise<SearchEngine> {
            return localVarFp.patchPluginHaloRunV1alpha1SearchEngine(requestParameters.name, requestParameters.jsonPatchInner, options).then((request) => request(axios, basePath));
        },
        /**
         * Update plugin.halo.run/v1alpha1/SearchEngine
         * @param {PluginHaloRunV1alpha1SearchEngineApiUpdatePluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updatePluginHaloRunV1alpha1SearchEngine(requestParameters: PluginHaloRunV1alpha1SearchEngineApiUpdatePluginHaloRunV1alpha1SearchEngineRequest, options?: RawAxiosRequestConfig): AxiosPromise<SearchEngine> {
            return localVarFp.updatePluginHaloRunV1alpha1SearchEngine(requestParameters.name, requestParameters.searchEngine, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for createPluginHaloRunV1alpha1SearchEngine operation in PluginHaloRunV1alpha1SearchEngineApi.
 * @export
 * @interface PluginHaloRunV1alpha1SearchEngineApiCreatePluginHaloRunV1alpha1SearchEngineRequest
 */
export interface PluginHaloRunV1alpha1SearchEngineApiCreatePluginHaloRunV1alpha1SearchEngineRequest {
    /**
     * Fresh searchengine
     * @type {SearchEngine}
     * @memberof PluginHaloRunV1alpha1SearchEngineApiCreatePluginHaloRunV1alpha1SearchEngine
     */
    readonly searchEngine?: SearchEngine
}

/**
 * Request parameters for deletePluginHaloRunV1alpha1SearchEngine operation in PluginHaloRunV1alpha1SearchEngineApi.
 * @export
 * @interface PluginHaloRunV1alpha1SearchEngineApiDeletePluginHaloRunV1alpha1SearchEngineRequest
 */
export interface PluginHaloRunV1alpha1SearchEngineApiDeletePluginHaloRunV1alpha1SearchEngineRequest {
    /**
     * Name of searchengine
     * @type {string}
     * @memberof PluginHaloRunV1alpha1SearchEngineApiDeletePluginHaloRunV1alpha1SearchEngine
     */
    readonly name: string
}

/**
 * Request parameters for getPluginHaloRunV1alpha1SearchEngine operation in PluginHaloRunV1alpha1SearchEngineApi.
 * @export
 * @interface PluginHaloRunV1alpha1SearchEngineApiGetPluginHaloRunV1alpha1SearchEngineRequest
 */
export interface PluginHaloRunV1alpha1SearchEngineApiGetPluginHaloRunV1alpha1SearchEngineRequest {
    /**
     * Name of searchengine
     * @type {string}
     * @memberof PluginHaloRunV1alpha1SearchEngineApiGetPluginHaloRunV1alpha1SearchEngine
     */
    readonly name: string
}

/**
 * Request parameters for listPluginHaloRunV1alpha1SearchEngine operation in PluginHaloRunV1alpha1SearchEngineApi.
 * @export
 * @interface PluginHaloRunV1alpha1SearchEngineApiListPluginHaloRunV1alpha1SearchEngineRequest
 */
export interface PluginHaloRunV1alpha1SearchEngineApiListPluginHaloRunV1alpha1SearchEngineRequest {
    /**
     * Page number. Default is 0.
     * @type {number}
     * @memberof PluginHaloRunV1alpha1SearchEngineApiListPluginHaloRunV1alpha1SearchEngine
     */
    readonly page?: number

    /**
     * Size number. Default is 0.
     * @type {number}
     * @memberof PluginHaloRunV1alpha1SearchEngineApiListPluginHaloRunV1alpha1SearchEngine
     */
    readonly size?: number

    /**
     * Label selector. e.g.: hidden!&#x3D;true
     * @type {Array<string>}
     * @memberof PluginHaloRunV1alpha1SearchEngineApiListPluginHaloRunV1alpha1SearchEngine
     */
    readonly labelSelector?: Array<string>

    /**
     * Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
     * @type {Array<string>}
     * @memberof PluginHaloRunV1alpha1SearchEngineApiListPluginHaloRunV1alpha1SearchEngine
     */
    readonly fieldSelector?: Array<string>

    /**
     * Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
     * @type {Array<string>}
     * @memberof PluginHaloRunV1alpha1SearchEngineApiListPluginHaloRunV1alpha1SearchEngine
     */
    readonly sort?: Array<string>
}

/**
 * Request parameters for patchPluginHaloRunV1alpha1SearchEngine operation in PluginHaloRunV1alpha1SearchEngineApi.
 * @export
 * @interface PluginHaloRunV1alpha1SearchEngineApiPatchPluginHaloRunV1alpha1SearchEngineRequest
 */
export interface PluginHaloRunV1alpha1SearchEngineApiPatchPluginHaloRunV1alpha1SearchEngineRequest {
    /**
     * Name of searchengine
     * @type {string}
     * @memberof PluginHaloRunV1alpha1SearchEngineApiPatchPluginHaloRunV1alpha1SearchEngine
     */
    readonly name: string

    /**
     * 
     * @type {Array<JsonPatchInner>}
     * @memberof PluginHaloRunV1alpha1SearchEngineApiPatchPluginHaloRunV1alpha1SearchEngine
     */
    readonly jsonPatchInner?: Array<JsonPatchInner>
}

/**
 * Request parameters for updatePluginHaloRunV1alpha1SearchEngine operation in PluginHaloRunV1alpha1SearchEngineApi.
 * @export
 * @interface PluginHaloRunV1alpha1SearchEngineApiUpdatePluginHaloRunV1alpha1SearchEngineRequest
 */
export interface PluginHaloRunV1alpha1SearchEngineApiUpdatePluginHaloRunV1alpha1SearchEngineRequest {
    /**
     * Name of searchengine
     * @type {string}
     * @memberof PluginHaloRunV1alpha1SearchEngineApiUpdatePluginHaloRunV1alpha1SearchEngine
     */
    readonly name: string

    /**
     * Updated searchengine
     * @type {SearchEngine}
     * @memberof PluginHaloRunV1alpha1SearchEngineApiUpdatePluginHaloRunV1alpha1SearchEngine
     */
    readonly searchEngine?: SearchEngine
}

/**
 * PluginHaloRunV1alpha1SearchEngineApi - object-oriented interface
 * @export
 * @class PluginHaloRunV1alpha1SearchEngineApi
 * @extends {BaseAPI}
 */
export class PluginHaloRunV1alpha1SearchEngineApi extends BaseAPI {
    /**
     * Create plugin.halo.run/v1alpha1/SearchEngine
     * @param {PluginHaloRunV1alpha1SearchEngineApiCreatePluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PluginHaloRunV1alpha1SearchEngineApi
     */
    public createPluginHaloRunV1alpha1SearchEngine(requestParameters: PluginHaloRunV1alpha1SearchEngineApiCreatePluginHaloRunV1alpha1SearchEngineRequest = {}, options?: RawAxiosRequestConfig) {
        return PluginHaloRunV1alpha1SearchEngineApiFp(this.configuration).createPluginHaloRunV1alpha1SearchEngine(requestParameters.searchEngine, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Delete plugin.halo.run/v1alpha1/SearchEngine
     * @param {PluginHaloRunV1alpha1SearchEngineApiDeletePluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PluginHaloRunV1alpha1SearchEngineApi
     */
    public deletePluginHaloRunV1alpha1SearchEngine(requestParameters: PluginHaloRunV1alpha1SearchEngineApiDeletePluginHaloRunV1alpha1SearchEngineRequest, options?: RawAxiosRequestConfig) {
        return PluginHaloRunV1alpha1SearchEngineApiFp(this.configuration).deletePluginHaloRunV1alpha1SearchEngine(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Get plugin.halo.run/v1alpha1/SearchEngine
     * @param {PluginHaloRunV1alpha1SearchEngineApiGetPluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PluginHaloRunV1alpha1SearchEngineApi
     */
    public getPluginHaloRunV1alpha1SearchEngine(requestParameters: PluginHaloRunV1alpha1SearchEngineApiGetPluginHaloRunV1alpha1SearchEngineRequest, options?: RawAxiosRequestConfig) {
        return PluginHaloRunV1alpha1SearchEngineApiFp(this.configuration).getPluginHaloRunV1alpha1SearchEngine(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * List plugin.halo.run/v1alpha1/SearchEngine
     * @param {PluginHaloRunV1alpha1SearchEngineApiListPluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PluginHaloRunV1alpha1SearchEngineApi
     */
    public listPluginHaloRunV1alpha1SearchEngine(requestParameters: PluginHaloRunV1alpha1SearchEngineApiListPluginHaloRunV1alpha1SearchEngineRequest = {}, options?: RawAxiosRequestConfig) {
        return PluginHaloRunV1alpha1SearchEngineApiFp(this.configuration).listPluginHaloRunV1alpha1SearchEngine(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Patch plugin.halo.run/v1alpha1/SearchEngine
     * @param {PluginHaloRunV1alpha1SearchEngineApiPatchPluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PluginHaloRunV1alpha1SearchEngineApi
     */
    public patchPluginHaloRunV1alpha1SearchEngine(requestParameters: PluginHaloRunV1alpha1SearchEngineApiPatchPluginHaloRunV1alpha1SearchEngineRequest, options?: RawAxiosRequestConfig) {
        return PluginHaloRunV1alpha1SearchEngineApiFp(this.configuration).patchPluginHaloRunV1alpha1SearchEngine(requestParameters.name, requestParameters.jsonPatchInner, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Update plugin.halo.run/v1alpha1/SearchEngine
     * @param {PluginHaloRunV1alpha1SearchEngineApiUpdatePluginHaloRunV1alpha1SearchEngineRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PluginHaloRunV1alpha1SearchEngineApi
     */
    public updatePluginHaloRunV1alpha1SearchEngine(requestParameters: PluginHaloRunV1alpha1SearchEngineApiUpdatePluginHaloRunV1alpha1SearchEngineRequest, options?: RawAxiosRequestConfig) {
        return PluginHaloRunV1alpha1SearchEngineApiFp(this.configuration).updatePluginHaloRunV1alpha1SearchEngine(requestParameters.name, requestParameters.searchEngine, options).then((request) => request(this.axios, this.basePath));
    }
}

