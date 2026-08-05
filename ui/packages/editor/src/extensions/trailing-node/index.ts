import {
  skipTrailingNodeMeta,
  TrailingNode,
  type TrailingNodeOptions,
} from "@tiptap/extensions";
import { Plugin, type Transaction } from "@/tiptap/pm";

function shouldSkipTrailingNode(transaction: Transaction) {
  let current: Transaction | undefined = transaction;
  const visited = new Set<Transaction>();

  while (current) {
    if (visited.has(current)) {
      break;
    }
    visited.add(current);

    if (current.getMeta(skipTrailingNodeMeta)) {
      return true;
    }

    current = current.getMeta("appendedTransaction") as Transaction | undefined;
  }

  return false;
}

export const ExtensionTrailingNode = TrailingNode.extend<TrailingNodeOptions>({
  addOptions() {
    return {
      ...this.parent!(),
      node: "paragraph",
      notAfter: ["paragraph"],
    };
  },

  addProseMirrorPlugins() {
    if (this.editor.options.editable === false) {
      return [];
    }

    return (this.parent?.() ?? []).map(preserveSkipMetaAcrossAppendRounds);
  },
});

function preserveSkipMetaAcrossAppendRounds(plugin: Plugin) {
  const appendTransaction = plugin.spec.appendTransaction;
  if (!appendTransaction) {
    return plugin;
  }

  return new Plugin({
    ...plugin.spec,
    appendTransaction(transactions, oldState, newState) {
      // A plugin ordered after TrailingNode can append another transaction
      // after the direct skip was observed. ProseMirror links that transaction
      // to its root through `appendedTransaction`.
      if (transactions.some(shouldSkipTrailingNode)) {
        return null;
      }
      return appendTransaction.call(plugin, transactions, oldState, newState);
    },
  });
}

export {
  skipTrailingNodeMeta,
  type TrailingNodeOptions,
} from "@tiptap/extensions";
