import dart from "highlight.js/lib/languages/dart";
import xml from "highlight.js/lib/languages/xml";
import { common, createLowlight } from "lowlight";

const lowlight = createLowlight(common);
lowlight.register("html", xml);
lowlight.register("dart", dart);
export default lowlight;
