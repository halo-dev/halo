/* tslint:disable */
/* eslint-disable */
/**
 * Halo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.17.0-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


// May contain unused imports in some cases
// @ts-ignore
import { ExtensionDefinition } from './extension-definition';

/**
 * 
 * @export
 * @interface ExtensionDefinitionList
 */
export interface ExtensionDefinitionList {
    /**
     * Indicates whether current page is the first page.
     * @type {boolean}
     * @memberof ExtensionDefinitionList
     */
    'first': boolean;
    /**
     * Indicates whether current page has previous page.
     * @type {boolean}
     * @memberof ExtensionDefinitionList
     */
    'hasNext': boolean;
    /**
     * Indicates whether current page has previous page.
     * @type {boolean}
     * @memberof ExtensionDefinitionList
     */
    'hasPrevious': boolean;
    /**
     * A chunk of items.
     * @type {Array<ExtensionDefinition>}
     * @memberof ExtensionDefinitionList
     */
    'items': Array<ExtensionDefinition>;
    /**
     * Indicates whether current page is the last page.
     * @type {boolean}
     * @memberof ExtensionDefinitionList
     */
    'last': boolean;
    /**
     * Page number, starts from 1. If not set or equal to 0, it means no pagination.
     * @type {number}
     * @memberof ExtensionDefinitionList
     */
    'page': number;
    /**
     * Size of each page. If not set or equal to 0, it means no pagination.
     * @type {number}
     * @memberof ExtensionDefinitionList
     */
    'size': number;
    /**
     * Total elements.
     * @type {number}
     * @memberof ExtensionDefinitionList
     */
    'total': number;
    /**
     * Indicates total pages.
     * @type {number}
     * @memberof ExtensionDefinitionList
     */
    'totalPages': number;
}

