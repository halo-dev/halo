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
import { Ref } from './ref';

/**
 * 
 * @export
 * @interface SnapShotSpec
 */
export interface SnapShotSpec {
    /**
     * 
     * @type {string}
     * @memberof SnapShotSpec
     */
    'contentPatch'?: string;
    /**
     * 
     * @type {Array<string>}
     * @memberof SnapShotSpec
     */
    'contributors'?: Array<string>;
    /**
     * 
     * @type {string}
     * @memberof SnapShotSpec
     */
    'lastModifyTime'?: string;
    /**
     * 
     * @type {string}
     * @memberof SnapShotSpec
     */
    'owner': string;
    /**
     * 
     * @type {string}
     * @memberof SnapShotSpec
     */
    'parentSnapshotName'?: string;
    /**
     * 
     * @type {string}
     * @memberof SnapShotSpec
     */
    'rawPatch'?: string;
    /**
     * 
     * @type {string}
     * @memberof SnapShotSpec
     */
    'rawType': string;
    /**
     * 
     * @type {Ref}
     * @memberof SnapShotSpec
     */
    'subjectRef': Ref;
}

