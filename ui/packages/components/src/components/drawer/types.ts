import type { DrawerPlacement, DrawerSnapPoint } from "./drag";

export type { DrawerPlacement, DrawerSnapPoint, DrawerSnapEntry } from "./drag";

export interface DrawerProps {
  /** Controlled visibility, usually bound with `v-model:visible`. */
  visible?: boolean;
  /** Initial visibility when used uncontrolled. */
  defaultVisible?: boolean;
  /** Edge of the viewport the drawer is anchored to. */
  placement?: DrawerPlacement;
  /**
   * Width for `left`/`right` drawers and height for `top`/`bottom` drawers.
   * Numbers are pixels, strings accept any CSS length such as `24rem` or `50%`.
   */
  size?: number | string;
  title?: string;
  description?: string;
  /** Accessible name used when no visible title is rendered. */
  ariaLabel?: string;
  /** Whether the drawer is modal. Modal drawers lock page scroll and trap focus. */
  modal?: boolean;
  /**
   * Float the drawer with a gap from the viewport edges and rounded corners.
   * Applies from the `sm` breakpoint; smaller screens stay edge to edge.
   */
  inset?: boolean;
  /** Whether clicking the overlay closes the drawer. */
  layerClosable?: boolean;
  /** Whether pressing Escape closes the drawer. */
  closeOnEsc?: boolean;
  /**
   * Called before the drawer closes. Return `false` (or resolve `false`) to
   * keep it open, for example to confirm unsaved changes.
   */
  beforeClose?: () => boolean | Promise<boolean>;
  /** Whether to render the built-in close button. */
  showClose?: boolean;
  closeLabel?: string;
  /** Whether to render the built-in drag handle. */
  showHandle?: boolean;
  /** Whether the drawer can be dismissed by dragging. */
  swipeToDismiss?: boolean;
  /**
   * When true, dragging is limited to the drag handle and header. Touch input
   * is always limited to those areas to avoid conflicting with scrolling.
   */
  handleOnly?: boolean;
  /** Fraction of the drawer size that dismisses it after a drag. */
  swipeThreshold?: number;
  /**
   * Snap points for bottom drawers. Fractions between 0 and 1 are viewport
   * fractions, numbers above 1 are pixels, and strings accept `px`, `rem`,
   * `em`, `vh` and `%`.
   */
  snapPoints?: DrawerSnapPoint[];
  /** Controlled active snap point, usually bound with `v-model:snap-point`. */
  snapPoint?: DrawerSnapPoint;
  /** Initial snap point when used uncontrolled. Defaults to the largest one. */
  defaultSnapPoint?: DrawerSnapPoint;
  /** Whether a drag can move a snap point drawer by more than one snap point. */
  snapToSequentialPoints?: boolean;
  /** Teleport the drawer to `body`. */
  mountToBody?: boolean;
  /** Keep the drawer mounted after closing. */
  unmountOnHide?: boolean;
  bodyClass?: string[];
  contentClass?: string[];
}
