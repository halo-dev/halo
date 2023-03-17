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

import type { Configuration } from "../configuration";
import type { AxiosPromise, AxiosInstance, AxiosRequestConfig } from "axios";
import globalAxios from "axios";
// Some imports not used depending on template conditions
// @ts-ignore
import {
  DUMMY_BASE_URL,
  assertParamExists,
  setApiKeyToObject,
  setBasicAuthToObject,
  setBearerAuthToObject,
  setOAuthToObject,
  setSearchParams,
  serializeDataIfNeeded,
  toPathString,
  createRequestFunction,
} from "../common";
// @ts-ignore
import {
  BASE_PATH,
  COLLECTION_FORMATS,
  RequestArgs,
  BaseAPI,
  RequiredError,
} from "../base";
// @ts-ignore
import { ListedAuthProvider } from "../models";
/**
 * ApiConsoleHaloRunV1alpha1AuthProviderApi - axios parameter creator
 * @export
 */
export const ApiConsoleHaloRunV1alpha1AuthProviderApiAxiosParamCreator =
  function (configuration?: Configuration) {
    return {
      /**
       * Lists all auth providers
       * @param {*} [options] Override http request option.
       * @throws {RequiredError}
       */
      listAuthProviders: async (
        options: AxiosRequestConfig = {}
      ): Promise<RequestArgs> => {
        const localVarPath = `/apis/api.console.halo.run/v1alpha1/auth-providers`;
        // use dummy base URL string because the URL constructor only accepts absolute URLs.
        const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
        let baseOptions;
        if (configuration) {
          baseOptions = configuration.baseOptions;
        }

        const localVarRequestOptions = {
          method: "GET",
          ...baseOptions,
          ...options,
        };
        const localVarHeaderParameter = {} as any;
        const localVarQueryParameter = {} as any;

        // authentication BasicAuth required
        // http basic authentication required
        setBasicAuthToObject(localVarRequestOptions, configuration);

        // authentication BearerAuth required
        // http bearer authentication required
        await setBearerAuthToObject(localVarHeaderParameter, configuration);

        setSearchParams(localVarUrlObj, localVarQueryParameter);
        let headersFromBaseOptions =
          baseOptions && baseOptions.headers ? baseOptions.headers : {};
        localVarRequestOptions.headers = {
          ...localVarHeaderParameter,
          ...headersFromBaseOptions,
          ...options.headers,
        };

        return {
          url: toPathString(localVarUrlObj),
          options: localVarRequestOptions,
        };
      },
    };
  };

/**
 * ApiConsoleHaloRunV1alpha1AuthProviderApi - functional programming interface
 * @export
 */
export const ApiConsoleHaloRunV1alpha1AuthProviderApiFp = function (
  configuration?: Configuration
) {
  const localVarAxiosParamCreator =
    ApiConsoleHaloRunV1alpha1AuthProviderApiAxiosParamCreator(configuration);
  return {
    /**
     * Lists all auth providers
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    async listAuthProviders(
      options?: AxiosRequestConfig
    ): Promise<
      (
        axios?: AxiosInstance,
        basePath?: string
      ) => AxiosPromise<ListedAuthProvider>
    > {
      const localVarAxiosArgs =
        await localVarAxiosParamCreator.listAuthProviders(options);
      return createRequestFunction(
        localVarAxiosArgs,
        globalAxios,
        BASE_PATH,
        configuration
      );
    },
  };
};

/**
 * ApiConsoleHaloRunV1alpha1AuthProviderApi - factory interface
 * @export
 */
export const ApiConsoleHaloRunV1alpha1AuthProviderApiFactory = function (
  configuration?: Configuration,
  basePath?: string,
  axios?: AxiosInstance
) {
  const localVarFp = ApiConsoleHaloRunV1alpha1AuthProviderApiFp(configuration);
  return {
    /**
     * Lists all auth providers
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    listAuthProviders(
      options?: AxiosRequestConfig
    ): AxiosPromise<ListedAuthProvider> {
      return localVarFp
        .listAuthProviders(options)
        .then((request) => request(axios, basePath));
    },
  };
};

/**
 * ApiConsoleHaloRunV1alpha1AuthProviderApi - object-oriented interface
 * @export
 * @class ApiConsoleHaloRunV1alpha1AuthProviderApi
 * @extends {BaseAPI}
 */
export class ApiConsoleHaloRunV1alpha1AuthProviderApi extends BaseAPI {
  /**
   * Lists all auth providers
   * @param {*} [options] Override http request option.
   * @throws {RequiredError}
   * @memberof ApiConsoleHaloRunV1alpha1AuthProviderApi
   */
  public listAuthProviders(options?: AxiosRequestConfig) {
    return ApiConsoleHaloRunV1alpha1AuthProviderApiFp(this.configuration)
      .listAuthProviders(options)
      .then((request) => request(this.axios, this.basePath));
  }
}
