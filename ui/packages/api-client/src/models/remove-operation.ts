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



/**
 * 
 * @export
 * @interface RemoveOperation
 */
export interface RemoveOperation {
    /**
     * 
     * @type {string}
     * @memberof RemoveOperation
     */
    'op': RemoveOperationOpEnum;
    /**
     * A JSON Pointer path pointing to the location to move/copy from.
     * @type {string}
     * @memberof RemoveOperation
     */
    'path': string;
}

export const RemoveOperationOpEnum = {
    Remove: 'remove'
} as const;

export type RemoveOperationOpEnum = typeof RemoveOperationOpEnum[keyof typeof RemoveOperationOpEnum];


