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


// May contain unused imports in some cases
// @ts-ignore
import { Metadata } from './metadata';
// May contain unused imports in some cases
// @ts-ignore
import { ReplySpec } from './reply-spec';
// May contain unused imports in some cases
// @ts-ignore
import { ReplyStatus } from './reply-status';

/**
 * 
 * @export
 * @interface Reply
 */
export interface Reply {
    /**
     * 
     * @type {string}
     * @memberof Reply
     */
    'apiVersion': string;
    /**
     * 
     * @type {string}
     * @memberof Reply
     */
    'kind': string;
    /**
     * 
     * @type {Metadata}
     * @memberof Reply
     */
    'metadata': Metadata;
    /**
     * 
     * @type {ReplySpec}
     * @memberof Reply
     */
    'spec': ReplySpec;
    /**
     * 
     * @type {ReplyStatus}
     * @memberof Reply
     */
    'status': ReplyStatus;
}

