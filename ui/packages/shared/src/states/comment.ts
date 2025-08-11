import type { Component, Raw } from "vue";

export interface CommentEditorProvider {
  component: Raw<Component>;
}

export interface CommentContentProvider {
  component: Raw<Component>;
}
