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
import { ExtensionPointSpec } from './extension-point-spec';
// May contain unused imports in some cases
// @ts-ignore
import { Metadata } from './metadata';

/**
 * 
 * @export
 * @interface ExtensionPointDefinition
 */
export interface ExtensionPointDefinition {
    /**
     * 
     * @type {string}
     * @memberof ExtensionPointDefinition
     */
    'apiVersion': string;
    /**
     * 
     * @type {string}
     * @memberof ExtensionPointDefinition
     */
    'kind': string;
    /**
     * 
     * @type {Metadata}
     * @memberof ExtensionPointDefinition
     */
    'metadata': Metadata;
    /**
     * 
     * @type {ExtensionPointSpec}
     * @memberof ExtensionPointDefinition
     */
    'spec': ExtensionPointSpec;
}

