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
import { UserConnection } from '../models';
// @ts-ignore
import { UserConnectionList } from '../models';
/**
 * AuthHaloRunV1alpha1UserConnectionApi - axios parameter creator
 * @export
 */
export const AuthHaloRunV1alpha1UserConnectionApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Create auth.halo.run/v1alpha1/UserConnection
         * @param {UserConnection} [userConnection] Fresh userconnection
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createAuthHaloRunV1alpha1UserConnection: async (userConnection?: UserConnection, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/auth.halo.run/v1alpha1/userconnections`;
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
            localVarRequestOptions.data = serializeDataIfNeeded(userConnection, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Delete auth.halo.run/v1alpha1/UserConnection
         * @param {string} name Name of userconnection
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deleteAuthHaloRunV1alpha1UserConnection: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('deleteAuthHaloRunV1alpha1UserConnection', 'name', name)
            const localVarPath = `/apis/auth.halo.run/v1alpha1/userconnections/{name}`
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
         * Get auth.halo.run/v1alpha1/UserConnection
         * @param {string} name Name of userconnection
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getAuthHaloRunV1alpha1UserConnection: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('getAuthHaloRunV1alpha1UserConnection', 'name', name)
            const localVarPath = `/apis/auth.halo.run/v1alpha1/userconnections/{name}`
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
         * List auth.halo.run/v1alpha1/UserConnection
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listAuthHaloRunV1alpha1UserConnection: async (page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/auth.halo.run/v1alpha1/userconnections`;
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
         * Patch auth.halo.run/v1alpha1/UserConnection
         * @param {string} name Name of userconnection
         * @param {Array<JsonPatchInner>} [jsonPatchInner] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        patchAuthHaloRunV1alpha1UserConnection: async (name: string, jsonPatchInner?: Array<JsonPatchInner>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('patchAuthHaloRunV1alpha1UserConnection', 'name', name)
            const localVarPath = `/apis/auth.halo.run/v1alpha1/userconnections/{name}`
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
         * Update auth.halo.run/v1alpha1/UserConnection
         * @param {string} name Name of userconnection
         * @param {UserConnection} [userConnection] Updated userconnection
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updateAuthHaloRunV1alpha1UserConnection: async (name: string, userConnection?: UserConnection, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('updateAuthHaloRunV1alpha1UserConnection', 'name', name)
            const localVarPath = `/apis/auth.halo.run/v1alpha1/userconnections/{name}`
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
            localVarRequestOptions.data = serializeDataIfNeeded(userConnection, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * AuthHaloRunV1alpha1UserConnectionApi - functional programming interface
 * @export
 */
export const AuthHaloRunV1alpha1UserConnectionApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = AuthHaloRunV1alpha1UserConnectionApiAxiosParamCreator(configuration)
    return {
        /**
         * Create auth.halo.run/v1alpha1/UserConnection
         * @param {UserConnection} [userConnection] Fresh userconnection
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async createAuthHaloRunV1alpha1UserConnection(userConnection?: UserConnection, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<UserConnection>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.createAuthHaloRunV1alpha1UserConnection(userConnection, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['AuthHaloRunV1alpha1UserConnectionApi.createAuthHaloRunV1alpha1UserConnection']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Delete auth.halo.run/v1alpha1/UserConnection
         * @param {string} name Name of userconnection
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async deleteAuthHaloRunV1alpha1UserConnection(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.deleteAuthHaloRunV1alpha1UserConnection(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['AuthHaloRunV1alpha1UserConnectionApi.deleteAuthHaloRunV1alpha1UserConnection']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Get auth.halo.run/v1alpha1/UserConnection
         * @param {string} name Name of userconnection
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getAuthHaloRunV1alpha1UserConnection(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<UserConnection>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getAuthHaloRunV1alpha1UserConnection(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['AuthHaloRunV1alpha1UserConnectionApi.getAuthHaloRunV1alpha1UserConnection']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * List auth.halo.run/v1alpha1/UserConnection
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async listAuthHaloRunV1alpha1UserConnection(page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<UserConnectionList>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.listAuthHaloRunV1alpha1UserConnection(page, size, labelSelector, fieldSelector, sort, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['AuthHaloRunV1alpha1UserConnectionApi.listAuthHaloRunV1alpha1UserConnection']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Patch auth.halo.run/v1alpha1/UserConnection
         * @param {string} name Name of userconnection
         * @param {Array<JsonPatchInner>} [jsonPatchInner] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async patchAuthHaloRunV1alpha1UserConnection(name: string, jsonPatchInner?: Array<JsonPatchInner>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<UserConnection>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.patchAuthHaloRunV1alpha1UserConnection(name, jsonPatchInner, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['AuthHaloRunV1alpha1UserConnectionApi.patchAuthHaloRunV1alpha1UserConnection']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Update auth.halo.run/v1alpha1/UserConnection
         * @param {string} name Name of userconnection
         * @param {UserConnection} [userConnection] Updated userconnection
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async updateAuthHaloRunV1alpha1UserConnection(name: string, userConnection?: UserConnection, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<UserConnection>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.updateAuthHaloRunV1alpha1UserConnection(name, userConnection, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['AuthHaloRunV1alpha1UserConnectionApi.updateAuthHaloRunV1alpha1UserConnection']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * AuthHaloRunV1alpha1UserConnectionApi - factory interface
 * @export
 */
export const AuthHaloRunV1alpha1UserConnectionApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = AuthHaloRunV1alpha1UserConnectionApiFp(configuration)
    return {
        /**
         * Create auth.halo.run/v1alpha1/UserConnection
         * @param {AuthHaloRunV1alpha1UserConnectionApiCreateAuthHaloRunV1alpha1UserConnectionRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createAuthHaloRunV1alpha1UserConnection(requestParameters: AuthHaloRunV1alpha1UserConnectionApiCreateAuthHaloRunV1alpha1UserConnectionRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<UserConnection> {
            return localVarFp.createAuthHaloRunV1alpha1UserConnection(requestParameters.userConnection, options).then((request) => request(axios, basePath));
        },
        /**
         * Delete auth.halo.run/v1alpha1/UserConnection
         * @param {AuthHaloRunV1alpha1UserConnectionApiDeleteAuthHaloRunV1alpha1UserConnectionRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deleteAuthHaloRunV1alpha1UserConnection(requestParameters: AuthHaloRunV1alpha1UserConnectionApiDeleteAuthHaloRunV1alpha1UserConnectionRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.deleteAuthHaloRunV1alpha1UserConnection(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * Get auth.halo.run/v1alpha1/UserConnection
         * @param {AuthHaloRunV1alpha1UserConnectionApiGetAuthHaloRunV1alpha1UserConnectionRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getAuthHaloRunV1alpha1UserConnection(requestParameters: AuthHaloRunV1alpha1UserConnectionApiGetAuthHaloRunV1alpha1UserConnectionRequest, options?: RawAxiosRequestConfig): AxiosPromise<UserConnection> {
            return localVarFp.getAuthHaloRunV1alpha1UserConnection(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * List auth.halo.run/v1alpha1/UserConnection
         * @param {AuthHaloRunV1alpha1UserConnectionApiListAuthHaloRunV1alpha1UserConnectionRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listAuthHaloRunV1alpha1UserConnection(requestParameters: AuthHaloRunV1alpha1UserConnectionApiListAuthHaloRunV1alpha1UserConnectionRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<UserConnectionList> {
            return localVarFp.listAuthHaloRunV1alpha1UserConnection(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(axios, basePath));
        },
        /**
         * Patch auth.halo.run/v1alpha1/UserConnection
         * @param {AuthHaloRunV1alpha1UserConnectionApiPatchAuthHaloRunV1alpha1UserConnectionRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        patchAuthHaloRunV1alpha1UserConnection(requestParameters: AuthHaloRunV1alpha1UserConnectionApiPatchAuthHaloRunV1alpha1UserConnectionRequest, options?: RawAxiosRequestConfig): AxiosPromise<UserConnection> {
            return localVarFp.patchAuthHaloRunV1alpha1UserConnection(requestParameters.name, requestParameters.jsonPatchInner, options).then((request) => request(axios, basePath));
        },
        /**
         * Update auth.halo.run/v1alpha1/UserConnection
         * @param {AuthHaloRunV1alpha1UserConnectionApiUpdateAuthHaloRunV1alpha1UserConnectionRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updateAuthHaloRunV1alpha1UserConnection(requestParameters: AuthHaloRunV1alpha1UserConnectionApiUpdateAuthHaloRunV1alpha1UserConnectionRequest, options?: RawAxiosRequestConfig): AxiosPromise<UserConnection> {
            return localVarFp.updateAuthHaloRunV1alpha1UserConnection(requestParameters.name, requestParameters.userConnection, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for createAuthHaloRunV1alpha1UserConnection operation in AuthHaloRunV1alpha1UserConnectionApi.
 * @export
 * @interface AuthHaloRunV1alpha1UserConnectionApiCreateAuthHaloRunV1alpha1UserConnectionRequest
 */
export interface AuthHaloRunV1alpha1UserConnectionApiCreateAuthHaloRunV1alpha1UserConnectionRequest {
    /**
     * Fresh userconnection
     * @type {UserConnection}
     * @memberof AuthHaloRunV1alpha1UserConnectionApiCreateAuthHaloRunV1alpha1UserConnection
     */
    readonly userConnection?: UserConnection
}

/**
 * Request parameters for deleteAuthHaloRunV1alpha1UserConnection operation in AuthHaloRunV1alpha1UserConnectionApi.
 * @export
 * @interface AuthHaloRunV1alpha1UserConnectionApiDeleteAuthHaloRunV1alpha1UserConnectionRequest
 */
export interface AuthHaloRunV1alpha1UserConnectionApiDeleteAuthHaloRunV1alpha1UserConnectionRequest {
    /**
     * Name of userconnection
     * @type {string}
     * @memberof AuthHaloRunV1alpha1UserConnectionApiDeleteAuthHaloRunV1alpha1UserConnection
     */
    readonly name: string
}

/**
 * Request parameters for getAuthHaloRunV1alpha1UserConnection operation in AuthHaloRunV1alpha1UserConnectionApi.
 * @export
 * @interface AuthHaloRunV1alpha1UserConnectionApiGetAuthHaloRunV1alpha1UserConnectionRequest
 */
export interface AuthHaloRunV1alpha1UserConnectionApiGetAuthHaloRunV1alpha1UserConnectionRequest {
    /**
     * Name of userconnection
     * @type {string}
     * @memberof AuthHaloRunV1alpha1UserConnectionApiGetAuthHaloRunV1alpha1UserConnection
     */
    readonly name: string
}

/**
 * Request parameters for listAuthHaloRunV1alpha1UserConnection operation in AuthHaloRunV1alpha1UserConnectionApi.
 * @export
 * @interface AuthHaloRunV1alpha1UserConnectionApiListAuthHaloRunV1alpha1UserConnectionRequest
 */
export interface AuthHaloRunV1alpha1UserConnectionApiListAuthHaloRunV1alpha1UserConnectionRequest {
    /**
     * Page number. Default is 0.
     * @type {number}
     * @memberof AuthHaloRunV1alpha1UserConnectionApiListAuthHaloRunV1alpha1UserConnection
     */
    readonly page?: number

    /**
     * Size number. Default is 0.
     * @type {number}
     * @memberof AuthHaloRunV1alpha1UserConnectionApiListAuthHaloRunV1alpha1UserConnection
     */
    readonly size?: number

    /**
     * Label selector. e.g.: hidden!&#x3D;true
     * @type {Array<string>}
     * @memberof AuthHaloRunV1alpha1UserConnectionApiListAuthHaloRunV1alpha1UserConnection
     */
    readonly labelSelector?: Array<string>

    /**
     * Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
     * @type {Array<string>}
     * @memberof AuthHaloRunV1alpha1UserConnectionApiListAuthHaloRunV1alpha1UserConnection
     */
    readonly fieldSelector?: Array<string>

    /**
     * Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
     * @type {Array<string>}
     * @memberof AuthHaloRunV1alpha1UserConnectionApiListAuthHaloRunV1alpha1UserConnection
     */
    readonly sort?: Array<string>
}

/**
 * Request parameters for patchAuthHaloRunV1alpha1UserConnection operation in AuthHaloRunV1alpha1UserConnectionApi.
 * @export
 * @interface AuthHaloRunV1alpha1UserConnectionApiPatchAuthHaloRunV1alpha1UserConnectionRequest
 */
export interface AuthHaloRunV1alpha1UserConnectionApiPatchAuthHaloRunV1alpha1UserConnectionRequest {
    /**
     * Name of userconnection
     * @type {string}
     * @memberof AuthHaloRunV1alpha1UserConnectionApiPatchAuthHaloRunV1alpha1UserConnection
     */
    readonly name: string

    /**
     * 
     * @type {Array<JsonPatchInner>}
     * @memberof AuthHaloRunV1alpha1UserConnectionApiPatchAuthHaloRunV1alpha1UserConnection
     */
    readonly jsonPatchInner?: Array<JsonPatchInner>
}

/**
 * Request parameters for updateAuthHaloRunV1alpha1UserConnection operation in AuthHaloRunV1alpha1UserConnectionApi.
 * @export
 * @interface AuthHaloRunV1alpha1UserConnectionApiUpdateAuthHaloRunV1alpha1UserConnectionRequest
 */
export interface AuthHaloRunV1alpha1UserConnectionApiUpdateAuthHaloRunV1alpha1UserConnectionRequest {
    /**
     * Name of userconnection
     * @type {string}
     * @memberof AuthHaloRunV1alpha1UserConnectionApiUpdateAuthHaloRunV1alpha1UserConnection
     */
    readonly name: string

    /**
     * Updated userconnection
     * @type {UserConnection}
     * @memberof AuthHaloRunV1alpha1UserConnectionApiUpdateAuthHaloRunV1alpha1UserConnection
     */
    readonly userConnection?: UserConnection
}

/**
 * AuthHaloRunV1alpha1UserConnectionApi - object-oriented interface
 * @export
 * @class AuthHaloRunV1alpha1UserConnectionApi
 * @extends {BaseAPI}
 */
export class AuthHaloRunV1alpha1UserConnectionApi extends BaseAPI {
    /**
     * Create auth.halo.run/v1alpha1/UserConnection
     * @param {AuthHaloRunV1alpha1UserConnectionApiCreateAuthHaloRunV1alpha1UserConnectionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof AuthHaloRunV1alpha1UserConnectionApi
     */
    public createAuthHaloRunV1alpha1UserConnection(requestParameters: AuthHaloRunV1alpha1UserConnectionApiCreateAuthHaloRunV1alpha1UserConnectionRequest = {}, options?: RawAxiosRequestConfig) {
        return AuthHaloRunV1alpha1UserConnectionApiFp(this.configuration).createAuthHaloRunV1alpha1UserConnection(requestParameters.userConnection, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Delete auth.halo.run/v1alpha1/UserConnection
     * @param {AuthHaloRunV1alpha1UserConnectionApiDeleteAuthHaloRunV1alpha1UserConnectionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof AuthHaloRunV1alpha1UserConnectionApi
     */
    public deleteAuthHaloRunV1alpha1UserConnection(requestParameters: AuthHaloRunV1alpha1UserConnectionApiDeleteAuthHaloRunV1alpha1UserConnectionRequest, options?: RawAxiosRequestConfig) {
        return AuthHaloRunV1alpha1UserConnectionApiFp(this.configuration).deleteAuthHaloRunV1alpha1UserConnection(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Get auth.halo.run/v1alpha1/UserConnection
     * @param {AuthHaloRunV1alpha1UserConnectionApiGetAuthHaloRunV1alpha1UserConnectionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof AuthHaloRunV1alpha1UserConnectionApi
     */
    public getAuthHaloRunV1alpha1UserConnection(requestParameters: AuthHaloRunV1alpha1UserConnectionApiGetAuthHaloRunV1alpha1UserConnectionRequest, options?: RawAxiosRequestConfig) {
        return AuthHaloRunV1alpha1UserConnectionApiFp(this.configuration).getAuthHaloRunV1alpha1UserConnection(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * List auth.halo.run/v1alpha1/UserConnection
     * @param {AuthHaloRunV1alpha1UserConnectionApiListAuthHaloRunV1alpha1UserConnectionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof AuthHaloRunV1alpha1UserConnectionApi
     */
    public listAuthHaloRunV1alpha1UserConnection(requestParameters: AuthHaloRunV1alpha1UserConnectionApiListAuthHaloRunV1alpha1UserConnectionRequest = {}, options?: RawAxiosRequestConfig) {
        return AuthHaloRunV1alpha1UserConnectionApiFp(this.configuration).listAuthHaloRunV1alpha1UserConnection(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Patch auth.halo.run/v1alpha1/UserConnection
     * @param {AuthHaloRunV1alpha1UserConnectionApiPatchAuthHaloRunV1alpha1UserConnectionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof AuthHaloRunV1alpha1UserConnectionApi
     */
    public patchAuthHaloRunV1alpha1UserConnection(requestParameters: AuthHaloRunV1alpha1UserConnectionApiPatchAuthHaloRunV1alpha1UserConnectionRequest, options?: RawAxiosRequestConfig) {
        return AuthHaloRunV1alpha1UserConnectionApiFp(this.configuration).patchAuthHaloRunV1alpha1UserConnection(requestParameters.name, requestParameters.jsonPatchInner, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Update auth.halo.run/v1alpha1/UserConnection
     * @param {AuthHaloRunV1alpha1UserConnectionApiUpdateAuthHaloRunV1alpha1UserConnectionRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof AuthHaloRunV1alpha1UserConnectionApi
     */
    public updateAuthHaloRunV1alpha1UserConnection(requestParameters: AuthHaloRunV1alpha1UserConnectionApiUpdateAuthHaloRunV1alpha1UserConnectionRequest, options?: RawAxiosRequestConfig) {
        return AuthHaloRunV1alpha1UserConnectionApiFp(this.configuration).updateAuthHaloRunV1alpha1UserConnection(requestParameters.name, requestParameters.userConnection, options).then((request) => request(this.axios, this.basePath));
    }
}

