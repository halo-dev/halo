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



/**
 * 
 * @export
 * @interface TestOperation
 */
export interface TestOperation {
    /**
     * 
     * @type {string}
     * @memberof TestOperation
     */
    'op': TestOperationOpEnum;
    /**
     * A JSON Pointer path pointing to the location to move/copy from.
     * @type {string}
     * @memberof TestOperation
     */
    'path': string;
    /**
     * Value can be any JSON value
     * @type {any}
     * @memberof TestOperation
     */
    'value': any;
}

export const TestOperationOpEnum = {
    Test: 'test'
} as const;

export type TestOperationOpEnum = typeof TestOperationOpEnum[keyof typeof TestOperationOpEnum];


