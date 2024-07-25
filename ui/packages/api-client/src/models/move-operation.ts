/* tslint:disable */
/* eslint-disable */
/**
 * Halo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.18.0-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */



/**
 * 
 * @export
 * @interface MoveOperation
 */
export interface MoveOperation {
    /**
     * A JSON Pointer path pointing to the location to move/copy from.
     * @type {string}
     * @memberof MoveOperation
     */
    'from': string;
    /**
     * 
     * @type {string}
     * @memberof MoveOperation
     */
    'op': MoveOperationOpEnum;
    /**
     * A JSON Pointer path pointing to the location to move/copy from.
     * @type {string}
     * @memberof MoveOperation
     */
    'path': string;
}

export const MoveOperationOpEnum = {
    Move: 'move'
} as const;

export type MoveOperationOpEnum = typeof MoveOperationOpEnum[keyof typeof MoveOperationOpEnum];


