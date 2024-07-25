/* tslint:disable */
/* eslint-disable */
/**
 * Halo
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 2.18.0-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


// May contain unused imports in some cases
// @ts-ignore
import { Category } from './category';
// May contain unused imports in some cases
// @ts-ignore
import { Contributor } from './contributor';
// May contain unused imports in some cases
// @ts-ignore
import { Post } from './post';
// May contain unused imports in some cases
// @ts-ignore
import { Stats } from './stats';
// May contain unused imports in some cases
// @ts-ignore
import { Tag } from './tag';

/**
 * A chunk of items.
 * @export
 * @interface ListedPost
 */
export interface ListedPost {
    /**
     * 
     * @type {Array<Category>}
     * @memberof ListedPost
     */
    'categories': Array<Category>;
    /**
     * 
     * @type {Array<Contributor>}
     * @memberof ListedPost
     */
    'contributors': Array<Contributor>;
    /**
     * 
     * @type {Contributor}
     * @memberof ListedPost
     */
    'owner': Contributor;
    /**
     * 
     * @type {Post}
     * @memberof ListedPost
     */
    'post': Post;
    /**
     * 
     * @type {Stats}
     * @memberof ListedPost
     */
    'stats': Stats;
    /**
     * 
     * @type {Array<Tag>}
     * @memberof ListedPost
     */
    'tags': Array<Tag>;
}

