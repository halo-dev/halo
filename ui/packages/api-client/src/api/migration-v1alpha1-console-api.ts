/* tslint:disable */
/* eslint-disable */
/**
 * Halo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.20.0-SNAPSHOT
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
import { BackupFile } from '../models';
/**
 * MigrationV1alpha1ConsoleApi - axios parameter creator
 * @export
 */
export const MigrationV1alpha1ConsoleApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * 
         * @param {string} name Backup name.
         * @param {string} filename Backup filename.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        downloadBackups: async (name: string, filename: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'name' is not null or undefined
            assertParamExists('downloadBackups', 'name', name)
            // verify required parameter 'filename' is not null or undefined
            assertParamExists('downloadBackups', 'filename', filename)
            const localVarPath = `/apis/console.api.migration.halo.run/v1alpha1/backups/{name}/files/{filename}`
                .replace(`{${"name"}}`, encodeURIComponent(String(name)))
                .replace(`{${"filename"}}`, encodeURIComponent(String(filename)));
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
         * Get backup files from backup root.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getBackupFiles: async (options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/console.api.migration.halo.run/v1alpha1/backup-files`;
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
         * Restore backup by uploading file or providing download link or backup name.
         * @param {string} [backupName] Backup metadata name.
         * @param {string} [downloadUrl] Remote backup HTTP URL.
         * @param {File} [file] 
         * @param {string} [filename] Filename of backup file in backups root.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        restoreBackup: async (backupName?: string, downloadUrl?: string, file?: File, filename?: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/apis/console.api.migration.halo.run/v1alpha1/restorations`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;
            const localVarFormParams = new ((configuration && configuration.formDataCtor) || FormData)();

            // authentication basicAuth required
            // http basic authentication required
            setBasicAuthToObject(localVarRequestOptions, configuration)

            // authentication bearerAuth required
            // http bearer authentication required
            await setBearerAuthToObject(localVarHeaderParameter, configuration)


            if (backupName !== undefined) { 
                localVarFormParams.append('backupName', backupName as any);
            }
    
            if (downloadUrl !== undefined) { 
                localVarFormParams.append('downloadUrl', downloadUrl as any);
            }
    
            if (file !== undefined) { 
                localVarFormParams.append('file', file as any);
            }
    
            if (filename !== undefined) { 
                localVarFormParams.append('filename', filename as any);
            }
    
    
            localVarHeaderParameter['Content-Type'] = 'multipart/form-data';
    
            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = localVarFormParams;

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * MigrationV1alpha1ConsoleApi - functional programming interface
 * @export
 */
export const MigrationV1alpha1ConsoleApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = MigrationV1alpha1ConsoleApiAxiosParamCreator(configuration)
    return {
        /**
         * 
         * @param {string} name Backup name.
         * @param {string} filename Backup filename.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async downloadBackups(name: string, filename: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.downloadBackups(name, filename, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['MigrationV1alpha1ConsoleApi.downloadBackups']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Get backup files from backup root.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async getBackupFiles(options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<Array<BackupFile>>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.getBackupFiles(options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['MigrationV1alpha1ConsoleApi.getBackupFiles']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Restore backup by uploading file or providing download link or backup name.
         * @param {string} [backupName] Backup metadata name.
         * @param {string} [downloadUrl] Remote backup HTTP URL.
         * @param {File} [file] 
         * @param {string} [filename] Filename of backup file in backups root.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async restoreBackup(backupName?: string, downloadUrl?: string, file?: File, filename?: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<void>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.restoreBackup(backupName, downloadUrl, file, filename, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['MigrationV1alpha1ConsoleApi.restoreBackup']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * MigrationV1alpha1ConsoleApi - factory interface
 * @export
 */
export const MigrationV1alpha1ConsoleApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = MigrationV1alpha1ConsoleApiFp(configuration)
    return {
        /**
         * 
         * @param {MigrationV1alpha1ConsoleApiDownloadBackupsRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        downloadBackups(requestParameters: MigrationV1alpha1ConsoleApiDownloadBackupsRequest, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.downloadBackups(requestParameters.name, requestParameters.filename, options).then((request) => request(axios, basePath));
        },
        /**
         * Get backup files from backup root.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        getBackupFiles(options?: RawAxiosRequestConfig): AxiosPromise<Array<BackupFile>> {
            return localVarFp.getBackupFiles(options).then((request) => request(axios, basePath));
        },
        /**
         * Restore backup by uploading file or providing download link or backup name.
         * @param {MigrationV1alpha1ConsoleApiRestoreBackupRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        restoreBackup(requestParameters: MigrationV1alpha1ConsoleApiRestoreBackupRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<void> {
            return localVarFp.restoreBackup(requestParameters.backupName, requestParameters.downloadUrl, requestParameters.file, requestParameters.filename, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for downloadBackups operation in MigrationV1alpha1ConsoleApi.
 * @export
 * @interface MigrationV1alpha1ConsoleApiDownloadBackupsRequest
 */
export interface MigrationV1alpha1ConsoleApiDownloadBackupsRequest {
    /**
     * Backup name.
     * @type {string}
     * @memberof MigrationV1alpha1ConsoleApiDownloadBackups
     */
    readonly name: string

    /**
     * Backup filename.
     * @type {string}
     * @memberof MigrationV1alpha1ConsoleApiDownloadBackups
     */
    readonly filename: string
}

/**
 * Request parameters for restoreBackup operation in MigrationV1alpha1ConsoleApi.
 * @export
 * @interface MigrationV1alpha1ConsoleApiRestoreBackupRequest
 */
export interface MigrationV1alpha1ConsoleApiRestoreBackupRequest {
    /**
     * Backup metadata name.
     * @type {string}
     * @memberof MigrationV1alpha1ConsoleApiRestoreBackup
     */
    readonly backupName?: string

    /**
     * Remote backup HTTP URL.
     * @type {string}
     * @memberof MigrationV1alpha1ConsoleApiRestoreBackup
     */
    readonly downloadUrl?: string

    /**
     * 
     * @type {File}
     * @memberof MigrationV1alpha1ConsoleApiRestoreBackup
     */
    readonly file?: File

    /**
     * Filename of backup file in backups root.
     * @type {string}
     * @memberof MigrationV1alpha1ConsoleApiRestoreBackup
     */
    readonly filename?: string
}

/**
 * MigrationV1alpha1ConsoleApi - object-oriented interface
 * @export
 * @class MigrationV1alpha1ConsoleApi
 * @extends {BaseAPI}
 */
export class MigrationV1alpha1ConsoleApi extends BaseAPI {
    /**
     * 
     * @param {MigrationV1alpha1ConsoleApiDownloadBackupsRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof MigrationV1alpha1ConsoleApi
     */
    public downloadBackups(requestParameters: MigrationV1alpha1ConsoleApiDownloadBackupsRequest, options?: RawAxiosRequestConfig) {
        return MigrationV1alpha1ConsoleApiFp(this.configuration).downloadBackups(requestParameters.name, requestParameters.filename, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Get backup files from backup root.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof MigrationV1alpha1ConsoleApi
     */
    public getBackupFiles(options?: RawAxiosRequestConfig) {
        return MigrationV1alpha1ConsoleApiFp(this.configuration).getBackupFiles(options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Restore backup by uploading file or providing download link or backup name.
     * @param {MigrationV1alpha1ConsoleApiRestoreBackupRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof MigrationV1alpha1ConsoleApi
     */
    public restoreBackup(requestParameters: MigrationV1alpha1ConsoleApiRestoreBackupRequest = {}, options?: RawAxiosRequestConfig) {
        return MigrationV1alpha1ConsoleApiFp(this.configuration).restoreBackup(requestParameters.backupName, requestParameters.downloadUrl, requestParameters.file, requestParameters.filename, options).then((request) => request(this.axios, this.basePath));
    }
}

