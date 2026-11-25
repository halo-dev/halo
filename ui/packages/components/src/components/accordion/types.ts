import type { Component, ComputedRef, InjectionKey, Slots } from "vue";

export type VAccordionType = "single" | "multiple";

/**
 * Visual layouts:
 * - `default`: items separated by a divider, without an outer container.
 * - `bordered`: items grouped in a bordered, rounded card; the open item gets a
 *   subtle background.
 * - `filled`: standalone rounded items that fill with a subtle background when
 *   open.
 * - `separated`: each item is its own rounded card with space between items.
 * - `plain`: no container chrome, for section headers inside an existing card
 *   or form.
 */
export type VAccordionVariant =
  | "default"
  | "bordered"
  | "filled"
  | "separated"
  | "plain";

export type VAccordionChevronPosition = "left" | "right";

export type VAccordionHeaderTag =
  | "h1"
  | "h2"
  | "h3"
  | "h4"
  | "h5"
  | "h6"
  | "div"
  | "span";

export type VAccordionHeadingOrder = 2 | 3 | 4 | 5 | 6;

export type VAccordionClassValue = string | string[] | Record<string, boolean>;

/**
 * Per-part classes for each rendered element of an item.
 */
export interface VAccordionClassNames {
  item?: VAccordionClassValue;
  trigger?: VAccordionClassValue;
  label?: VAccordionClassValue;
  leading?: VAccordionClassValue;
  chevron?: VAccordionClassValue;
  content?: VAccordionClassValue;
}

export interface VAccordionContext {
  variant: VAccordionVariant;
  chevronPosition: VAccordionChevronPosition;
  disableChevronRotation: boolean;
  transitionDuration: number;
  headerAs: VAccordionHeaderTag;
}

export const defaultAccordionContext: VAccordionContext = {
  variant: "default",
  chevronPosition: "right",
  disableChevronRotation: false,
  transitionDuration: 200,
  headerAs: "h3",
};

export const accordionContextKey: InjectionKey<ComputedRef<VAccordionContext>> =
  Symbol("VAccordionContext");

/**
 * The root `#chevron` slot is forwarded to every item so a custom chevron can
 * be defined once for the whole accordion.
 */
export const accordionSlotsKey: InjectionKey<Slots> = Symbol("VAccordionSlots");

export type VAccordionChevron = Component;
