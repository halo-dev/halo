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
import { InterestReasonSubject } from './interest-reason-subject';

/**
 * The reason to be interested in
 * @export
 * @interface InterestReason
 */
export interface InterestReason {
    /**
     * The expression to be interested in
     * @type {string}
     * @memberof InterestReason
     */
    'expression'?: string;
    /**
     * The name of the reason definition to be interested in
     * @type {string}
     * @memberof InterestReason
     */
    'reasonType': string;
    /**
     * 
     * @type {InterestReasonSubject}
     * @memberof InterestReason
     */
    'subject': InterestReasonSubject;
}

