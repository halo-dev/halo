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
import { NotifierDescriptor } from '../models';
// @ts-ignore
import { NotifierDescriptorList } from '../models';
/**
 * NotificationHaloRunV1alpha1NotifierDescriptorApi - axios parameter creator
 * @export
 */
export const NotificationHaloRunV1alpha1NotifierDescriptorApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Create notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {NotifierDescriptor} [notifierDescriptor] Fresh notifierDescriptor
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createNotificationHaloRunV1alpha1NotifierDescriptor: async (notifierDescriptor?: NotifierDescriptor, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/notification.halo.run/v1alpha1/notifierDescriptors`;
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
            localVarRequestOptions.data = serializeDataIfNeeded(notifierDescriptor, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Delete notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {string} name Name of notifierDescriptor
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deleteNotificationHaloRunV1alpha1NotifierDescriptor: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('deleteNotificationHaloRunV1alpha1NotifierDescriptor', 'name', name)
            const localVarPath = `/apis/notification.halo.run/v1alpha1/notifierDescriptors/{name}`
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
         * Get notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {string} name Name of notifierDescriptor
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getNotificationHaloRunV1alpha1NotifierDescriptor: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('getNotificationHaloRunV1alpha1NotifierDescriptor', 'name', name)
            const localVarPath = `/apis/notification.halo.run/v1alpha1/notifierDescriptors/{name}`
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
         * List notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listNotificationHaloRunV1alpha1NotifierDescriptor: async (page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/notification.halo.run/v1alpha1/notifierDescriptors`;
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
         * Update notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {string} name Name of notifierDescriptor
         * @param {NotifierDescriptor} [notifierDescriptor] Updated notifierDescriptor
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updateNotificationHaloRunV1alpha1NotifierDescriptor: async (name: string, notifierDescriptor?: NotifierDescriptor, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('updateNotificationHaloRunV1alpha1NotifierDescriptor', 'name', name)
            const localVarPath = `/apis/notification.halo.run/v1alpha1/notifierDescriptors/{name}`
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
            localVarRequestOptions.data = serializeDataIfNeeded(notifierDescriptor, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * NotificationHaloRunV1alpha1NotifierDescriptorApi - functional programming interface
 * @export
 */
export const NotificationHaloRunV1alpha1NotifierDescriptorApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = NotificationHaloRunV1alpha1NotifierDescriptorApiAxiosParamCreator(configuration)
    return {
        /**
         * Create notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {NotifierDescriptor} [notifierDescriptor] Fresh notifierDescriptor
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async createNotificationHaloRunV1alpha1NotifierDescriptor(notifierDescriptor?: NotifierDescriptor, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<NotifierDescriptor>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.createNotificationHaloRunV1alpha1NotifierDescriptor(notifierDescriptor, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['NotificationHaloRunV1alpha1NotifierDescriptorApi.createNotificationHaloRunV1alpha1NotifierDescriptor']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Delete notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {string} name Name of notifierDescriptor
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async deleteNotificationHaloRunV1alpha1NotifierDescriptor(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.deleteNotificationHaloRunV1alpha1NotifierDescriptor(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['NotificationHaloRunV1alpha1NotifierDescriptorApi.deleteNotificationHaloRunV1alpha1NotifierDescriptor']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Get notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {string} name Name of notifierDescriptor
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getNotificationHaloRunV1alpha1NotifierDescriptor(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<NotifierDescriptor>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getNotificationHaloRunV1alpha1NotifierDescriptor(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['NotificationHaloRunV1alpha1NotifierDescriptorApi.getNotificationHaloRunV1alpha1NotifierDescriptor']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * List notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async listNotificationHaloRunV1alpha1NotifierDescriptor(page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<NotifierDescriptorList>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.listNotificationHaloRunV1alpha1NotifierDescriptor(page, size, labelSelector, fieldSelector, sort, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['NotificationHaloRunV1alpha1NotifierDescriptorApi.listNotificationHaloRunV1alpha1NotifierDescriptor']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Update notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {string} name Name of notifierDescriptor
         * @param {NotifierDescriptor} [notifierDescriptor] Updated notifierDescriptor
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async updateNotificationHaloRunV1alpha1NotifierDescriptor(name: string, notifierDescriptor?: NotifierDescriptor, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<NotifierDescriptor>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.updateNotificationHaloRunV1alpha1NotifierDescriptor(name, notifierDescriptor, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['NotificationHaloRunV1alpha1NotifierDescriptorApi.updateNotificationHaloRunV1alpha1NotifierDescriptor']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * NotificationHaloRunV1alpha1NotifierDescriptorApi - factory interface
 * @export
 */
export const NotificationHaloRunV1alpha1NotifierDescriptorApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = NotificationHaloRunV1alpha1NotifierDescriptorApiFp(configuration)
    return {
        /**
         * Create notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {NotificationHaloRunV1alpha1NotifierDescriptorApiCreateNotificationHaloRunV1alpha1NotifierDescriptorRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters: NotificationHaloRunV1alpha1NotifierDescriptorApiCreateNotificationHaloRunV1alpha1NotifierDescriptorRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<NotifierDescriptor> {
            return localVarFp.createNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters.notifierDescriptor, options).then((request) => request(axios, basePath));
        },
        /**
         * Delete notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {NotificationHaloRunV1alpha1NotifierDescriptorApiDeleteNotificationHaloRunV1alpha1NotifierDescriptorRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deleteNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters: NotificationHaloRunV1alpha1NotifierDescriptorApiDeleteNotificationHaloRunV1alpha1NotifierDescriptorRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.deleteNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * Get notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {NotificationHaloRunV1alpha1NotifierDescriptorApiGetNotificationHaloRunV1alpha1NotifierDescriptorRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters: NotificationHaloRunV1alpha1NotifierDescriptorApiGetNotificationHaloRunV1alpha1NotifierDescriptorRequest, options?: RawAxiosRequestConfig): AxiosPromise<NotifierDescriptor> {
            return localVarFp.getNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * List notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {NotificationHaloRunV1alpha1NotifierDescriptorApiListNotificationHaloRunV1alpha1NotifierDescriptorRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters: NotificationHaloRunV1alpha1NotifierDescriptorApiListNotificationHaloRunV1alpha1NotifierDescriptorRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<NotifierDescriptorList> {
            return localVarFp.listNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(axios, basePath));
        },
        /**
         * Update notification.halo.run/v1alpha1/NotifierDescriptor
         * @param {NotificationHaloRunV1alpha1NotifierDescriptorApiUpdateNotificationHaloRunV1alpha1NotifierDescriptorRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updateNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters: NotificationHaloRunV1alpha1NotifierDescriptorApiUpdateNotificationHaloRunV1alpha1NotifierDescriptorRequest, options?: RawAxiosRequestConfig): AxiosPromise<NotifierDescriptor> {
            return localVarFp.updateNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters.name, requestParameters.notifierDescriptor, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for createNotificationHaloRunV1alpha1NotifierDescriptor operation in NotificationHaloRunV1alpha1NotifierDescriptorApi.
 * @export
 * @interface NotificationHaloRunV1alpha1NotifierDescriptorApiCreateNotificationHaloRunV1alpha1NotifierDescriptorRequest
 */
export interface NotificationHaloRunV1alpha1NotifierDescriptorApiCreateNotificationHaloRunV1alpha1NotifierDescriptorRequest {
    /**
     * Fresh notifierDescriptor
     * @type {NotifierDescriptor}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApiCreateNotificationHaloRunV1alpha1NotifierDescriptor
     */
    readonly notifierDescriptor?: NotifierDescriptor
}

/**
 * Request parameters for deleteNotificationHaloRunV1alpha1NotifierDescriptor operation in NotificationHaloRunV1alpha1NotifierDescriptorApi.
 * @export
 * @interface NotificationHaloRunV1alpha1NotifierDescriptorApiDeleteNotificationHaloRunV1alpha1NotifierDescriptorRequest
 */
export interface NotificationHaloRunV1alpha1NotifierDescriptorApiDeleteNotificationHaloRunV1alpha1NotifierDescriptorRequest {
    /**
     * Name of notifierDescriptor
     * @type {string}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApiDeleteNotificationHaloRunV1alpha1NotifierDescriptor
     */
    readonly name: string
}

/**
 * Request parameters for getNotificationHaloRunV1alpha1NotifierDescriptor operation in NotificationHaloRunV1alpha1NotifierDescriptorApi.
 * @export
 * @interface NotificationHaloRunV1alpha1NotifierDescriptorApiGetNotificationHaloRunV1alpha1NotifierDescriptorRequest
 */
export interface NotificationHaloRunV1alpha1NotifierDescriptorApiGetNotificationHaloRunV1alpha1NotifierDescriptorRequest {
    /**
     * Name of notifierDescriptor
     * @type {string}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApiGetNotificationHaloRunV1alpha1NotifierDescriptor
     */
    readonly name: string
}

/**
 * Request parameters for listNotificationHaloRunV1alpha1NotifierDescriptor operation in NotificationHaloRunV1alpha1NotifierDescriptorApi.
 * @export
 * @interface NotificationHaloRunV1alpha1NotifierDescriptorApiListNotificationHaloRunV1alpha1NotifierDescriptorRequest
 */
export interface NotificationHaloRunV1alpha1NotifierDescriptorApiListNotificationHaloRunV1alpha1NotifierDescriptorRequest {
    /**
     * Page number. Default is 0.
     * @type {number}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApiListNotificationHaloRunV1alpha1NotifierDescriptor
     */
    readonly page?: number

    /**
     * Size number. Default is 0.
     * @type {number}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApiListNotificationHaloRunV1alpha1NotifierDescriptor
     */
    readonly size?: number

    /**
     * Label selector. e.g.: hidden!&#x3D;true
     * @type {Array<string>}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApiListNotificationHaloRunV1alpha1NotifierDescriptor
     */
    readonly labelSelector?: Array<string>

    /**
     * Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
     * @type {Array<string>}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApiListNotificationHaloRunV1alpha1NotifierDescriptor
     */
    readonly fieldSelector?: Array<string>

    /**
     * Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
     * @type {Array<string>}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApiListNotificationHaloRunV1alpha1NotifierDescriptor
     */
    readonly sort?: Array<string>
}

/**
 * Request parameters for updateNotificationHaloRunV1alpha1NotifierDescriptor operation in NotificationHaloRunV1alpha1NotifierDescriptorApi.
 * @export
 * @interface NotificationHaloRunV1alpha1NotifierDescriptorApiUpdateNotificationHaloRunV1alpha1NotifierDescriptorRequest
 */
export interface NotificationHaloRunV1alpha1NotifierDescriptorApiUpdateNotificationHaloRunV1alpha1NotifierDescriptorRequest {
    /**
     * Name of notifierDescriptor
     * @type {string}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApiUpdateNotificationHaloRunV1alpha1NotifierDescriptor
     */
    readonly name: string

    /**
     * Updated notifierDescriptor
     * @type {NotifierDescriptor}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApiUpdateNotificationHaloRunV1alpha1NotifierDescriptor
     */
    readonly notifierDescriptor?: NotifierDescriptor
}

/**
 * NotificationHaloRunV1alpha1NotifierDescriptorApi - object-oriented interface
 * @export
 * @class NotificationHaloRunV1alpha1NotifierDescriptorApi
 * @extends {BaseAPI}
 */
export class NotificationHaloRunV1alpha1NotifierDescriptorApi extends BaseAPI {
    /**
     * Create notification.halo.run/v1alpha1/NotifierDescriptor
     * @param {NotificationHaloRunV1alpha1NotifierDescriptorApiCreateNotificationHaloRunV1alpha1NotifierDescriptorRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApi
     */
    public createNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters: NotificationHaloRunV1alpha1NotifierDescriptorApiCreateNotificationHaloRunV1alpha1NotifierDescriptorRequest = {}, options?: RawAxiosRequestConfig) {
        return NotificationHaloRunV1alpha1NotifierDescriptorApiFp(this.configuration).createNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters.notifierDescriptor, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Delete notification.halo.run/v1alpha1/NotifierDescriptor
     * @param {NotificationHaloRunV1alpha1NotifierDescriptorApiDeleteNotificationHaloRunV1alpha1NotifierDescriptorRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApi
     */
    public deleteNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters: NotificationHaloRunV1alpha1NotifierDescriptorApiDeleteNotificationHaloRunV1alpha1NotifierDescriptorRequest, options?: RawAxiosRequestConfig) {
        return NotificationHaloRunV1alpha1NotifierDescriptorApiFp(this.configuration).deleteNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Get notification.halo.run/v1alpha1/NotifierDescriptor
     * @param {NotificationHaloRunV1alpha1NotifierDescriptorApiGetNotificationHaloRunV1alpha1NotifierDescriptorRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApi
     */
    public getNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters: NotificationHaloRunV1alpha1NotifierDescriptorApiGetNotificationHaloRunV1alpha1NotifierDescriptorRequest, options?: RawAxiosRequestConfig) {
        return NotificationHaloRunV1alpha1NotifierDescriptorApiFp(this.configuration).getNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * List notification.halo.run/v1alpha1/NotifierDescriptor
     * @param {NotificationHaloRunV1alpha1NotifierDescriptorApiListNotificationHaloRunV1alpha1NotifierDescriptorRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApi
     */
    public listNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters: NotificationHaloRunV1alpha1NotifierDescriptorApiListNotificationHaloRunV1alpha1NotifierDescriptorRequest = {}, options?: RawAxiosRequestConfig) {
        return NotificationHaloRunV1alpha1NotifierDescriptorApiFp(this.configuration).listNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Update notification.halo.run/v1alpha1/NotifierDescriptor
     * @param {NotificationHaloRunV1alpha1NotifierDescriptorApiUpdateNotificationHaloRunV1alpha1NotifierDescriptorRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof NotificationHaloRunV1alpha1NotifierDescriptorApi
     */
    public updateNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters: NotificationHaloRunV1alpha1NotifierDescriptorApiUpdateNotificationHaloRunV1alpha1NotifierDescriptorRequest, options?: RawAxiosRequestConfig) {
        return NotificationHaloRunV1alpha1NotifierDescriptorApiFp(this.configuration).updateNotificationHaloRunV1alpha1NotifierDescriptor(requestParameters.name, requestParameters.notifierDescriptor, options).then((request) => request(this.axios, this.basePath));
    }
}

