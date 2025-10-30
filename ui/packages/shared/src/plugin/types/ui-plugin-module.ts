import type {
  Attachment,
  Backup,
  ListedComment,
  ListedPost,
  ListedReply,
  ListedSinglePage,
  Plugin,
  Theme,
} from "@halo-dev/api-client";
import type { AnyExtension } from "@halo-dev/richtext-editor";
import type { Component, Ref } from "vue";
import type { RouteRecordName, RouteRecordRaw } from "vue-router";
import type { AttachmentSelectProvider } from "./attachment-selector";
import type { BackupTab } from "./backup";
import type {
  CommentContentProvider,
  CommentEditorProvider,
  CommentSubjectRefProvider,
} from "./comment";
import type {
  DashboardWidgetDefinition,
  DashboardWidgetQuickActionItem,
} from "./dashboard-widget";
import type { EditorProvider } from "./editor-provider";
import type { EntityFieldItem } from "./list-entity-field";
import type { OperationItem } from "./list-operation";
import type { PluginInstallationTab } from "./plugin-installation-tab";
import type { PluginTab } from "./plugin-tab";
import type { ThemeListTab } from "./theme-list-tab";
import type { UserProfileTab, UserTab } from "./user-tab";

/**
 * Represents a route record that will be appended to a parent route.
 * Used to extend existing routes with additional child routes.
 */
export interface RouteRecordAppend {
  /**
   * The name of the parent route to which this route will be appended.
   */
  parentName: NonNullable<RouteRecordName>;

  /**
   * The route definition to be appended as a child route.
   */
  route: RouteRecordRaw;
}

/**
 * Defines extension points that plugins can hook into to extend Halo's functionality.
 * Extension points follow a naming convention: `<feature>:<action>:<operation>`.
 *
 * @remarks
 * Extension points are the primary mechanism for plugins to integrate with the Halo console and uc.
 * Each extension point represents a specific place where plugins can inject custom functionality.
 */
export interface ExtensionPoint {
  /**
   * Creates custom attachment selector providers.
   * Allows plugins to provide alternative attachment selection interfaces.
   *
   * @returns An array of attachment select providers or a promise resolving to them.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/attachment-selector-create
   */
  "attachment:selector:create"?: () =>
    | AttachmentSelectProvider[]
    | Promise<AttachmentSelectProvider[]>;

  /**
   * Creates custom editor providers.
   * Allows plugins to register custom content editors.
   *
   * @returns An array of editor providers or a promise resolving to them.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/editor-create
   */
  "editor:create"?: () => EditorProvider[] | Promise<EditorProvider[]>;

  /**
   * Creates tabs for the plugin's own settings page.
   * Used to add configuration tabs within a plugin's detail view.
   *
   * @returns An array of plugin tabs or a promise resolving to them.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/plugin-self-tabs-create
   */
  "plugin:self:tabs:create"?: () => PluginTab[] | Promise<PluginTab[]>;

  /**
   * Creates extensions for the default rich-text editor.
   * Allows plugins to extend the editor with custom nodes, marks, or functionality.
   *
   * @returns An array of editor extensions or a promise resolving to them.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/default-editor-extension-create
   */
  "default:editor:extension:create"?: () =>
    | AnyExtension[]
    | Promise<AnyExtension[]>;

  /**
   * Creates comment subject reference providers.
   * Allows plugins to define custom content types that can receive comments.
   *
   * @returns An array of comment subject reference providers.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/comment-subject-ref-create
   */
  "comment:subject-ref:create"?: () => CommentSubjectRefProvider[];

  /**
   * Replaces the default comment editor.
   * Allows plugins to provide a custom comment editing interface.
   *
   * @returns A comment editor provider or a promise resolving to it.
   */
  "comment:editor:replace"?: () =>
    | CommentEditorProvider
    | Promise<CommentEditorProvider>;

  /**
   * Replaces the default comment list item content display.
   * Allows plugins to customize how comment content is rendered in lists.
   *
   * @returns A comment content provider or a promise resolving to it.
   */
  "comment:list-item:content:replace"?: () =>
    | CommentContentProvider
    | Promise<CommentContentProvider>;

