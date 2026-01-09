import { Extension } from "@tiptap/core";

export interface ExtensionBlockPositionOptions {
  /**
   * The types where the block position attribute can be applied.
   * @default []
   * @example ['figure']
   */
  types: string[];

  /**
   * The positions which are allowed.
   * @default ['start', 'center', 'end']
   * @example ['start', 'center']
   */
  positions: string[];

  /**
   * The default position.
   * @default null
   * @example 'start'
   */
  defaultPosition: string | null;
}

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    blockPosition: {
      /**
       * Set the block position attribute
       * @param position The position ("start", "center", "end")
       * @example editor.commands.setBlockPosition('start')
       */
      setBlockPosition: (position: string) => ReturnType;
      /**
       * Unset the block position attribute
       * @example editor.commands.unsetBlockPosition()
       */
      unsetBlockPosition: () => ReturnType;
      /**
       * Toggle the block position attribute
       * @param alignment The alignment
       * @example editor.commands.toggleBlockPosition('end')
       */
      toggleBlockPosition: (position: string) => ReturnType;
    };
  }
}

/**
 * This extension allows to set the block position.
 */
export const ExtensionBlockPosition =
  Extension.create<ExtensionBlockPositionOptions>({
    name: "blockPosition",

    addOptions() {
      return {
        types: [],
        positions: ["start", "center", "end"],
        defaultPosition: null,
      };
    },

    addGlobalAttributes() {
      return [
        {
          types: this.options.types,
          attributes: {
            alignItems: {
              default: this.options.defaultPosition,
              parseHTML: (element) => {
                const position = element.style.alignItems;

                return this.options.positions.includes(position)
                  ? position
                  : this.options.defaultPosition;
              },
              renderHTML: (attributes) => {
                if (!attributes.alignItems) {
                  return {};
                }

                return { style: `align-items: ${attributes.alignItems}` };
              },
            },
          },
        },
      ];
    },

    addCommands() {
      return {
        setBlockPosition:
          (position: string) =>
          ({ commands }) => {
            if (!this.options.positions.includes(position)) {
              return false;
            }

            return this.options.types
              .map((type) =>
                commands.updateAttributes(type, { alignItems: position })
              )
              .some((response) => response);
          },

        unsetBlockPosition:
          () =>
          ({ commands }) => {
            return this.options.types
              .map((type) => commands.resetAttributes(type, "alignItems"))
              .some((response) => response);
          },

        toggleBlockPosition:
          (position) =>
          ({ editor, commands }) => {
            if (!this.options.positions.includes(position)) {
              return false;
            }

            if (editor.isActive({ alignItems: position })) {
              return commands.unsetBlockPosition();
            }
            return commands.setBlockPosition(position);
          },
      };
    },

    addKeyboardShortcuts() {
      return {
        "Mod-Shift-l": () => this.editor.commands.setBlockPosition("left"),
        "Mod-Shift-e": () => this.editor.commands.setBlockPosition("center"),
        "Mod-Shift-r": () => this.editor.commands.setBlockPosition("right"),
      };
    },
  }).configure({
    types: ["figure"],
    positions: ["start", "center", "end"],
  });
