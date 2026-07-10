import { ExtensionAudio } from "@/extensions/audio";
import { ExtensionImage } from "@/extensions/image";
import { ExtensionVideo } from "@/extensions/video";
import type { PMNode } from "@/tiptap";

export function getChangedAssetNodes(
  docBeforePaste: PMNode,
  docAfterPaste: PMNode
) {
  const from = docBeforePaste.content.findDiffStart(docAfterPaste.content);
  const diffEnd = docBeforePaste.content.findDiffEnd(docAfterPaste.content);
  if (from === null || !diffEnd) {
    return [];
  }

  const assetNodes: Array<{
    node: PMNode;
    pos: number;
    index: number;
    parent: PMNode | null;
  }> = [];
  docAfterPaste.nodesBetween(
    from,
    Math.max(from, diffEnd.b),
    (node, pos, parent, index) => {
      if (
        [
          ExtensionAudio.name,
          ExtensionVideo.name,
          ExtensionImage.name,
        ].includes(node.type.name)
      ) {
        assetNodes.push({ node, pos, parent, index });
      }
    }
  );
  return assetNodes;
}
