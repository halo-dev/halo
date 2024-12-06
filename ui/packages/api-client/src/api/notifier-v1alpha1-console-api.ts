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
import { EmailConfigValidationRequest } from '../models';
/**
 * NotifierV1alpha1ConsoleApi - axios parameter creator
 * @export
 */
export const NotifierV1alpha1ConsoleApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Fetch sender config of notifier
         * @param {string} name Notifier name
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        fetchSenderConfig: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('fetchSenderConfig', 'name', name)
            const localVarPath = `/apis/api.console.halo.run/v1alpha1/notifiers/{name}/sender-config`
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
         * Save sender config of notifier
         * @param {string} name Notifier name
         * @param {object} body 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        saveSenderConfig: async (name: string, body: object, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('saveSenderConfig', 'name', name)
            // verify required parameter 'body' is not null or undefined
            assertParamExists('saveSenderConfig', 'body', body)
            const localVarPath = `/apis/api.console.halo.run/v1alpha1/notifiers/{name}/sender-config`
                .replace(`{${"name"}}`, encodeURIComponent(String(name)));
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
            localVarRequestOptions.data = serializeDataIfNeeded(body, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Verify email sender config.
         * @param {EmailConfigValidationRequest} emailConfigValidationRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        verifyEmailSenderConfig: async (emailConfigValidationRequest: EmailConfigValidationRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'emailConfigValidationRequest' is not null or undefined
            assertParamExists('verifyEmailSenderConfig', 'emailConfigValidationRequest', emailConfigValidationRequest)
            const localVarPath = `/apis/console.api.notification.halo.run/v1alpha1/notifiers/default-email-notifier/verify-connection`;
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
            localVarRequestOptions.data = serializeDataIfNeeded(emailConfigValidationRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * NotifierV1alpha1ConsoleApi - functional programming interface
 * @export
 */
export const NotifierV1alpha1ConsoleApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = NotifierV1alpha1ConsoleApiAxiosParamCreator(configuration)
    return {
        /**
         * Fetch sender config of notifier
         * @param {string} name Notifier name
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async fetchSenderConfig(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<object>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.fetchSenderConfig(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['NotifierV1alpha1ConsoleApi.fetchSenderConfig']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Save sender config of notifier
         * @param {string} name Notifier name
         * @param {object} body 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async saveSenderConfig(name: string, body: object, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.saveSenderConfig(name, body, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['NotifierV1alpha1ConsoleApi.saveSenderConfig']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Verify email sender config.
         * @param {EmailConfigValidationRequest} emailConfigValidationRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async verifyEmailSenderConfig(emailConfigValidationRequest: EmailConfigValidationRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.verifyEmailSenderConfig(emailConfigValidationRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['NotifierV1alpha1ConsoleApi.verifyEmailSenderConfig']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * NotifierV1alpha1ConsoleApi - factory interface
 * @export
 */
export const NotifierV1alpha1ConsoleApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = NotifierV1alpha1ConsoleApiFp(configuration)
    return {
        /**
         * Fetch sender config of notifier
         * @param {NotifierV1alpha1ConsoleApiFetchSenderConfigRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        fetchSenderConfig(requestParameters: NotifierV1alpha1ConsoleApiFetchSenderConfigRequest, options?: RawAxiosRequestConfig): AxiosPromise<object> {
            return localVarFp.fetchSenderConfig(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * Save sender config of notifier
         * @param {NotifierV1alpha1ConsoleApiSaveSenderConfigRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        saveSenderConfig(requestParameters: NotifierV1alpha1ConsoleApiSaveSenderConfigRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.saveSenderConfig(requestParameters.name, requestParameters.body, options).then((request) => request(axios, basePath));
        },
        /**
         * Verify email sender config.
         * @param {NotifierV1alpha1ConsoleApiVerifyEmailSenderConfigRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        verifyEmailSenderConfig(requestParameters: NotifierV1alpha1ConsoleApiVerifyEmailSenderConfigRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.verifyEmailSenderConfig(requestParameters.emailConfigValidationRequest, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for fetchSenderConfig operation in NotifierV1alpha1ConsoleApi.
 * @export
 * @interface NotifierV1alpha1ConsoleApiFetchSenderConfigRequest
 */
export interface NotifierV1alpha1ConsoleApiFetchSenderConfigRequest {
    /**
     * Notifier name
     * @type {string}
     * @memberof NotifierV1alpha1ConsoleApiFetchSenderConfig
     */
    readonly name: string
}

/**
 * Request parameters for saveSenderConfig operation in NotifierV1alpha1ConsoleApi.
 * @export
 * @interface NotifierV1alpha1ConsoleApiSaveSenderConfigRequest
 */
export interface NotifierV1alpha1ConsoleApiSaveSenderConfigRequest {
    /**
     * Notifier name
     * @type {string}
     * @memberof NotifierV1alpha1ConsoleApiSaveSenderConfig
     */
    readonly name: string

    /**
     * 
     * @type {object}
     * @memberof NotifierV1alpha1ConsoleApiSaveSenderConfig
     */
    readonly body: object
}

/**
 * Request parameters for verifyEmailSenderConfig operation in NotifierV1alpha1ConsoleApi.
 * @export
 * @interface NotifierV1alpha1ConsoleApiVerifyEmailSenderConfigRequest
 */
export interface NotifierV1alpha1ConsoleApiVerifyEmailSenderConfigRequest {
    /**
     * 
     * @type {EmailConfigValidationRequest}
     * @memberof NotifierV1alpha1ConsoleApiVerifyEmailSenderConfig
     */
    readonly emailConfigValidationRequest: EmailConfigValidationRequest
}

/**
 * NotifierV1alpha1ConsoleApi - object-oriented interface
 * @export
 * @class NotifierV1alpha1ConsoleApi
 * @extends {BaseAPI}
 */
export class NotifierV1alpha1ConsoleApi extends BaseAPI {
    /**
     * Fetch sender config of notifier
     * @param {NotifierV1alpha1ConsoleApiFetchSenderConfigRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof NotifierV1alpha1ConsoleApi
     */
    public fetchSenderConfig(requestParameters: NotifierV1alpha1ConsoleApiFetchSenderConfigRequest, options?: RawAxiosRequestConfig) {
        return NotifierV1alpha1ConsoleApiFp(this.configuration).fetchSenderConfig(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Save sender config of notifier
     * @param {NotifierV1alpha1ConsoleApiSaveSenderConfigRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof NotifierV1alpha1ConsoleApi
     */
    public saveSenderConfig(requestParameters: NotifierV1alpha1ConsoleApiSaveSenderConfigRequest, options?: RawAxiosRequestConfig) {
        return NotifierV1alpha1ConsoleApiFp(this.configuration).saveSenderConfig(requestParameters.name, requestParameters.body, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Verify email sender config.
     * @param {NotifierV1alpha1ConsoleApiVerifyEmailSenderConfigRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof NotifierV1alpha1ConsoleApi
     */
    public verifyEmailSenderConfig(requestParameters: NotifierV1alpha1ConsoleApiVerifyEmailSenderConfigRequest, options?: RawAxiosRequestConfig) {
        return NotifierV1alpha1ConsoleApiFp(this.configuration).verifyEmailSenderConfig(requestParameters.emailConfigValidationRequest, options).then((request) => request(this.axios, this.basePath));
    }
}

