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
// May contain unused imports in some cases
// @ts-ignore
import { PolicyTemplateSpec } from './policy-template-spec';

/**
 * 
 * @export
 * @interface PolicyTemplate
 */
export interface PolicyTemplate {
    /**
     * 
     * @type {string}
     * @memberof PolicyTemplate
     */
    'apiVersion': string;
    /**
     * 
     * @type {string}
     * @memberof PolicyTemplate
     */
    'kind': string;
    /**
     * 
     * @type {Metadata}
     * @memberof PolicyTemplate
     */
    'metadata': Metadata;
    /**
     * 
     * @type {PolicyTemplateSpec}
     * @memberof PolicyTemplate
     */
    'spec'?: PolicyTemplateSpec;
}

