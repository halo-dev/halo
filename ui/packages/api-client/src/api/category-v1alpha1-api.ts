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
import { Category } from '../models';
// @ts-ignore
import { CategoryList } from '../models';
// @ts-ignore
import { JsonPatchInner } from '../models';
/**
 * CategoryV1alpha1Api - axios parameter creator
 * @export
 */
export const CategoryV1alpha1ApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Create Category
         * @param {Category} [category] Fresh category
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createCategory: async (category?: Category, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/content.halo.run/v1alpha1/categories`;
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
            localVarRequestOptions.data = serializeDataIfNeeded(category, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Delete Category
         * @param {string} name Name of category
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deleteCategory: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('deleteCategory', 'name', name)
            const localVarPath = `/apis/content.halo.run/v1alpha1/categories/{name}`
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
         * Get Category
         * @param {string} name Name of category
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getCategory: async (name: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('getCategory', 'name', name)
            const localVarPath = `/apis/content.halo.run/v1alpha1/categories/{name}`
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
         * List Category
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listCategory: async (page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/content.halo.run/v1alpha1/categories`;
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
         * Patch Category
         * @param {string} name Name of category
         * @param {Array<JsonPatchInner>} [jsonPatchInner] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        patchCategory: async (name: string, jsonPatchInner?: Array<JsonPatchInner>, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('patchCategory', 'name', name)
            const localVarPath = `/apis/content.halo.run/v1alpha1/categories/{name}`
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
         * Update Category
         * @param {string} name Name of category
         * @param {Category} [category] Updated category
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updateCategory: async (name: string, category?: Category, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('updateCategory', 'name', name)
            const localVarPath = `/apis/content.halo.run/v1alpha1/categories/{name}`
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
            localVarRequestOptions.data = serializeDataIfNeeded(category, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * CategoryV1alpha1Api - functional programming interface
 * @export
 */
export const CategoryV1alpha1ApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = CategoryV1alpha1ApiAxiosParamCreator(configuration)
    return {
        /**
         * Create Category
         * @param {Category} [category] Fresh category
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async createCategory(category?: Category, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Category>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.createCategory(category, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['CategoryV1alpha1Api.createCategory']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Delete Category
         * @param {string} name Name of category
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async deleteCategory(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.deleteCategory(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['CategoryV1alpha1Api.deleteCategory']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Get Category
         * @param {string} name Name of category
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getCategory(name: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Category>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getCategory(name, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['CategoryV1alpha1Api.getCategory']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * List Category
         * @param {number} [page] Page number. Default is 0.
         * @param {number} [size] Size number. Default is 0.
         * @param {Array<string>} [labelSelector] Label selector. e.g.: hidden!&#x3D;true
         * @param {Array<string>} [fieldSelector] Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
         * @param {Array<string>} [sort] Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async listCategory(page?: number, size?: number, labelSelector?: Array<string>, fieldSelector?: Array<string>, sort?: Array<string>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<CategoryList>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.listCategory(page, size, labelSelector, fieldSelector, sort, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['CategoryV1alpha1Api.listCategory']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Patch Category
         * @param {string} name Name of category
         * @param {Array<JsonPatchInner>} [jsonPatchInner] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async patchCategory(name: string, jsonPatchInner?: Array<JsonPatchInner>, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Category>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.patchCategory(name, jsonPatchInner, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['CategoryV1alpha1Api.patchCategory']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Update Category
         * @param {string} name Name of category
         * @param {Category} [category] Updated category
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async updateCategory(name: string, category?: Category, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Category>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.updateCategory(name, category, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['CategoryV1alpha1Api.updateCategory']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * CategoryV1alpha1Api - factory interface
 * @export
 */
export const CategoryV1alpha1ApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = CategoryV1alpha1ApiFp(configuration)
    return {
        /**
         * Create Category
         * @param {CategoryV1alpha1ApiCreateCategoryRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        createCategory(requestParameters: CategoryV1alpha1ApiCreateCategoryRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<Category> {
            return localVarFp.createCategory(requestParameters.category, options).then((request) => request(axios, basePath));
        },
        /**
         * Delete Category
         * @param {CategoryV1alpha1ApiDeleteCategoryRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        deleteCategory(requestParameters: CategoryV1alpha1ApiDeleteCategoryRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.deleteCategory(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * Get Category
         * @param {CategoryV1alpha1ApiGetCategoryRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getCategory(requestParameters: CategoryV1alpha1ApiGetCategoryRequest, options?: RawAxiosRequestConfig): AxiosPromise<Category> {
            return localVarFp.getCategory(requestParameters.name, options).then((request) => request(axios, basePath));
        },
        /**
         * List Category
         * @param {CategoryV1alpha1ApiListCategoryRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        listCategory(requestParameters: CategoryV1alpha1ApiListCategoryRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<CategoryList> {
            return localVarFp.listCategory(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(axios, basePath));
        },
        /**
         * Patch Category
         * @param {CategoryV1alpha1ApiPatchCategoryRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        patchCategory(requestParameters: CategoryV1alpha1ApiPatchCategoryRequest, options?: RawAxiosRequestConfig): AxiosPromise<Category> {
            return localVarFp.patchCategory(requestParameters.name, requestParameters.jsonPatchInner, options).then((request) => request(axios, basePath));
        },
        /**
         * Update Category
         * @param {CategoryV1alpha1ApiUpdateCategoryRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        updateCategory(requestParameters: CategoryV1alpha1ApiUpdateCategoryRequest, options?: RawAxiosRequestConfig): AxiosPromise<Category> {
            return localVarFp.updateCategory(requestParameters.name, requestParameters.category, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for createCategory operation in CategoryV1alpha1Api.
 * @export
 * @interface CategoryV1alpha1ApiCreateCategoryRequest
 */
export interface CategoryV1alpha1ApiCreateCategoryRequest {
    /**
     * Fresh category
     * @type {Category}
     * @memberof CategoryV1alpha1ApiCreateCategory
     */
    readonly category?: Category
}

/**
 * Request parameters for deleteCategory operation in CategoryV1alpha1Api.
 * @export
 * @interface CategoryV1alpha1ApiDeleteCategoryRequest
 */
export interface CategoryV1alpha1ApiDeleteCategoryRequest {
    /**
     * Name of category
     * @type {string}
     * @memberof CategoryV1alpha1ApiDeleteCategory
     */
    readonly name: string
}

/**
 * Request parameters for getCategory operation in CategoryV1alpha1Api.
 * @export
 * @interface CategoryV1alpha1ApiGetCategoryRequest
 */
export interface CategoryV1alpha1ApiGetCategoryRequest {
    /**
     * Name of category
     * @type {string}
     * @memberof CategoryV1alpha1ApiGetCategory
     */
    readonly name: string
}

/**
 * Request parameters for listCategory operation in CategoryV1alpha1Api.
 * @export
 * @interface CategoryV1alpha1ApiListCategoryRequest
 */
export interface CategoryV1alpha1ApiListCategoryRequest {
    /**
     * Page number. Default is 0.
     * @type {number}
     * @memberof CategoryV1alpha1ApiListCategory
     */
    readonly page?: number

    /**
     * Size number. Default is 0.
     * @type {number}
     * @memberof CategoryV1alpha1ApiListCategory
     */
    readonly size?: number

    /**
     * Label selector. e.g.: hidden!&#x3D;true
     * @type {Array<string>}
     * @memberof CategoryV1alpha1ApiListCategory
     */
    readonly labelSelector?: Array<string>

    /**
     * Field selector. e.g.: metadata.name&#x3D;&#x3D;halo
     * @type {Array<string>}
     * @memberof CategoryV1alpha1ApiListCategory
     */
    readonly fieldSelector?: Array<string>

    /**
     * Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported.
     * @type {Array<string>}
     * @memberof CategoryV1alpha1ApiListCategory
     */
    readonly sort?: Array<string>
}

/**
 * Request parameters for patchCategory operation in CategoryV1alpha1Api.
 * @export
 * @interface CategoryV1alpha1ApiPatchCategoryRequest
 */
export interface CategoryV1alpha1ApiPatchCategoryRequest {
    /**
     * Name of category
     * @type {string}
     * @memberof CategoryV1alpha1ApiPatchCategory
     */
    readonly name: string

    /**
     * 
     * @type {Array<JsonPatchInner>}
     * @memberof CategoryV1alpha1ApiPatchCategory
     */
    readonly jsonPatchInner?: Array<JsonPatchInner>
}

/**
 * Request parameters for updateCategory operation in CategoryV1alpha1Api.
 * @export
 * @interface CategoryV1alpha1ApiUpdateCategoryRequest
 */
export interface CategoryV1alpha1ApiUpdateCategoryRequest {
    /**
     * Name of category
     * @type {string}
     * @memberof CategoryV1alpha1ApiUpdateCategory
     */
    readonly name: string

    /**
     * Updated category
     * @type {Category}
     * @memberof CategoryV1alpha1ApiUpdateCategory
     */
    readonly category?: Category
}

/**
 * CategoryV1alpha1Api - object-oriented interface
 * @export
 * @class CategoryV1alpha1Api
 * @extends {BaseAPI}
 */
export class CategoryV1alpha1Api extends BaseAPI {
    /**
     * Create Category
     * @param {CategoryV1alpha1ApiCreateCategoryRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof CategoryV1alpha1Api
     */
    public createCategory(requestParameters: CategoryV1alpha1ApiCreateCategoryRequest = {}, options?: RawAxiosRequestConfig) {
        return CategoryV1alpha1ApiFp(this.configuration).createCategory(requestParameters.category, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Delete Category
     * @param {CategoryV1alpha1ApiDeleteCategoryRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof CategoryV1alpha1Api
     */
    public deleteCategory(requestParameters: CategoryV1alpha1ApiDeleteCategoryRequest, options?: RawAxiosRequestConfig) {
        return CategoryV1alpha1ApiFp(this.configuration).deleteCategory(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Get Category
     * @param {CategoryV1alpha1ApiGetCategoryRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof CategoryV1alpha1Api
     */
    public getCategory(requestParameters: CategoryV1alpha1ApiGetCategoryRequest, options?: RawAxiosRequestConfig) {
        return CategoryV1alpha1ApiFp(this.configuration).getCategory(requestParameters.name, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * List Category
     * @param {CategoryV1alpha1ApiListCategoryRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof CategoryV1alpha1Api
     */
    public listCategory(requestParameters: CategoryV1alpha1ApiListCategoryRequest = {}, options?: RawAxiosRequestConfig) {
        return CategoryV1alpha1ApiFp(this.configuration).listCategory(requestParameters.page, requestParameters.size, requestParameters.labelSelector, requestParameters.fieldSelector, requestParameters.sort, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Patch Category
     * @param {CategoryV1alpha1ApiPatchCategoryRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof CategoryV1alpha1Api
     */
    public patchCategory(requestParameters: CategoryV1alpha1ApiPatchCategoryRequest, options?: RawAxiosRequestConfig) {
        return CategoryV1alpha1ApiFp(this.configuration).patchCategory(requestParameters.name, requestParameters.jsonPatchInner, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Update Category
     * @param {CategoryV1alpha1ApiUpdateCategoryRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof CategoryV1alpha1Api
     */
    public updateCategory(requestParameters: CategoryV1alpha1ApiUpdateCategoryRequest, options?: RawAxiosRequestConfig) {
        return CategoryV1alpha1ApiFp(this.configuration).updateCategory(requestParameters.name, requestParameters.category, options).then((request) => request(this.axios, this.basePath));
    }
}

