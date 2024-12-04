/* tslint:disable */
/* eslint-disable */
/**
 * Halo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.20.11-SNAPSHOT
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
import { Attachment } from '../models';
// @ts-ignore
import { AttachmentList } from '../models';
// @ts-ignore
import { JsonPatchInner } from '../models';
/**
 * AttachmentV1alpha1Api - axios parameter creator
 * @export
 */
export const AttachmentV1alpha1ApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Create Attachment
         * @param {Attachment} [attachment] Fresh attachment
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createAttachment: async (attachment?: Attachment, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/storage.halo.run/v1alpha1/attachments`;
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
            localVarRequestOptions.data = serializeDataIfNeeded(attachment, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Delete Attachment
         * @param {string} name Name of attachment
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deleteAttachment: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('deleteAttachment', 'name', name)
            const localVarPath = `/apis/storage.halo.run/v1alpha1/attachments/{name}`
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
         * Get Attachment
         * @param {string} name Name of attachment
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getAttachment: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('getAttachment', 'name', name)
            const localVarPath = `/apis/storage.halo.run/v1alpha1/attachments/{name}`
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
         * List Attachment
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listAttachment: async (page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/storage.halo.run/v1alpha1/attachments`;
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
         * Patch Attachment
         * @param {string} name Name of attachment
         * @param {Array<JsonPatchInner>} [jsonPatchInner] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        patchAttachment: async (name: string, jsonPatchInner?: Array<JsonPatchInner>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('patchAttachment', 'name', name)
            const localVarPath = `/apis/storage.halo.run/v1alpha1/attachments/{name}`
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

            // authentication basicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication bearerAuth required
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
         * Update Attachment
         * @param {string} name Name of attachment
         * @param {Attachment} [attachment] Updated attachment
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updateAttachment: async (name: string, attachment?: Attachment, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('updateAttachment', 'name', name)
            const localVarPath = `/apis/storage.halo.run/v1alpha1/attachments/{name}`
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
            localVarRequestOptions.data = serializeDataIfNeeded(attachment, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * AttachmentV1alpha1Api - functional programming interface
 * @export
 */
export const AttachmentV1alpha1ApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = AttachmentV1alpha1ApiAxiosParamCreator(configuration)
    return {
        /**
         * Create Attachment
         * @param {Attachment} [attachment] Fresh attachment
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async createAttachment(attachment?: Attachment, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Attachment>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.createAttachment(attachment, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['AttachmentV1alpha1Api.createAttachment']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Delete Attachment
         * @param {string} name Name of attachment
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async deleteAttachment(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.deleteAttachment(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['AttachmentV1alpha1Api.deleteAttachment']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Get Attachment
         * @param {string} name Name of attachment
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getAttachment(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Attachment>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getAttachment(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['AttachmentV1alpha1Api.getAttachment']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * List Attachment
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async listAttachment(page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<AttachmentList>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.listAttachment(page, size, labelSelector, fieldSelector, sort, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['AttachmentV1alpha1Api.listAttachment']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Patch Attachment
         * @param {string} name Name of attachment
         * @param {Array<JsonPatchInner>} [jsonPatchInner] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async patchAttachment(name: string, jsonPatchInner?: Array<JsonPatchInner>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Attachment>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.patchAttachment(name, jsonPatchInner, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['AttachmentV1alpha1Api.patchAttachment']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Update Attachment
         * @param {string} name Name of attachment
         * @param {Attachment} [attachment] Updated attachment
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async updateAttachment(name: string, attachment?: Attachment, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Attachment>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.updateAttachment(name, attachment, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['AttachmentV1alpha1Api.updateAttachment']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * AttachmentV1alpha1Api - factory interface
 * @export
 */
export const AttachmentV1alpha1ApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = AttachmentV1alpha1ApiFp(configuration)
    return {
        /**
         * Create Attachment
         * @param {AttachmentV1alpha1ApiCreateAttachmentRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createAttachment(requestParameters: AttachmentV1alpha1ApiCreateAttachmentRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<Attachment> {
            return localVarFp.createAttachment(requestParameters.attachment, options).then((request) => request(axios, basePath));
        },
        /**
         * Delete Attachment
         * @param {AttachmentV1alpha1ApiDeleteAttachmentRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deleteAttachment(requestParameters: AttachmentV1alpha1ApiDeleteAttachmentRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.deleteAttachment(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * Get Attachment
         * @param {AttachmentV1alpha1ApiGetAttachmentRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getAttachment(requestParameters: AttachmentV1alpha1ApiGetAttachmentRequest, options?: RawAxiosRequestConfig): AxiosPromise<Attachment> {
            return localVarFp.getAttachment(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * List Attachment
         * @param {AttachmentV1alpha1ApiListAttachmentRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listAttachment(requestParameters: AttachmentV1alpha1ApiListAttachmentRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<AttachmentList> {
            return localVarFp.listAttachment(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(axios, basePath));
        },
        /**
         * Patch Attachment
         * @param {AttachmentV1alpha1ApiPatchAttachmentRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        patchAttachment(requestParameters: AttachmentV1alpha1ApiPatchAttachmentRequest, options?: RawAxiosRequestConfig): AxiosPromise<Attachment> {
            return localVarFp.patchAttachment(requestParameters.name, requestParameters.jsonPatchInner, options).then((request) => request(axios, basePath));
        },
        /**
         * Update Attachment
         * @param {AttachmentV1alpha1ApiUpdateAttachmentRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updateAttachment(requestParameters: AttachmentV1alpha1ApiUpdateAttachmentRequest, options?: RawAxiosRequestConfig): AxiosPromise<Attachment> {
            return localVarFp.updateAttachment(requestParameters.name, requestParameters.attachment, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for createAttachment operation in AttachmentV1alpha1Api.
 * @export
 * @interface AttachmentV1alpha1ApiCreateAttachmentRequest
 */
export interface AttachmentV1alpha1ApiCreateAttachmentRequest {
    /**
     * Fresh attachment
     * @type {Attachment}
     * @memberof AttachmentV1alpha1ApiCreateAttachment
     */
    readonly attachment?: Attachment
}

/**
 * Request parameters for deleteAttachment operation in AttachmentV1alpha1Api.
 * @export
 * @interface AttachmentV1alpha1ApiDeleteAttachmentRequest
 */
export interface AttachmentV1alpha1ApiDeleteAttachmentRequest {
    /**
     * Name of attachment
     * @type {string}
     * @memberof AttachmentV1alpha1ApiDeleteAttachment
     */
    readonly name: string
}

/**
 * Request parameters for getAttachment operation in AttachmentV1alpha1Api.
 * @export
 * @interface AttachmentV1alpha1ApiGetAttachmentRequest
 */
export interface AttachmentV1alpha1ApiGetAttachmentRequest {
    /**
     * Name of attachment
     * @type {string}
     * @memberof AttachmentV1alpha1ApiGetAttachment
     */
    readonly name: string
}

/**
 * Request parameters for listAttachment operation in AttachmentV1alpha1Api.
 * @export
 * @interface AttachmentV1alpha1ApiListAttachmentRequest
 */
export interface AttachmentV1alpha1ApiListAttachmentRequest {
    /**
     * Page number. Default is 0.
     * @type {number}
     * @memberof AttachmentV1alpha1ApiListAttachment
     */
    readonly page?: number

    /**
     * Size number. Default is 0.
     * @type {number}
     * @memberof AttachmentV1alpha1ApiListAttachment
     */
    readonly size?: number

    /**
     * Label selector. e.g.: hidden!&#x3D;true
     * @type {Array<string>}
     * @memberof AttachmentV1alpha1ApiListAttachment
     */
    readonly labelSelector?: Array<string>

    /**
     * Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
     * @type {Array<string>}
     * @memberof AttachmentV1alpha1ApiListAttachment
     */
    readonly fieldSelector?: Array<string>

    /**
     * Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
     * @type {Array<string>}
     * @memberof AttachmentV1alpha1ApiListAttachment
     */
    readonly sort?: Array<string>
}

/**
 * Request parameters for patchAttachment operation in AttachmentV1alpha1Api.
 * @export
 * @interface AttachmentV1alpha1ApiPatchAttachmentRequest
 */
export interface AttachmentV1alpha1ApiPatchAttachmentRequest {
    /**
     * Name of attachment
     * @type {string}
     * @memberof AttachmentV1alpha1ApiPatchAttachment
     */
    readonly name: string

    /**
     * 
     * @type {Array<JsonPatchInner>}
     * @memberof AttachmentV1alpha1ApiPatchAttachment
     */
    readonly jsonPatchInner?: Array<JsonPatchInner>
}

/**
 * Request parameters for updateAttachment operation in AttachmentV1alpha1Api.
 * @export
 * @interface AttachmentV1alpha1ApiUpdateAttachmentRequest
 */
export interface AttachmentV1alpha1ApiUpdateAttachmentRequest {
    /**
     * Name of attachment
     * @type {string}
     * @memberof AttachmentV1alpha1ApiUpdateAttachment
     */
    readonly name: string

    /**
     * Updated attachment
     * @type {Attachment}
     * @memberof AttachmentV1alpha1ApiUpdateAttachment
     */
    readonly attachment?: Attachment
}

/**
 * AttachmentV1alpha1Api - object-oriented interface
 * @export
 * @class AttachmentV1alpha1Api
 * @extends {BaseAPI}
 */
export class AttachmentV1alpha1Api extends BaseAPI {
    /**
     * Create Attachment
     * @param {AttachmentV1alpha1ApiCreateAttachmentRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof AttachmentV1alpha1Api
     */
    public createAttachment(requestParameters: AttachmentV1alpha1ApiCreateAttachmentRequest = {}, options?: RawAxiosRequestConfig) {
        return AttachmentV1alpha1ApiFp(this.configuration).createAttachment(requestParameters.attachment, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Delete Attachment
     * @param {AttachmentV1alpha1ApiDeleteAttachmentRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof AttachmentV1alpha1Api
     */
    public deleteAttachment(requestParameters: AttachmentV1alpha1ApiDeleteAttachmentRequest, options?: RawAxiosRequestConfig) {
        return AttachmentV1alpha1ApiFp(this.configuration).deleteAttachment(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Get Attachment
     * @param {AttachmentV1alpha1ApiGetAttachmentRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof AttachmentV1alpha1Api
     */
    public getAttachment(requestParameters: AttachmentV1alpha1ApiGetAttachmentRequest, options?: RawAxiosRequestConfig) {
        return AttachmentV1alpha1ApiFp(this.configuration).getAttachment(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * List Attachment
     * @param {AttachmentV1alpha1ApiListAttachmentRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof AttachmentV1alpha1Api
     */
    public listAttachment(requestParameters: AttachmentV1alpha1ApiListAttachmentRequest = {}, options?: RawAxiosRequestConfig) {
        return AttachmentV1alpha1ApiFp(this.configuration).listAttachment(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Patch Attachment
     * @param {AttachmentV1alpha1ApiPatchAttachmentRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof AttachmentV1alpha1Api
     */
    public patchAttachment(requestParameters: AttachmentV1alpha1ApiPatchAttachmentRequest, options?: RawAxiosRequestConfig) {
        return AttachmentV1alpha1ApiFp(this.configuration).patchAttachment(requestParameters.name, requestParameters.jsonPatchInner, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Update Attachment
     * @param {AttachmentV1alpha1ApiUpdateAttachmentRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof AttachmentV1alpha1Api
     */
    public updateAttachment(requestParameters: AttachmentV1alpha1ApiUpdateAttachmentRequest, options?: RawAxiosRequestConfig) {
        return AttachmentV1alpha1ApiFp(this.configuration).updateAttachment(requestParameters.name, requestParameters.attachment, options).then((request) => request(this.axios, this.basePath));
    }
}

