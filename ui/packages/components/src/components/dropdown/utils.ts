export function isFloatingPopperTarget(target: EventTarget | null) {
  return target instanceof Element && !!target.closest(".v-popper__popper");
}
