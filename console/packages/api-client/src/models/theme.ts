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
import { Metadata } from './metadata'
// May contain unused imports in some cases
// @ts-ignore
import { ThemeSpec } from './theme-spec'
// May contain unused imports in some cases
// @ts-ignore
import { ThemeStatus } from './theme-status'

/**
 *
 * @export
 * @interface Theme
 */
export interface Theme {
  /**
   *
   * @type {ThemeSpec}
   * @memberof Theme
   */
  spec: ThemeSpec
  /**
   *
   * @type {ThemeStatus}
   * @memberof Theme
   */
  status?: ThemeStatus
  /**
   *
   * @type {string}
   * @memberof Theme
   */
  apiVersion: string
  /**
   *
   * @type {string}
   * @memberof Theme
   */
  kind: string
  /**
   *
   * @type {Metadata}
   * @memberof Theme
   */
  metadata: Metadata
}
