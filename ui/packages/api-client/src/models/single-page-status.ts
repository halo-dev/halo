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


// May contain unused imports in some cases
// @ts-ignore
import { Condition } from './condition';

/**
 * 
 * @export
 * @interface SinglePageStatus
 */
export interface SinglePageStatus {
    /**
     * 
     * @type {number}
     * @memberof SinglePageStatus
     */
    'commentsCount'?: number;
    /**
     * 
     * @type {Array<Condition>}
     * @memberof SinglePageStatus
     */
    'conditions'?: Array<Condition>;
    /**
     * 
     * @type {Array<string>}
     * @memberof SinglePageStatus
     */
    'contributors'?: Array<string>;
    /**
     * 
     * @type {string}
     * @memberof SinglePageStatus
     */
    'excerpt'?: string;
    /**
     * 
     * @type {boolean}
     * @memberof SinglePageStatus
     */
    'hideFromList'?: boolean;
    /**
     * 
     * @type {boolean}
     * @memberof SinglePageStatus
     */
    'inProgress'?: boolean;
    /**
     * 
     * @type {string}
     * @memberof SinglePageStatus
     */
    'lastModifyTime'?: string;
    /**
     * 
     * @type {number}
     * @memberof SinglePageStatus
     */
    'observedVersion'?: number;
    /**
     * 
     * @type {string}
     * @memberof SinglePageStatus
     */
    'permalink'?: string;
    /**
     * 
     * @type {string}
     * @memberof SinglePageStatus
     */
    'phase'?: string;
}

