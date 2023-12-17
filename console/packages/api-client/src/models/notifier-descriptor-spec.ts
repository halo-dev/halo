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
import { NotifierSettingRef } from "./notifier-setting-ref";

/**
 *
 * @export
 * @interface NotifierDescriptorSpec
 */
export interface NotifierDescriptorSpec {
  /**
   *
   * @type {string}
   * @memberof NotifierDescriptorSpec
   */
  description?: string;
  /**
   *
   * @type {string}
   * @memberof NotifierDescriptorSpec
   */
  displayName: string;
  /**
   *
   * @type {string}
   * @memberof NotifierDescriptorSpec
   */
  notifierExtName: string;
  /**
   *
   * @type {NotifierSettingRef}
   * @memberof NotifierDescriptorSpec
   */
  receiverSettingRef?: NotifierSettingRef;
  /**
   *
   * @type {NotifierSettingRef}
   * @memberof NotifierDescriptorSpec
   */
  senderSettingRef?: NotifierSettingRef;
}
