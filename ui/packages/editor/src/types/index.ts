import type {
  Editor,
  EditorState,
  EditorView,
  PluginKey,
  PMNode,
  Range,
  ResolvedPos,
} from "@/tiptap";
import type {
  arrow,
  autoPlacement,
  flip,
  hide,
  inline,
  offset,
  shift,
  size,
  VirtualElement,
} from "@floating-ui/dom";
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

export interface BubbleMenuOptions {
  strategy?: "absolute" | "fixed";
  placement?:
    | "top"
    | "right"
    | "bottom"
    | "left"
    | "top-start"
    | "top-end"
    | "right-start"
    | "right-end"
    | "bottom-start"
    | "bottom-end"
    | "left-start"
    | "left-end";
  offset?: Parameters<typeof offset>[0] | boolean;
  flip?: Parameters<typeof flip>[0] | boolean;
  shift?: Parameters<typeof shift>[0] | boolean;
  arrow?: Parameters<typeof arrow>[0] | false;
  size?: Parameters<typeof size>[0] | boolean;
  autoPlacement?: Parameters<typeof autoPlacement>[0] | boolean;
  hide?: Parameters<typeof hide>[0] | boolean;
  inline?: Parameters<typeof inline>[0] | boolean;
  onShow?: () => void;
  onHide?: () => void;
  onUpdate?: () => void;
  onDestroy?: () => void;
  /**
   * The scrollable element that should be listened to when updating the position of the bubble menu.
   * If not provided, the window will be used.
   * @type {HTMLElement | Window}
   */
  scrollTarget?: HTMLElement | Window;
}

export interface DragButtonItemProps {
  extendsKey?: string;
  priority?: number;
  title?:
    | string
    | (({
        editor,
        node,
        pos,
      }: {
        editor: Editor;
        node: PMNode | null;
        pos: number;
      }) => string);
  icon?: Component;
  key?: string;
  action?: ({
    editor,
    node,
    pos,
    close,
  }: {
    editor: Editor;
    node: PMNode | null;
    pos: number;
    close: () => void;
  }) => Component | boolean | void | Promise<Component | boolean | void>;
  iconStyle?: string;
  class?: string;
  visible?: ({
    editor,
    node,
    pos,
  }: {
    editor: Editor;
    node: PMNode | null;
    pos: number;
  }) => boolean;
  isActive?: ({
    editor,
    node,
    pos,
  }: {
    editor: Editor;
    node: PMNode | null;
    pos: number;
  }) => boolean;
  disabled?: ({
    editor,
    node,
    pos,
  }: {
    editor: Editor;
    node: PMNode | null;
    pos: number;
  }) => boolean;
  keyboard?: string;
  component?: Component;
  [key: string]: unknown;
}
export interface DragButtonType extends DragButtonItemProps {
  children?: {
    component?: Component;
    items?: DragButtonItemProps[];
  };
}

export interface BubbleMenuProps {
  pluginKey?: string | PluginKey;
  editor?: Editor;
  shouldShow?: (props: {
    editor: Editor;
    element: HTMLElement;
    view: EditorView;
    state: EditorState;
    oldState?: EditorState;
    from: number;
    to: number;
  }) => boolean;
  appendTo?: HTMLElement | (() => HTMLElement) | undefined;
  getReferencedVirtualElement?: () => VirtualElement | null;
  options?: BubbleMenuOptions | null;
}

export interface NodeBubbleMenuType extends BubbleMenuProps {
  component?: Component;
  items?: BubbleItemType[];
  extendsKey?: string | PluginKey;
}
export interface BubbleItemType {
  priority: number;
  component?: Component;
  key?: string;
  props?: {
    isActive?: ({ editor }: { editor: Editor }) => boolean;
    visible?: ({ editor }: { editor: Editor }) => boolean;
    icon?: Component;
    iconStyle?: string;
    title?: string;
    action?: ({ editor }: { editor: Editor }) => Component | boolean | void;
  } & Record<string, unknown>;
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

  getDraggableMenuItems?: ({
    editor,
  }: {
    editor: Editor;
  }) => DragButtonType | DragButtonType[];
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
  node?: PMNode;
  el: HTMLElement;
  nodeOffset?: number;
  dragDomOffset?: {
    x?: number;
    y?: number;
  };
}
