const TABLE_NAME = "hljs-ln";
const LINE_NAME = "hljs-ln-line";
const CODE_BLOCK_NAME = "hljs-ln-code";
const NUMBERS_BLOCK_NAME = "hljs-ln-numbers";
const NUMBER_LINE_NAME = "hljs-ln-n";
const DATA_ATTR_NAME = "data-line-number";
const BREAK_LINE_REGEXP = /\r\n|\r|\n/g;

function isHljsLnCodeDescendant(domElt) {
  let curElt = domElt;
  while (curElt) {
    if (curElt.className && curElt.className.indexOf("hljs-ln-code") !== -1) {
      return true;
    }
    curElt = curElt.parentNode;
  }
  return false;
}

function getHljsLnTable(hljsLnDomElt) {
  let curElt = hljsLnDomElt;
  while (curElt.nodeName !== "TABLE") {
    curElt = curElt.parentNode;
  }
  return curElt;
}

// Function to workaround a copy issue with Microsoft Edge.
// Due to hljs-ln wrapping the lines of code inside a <table> element,
// itself wrapped inside a <pre> element, window.getSelection().toString()
// does not contain any line breaks. So we need to get them back using the
// rendered code in the DOM as reference.
function edgeGetSelectedCodeLines(selection) {
  // current selected text without line breaks
  const selectionText = selection.toString();

  // get the <td> element wrapping the first line of selected code
  let tdAnchor = selection.anchorNode;
  while (tdAnchor.nodeName !== "TD") {
    tdAnchor = tdAnchor.parentNode;
  }

  // get the <td> element wrapping the last line of selected code
  let tdFocus = selection.focusNode;
  while (tdFocus.nodeName !== "TD") {
    tdFocus = tdFocus.parentNode;
  }

  // extract line numbers
  let firstLineNumber = parseInt(tdAnchor.dataset.lineNumber);
  let lastLineNumber = parseInt(tdFocus.dataset.lineNumber);

  // multi-lines copied case
  if (firstLineNumber !== lastLineNumber) {
    let firstLineText = tdAnchor.textContent;
    let lastLineText = tdFocus.textContent;

    // if the selection was made backward, swap values
    if (firstLineNumber > lastLineNumber) {
      let tmp = firstLineNumber;
      firstLineNumber = lastLineNumber;
      lastLineNumber = tmp;
      tmp = firstLineText;
      firstLineText = lastLineText;
      lastLineText = tmp;
    }

    // discard not copied characters in first line
    while (selectionText.indexOf(firstLineText) !== 0) {
      firstLineText = firstLineText.slice(1);
    }

    // discard not copied characters in last line
    while (selectionText.lastIndexOf(lastLineText) === -1) {
      lastLineText = lastLineText.slice(0, -1);
    }

    // reconstruct and return the real copied text
    let selectedText = firstLineText;
    const hljsLnTable = getHljsLnTable(tdAnchor);
    for (let i = firstLineNumber + 1; i < lastLineNumber; ++i) {
      const codeLineSel = `.${CODE_BLOCK_NAME}[${DATA_ATTR_NAME}="${i}"]`;
      const codeLineElt = hljsLnTable.querySelector(codeLineSel);
      selectedText += "\n" + codeLineElt.textContent;
    }
    selectedText += "\n" + lastLineText;
    return selectedText;
    // single copied line case
  } else {
    return selectionText;
  }
}

// ensure consistent code copy/paste behavior across all browsers
// (see https://github.com/wcoder/highlightjs-line-numbers.js/issues/51)
document.addEventListener("copy", function (e) {
  // get current selection
  const selection = window.getSelection();
  // override behavior when one wants to copy line of codes
  if (isHljsLnCodeDescendant(selection.anchorNode)) {
    let selectionText;
    // workaround an issue with Microsoft Edge as copied line breaks
    // are removed otherwise from the selection string
    if (window.navigator.userAgent.indexOf("Edge") !== -1) {
      selectionText = edgeGetSelectedCodeLines(selection);
    } else {
      // other browsers can directly use the selection string
      selectionText = selection.toString();
    }
    e.clipboardData.setData("text/plain", selectionText);
    e.preventDefault();
  }
});

function initLineNumbersOnLoad(options) {
  if (window.document.readyState === "interactive" || window.document.readyState === "complete") {
    documentReady(options);
  } else {
    window.addEventListener("DOMContentLoaded", function () {
      documentReady(options);
    });
  }
}

function documentReady(options) {
  try {
    const blocks = window.document.querySelectorAll("code.hljs,code.nohighlight");

    for (const elm in blocks) {
      if (!isPluginDisabledForBlock(elm)) {
        lineNumbersBlock(elm, options);
      }
    }
  } catch (e) {
    throw new Error("LineNumbers error: ", e);
  }
}

function isPluginDisabledForBlock(element) {
  return element.classList.contains("nohljsln");
}

function lineNumbersBlock(element, options) {
  if (typeof element !== "object") return;

  setTimeout(() => {
    element.innerHTML = lineNumbersInternal(element, options);
  });
}

function lineNumbersValue(value, options) {
  if (typeof value !== "string") return;

  const element = document.createElement("code");
  element.innerHTML = value;

  return lineNumbersInternal(element, options);
}

