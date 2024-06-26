// From DOMPurify
// https://github.com/cure53/DOMPurify/blob/main/src/regexp.js
// see https://github.com/ueberdosis/tiptap/pull/5160

const ATTR_WHITESPACE =
  /[\u0000-\u0020\u00A0\u1680\u180E\u2000-\u2029\u205F\u3000]/g; // eslint-disable-line no-control-regex
const IS_ALLOWED_URI =
  /^(?:(?:(?:f|ht)tps?|mailto|tel|callto|sms|cid|xmpp):|[^a-z]|[a-z+.\-]+(?:[^a-z+.\-:]|$))/i; // eslint-disable-line no-useless-escape

export function isAllowedUri(uri: string | undefined) {
  return !uri || uri.replace(ATTR_WHITESPACE, "").match(IS_ALLOWED_URI);
}
