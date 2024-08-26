/* tslint:disable */
/* eslint-disable */
/**
 * Halo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.19.0-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


// May contain unused imports in some cases
// @ts-ignore
import { LocalThumbnailSpec } from './local-thumbnail-spec';
// May contain unused imports in some cases
// @ts-ignore
import { LocalThumbnailStatus } from './local-thumbnail-status';
// May contain unused imports in some cases
// @ts-ignore
import { Metadata } from './metadata';

/**
 * 
 * @export
 * @interface LocalThumbnail
 */
export interface LocalThumbnail {
    /**
     * 
     * @type {string}
     * @memberof LocalThumbnail
     */
    'apiVersion': string;
    /**
     * 
     * @type {string}
     * @memberof LocalThumbnail
     */
    'kind': string;
    /**
     * 
     * @type {Metadata}
     * @memberof LocalThumbnail
     */
    'metadata': Metadata;
    /**
     * 
     * @type {LocalThumbnailSpec}
     * @memberof LocalThumbnail
     */
    'spec': LocalThumbnailSpec;
    /**
     * 
     * @type {LocalThumbnailStatus}
     * @memberof LocalThumbnail
     */
    'status'?: LocalThumbnailStatus;
}

