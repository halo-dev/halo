import type {
  EditorState,
  EditorView,
  Node,
  ResolvedPos,
  Selection,
  Slice,
} from "@/tiptap/pm";
import type { Editor, Range } from "@/tiptap/vue-3";
import type { Component } from "vue";
export interface ToolbarItemType {
  priority: number;
  component: Component;
  props: {
    editor: Editor;
    isActive: boolean;
    disabled?: boolean;
    icon?: Component;
    title?: string;
    action?: () => void;
  };
  children?: ToolbarItemType[];
}

interface BubbleMenuProps {
  pluginKey?: string;
  editor?: Editor;
  shouldShow: (props: {
    editor: Editor;
    state: EditorState;
    node?: HTMLElement;
    view?: EditorView;
    oldState?: EditorState;
    from?: number;
    to?: number;
  }) => boolean;
  tippyOptions?: Record<string, unknown>;
  getRenderContainer?: (node: HTMLElement) => HTMLElement;
  defaultAnimation?: boolean;
}

export interface NodeBubbleMenuType extends BubbleMenuProps {
  component?: Component;
  items?: BubbleItemType[];
}

export interface BubbleItemType {
  priority: number;
  component?: Component;
  props?: {
    isActive?: ({ editor }: { editor: Editor }) => boolean;
    visible?: ({ editor }: { editor: Editor }) => boolean;
    icon?: Component;
    iconStyle?: string;
    title?: string;
    action?: ({ editor }: { editor: Editor }) => Component | void;
  };
}
export interface ToolboxItemType {
  priority: number;
  component: Component;
  props: {
    editor: Editor;
    icon?: Component;
    title?: string;
    description?: string;
    action?: () => void;
  };
}

export interface ExtensionOptions {
  getToolbarItems?: ({
    editor,
  }: {
    editor: Editor;
  }) => ToolbarItemType | ToolbarItemType[];

  getCommandMenuItems?: () => CommandMenuItemType | CommandMenuItemType[];

  getBubbleMenu?: ({ editor }: { editor: Editor }) => NodeBubbleMenuType;

  getToolboxItems?: ({
    editor,
  }: {
    editor: Editor;
  }) => ToolboxItemType | ToolboxItemType[];

  getDraggable?: ({
    editor,
  }: {
    editor: Editor;
  }) => DraggableItemType | boolean;
}

export interface CommandMenuItemType {
  priority: number;
  icon: Component;
  title: string;
  keywords: string[];
  command: ({ editor, range }: { editor: Editor; range: Range }) => void;
}

export interface DragSelectionNodeType {
  $pos?: ResolvedPos;
  node?: Node;
  el: HTMLElement;
  nodeOffset?: number;
  dragDomOffset?: {
    x?: number;
    y?: number;
  };
}

export interface DraggableItemType {
  getRenderContainer?: ({
    dom,
    view,
  }: {
    dom: HTMLElement;
    view: EditorView;
  }) => DragSelectionNodeType;
  handleDrop?: ({
    view,
    event,
    slice,
    insertPos,
    node,
    selection,
  }: {
    view: EditorView;
    event: DragEvent;
    slice: Slice;
    insertPos: number;
    node: Node;
    selection: Selection;
  }) => boolean | void;
  // allow drag-and-drop query propagation downward
  allowPropagationDownward?: boolean;
}
