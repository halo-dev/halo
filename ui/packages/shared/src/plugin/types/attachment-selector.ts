import type { Attachment } from "@halo-dev/api-client";
import type { Component } from "vue";

/**
 * Represents a simplified attachment object with essential properties.
 * Used for lightweight attachment representations without full API client dependencies.
 */
export interface AttachmentSimple {
  /**
   * The URL of the attachment.
   */
  url: string;

  /**
   * The MIME type of the attachment (e.g., 'image/png', 'video/mp4').
   */
  mediaType?: string;

  /**
   * Alternative text for the attachment, used for accessibility.
   */
  alt?: string;

  /**
   * Caption or description for the attachment.
   */
  caption?: string;
}

/**
 * Union type representing different forms of attachment references.
 *
 * @remarks
 * This flexible type allows working with attachments in various formats:
 * - Full `Attachment` object from the API client
 * - Simplified `AttachmentSimple` object with basic properties
 * - String URL directly referencing the attachment
 */
export type AttachmentLike = Attachment | AttachmentSimple | string;

/**
 * Defines a custom attachment selector provider that plugins can register.
 *
 * @remarks
 * Attachment selector providers allow plugins to create custom UI components
 * for selecting attachments from different sources (e.g., local upload,
 * external services, galleries, etc.).
 */
export interface AttachmentSelectProvider {
  /**
   * Unique identifier for this attachment selector provider.
   * Should follow a namespaced pattern to avoid conflicts (e.g., 'plugin-name:selector-id').
   */
  id: string;

  /**
   * Display label for the attachment selector.
   * This label will be shown in the UI where users can choose between different selectors.
   */
  label: string;

  /**
   * The Vue component that implements the attachment selection interface.
   * Can be either a component definition or a component name string.
   *
   * @remarks
   * The component should emit selected attachments through the provided callback
   * or emit events that the parent component can handle.
   */
  component: Component | string;

  /**
   * Optional callback function invoked when attachments are selected.
   *
   * @param attachments - Array of selected attachments in various formats.
   *
   * @remarks
   * This callback is triggered after the user confirms their selection.
   * It allows the provider to handle post-selection logic like validation,
   * transformation, or triggering additional actions.
   */
  callback?: (attachments: AttachmentLike[]) => void;
}
