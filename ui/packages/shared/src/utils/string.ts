const textEncoder = new TextEncoder();

export function utf8ByteLength(value: string) {
  return textEncoder.encode(value).byteLength;
}
