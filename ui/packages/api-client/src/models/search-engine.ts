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


// May contain unused imports in some cases
// @ts-ignore
import { Metadata } from './metadata';
// May contain unused imports in some cases
// @ts-ignore
import { SearchEngineSpec } from './search-engine-spec';

/**
 * 
 * @export
 * @interface SearchEngine
 */
export interface SearchEngine {
    /**
     * 
     * @type {string}
     * @memberof SearchEngine
     */
    'apiVersion': string;
    /**
     * 
     * @type {string}
     * @memberof SearchEngine
     */
    'kind': string;
    /**
     * 
     * @type {Metadata}
     * @memberof SearchEngine
     */
    'metadata': Metadata;
    /**
     * 
     * @type {SearchEngineSpec}
     * @memberof SearchEngine
     */
    'spec': SearchEngineSpec;
}

