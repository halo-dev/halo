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
import { User } from './user';

/**
 * 
 * @export
 * @interface SignUpRequest
 */
export interface SignUpRequest {
    /**
     * 
     * @type {string}
     * @memberof SignUpRequest
     */
    'password': string;
    /**
     * 
     * @type {User}
     * @memberof SignUpRequest
     */
    'user': User;
    /**
     * 
     * @type {string}
     * @memberof SignUpRequest
     */
    'verifyCode'?: string;
}

