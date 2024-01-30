export function generateAnchor(text: string) {
  return encodeURIComponent(
    String(text).trim().toLowerCase().replace(/\s+/g, "-")
  );
}

export const generateAnchorId = (text: string, ids: string[]) => {
  const originId = generateAnchor(text);
  let id = originId;
  while (ids.includes(id)) {
    const temporarySuffix = id.replace(originId, "");
    const match = temporarySuffix.match(/-(\d+)$/);
    if (match) {
      id = `${originId}-${Number(match[1]) + 1}`;
    } else {
      id = `${originId}-1`;
    }
  }
  return id;
};
