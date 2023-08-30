import type { Component } from "vue";
import type { RouteRecordRaw, RouteRecordName } from "vue-router";
import type { FunctionalPage } from "../states/pages";
import type { AttachmentSelectProvider } from "../states/attachment-selector";
import type { EditorProvider, PluginTab } from "..";
import type { AnyExtension } from "@tiptap/vue-3";
import type { CommentSubjectRefProvider } from "@/states/comment-subject-ref";
import type { BackupTab } from "@/states/backup";
import type { PluginInstallationTab } from "@/states/plugin-installation-tabs";
import type { EntityDropdownItem } from "@/states/entity";
import type { ThemeListTab } from "@/states/theme-list-tabs";
import type { Backup, ListedPost, Plugin } from "@halo-dev/api-client";

export interface RouteRecordAppend {
  parentName: RouteRecordName;
  route: RouteRecordRaw;
}

export interface ExtensionPoint {
  // @deprecated
  "page:functional:create"?: () => FunctionalPage[] | Promise<FunctionalPage[]>;

  "attachment:selector:create"?: () =>
    | AttachmentSelectProvider[]
    | Promise<AttachmentSelectProvider[]>;

  "editor:create"?: () => EditorProvider[] | Promise<EditorProvider[]>;

  "plugin:self:tabs:create"?: () => PluginTab[] | Promise<PluginTab[]>;

  "default:editor:extension:create"?: () =>
    | AnyExtension[]
    | Promise<AnyExtension[]>;

  "comment:subject-ref:create"?: () => CommentSubjectRefProvider[];

  "backup:tabs:create"?: () => BackupTab[] | Promise<BackupTab[]>;

  "plugin:installation:tabs:create"?: () =>
    | PluginInstallationTab[]
    | Promise<PluginInstallationTab[]>;

  "post:list-item:operation:create"?: () =>
    | EntityDropdownItem<ListedPost>[]
    | Promise<EntityDropdownItem<ListedPost>[]>;

  "plugin:list-item:operation:create"?: () =>
    | EntityDropdownItem<Plugin>[]
    | Promise<EntityDropdownItem<Plugin>[]>;

  "backup:list-item:operation:create"?: () =>
    | EntityDropdownItem<Backup>[]
    | Promise<EntityDropdownItem<Backup>[]>;

  "theme:list:tabs:create"?: () => ThemeListTab[] | Promise<ThemeListTab[]>;
}

export interface PluginModule {
  /**
   * These components will be registered when plugin is activated.
   */
  components?: Record<string, Component>;

  /**
   * Activate hook will be called when plugin is activated.
   */
  activated?: () => void;

  /**
   * Deactivate hook will be called when plugin is deactivated.
   */
  deactivated?: () => void;

  routes?: RouteRecordRaw[] | RouteRecordAppend[];

  extensionPoints?: ExtensionPoint;
}
