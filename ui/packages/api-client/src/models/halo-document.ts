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



/**
 * 
 * @export
 * @interface HaloDocument
 */
export interface HaloDocument {
    /**
     * 
     * @type {{ [key: string]: string; }}
     * @memberof HaloDocument
     */
    'annotations'?: { [key: string]: string; };
    /**
     * 
     * @type {Array<string>}
     * @memberof HaloDocument
     */
    'categories'?: Array<string>;
    /**
     * 
     * @type {string}
     * @memberof HaloDocument
     */
    'content': string;
    /**
     * 
     * @type {string}
     * @memberof HaloDocument
     */
    'creationTimestamp'?: string;
    /**
     * 
     * @type {string}
     * @memberof HaloDocument
     */
    'description'?: string;
    /**
     * 
     * @type {boolean}
     * @memberof HaloDocument
     */
    'exposed'?: boolean;
    /**
     * 
     * @type {string}
     * @memberof HaloDocument
     */
    'id': string;
    /**
     * 
     * @type {string}
     * @memberof HaloDocument
     */
    'metadataName': string;
    /**
     * 
     * @type {string}
     * @memberof HaloDocument
     */
    'ownerName': string;
    /**
     * 
     * @type {string}
     * @memberof HaloDocument
     */
    'permalink': string;
    /**
     * 
     * @type {boolean}
     * @memberof HaloDocument
     */
    'published'?: boolean;
    /**
     * 
     * @type {boolean}
     * @memberof HaloDocument
     */
    'recycled'?: boolean;
    /**
     * 
     * @type {Array<string>}
     * @memberof HaloDocument
     */
    'tags'?: Array<string>;
    /**
     * 
     * @type {string}
     * @memberof HaloDocument
     */
    'title': string;
    /**
     * 
     * @type {string}
     * @memberof HaloDocument
     */
    'type': string;
    /**
     * 
     * @type {string}
     * @memberof HaloDocument
     */
    'updateTimestamp'?: string;
}

