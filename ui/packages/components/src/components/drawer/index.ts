export { default as VDrawer } from "./Drawer.vue";
export * from "./types";
export {
  findNearestSnapIndex,
  isHorizontalPlacement,
  normalizeSnapPoints,
  resolveDrawerSize,
  resolveSnapPointSize,
  resolveSnapRelease,
  shouldDismissOnDrag,
} from "./drag";
export type {
  DragDismissOptions,
  SnapReleaseOptions,
  SnapReleaseResult,
} from "./drag";
