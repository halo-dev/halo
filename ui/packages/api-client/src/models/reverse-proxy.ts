/* tslint:disable */
/* eslint-disable */
/**
 * Halo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.20.10-SNAPSHOT
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
import { ReverseProxyRule } from './reverse-proxy-rule';

/**
 * 
 * @export
 * @interface ReverseProxy
 */
export interface ReverseProxy {
    /**
     * 
     * @type {string}
     * @memberof ReverseProxy
     */
    'apiVersion': string;
    /**
     * 
     * @type {string}
     * @memberof ReverseProxy
     */
    'kind': string;
    /**
     * 
     * @type {Metadata}
     * @memberof ReverseProxy
     */
    'metadata': Metadata;
    /**
     * 
     * @type {Array<ReverseProxyRule>}
     * @memberof ReverseProxy
     */
    'rules'?: Array<ReverseProxyRule>;
}

