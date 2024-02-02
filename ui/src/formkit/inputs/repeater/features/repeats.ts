import type { FormKitNode } from "@formkit/core";
import { undefine } from "@formkit/utils";

export const repeats = function (node: FormKitNode) {
  node._c.sync = true;
  node.on("created", repeaterFeature.bind(null, node));
};

type FnType = (index: number) => object;

function createValue(num: number, fn: FnType) {
  return new Array(num).fill("").map((value, index) => fn(index));
}

function repeaterFeature(node: FormKitNode) {
  node.props.removeControl = node.props.removeControl ?? true;
  node.props.upControl = node.props.upControl ?? true;
  node.props.downControl = node.props.downControl ?? true;
  node.props.insertControl = node.props.insertControl ?? true;
  node.props.addButton = node.props.addButton ?? true;
  node.props.addLabel = node.props.addLabel ?? false;
  node.props.addAttrs = node.props.addAttrs ?? {};
  node.props.min = node.props.min ? Number(node.props.min) : 0;
  node.props.max = node.props.max ? Number(node.props.max) : 1 / 0;
  if (node.props.min > node.props.max) {
    throw Error("Repeater: min must be less than max");
  }

  if ("disabled" in node.props) {
    node.props.disabled = undefine(node.props.disabled);
  }

  if (Array.isArray(node.value)) {
    if (node.value.length < node.props.min) {
      const value = createValue(node.props.min - node.value.length, () => ({}));
      node.input(node.value.concat(value), false);
    } else {
      if (node.value.length > node.props.max) {
        node.input(node.value.slice(0, node.props.max), false);
      }
    }
  } else {
    node.input(
      createValue(node.props.min, () => ({})),
      false
    );
  }

  if (node.context) {
    const fns = node.context.fns;
    fns.createShift = (index: number, offset: number) => () => {
      const value = node._value as unknown[];
      value.splice(index + offset, 0, value.splice(index, 1)[0]),
        node.input(value, false);
    };
    fns.createInsert = (index: number) => () => {
      const value = node._value as unknown[];
      value.splice(index + 1, 0, {}), node.input(value, false);
    };
    fns.createAppend = () => () => {
      const value = node._value as unknown[];
      value.push({}), node.input(value, false);
    };
    fns.createRemover = (index: number) => () => {
      const value = node._value as unknown[];
      value.splice(index, 1), node.input(value, false);
    };
  }
}
