package run.halo.app.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * <p> 将xml解析为json </p>
 *
 * @author guqing
 * @author ryanwang
 * @date 2019-11-17 13:33
 */
public class XmlMigrateUtils {

    /**
     * xml to json.
     *
     * @param in {@link java.io.FileInputStream}
     * @return json result
     * @throws JSONException throw JSONException
     * @throws IOException   throw IOException
     */
    public static String xml2jsonString(FileInputStream in) throws JSONException, IOException {
        String xml = IOUtils.toString(in, StandardCharsets.UTF_8);
        JSONObject jsonObject = XML.toJSONObject(xml);
        return jsonObject.toString();
    }

    /**
     * xml to json.
     *
     * @param xml xml
     * @return json result
     */
    public static String xml2jsonString(String xml) {
        JSONObject jsonObject = XML.toJSONObject(xml);
        return jsonObject.toString();
    }
}