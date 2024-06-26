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
import { CommentSpec } from './comment-spec';
// May contain unused imports in some cases
// @ts-ignore
import { CommentStatsVo } from './comment-stats-vo';
// May contain unused imports in some cases
// @ts-ignore
import { CommentStatus } from './comment-status';
// May contain unused imports in some cases
// @ts-ignore
import { ListResultReplyVo } from './list-result-reply-vo';
// May contain unused imports in some cases
// @ts-ignore
import { Metadata } from './metadata';
// May contain unused imports in some cases
// @ts-ignore
import { OwnerInfo } from './owner-info';

/**
 * A chunk of items.
 * @export
 * @interface CommentWithReplyVo
 */
export interface CommentWithReplyVo {
    /**
     * 
     * @type {Metadata}
     * @memberof CommentWithReplyVo
     */
    'metadata': Metadata;
    /**
     * 
     * @type {OwnerInfo}
     * @memberof CommentWithReplyVo
     */
    'owner': OwnerInfo;
    /**
     * 
     * @type {ListResultReplyVo}
     * @memberof CommentWithReplyVo
     */
    'replies'?: ListResultReplyVo;
    /**
     * 
     * @type {CommentSpec}
     * @memberof CommentWithReplyVo
     */
    'spec': CommentSpec;
    /**
     * 
     * @type {CommentStatsVo}
     * @memberof CommentWithReplyVo
     */
    'stats': CommentStatsVo;
    /**
     * 
     * @type {CommentStatus}
     * @memberof CommentWithReplyVo
     */
    'status'?: CommentStatus;
}

