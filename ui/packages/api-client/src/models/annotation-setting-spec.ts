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
import { GroupKind } from './group-kind';

/**
 * 
 * @export
 * @interface AnnotationSettingSpec
 */
export interface AnnotationSettingSpec {
    /**
     * 
     * @type {Array<object>}
     * @memberof AnnotationSettingSpec
     */
    'formSchema': Array<object>;
    /**
     * 
     * @type {GroupKind}
     * @memberof AnnotationSettingSpec
     */
    'targetRef': GroupKind;
}

