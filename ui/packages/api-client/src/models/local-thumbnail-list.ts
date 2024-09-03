/* tslint:disable */
/* eslint-disable */
/**
 * Halo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.20.0-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


// May contain unused imports in some cases
// @ts-ignore
import { LocalThumbnail } from './local-thumbnail';

/**
 * 
 * @export
 * @interface LocalThumbnailList
 */
export interface LocalThumbnailList {
    /**
     * Indicates whether current page is the first page.
     * @type {boolean}
     * @memberof LocalThumbnailList
     */
    'first': boolean;
    /**
     * Indicates whether current page has previous page.
     * @type {boolean}
     * @memberof LocalThumbnailList
     */
    'hasNext': boolean;
    /**
     * Indicates whether current page has previous page.
     * @type {boolean}
     * @memberof LocalThumbnailList
     */
    'hasPrevious': boolean;
    /**
     * A chunk of items.
     * @type {Array<LocalThumbnail>}
     * @memberof LocalThumbnailList
     */
    'items': Array<LocalThumbnail>;
    /**
     * Indicates whether current page is the last page.
     * @type {boolean}
     * @memberof LocalThumbnailList
     */
    'last': boolean;
    /**
     * Page number, starts from 1. If not set or equal to 0, it means no pagination.
     * @type {number}
     * @memberof LocalThumbnailList
     */
    'page': number;
    /**
     * Size of each page. If not set or equal to 0, it means no pagination.
     * @type {number}
     * @memberof LocalThumbnailList
     */
    'size': number;
    /**
     * Total elements.
     * @type {number}
     * @memberof LocalThumbnailList
     */
    'total': number;
    /**
     * Indicates total pages.
     * @type {number}
     * @memberof LocalThumbnailList
     */
    'totalPages': number;
}

