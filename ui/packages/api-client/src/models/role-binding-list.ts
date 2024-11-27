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
import { RoleBinding } from './role-binding';

/**
 * 
 * @export
 * @interface RoleBindingList
 */
export interface RoleBindingList {
    /**
     * Indicates whether current page is the first page.
     * @type {boolean}
     * @memberof RoleBindingList
     */
    'first': boolean;
    /**
     * Indicates whether current page has previous page.
     * @type {boolean}
     * @memberof RoleBindingList
     */
    'hasNext': boolean;
    /**
     * Indicates whether current page has previous page.
     * @type {boolean}
     * @memberof RoleBindingList
     */
    'hasPrevious': boolean;
    /**
     * A chunk of items.
     * @type {Array<RoleBinding>}
     * @memberof RoleBindingList
     */
    'items': Array<RoleBinding>;
    /**
     * Indicates whether current page is the last page.
     * @type {boolean}
     * @memberof RoleBindingList
     */
    'last': boolean;
    /**
     * Page number, starts from 1. If not set or equal to 0, it means no pagination.
     * @type {number}
     * @memberof RoleBindingList
     */
    'page': number;
    /**
     * Size of each page. If not set or equal to 0, it means no pagination.
     * @type {number}
     * @memberof RoleBindingList
     */
    'size': number;
    /**
     * Total elements.
     * @type {number}
     * @memberof RoleBindingList
     */
    'total': number;
    /**
     * Indicates total pages.
     * @type {number}
     * @memberof RoleBindingList
     */
    'totalPages': number;
}

