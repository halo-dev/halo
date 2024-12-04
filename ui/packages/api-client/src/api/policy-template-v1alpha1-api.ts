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
import { JsonPatchInner } from '../models';
// @ts-ignore
import { PolicyTemplate } from '../models';
// @ts-ignore
import { PolicyTemplateList } from '../models';
/**
 * PolicyTemplateV1alpha1Api - axios parameter creator
 * @export
 */
export const PolicyTemplateV1alpha1ApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Create PolicyTemplate
         * @param {PolicyTemplate} [policyTemplate] Fresh policytemplate
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createPolicyTemplate: async (policyTemplate?: PolicyTemplate, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/storage.halo.run/v1alpha1/policytemplates`;
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
            localVarRequestOptions.data = serializeDataIfNeeded(policyTemplate, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Delete PolicyTemplate
         * @param {string} name Name of policytemplate
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deletePolicyTemplate: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('deletePolicyTemplate', 'name', name)
            const localVarPath = `/apis/storage.halo.run/v1alpha1/policytemplates/{name}`
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
         * Get PolicyTemplate
         * @param {string} name Name of policytemplate
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getPolicyTemplate: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('getPolicyTemplate', 'name', name)
            const localVarPath = `/apis/storage.halo.run/v1alpha1/policytemplates/{name}`
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
         * List PolicyTemplate
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listPolicyTemplate: async (page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/storage.halo.run/v1alpha1/policytemplates`;
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
         * Patch PolicyTemplate
         * @param {string} name Name of policytemplate
         * @param {Array<JsonPatchInner>} [jsonPatchInner] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        patchPolicyTemplate: async (name: string, jsonPatchInner?: Array<JsonPatchInner>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('patchPolicyTemplate', 'name', name)
            const localVarPath = `/apis/storage.halo.run/v1alpha1/policytemplates/{name}`
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
         * Update PolicyTemplate
         * @param {string} name Name of policytemplate
         * @param {PolicyTemplate} [policyTemplate] Updated policytemplate
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updatePolicyTemplate: async (name: string, policyTemplate?: PolicyTemplate, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('updatePolicyTemplate', 'name', name)
            const localVarPath = `/apis/storage.halo.run/v1alpha1/policytemplates/{name}`
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
            localVarRequestOptions.data = serializeDataIfNeeded(policyTemplate, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * PolicyTemplateV1alpha1Api - functional programming interface
 * @export
 */
export const PolicyTemplateV1alpha1ApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = PolicyTemplateV1alpha1ApiAxiosParamCreator(configuration)
    return {
        /**
         * Create PolicyTemplate
         * @param {PolicyTemplate} [policyTemplate] Fresh policytemplate
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async createPolicyTemplate(policyTemplate?: PolicyTemplate, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<PolicyTemplate>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.createPolicyTemplate(policyTemplate, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PolicyTemplateV1alpha1Api.createPolicyTemplate']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Delete PolicyTemplate
         * @param {string} name Name of policytemplate
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async deletePolicyTemplate(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.deletePolicyTemplate(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PolicyTemplateV1alpha1Api.deletePolicyTemplate']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Get PolicyTemplate
         * @param {string} name Name of policytemplate
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getPolicyTemplate(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<PolicyTemplate>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getPolicyTemplate(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PolicyTemplateV1alpha1Api.getPolicyTemplate']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * List PolicyTemplate
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async listPolicyTemplate(page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<PolicyTemplateList>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.listPolicyTemplate(page, size, labelSelector, fieldSelector, sort, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PolicyTemplateV1alpha1Api.listPolicyTemplate']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Patch PolicyTemplate
         * @param {string} name Name of policytemplate
         * @param {Array<JsonPatchInner>} [jsonPatchInner] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async patchPolicyTemplate(name: string, jsonPatchInner?: Array<JsonPatchInner>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<PolicyTemplate>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.patchPolicyTemplate(name, jsonPatchInner, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PolicyTemplateV1alpha1Api.patchPolicyTemplate']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Update PolicyTemplate
         * @param {string} name Name of policytemplate
         * @param {PolicyTemplate} [policyTemplate] Updated policytemplate
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async updatePolicyTemplate(name: string, policyTemplate?: PolicyTemplate, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<PolicyTemplate>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.updatePolicyTemplate(name, policyTemplate, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['PolicyTemplateV1alpha1Api.updatePolicyTemplate']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * PolicyTemplateV1alpha1Api - factory interface
 * @export
 */
export const PolicyTemplateV1alpha1ApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = PolicyTemplateV1alpha1ApiFp(configuration)
    return {
        /**
         * Create PolicyTemplate
         * @param {PolicyTemplateV1alpha1ApiCreatePolicyTemplateRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createPolicyTemplate(requestParameters: PolicyTemplateV1alpha1ApiCreatePolicyTemplateRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<PolicyTemplate> {
            return localVarFp.createPolicyTemplate(requestParameters.policyTemplate, options).then((request) => request(axios, basePath));
        },
        /**
         * Delete PolicyTemplate
         * @param {PolicyTemplateV1alpha1ApiDeletePolicyTemplateRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deletePolicyTemplate(requestParameters: PolicyTemplateV1alpha1ApiDeletePolicyTemplateRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.deletePolicyTemplate(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * Get PolicyTemplate
         * @param {PolicyTemplateV1alpha1ApiGetPolicyTemplateRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getPolicyTemplate(requestParameters: PolicyTemplateV1alpha1ApiGetPolicyTemplateRequest, options?: RawAxiosRequestConfig): AxiosPromise<PolicyTemplate> {
            return localVarFp.getPolicyTemplate(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * List PolicyTemplate
         * @param {PolicyTemplateV1alpha1ApiListPolicyTemplateRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listPolicyTemplate(requestParameters: PolicyTemplateV1alpha1ApiListPolicyTemplateRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<PolicyTemplateList> {
            return localVarFp.listPolicyTemplate(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(axios, basePath));
        },
        /**
         * Patch PolicyTemplate
         * @param {PolicyTemplateV1alpha1ApiPatchPolicyTemplateRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        patchPolicyTemplate(requestParameters: PolicyTemplateV1alpha1ApiPatchPolicyTemplateRequest, options?: RawAxiosRequestConfig): AxiosPromise<PolicyTemplate> {
            return localVarFp.patchPolicyTemplate(requestParameters.name, requestParameters.jsonPatchInner, options).then((request) => request(axios, basePath));
        },
        /**
         * Update PolicyTemplate
         * @param {PolicyTemplateV1alpha1ApiUpdatePolicyTemplateRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updatePolicyTemplate(requestParameters: PolicyTemplateV1alpha1ApiUpdatePolicyTemplateRequest, options?: RawAxiosRequestConfig): AxiosPromise<PolicyTemplate> {
            return localVarFp.updatePolicyTemplate(requestParameters.name, requestParameters.policyTemplate, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for createPolicyTemplate operation in PolicyTemplateV1alpha1Api.
 * @export
 * @interface PolicyTemplateV1alpha1ApiCreatePolicyTemplateRequest
 */
export interface PolicyTemplateV1alpha1ApiCreatePolicyTemplateRequest {
    /**
     * Fresh policytemplate
     * @type {PolicyTemplate}
     * @memberof PolicyTemplateV1alpha1ApiCreatePolicyTemplate
     */
    readonly policyTemplate?: PolicyTemplate
}

/**
 * Request parameters for deletePolicyTemplate operation in PolicyTemplateV1alpha1Api.
 * @export
 * @interface PolicyTemplateV1alpha1ApiDeletePolicyTemplateRequest
 */
export interface PolicyTemplateV1alpha1ApiDeletePolicyTemplateRequest {
    /**
     * Name of policytemplate
     * @type {string}
     * @memberof PolicyTemplateV1alpha1ApiDeletePolicyTemplate
     */
    readonly name: string
}

/**
 * Request parameters for getPolicyTemplate operation in PolicyTemplateV1alpha1Api.
 * @export
 * @interface PolicyTemplateV1alpha1ApiGetPolicyTemplateRequest
 */
export interface PolicyTemplateV1alpha1ApiGetPolicyTemplateRequest {
    /**
     * Name of policytemplate
     * @type {string}
     * @memberof PolicyTemplateV1alpha1ApiGetPolicyTemplate
     */
    readonly name: string
}

/**
 * Request parameters for listPolicyTemplate operation in PolicyTemplateV1alpha1Api.
 * @export
 * @interface PolicyTemplateV1alpha1ApiListPolicyTemplateRequest
 */
export interface PolicyTemplateV1alpha1ApiListPolicyTemplateRequest {
    /**
     * Page number. Default is 0.
     * @type {number}
     * @memberof PolicyTemplateV1alpha1ApiListPolicyTemplate
     */
    readonly page?: number

    /**
     * Size number. Default is 0.
     * @type {number}
     * @memberof PolicyTemplateV1alpha1ApiListPolicyTemplate
     */
    readonly size?: number

    /**
     * Label selector. e.g.: hidden!&#x3D;true
     * @type {Array<string>}
     * @memberof PolicyTemplateV1alpha1ApiListPolicyTemplate
     */
    readonly labelSelector?: Array<string>

    /**
     * Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
     * @type {Array<string>}
     * @memberof PolicyTemplateV1alpha1ApiListPolicyTemplate
     */
    readonly fieldSelector?: Array<string>

    /**
     * Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
     * @type {Array<string>}
     * @memberof PolicyTemplateV1alpha1ApiListPolicyTemplate
     */
    readonly sort?: Array<string>
}

/**
 * Request parameters for patchPolicyTemplate operation in PolicyTemplateV1alpha1Api.
 * @export
 * @interface PolicyTemplateV1alpha1ApiPatchPolicyTemplateRequest
 */
export interface PolicyTemplateV1alpha1ApiPatchPolicyTemplateRequest {
    /**
     * Name of policytemplate
     * @type {string}
     * @memberof PolicyTemplateV1alpha1ApiPatchPolicyTemplate
     */
    readonly name: string

    /**
     * 
     * @type {Array<JsonPatchInner>}
     * @memberof PolicyTemplateV1alpha1ApiPatchPolicyTemplate
     */
    readonly jsonPatchInner?: Array<JsonPatchInner>
}

/**
 * Request parameters for updatePolicyTemplate operation in PolicyTemplateV1alpha1Api.
 * @export
 * @interface PolicyTemplateV1alpha1ApiUpdatePolicyTemplateRequest
 */
export interface PolicyTemplateV1alpha1ApiUpdatePolicyTemplateRequest {
    /**
     * Name of policytemplate
     * @type {string}
     * @memberof PolicyTemplateV1alpha1ApiUpdatePolicyTemplate
     */
    readonly name: string

    /**
     * Updated policytemplate
     * @type {PolicyTemplate}
     * @memberof PolicyTemplateV1alpha1ApiUpdatePolicyTemplate
     */
    readonly policyTemplate?: PolicyTemplate
}

/**
 * PolicyTemplateV1alpha1Api - object-oriented interface
 * @export
 * @class PolicyTemplateV1alpha1Api
 * @extends {BaseAPI}
 */
export class PolicyTemplateV1alpha1Api extends BaseAPI {
    /**
     * Create PolicyTemplate
     * @param {PolicyTemplateV1alpha1ApiCreatePolicyTemplateRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PolicyTemplateV1alpha1Api
     */
    public createPolicyTemplate(requestParameters: PolicyTemplateV1alpha1ApiCreatePolicyTemplateRequest = {}, options?: RawAxiosRequestConfig) {
        return PolicyTemplateV1alpha1ApiFp(this.configuration).createPolicyTemplate(requestParameters.policyTemplate, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Delete PolicyTemplate
     * @param {PolicyTemplateV1alpha1ApiDeletePolicyTemplateRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PolicyTemplateV1alpha1Api
     */
    public deletePolicyTemplate(requestParameters: PolicyTemplateV1alpha1ApiDeletePolicyTemplateRequest, options?: RawAxiosRequestConfig) {
        return PolicyTemplateV1alpha1ApiFp(this.configuration).deletePolicyTemplate(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Get PolicyTemplate
     * @param {PolicyTemplateV1alpha1ApiGetPolicyTemplateRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PolicyTemplateV1alpha1Api
     */
    public getPolicyTemplate(requestParameters: PolicyTemplateV1alpha1ApiGetPolicyTemplateRequest, options?: RawAxiosRequestConfig) {
        return PolicyTemplateV1alpha1ApiFp(this.configuration).getPolicyTemplate(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * List PolicyTemplate
     * @param {PolicyTemplateV1alpha1ApiListPolicyTemplateRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PolicyTemplateV1alpha1Api
     */
    public listPolicyTemplate(requestParameters: PolicyTemplateV1alpha1ApiListPolicyTemplateRequest = {}, options?: RawAxiosRequestConfig) {
        return PolicyTemplateV1alpha1ApiFp(this.configuration).listPolicyTemplate(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Patch PolicyTemplate
     * @param {PolicyTemplateV1alpha1ApiPatchPolicyTemplateRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PolicyTemplateV1alpha1Api
     */
    public patchPolicyTemplate(requestParameters: PolicyTemplateV1alpha1ApiPatchPolicyTemplateRequest, options?: RawAxiosRequestConfig) {
        return PolicyTemplateV1alpha1ApiFp(this.configuration).patchPolicyTemplate(requestParameters.name, requestParameters.jsonPatchInner, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Update PolicyTemplate
     * @param {PolicyTemplateV1alpha1ApiUpdatePolicyTemplateRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof PolicyTemplateV1alpha1Api
     */
    public updatePolicyTemplate(requestParameters: PolicyTemplateV1alpha1ApiUpdatePolicyTemplateRequest, options?: RawAxiosRequestConfig) {
        return PolicyTemplateV1alpha1ApiFp(this.configuration).updatePolicyTemplate(requestParameters.name, requestParameters.policyTemplate, options).then((request) => request(this.axios, this.basePath));
    }
}

