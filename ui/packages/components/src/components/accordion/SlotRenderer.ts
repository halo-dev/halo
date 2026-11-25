import type { PropType, Slot } from "vue";
import { defineComponent } from "vue";

/**
 * Renders a slot function that was forwarded through provide/inject.
 *
 * The accordion root provides its `#chevron` slot to every item. Calling that
 * slot from the item's setup would run outside a render function, so items
 * render it through this tiny helper instead.
 */
export const VAccordionSlotRenderer = defineComponent({
  name: "VAccordionSlotRenderer",
  props: {
    source: {
      type: Function as PropType<Slot>,
      required: true,
    },
    slotProps: {
      type: Object as PropType<Record<string, unknown>>,
      default: () => ({}),
    },
  },
  setup(props) {
    return () => props.source(props.slotProps);
  },
});

export default VAccordionSlotRenderer;
