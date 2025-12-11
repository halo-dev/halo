import type { Component } from "vue";

/**
 * Defines a custom content editor provider.
 *
 * @remarks
 * Editor providers allow plugins to register alternative content editors beyond
 * the default editor. This enables support for different content formats, editing
 * experiences, and specialized workflows (e.g., Markdown, rich-text, code, visual builders).
 *
 * @example
 * ```typescript
 * const markdownEditor: EditorProvider = {
 *   name: 'markdown-editor',
 *   displayName: 'Markdown Editor',
 *   logo: '/path/to/markdown-logo.svg',
 *   component: MarkdownEditorComponent,
 *   rawType: 'markdown'
 * };
 * ```
 */
export interface EditorProvider {
  /**
   * Unique identifier for the editor provider.
   * Should use kebab-case naming (e.g., 'markdown-editor', 'wysiwyg-editor').
   */
  name: string;

  /**
   * Human-readable display name shown in the editor selection UI.
   */
  displayName: string;

  /**
   * Optional URL or path to the editor's logo image.
   * Displayed in the editor selection interface for visual identification.
   */
  logo?: string;

  /**
   * The Vue component that implements the editor interface.
   *
   * @remarks
   * The component should handle content editing, emit appropriate events for
   * content changes, and integrate with the parent form for validation and submission.
   */
  component: Component;

  /**
   * The content format or MIME type that this editor produces.
   *
   * @remarks
   * This identifier is stored with the content to indicate which editor should be
   * used when reopening for editing. Common values include:
   * - 'markdown' for Markdown content
   * - 'html' for HTML content
   * - 'json' for structured JSON data
   * - Custom identifiers for proprietary formats
   */
  rawType: string;
}