  /**
   * Creates tabs for the backup management page.
   * Allows plugins to add custom backup-related functionality.
   *
   * @returns An array of backup tabs or a promise resolving to them.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/backup-tabs-create
   */
  "backup:tabs:create"?: () => BackupTab[] | Promise<BackupTab[]>;

  /**
   * Creates tabs for the plugin installation page.
   * Allows plugins to add custom installation methods or configurations.
   *
   * @returns An array of plugin installation tabs or a promise resolving to them.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/plugin-installation-tabs-create
   */
  "plugin:installation:tabs:create"?: () =>
    | PluginInstallationTab[]
    | Promise<PluginInstallationTab[]>;

  /**
   * Creates custom operations for post list items.
   * Allows plugins to add action buttons or menu items to post list rows.
   *
   * @param post - A reactive reference to the post object.
   * @returns An array of operation items for the post.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/post-list-item-operation-create
   */
  "post:list-item:operation:create"?: (
    post: Ref<ListedPost>
  ) => OperationItem<ListedPost>[];

  /**
   * Creates custom operations for single page list items.
   * Allows plugins to add action buttons or menu items to single page list rows.
   *
   * @param singlePage - A reactive reference to the single page object.
   * @returns An array of operation items for the single page.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/single-page-list-item-operation-create
   */
  "single-page:list-item:operation:create"?: (
    singlePage: Ref<ListedSinglePage>
  ) => OperationItem<ListedSinglePage>[];

  /**
   * Creates custom operations for comment list items.
   * Allows plugins to add action buttons or menu items to comment list rows.
   *
   * @param comment - A reactive reference to the comment object.
   * @returns An array of operation items for the comment.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/comment-list-item-operation-create
   */
  "comment:list-item:operation:create"?: (
    comment: Ref<ListedComment>
  ) => OperationItem<ListedComment>[];

  /**
   * Creates custom operations for reply list items.
   * Allows plugins to add action buttons or menu items to reply list rows.
   *
   * @param reply - A reactive reference to the reply object.
   * @returns An array of operation items for the reply.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/reply-list-item-operation-create
   */
  "reply:list-item:operation:create"?: (
    reply: Ref<ListedReply>
  ) => OperationItem<ListedReply>[];

  /**
   * Creates custom operations for plugin list items.
   * Allows plugins to add action buttons or menu items to plugin list rows.
   *
   * @param plugin - A reactive reference to the plugin object.
   * @returns An array of operation items for the plugin.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/plugin-list-item-operation-create
   */
  "plugin:list-item:operation:create"?: (
    plugin: Ref<Plugin>
  ) => OperationItem<Plugin>[];

  /**
   * Creates custom operations for backup list items.
   * Allows plugins to add action buttons or menu items to backup list rows.
   *
   * @param backup - A reactive reference to the backup object.
   * @returns An array of operation items for the backup.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/backup-list-item-operation-create
   */
  "backup:list-item:operation:create"?: (
    backup: Ref<Backup>
  ) => OperationItem<Backup>[];

  /**
   * Creates custom operations for attachment list items.
   * Allows plugins to add action buttons or menu items to attachment list rows.
   *
   * @param attachment - A reactive reference to the attachment object.
   * @returns An array of operation items for the attachment.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/attachment-list-item-operation-create
   */
  "attachment:list-item:operation:create"?: (
    attachment: Ref<Attachment>
  ) => OperationItem<Attachment>[];

  /**
   * Creates custom fields for plugin list items.
   * Allows plugins to add custom columns or metadata fields to the plugin list display.
   *
   * @param plugin - A reactive reference to the plugin object.
   * @returns An array of entity field items for the plugin.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/plugin-list-item-field-create
   */
  "plugin:list-item:field:create"?: (plugin: Ref<Plugin>) => EntityFieldItem[];

  /**
   * Creates custom fields for post list items.
   * Allows plugins to add custom columns or metadata fields to the post list display.
   *
   * @param post - A reactive reference to the post object.
   * @returns An array of entity field items for the post.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/post-list-item-field-create
   */
  "post:list-item:field:create"?: (post: Ref<ListedPost>) => EntityFieldItem[];

  /**
   * Creates custom fields for single page list items.
   * Allows plugins to add custom columns or metadata fields to the single page list display.
   *
   * @param singlePage - A reactive reference to the single page object.
   * @returns An array of entity field items for the single page.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/single-page-list-item-field-create
   */
  "single-page:list-item:field:create"?: (
    singlePage: Ref<ListedSinglePage>
  ) => EntityFieldItem[];

