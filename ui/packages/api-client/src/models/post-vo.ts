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
import { CategoryVo } from './category-vo';
// May contain unused imports in some cases
// @ts-ignore
import { ContentVo } from './content-vo';
// May contain unused imports in some cases
// @ts-ignore
import { ContributorVo } from './contributor-vo';
// May contain unused imports in some cases
// @ts-ignore
import { Metadata } from './metadata';
// May contain unused imports in some cases
// @ts-ignore
import { PostSpec } from './post-spec';
// May contain unused imports in some cases
// @ts-ignore
import { PostStatus } from './post-status';
// May contain unused imports in some cases
// @ts-ignore
import { StatsVo } from './stats-vo';
// May contain unused imports in some cases
// @ts-ignore
import { TagVo } from './tag-vo';

/**
 * 
 * @export
 * @interface PostVo
 */
export interface PostVo {
    /**
     * 
     * @type {Array<CategoryVo>}
     * @memberof PostVo
     */
    'categories'?: Array<CategoryVo>;
    /**
     * 
     * @type {ContentVo}
     * @memberof PostVo
     */
    'content'?: ContentVo;
    /**
     * 
     * @type {Array<ContributorVo>}
     * @memberof PostVo
     */
    'contributors'?: Array<ContributorVo>;
    /**
     * 
     * @type {Metadata}
     * @memberof PostVo
     */
    'metadata': Metadata;
    /**
     * 
     * @type {ContributorVo}
     * @memberof PostVo
     */
    'owner'?: ContributorVo;
    /**
     * 
     * @type {PostSpec}
     * @memberof PostVo
     */
    'spec'?: PostSpec;
    /**
     * 
     * @type {StatsVo}
     * @memberof PostVo
     */
    'stats'?: StatsVo;
    /**
     * 
     * @type {PostStatus}
     * @memberof PostVo
     */
    'status'?: PostStatus;
    /**
     * 
     * @type {Array<TagVo>}
     * @memberof PostVo
     */
    'tags'?: Array<TagVo>;
}

