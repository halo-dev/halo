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
import { ContentVo } from "./content-vo";
// May contain unused imports in some cases
// @ts-ignore
import { ContributorVo } from "./contributor-vo";
// May contain unused imports in some cases
// @ts-ignore
import { Metadata } from "./metadata";
// May contain unused imports in some cases
// @ts-ignore
import { SinglePageSpec } from "./single-page-spec";
// May contain unused imports in some cases
// @ts-ignore
import { SinglePageStatus } from "./single-page-status";
// May contain unused imports in some cases
// @ts-ignore
import { StatsVo } from "./stats-vo";

/**
 *
 * @export
 * @interface SinglePageVo
 */
export interface SinglePageVo {
  /**
   *
   * @type {ContentVo}
   * @memberof SinglePageVo
   */
  content?: ContentVo;
  /**
   *
   * @type {Array<ContributorVo>}
   * @memberof SinglePageVo
   */
  contributors?: Array<ContributorVo>;
  /**
   *
   * @type {Metadata}
   * @memberof SinglePageVo
   */
  metadata: Metadata;
  /**
   *
   * @type {ContributorVo}
   * @memberof SinglePageVo
   */
  owner?: ContributorVo;
  /**
   *
   * @type {SinglePageSpec}
   * @memberof SinglePageVo
   */
  spec?: SinglePageSpec;
  /**
   *
   * @type {StatsVo}
   * @memberof SinglePageVo
   */
  stats?: StatsVo;
  /**
   *
   * @type {SinglePageStatus}
   * @memberof SinglePageVo
   */
  status?: SinglePageStatus;
}