function lineNumbersInternal(element, options) {
  const internalOptions = mapOptions(element, options);

  duplicateMultilineNodes(element);

  return addLineNumbersBlockFor(element.innerHTML, internalOptions);
}

function addLineNumbersBlockFor(inputHtml, options) {
  const lines = getLines(inputHtml);

  // if last line contains only carriage return remove it
  if (lines[lines.length - 1].trim() === "") {
    lines.pop();
  }

  if (lines.length > 1 || options.singleLine) {
    var html = "";

    for (var i = 0, l = lines.length; i < l; i++) {
      html += format(
        "<tr>" +
          '<td class="{0} {1}" {3}="{5}">' +
          '<div class="{2}" {3}="{5}"></div>' +
          "</td>" +
          '<td class="{0} {4}" {3}="{5}">' +
          "{6}" +
          "</td>" +
          "</tr>",
        [
          LINE_NAME,
          NUMBERS_BLOCK_NAME,
          NUMBER_LINE_NAME,
          DATA_ATTR_NAME,
          CODE_BLOCK_NAME,
          i + options.startFrom,
          lines[i].length > 0 ? lines[i] : " ",
        ]
      );
    }

    return format('<table class="{0}">{1}</table>', [TABLE_NAME, html]);
  }

  return inputHtml;
}

/**
 * {@link https://wcoder.github.io/notes/string-format-for-string-formating-in-javascript}
 * @param {string} format
 * @param {array} args
 */
function format(format, args) {
  return format.replace(/\{(\d+)\}/g, function (m, n) {
    return args[n] !== undefined ? args[n] : m;
  });
}

/**
 * @param {HTMLElement} element Code block.
 * @param {Object} options External API options.
 * @returns {Object} Internal API options.
 */
function mapOptions(element, options) {
  options = options || {};
  return {
    singleLine: getSingleLineOption(options),
    startFrom: getStartFromOption(element, options),
  };
}

function getSingleLineOption(options) {
  const defaultValue = false;
  if (options.singleLine) {
    return options.singleLine;
  }
  return defaultValue;
}

function getStartFromOption(element, options) {
  const defaultValue = 1;
  let startFrom = defaultValue;

  if (isFinite(options.startFrom)) {
    startFrom = options.startFrom;
  }

  // can be overridden because local option is priority
  const value = getAttribute(element, "data-ln-start-from");
  if (value !== null) {
    startFrom = toNumber(value, defaultValue);
  }

  return startFrom;
}

/**
 * Recursive method for fix multi-line elements implementation in highlight.js
 * Doing deep passage on child nodes.
 * @param {HTMLElement} element
 */
function duplicateMultilineNodes(element) {
  const nodes = element.childNodes;
  for (const node of nodes) {
    if (getLinesCount(node.textContent) > 0) {
      if (node.childNodes.length > 0) {
        duplicateMultilineNodes(node);
      } else {
        duplicateMultilineNode(node.parentNode);
      }
    }
  }
}

/**
 * Method for fix multi-line elements implementation in highlight.js
 * @param {HTMLElement} element
 */
function duplicateMultilineNode(element) {
  var className = element.className;

  if (!/hljs-/.test(className)) return;

  var lines = getLines(element.innerHTML);

  for (var i = 0, result = ""; i < lines.length; i++) {
    var lineText = lines[i].length > 0 ? lines[i] : " ";
    result += format('<span class="{0}">{1}</span>\n', [className, lineText]);
  }

  element.innerHTML = result.trim();
}

function getLines(text) {
  if (text.length === 0) return [];
  return text.split(BREAK_LINE_REGEXP);
}

function getLinesCount(text) {
  return (text.trim().match(BREAK_LINE_REGEXP) || []).length;
}

///
/// HELPERS
///

/**
 * @param {HTMLElement} element Code block.
 * @param {String} attrName Attribute name.
 * @returns {String} Attribute value or empty.
 */
function getAttribute(element, attrName) {
  return element.hasAttribute(attrName) ? element.getAttribute(attrName) : null;
}

/**
 * @param {String} str Source string.
 * @param {Number} fallback Fallback value.
 * @returns Parsed number or fallback value.
 */
function toNumber(str, fallback) {
  if (!str) return fallback;
  const number = Number(str);
  return isFinite(number) ? number : fallback;
}

/// EXPORTS

export function registerHljsLineNumbers(hljs) {
  if (!hljs) {
    throw new Error("registerHljsLineNumbers: hljs was not provided!");
  }

  hljs.initLineNumbersOnLoad = initLineNumbersOnLoad;
  hljs.lineNumbersBlock = lineNumbersBlock;
  hljs.lineNumbersValue = lineNumbersValue;
}

export function injectHljsLineNumbersCss() {
  if (document.getElementById("hljs-ln-css")) {
    return;
  }
  const css = window.document.createElement("style");
  css.id = "hljs-ln-css";
  css.innerHTML = `
  .${TABLE_NAME}{border-collapse:collapse}
  .${TABLE_NAME} td{padding:0}
  .${NUMBER_LINE_NAME}:before{content:attr(${DATA_ATTR_NAME})}
  `;
  window.document.getElementsByTagName("head")[0].appendChild(css);
}
