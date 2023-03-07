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
import { Ref } from "./ref";

/**
 * The spec of menu item.
 * @export
 * @interface MenuItemSpec
 */
export interface MenuItemSpec {
  /**
   * Children of this menu item
   * @type {Array<string>}
   * @memberof MenuItemSpec
   */
  children?: Array<string>;
  /**
   * The display name of menu item.
   * @type {string}
   * @memberof MenuItemSpec
   */
  displayName?: string;
  /**
   * The href of this menu item.
   * @type {string}
   * @memberof MenuItemSpec
   */
  href?: string;
  /**
   * The priority is for ordering.
   * @type {number}
   * @memberof MenuItemSpec
   */
  priority?: number;
  /**
   * The <a> target attribute of this menu item.
   * @type {string}
   * @memberof MenuItemSpec
   */
  target?: MenuItemSpecTargetEnum;
  /**
   *
   * @type {Ref}
   * @memberof MenuItemSpec
   */
  targetRef?: Ref;
}

export const MenuItemSpecTargetEnum = {
  Blank: "_blank",
  Self: "_self",
  Parent: "_parent",
  Top: "_top",
} as const;

export type MenuItemSpecTargetEnum =
  typeof MenuItemSpecTargetEnum[keyof typeof MenuItemSpecTargetEnum];
