export function matchMediaType(mediaType: string, accept: string) {
  const regex = new RegExp(accept.toLowerCase().replace(/\*/g, ".*"));

  return regex.test(mediaType);
}

export function matchMediaTypes(mediaType: string, accepts: string[]) {
  return accepts.some((accept) => matchMediaType(mediaType, accept));
}
