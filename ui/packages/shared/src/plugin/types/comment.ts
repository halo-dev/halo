import type { Extension } from "@halo-dev/api-client";
import type { Component, Raw } from "vue";
import type { RouteLocationRaw } from "vue-router";

/**
 * Represents the result of resolving a comment subject reference.
 * Contains display information and navigation details for the commented content.
 */
export interface CommentSubjectRefResult {
  /**
   * Short label or type identifier for the subject (e.g., 'Post', 'Page', 'Custom').
   */
  label: string;

  /**
   * The title or name of the content being commented on.
   */
  title: string;

  /**
   * Internal route location to navigate to the subject.
   * Used for navigating within the Halo console.
   */
  route?: RouteLocationRaw;

  /**
   * External URL to the subject if it's hosted outside the console.
   * Typically used for public-facing content.
   */
  externalUrl?: string;
}

/**
 * Defines a provider that resolves comment subject references.
 *
 * @remarks
 * Comment subject reference providers allow plugins to make their custom content types
 * commentable. The provider resolves extension references into human-readable information
 * that can be displayed in the comment management interface.
 */
export type CommentSubjectRefProvider = {
  /**
   * The kind of the extension that this provider handles.
   * Must match the `kind` field in the extension's metadata.
   */
  kind: string;

  /**
   * The API group of the extension that this provider handles.
   * Must match the `group` field in the extension's metadata.
   */
  group: string;

  /**
   * Resolves an extension reference into displayable comment subject information.
   *
   * @param subject - The extension object representing the commented content.
   * @returns The resolved subject information including label, title, and navigation details.
   */
  resolve: (subject: Extension) => CommentSubjectRefResult;
};

/**
 * Defines a custom comment editor provider.
 *
 * @remarks
 * Allows plugins to replace the default comment editor with a custom implementation.
 * Useful for providing rich-text editing, markdown support, or specialized input methods.
 */
export interface CommentEditorProvider {
  /**
   * The Vue component that implements the custom comment editor.
   * Must be wrapped with `markRaw` to prevent Vue from making it reactive.
   *
   * @remarks
   * The component should emit appropriate events for content changes and
   * integrate with the parent form for submission handling.
   */
  component: Raw<Component>;
}

/**
 * Defines a custom comment content renderer.
 *
 * @remarks
 * Allows plugins to customize how comment content is displayed in comment lists.
 * Useful when comments are stored in a custom format that requires special rendering.
 */
export interface CommentContentProvider {
  /**
   * The Vue component that renders the comment content.
   * Must be wrapped with `markRaw` to prevent Vue from making it reactive.
   *
   * @remarks
   * The component receives the comment content as props and should handle
   * rendering, sanitization, and any interactive features.
   */
  component: Raw<Component>;
}
