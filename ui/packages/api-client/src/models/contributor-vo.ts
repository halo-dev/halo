/* tslint:disable */
/* eslint-disable */
/**
 * Halo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.17.0-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


// May contain unused imports in some cases
// @ts-ignore
import { Metadata } from './metadata';

/**
 * 
 * @export
 * @interface ContributorVo
 */
export interface ContributorVo {
    /**
     * 
     * @type {string}
     * @memberof ContributorVo
     */
    'avatar'?: string;
    /**
     * 
     * @type {string}
     * @memberof ContributorVo
     */
    'bio'?: string;
    /**
     * 
     * @type {string}
     * @memberof ContributorVo
     */
    'displayName'?: string;
    /**
     * 
     * @type {Metadata}
     * @memberof ContributorVo
     */
    'metadata': Metadata;
    /**
     * 
     * @type {string}
     * @memberof ContributorVo
     */
    'name'?: string;
    /**
     * 
     * @type {string}
     * @memberof ContributorVo
     */
    'permalink'?: string;
}

