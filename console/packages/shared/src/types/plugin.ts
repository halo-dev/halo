import type { Component } from "vue";
import type { RouteRecordRaw, RouteRecordName } from "vue-router";
import type { FunctionalPage } from "../states/pages";
import type { AttachmentSelectProvider } from "../states/attachment-selector";
import type { EditorProvider } from "..";
import type { CommentSubjectRefProvider } from "@/states/comment-subject-ref";

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

  "comment:subject-ref:create"?: () => CommentSubjectRefProvider[];
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
