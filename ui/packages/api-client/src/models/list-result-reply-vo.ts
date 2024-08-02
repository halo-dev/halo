/* tslint:disable */
/* eslint-disable */
/**
 * Halo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.19.0-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


// May contain unused imports in some cases
// @ts-ignore
import { ReplyVo } from './reply-vo';

/**
 * 
 * @export
 * @interface ListResultReplyVo
 */
export interface ListResultReplyVo {
    /**
     * Indicates whether current page is the first page.
     * @type {boolean}
     * @memberof ListResultReplyVo
     */
    'first': boolean;
    /**
     * Indicates whether current page has previous page.
     * @type {boolean}
     * @memberof ListResultReplyVo
     */
    'hasNext': boolean;
    /**
     * Indicates whether current page has previous page.
     * @type {boolean}
     * @memberof ListResultReplyVo
     */
    'hasPrevious': boolean;
    /**
     * A chunk of items.
     * @type {Array<ReplyVo>}
     * @memberof ListResultReplyVo
     */
    'items': Array<ReplyVo>;
    /**
     * Indicates whether current page is the last page.
     * @type {boolean}
     * @memberof ListResultReplyVo
     */
    'last': boolean;
    /**
     * Page number, starts from 1. If not set or equal to 0, it means no pagination.
     * @type {number}
     * @memberof ListResultReplyVo
     */
    'page': number;
    /**
     * Size of each page. If not set or equal to 0, it means no pagination.
     * @type {number}
     * @memberof ListResultReplyVo
     */
    'size': number;
    /**
     * Total elements.
     * @type {number}
     * @memberof ListResultReplyVo
     */
    'total': number;
    /**
     * Indicates total pages.
     * @type {number}
     * @memberof ListResultReplyVo
     */
    'totalPages': number;
}

