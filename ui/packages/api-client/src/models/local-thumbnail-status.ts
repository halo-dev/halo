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



/**
 * 
 * @export
 * @interface LocalThumbnailStatus
 */
export interface LocalThumbnailStatus {
    /**
     * 
     * @type {string}
     * @memberof LocalThumbnailStatus
     */
    'phase'?: LocalThumbnailStatusPhaseEnum;
}

export const LocalThumbnailStatusPhaseEnum = {
    Pending: 'PENDING',
    Succeeded: 'SUCCEEDED',
    Failed: 'FAILED'
} as const;

export type LocalThumbnailStatusPhaseEnum = typeof LocalThumbnailStatusPhaseEnum[keyof typeof LocalThumbnailStatusPhaseEnum];