  /**
   * Creates tabs for the theme list page.
   * Allows plugins to add custom theme management or configuration sections.
   *
   * @returns An array of theme list tabs or a promise resolving to them.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/theme-list-tabs-create
   */
  "theme:list:tabs:create"?: () => ThemeListTab[] | Promise<ThemeListTab[]>;

  /**
   * Creates custom operations for theme list items.
   * Allows plugins to add action buttons or menu items to theme list rows.
   *
   * @param theme - A reactive reference to the theme object.
   * @returns An array of operation items for the theme.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/theme-list-item-operation-create
   */
  "theme:list-item:operation:create"?: (
    theme: Ref<Theme>
  ) => OperationItem<Theme>[];

  /**
   * Creates tabs for the user detail page in the console.
   * Allows plugins to add custom user management sections.
   *
   * @returns An array of user tabs or a promise resolving to them.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/user-detail-tabs-create
   */
  "user:detail:tabs:create"?: () => UserTab[] | Promise<UserTab[]>;

  /**
   * Creates tabs for the user profile page in the user center.
   * Allows plugins to extend user profile settings and information.
   *
   * @returns An array of user profile tabs or a promise resolving to them.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/uc-user-profile-tabs-create
   */
  "uc:user:profile:tabs:create"?: () =>
    | UserProfileTab[]
    | Promise<UserProfileTab[]>;

  /**
   * Creates custom dashboard widgets for the console.
   * Allows plugins to add informational or interactive widgets to the main dashboard.
   *
   * @returns An array of dashboard widget definitions or a promise resolving to them.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/dashboard-widgets#consoledashboardwidgets
   */
  "console:dashboard:widgets:create"?: () =>
    | DashboardWidgetDefinition[]
    | Promise<DashboardWidgetDefinition[]>;

  /**
   * Creates quick action items for internal dashboard widgets.
   * Allows plugins to add quick access buttons or shortcuts to dashboard widgets.
   *
   * @returns An array of dashboard widget quick action items or a promise resolving to them.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui/dashboard-widgets#consoledashboardwidgetsinternalquick-actionitem
   */
  "console:dashboard:widgets:internal:quick-action:item:create"?: () =>
    | DashboardWidgetQuickActionItem[]
    | Promise<DashboardWidgetQuickActionItem[]>;
}

/**
 * Defines the structure of a UI plugin module for the Halo console and user center.
 *
 * @see https://docs.halo.run/developer-guide/plugin/basics/ui/entry
 */
export interface PluginModule {
  /**
   * Components that will be globally registered when the plugin is activated.
   * These components can be used throughout the Halo console without explicit imports.
   *
   * @remarks
   * The key is the component name (in kebab-case is recommended),
   * and the value is the Vue component definition.
   */
  components?: Record<string, Component>;

  /**
   * Console routes that will be registered when the plugin is activated.
   * Can be either standalone routes or routes that append to existing parent routes.
   *
   * @remarks
   * - Use `RouteRecordRaw[]` for standalone routes
   * - Use `RouteRecordAppend[]` to append routes to existing parent routes
   * @see https://docs.halo.run/developer-guide/plugin/api-reference/ui/route
   */
  routes?: RouteRecordRaw[] | RouteRecordAppend[];

  /**
   * User center routes that will be registered when the plugin is activated.
   * These routes are specifically for the user-facing area (UC).
   *
   * @remarks
   * - Use `RouteRecordRaw[]` for standalone routes
   * - Use `RouteRecordAppend[]` to append routes to existing parent routes
   * @see https://docs.halo.run/developer-guide/plugin/api-reference/ui/route
   */
  ucRoutes?: RouteRecordRaw[] | RouteRecordAppend[];

  /**
   * Extension points that the plugin hooks into.
   * This is where plugins register their custom functionality to extend Halo.
   *
   * @remarks
   * Each extension point can provide custom functionality that integrates
   * with specific parts of the Halo console.
   * @see https://docs.halo.run/developer-guide/plugin/extension-points/ui
   */
  extensionPoints?: ExtensionPoint;
}
