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
import { ExtensionDefinition } from '../models';
// @ts-ignore
import { ExtensionDefinitionList } from '../models';
/**
 * PluginHaloRunV1alpha1ExtensionDefinitionApi - axios parameter creator
 * @export
 */
export const PluginHaloRunV1alpha1ExtensionDefinitionApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Create plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {ExtensionDefinition} [extensionDefinition] Fresh extensiondefinition
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createPluginHaloRunV1alpha1ExtensionDefinition: async (extensionDefinition?: ExtensionDefinition, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/plugin.halo.run/v1alpha1/extensiondefinitions`;
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
            localVarRequestOptions.data = serializeDataIfNeeded(extensionDefinition, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Delete plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {string} name Name of extensiondefinition
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deletePluginHaloRunV1alpha1ExtensionDefinition: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('deletePluginHaloRunV1alpha1ExtensionDefinition', 'name', name)
            const localVarPath = `/apis/plugin.halo.run/v1alpha1/extensiondefinitions/{name}`
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
         * Get plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {string} name Name of extensiondefinition
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getPluginHaloRunV1alpha1ExtensionDefinition: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('getPluginHaloRunV1alpha1ExtensionDefinition', 'name', name)
            const localVarPath = `/apis/plugin.halo.run/v1alpha1/extensiondefinitions/{name}`
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
         * List plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listPluginHaloRunV1alpha1ExtensionDefinition: async (page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/plugin.halo.run/v1alpha1/extensiondefinitions`;
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
         * Update plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {string} name Name of extensiondefinition
         * @param {ExtensionDefinition} [extensionDefinition] Updated extensiondefinition
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updatePluginHaloRunV1alpha1ExtensionDefinition: async (name: string, extensionDefinition?: ExtensionDefinition, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('updatePluginHaloRunV1alpha1ExtensionDefinition', 'name', name)
            const localVarPath = `/apis/plugin.halo.run/v1alpha1/extensiondefinitions/{name}`
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
            localVarRequestOptions.data = serializeDataIfNeeded(extensionDefinition, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * PluginHaloRunV1alpha1ExtensionDefinitionApi - functional programming interface
 * @export
 */
export const PluginHaloRunV1alpha1ExtensionDefinitionApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = PluginHaloRunV1alpha1ExtensionDefinitionApiAxiosParamCreator(configuration)
    return {
        /**
         * Create plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {ExtensionDefinition} [extensionDefinition] Fresh extensiondefinition
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async createPluginHaloRunV1alpha1ExtensionDefinition(extensionDefinition?: ExtensionDefinition, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ExtensionDefinition>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.createPluginHaloRunV1alpha1ExtensionDefinition(extensionDefinition, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PluginHaloRunV1alpha1ExtensionDefinitionApi.createPluginHaloRunV1alpha1ExtensionDefinition']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Delete plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {string} name Name of extensiondefinition
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async deletePluginHaloRunV1alpha1ExtensionDefinition(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.deletePluginHaloRunV1alpha1ExtensionDefinition(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PluginHaloRunV1alpha1ExtensionDefinitionApi.deletePluginHaloRunV1alpha1ExtensionDefinition']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Get plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {string} name Name of extensiondefinition
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getPluginHaloRunV1alpha1ExtensionDefinition(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ExtensionDefinition>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getPluginHaloRunV1alpha1ExtensionDefinition(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PluginHaloRunV1alpha1ExtensionDefinitionApi.getPluginHaloRunV1alpha1ExtensionDefinition']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * List plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async listPluginHaloRunV1alpha1ExtensionDefinition(page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ExtensionDefinitionList>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.listPluginHaloRunV1alpha1ExtensionDefinition(page, size, labelSelector, fieldSelector, sort, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PluginHaloRunV1alpha1ExtensionDefinitionApi.listPluginHaloRunV1alpha1ExtensionDefinition']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Update plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {string} name Name of extensiondefinition
         * @param {ExtensionDefinition} [extensionDefinition] Updated extensiondefinition
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async updatePluginHaloRunV1alpha1ExtensionDefinition(name: string, extensionDefinition?: ExtensionDefinition, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ExtensionDefinition>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.updatePluginHaloRunV1alpha1ExtensionDefinition(name, extensionDefinition, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PluginHaloRunV1alpha1ExtensionDefinitionApi.updatePluginHaloRunV1alpha1ExtensionDefinition']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * PluginHaloRunV1alpha1ExtensionDefinitionApi - factory interface
 * @export
 */
export const PluginHaloRunV1alpha1ExtensionDefinitionApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = PluginHaloRunV1alpha1ExtensionDefinitionApiFp(configuration)
    return {
        /**
         * Create plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiCreatePluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createPluginHaloRunV1alpha1ExtensionDefinition(requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiCreatePluginHaloRunV1alpha1ExtensionDefinitionRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<ExtensionDefinition> {
            return localVarFp.createPluginHaloRunV1alpha1ExtensionDefinition(requestParameters.extensionDefinition, options).then((request) => request(axios, basePath));
        },
        /**
         * Delete plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiDeletePluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deletePluginHaloRunV1alpha1ExtensionDefinition(requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiDeletePluginHaloRunV1alpha1ExtensionDefinitionRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.deletePluginHaloRunV1alpha1ExtensionDefinition(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * Get plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiGetPluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getPluginHaloRunV1alpha1ExtensionDefinition(requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiGetPluginHaloRunV1alpha1ExtensionDefinitionRequest, options?: RawAxiosRequestConfig): AxiosPromise<ExtensionDefinition> {
            return localVarFp.getPluginHaloRunV1alpha1ExtensionDefinition(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * List plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiListPluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listPluginHaloRunV1alpha1ExtensionDefinition(requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiListPluginHaloRunV1alpha1ExtensionDefinitionRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<ExtensionDefinitionList> {
            return localVarFp.listPluginHaloRunV1alpha1ExtensionDefinition(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(axios, basePath));
        },
        /**
         * Update plugin.halo.run/v1alpha1/ExtensionDefinition
         * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatePluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updatePluginHaloRunV1alpha1ExtensionDefinition(requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatePluginHaloRunV1alpha1ExtensionDefinitionRequest, options?: RawAxiosRequestConfig): AxiosPromise<ExtensionDefinition> {
            return localVarFp.updatePluginHaloRunV1alpha1ExtensionDefinition(requestParameters.name, requestParameters.extensionDefinition, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for createPluginHaloRunV1alpha1ExtensionDefinition operation in PluginHaloRunV1alpha1ExtensionDefinitionApi.
 * @export
 * @interface PluginHaloRunV1alpha1ExtensionDefinitionApiCreatePluginHaloRunV1alpha1ExtensionDefinitionRequest
 */
export interface PluginHaloRunV1alpha1ExtensionDefinitionApiCreatePluginHaloRunV1alpha1ExtensionDefinitionRequest {
    /**
     * Fresh extensiondefinition
     * @type {ExtensionDefinition}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiCreatePluginHaloRunV1alpha1ExtensionDefinition
     */
    readonly extensionDefinition?: ExtensionDefinition
}

/**
 * Request parameters for deletePluginHaloRunV1alpha1ExtensionDefinition operation in PluginHaloRunV1alpha1ExtensionDefinitionApi.
 * @export
 * @interface PluginHaloRunV1alpha1ExtensionDefinitionApiDeletePluginHaloRunV1alpha1ExtensionDefinitionRequest
 */
export interface PluginHaloRunV1alpha1ExtensionDefinitionApiDeletePluginHaloRunV1alpha1ExtensionDefinitionRequest {
    /**
     * Name of extensiondefinition
     * @type {string}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiDeletePluginHaloRunV1alpha1ExtensionDefinition
     */
    readonly name: string
}

/**
 * Request parameters for getPluginHaloRunV1alpha1ExtensionDefinition operation in PluginHaloRunV1alpha1ExtensionDefinitionApi.
 * @export
 * @interface PluginHaloRunV1alpha1ExtensionDefinitionApiGetPluginHaloRunV1alpha1ExtensionDefinitionRequest
 */
export interface PluginHaloRunV1alpha1ExtensionDefinitionApiGetPluginHaloRunV1alpha1ExtensionDefinitionRequest {
    /**
     * Name of extensiondefinition
     * @type {string}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiGetPluginHaloRunV1alpha1ExtensionDefinition
     */
    readonly name: string
}

/**
 * Request parameters for listPluginHaloRunV1alpha1ExtensionDefinition operation in PluginHaloRunV1alpha1ExtensionDefinitionApi.
 * @export
 * @interface PluginHaloRunV1alpha1ExtensionDefinitionApiListPluginHaloRunV1alpha1ExtensionDefinitionRequest
 */
export interface PluginHaloRunV1alpha1ExtensionDefinitionApiListPluginHaloRunV1alpha1ExtensionDefinitionRequest {
    /**
     * Page number. Default is 0.
     * @type {number}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiListPluginHaloRunV1alpha1ExtensionDefinition
     */
    readonly page?: number

    /**
     * Size number. Default is 0.
     * @type {number}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiListPluginHaloRunV1alpha1ExtensionDefinition
     */
    readonly size?: number

    /**
     * Label selector. e.g.: hidden!&#x3D;true
     * @type {Array<string>}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiListPluginHaloRunV1alpha1ExtensionDefinition
     */
    readonly labelSelector?: Array<string>

    /**
     * Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
     * @type {Array<string>}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiListPluginHaloRunV1alpha1ExtensionDefinition
     */
    readonly fieldSelector?: Array<string>

    /**
     * Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
     * @type {Array<string>}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiListPluginHaloRunV1alpha1ExtensionDefinition
     */
    readonly sort?: Array<string>
}

/**
 * Request parameters for updatePluginHaloRunV1alpha1ExtensionDefinition operation in PluginHaloRunV1alpha1ExtensionDefinitionApi.
 * @export
 * @interface PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatePluginHaloRunV1alpha1ExtensionDefinitionRequest
 */
export interface PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatePluginHaloRunV1alpha1ExtensionDefinitionRequest {
    /**
     * Name of extensiondefinition
     * @type {string}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatePluginHaloRunV1alpha1ExtensionDefinition
     */
    readonly name: string

    /**
     * Updated extensiondefinition
     * @type {ExtensionDefinition}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatePluginHaloRunV1alpha1ExtensionDefinition
     */
    readonly extensionDefinition?: ExtensionDefinition
}

/**
 * PluginHaloRunV1alpha1ExtensionDefinitionApi - object-oriented interface
 * @export
 * @class PluginHaloRunV1alpha1ExtensionDefinitionApi
 * @extends {BaseAPI}
 */
export class PluginHaloRunV1alpha1ExtensionDefinitionApi extends BaseAPI {
    /**
     * Create plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiCreatePluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApi
     */
    public createPluginHaloRunV1alpha1ExtensionDefinition(requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiCreatePluginHaloRunV1alpha1ExtensionDefinitionRequest = {}, options?: RawAxiosRequestConfig) {
        return PluginHaloRunV1alpha1ExtensionDefinitionApiFp(this.configuration).createPluginHaloRunV1alpha1ExtensionDefinition(requestParameters.extensionDefinition, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Delete plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiDeletePluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApi
     */
    public deletePluginHaloRunV1alpha1ExtensionDefinition(requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiDeletePluginHaloRunV1alpha1ExtensionDefinitionRequest, options?: RawAxiosRequestConfig) {
        return PluginHaloRunV1alpha1ExtensionDefinitionApiFp(this.configuration).deletePluginHaloRunV1alpha1ExtensionDefinition(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Get plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiGetPluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApi
     */
    public getPluginHaloRunV1alpha1ExtensionDefinition(requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiGetPluginHaloRunV1alpha1ExtensionDefinitionRequest, options?: RawAxiosRequestConfig) {
        return PluginHaloRunV1alpha1ExtensionDefinitionApiFp(this.configuration).getPluginHaloRunV1alpha1ExtensionDefinition(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * List plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiListPluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApi
     */
    public listPluginHaloRunV1alpha1ExtensionDefinition(requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiListPluginHaloRunV1alpha1ExtensionDefinitionRequest = {}, options?: RawAxiosRequestConfig) {
        return PluginHaloRunV1alpha1ExtensionDefinitionApiFp(this.configuration).listPluginHaloRunV1alpha1ExtensionDefinition(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Update plugin.halo.run/v1alpha1/ExtensionDefinition
     * @param {PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatePluginHaloRunV1alpha1ExtensionDefinitionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PluginHaloRunV1alpha1ExtensionDefinitionApi
     */
    public updatePluginHaloRunV1alpha1ExtensionDefinition(requestParameters: PluginHaloRunV1alpha1ExtensionDefinitionApiUpdatePluginHaloRunV1alpha1ExtensionDefinitionRequest, options?: RawAxiosRequestConfig) {
        return PluginHaloRunV1alpha1ExtensionDefinitionApiFp(this.configuration).updatePluginHaloRunV1alpha1ExtensionDefinition(requestParameters.name, requestParameters.extensionDefinition, options).then((request) => request(this.axios, this.basePath));
    }
}

