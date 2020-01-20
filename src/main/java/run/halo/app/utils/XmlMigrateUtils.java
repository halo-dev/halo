package run.halo.app.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * <p> 将xml解析为json </p>
 *
 * @author guqing
 * @date 2019-11-17 13:33
 */
public class XmlMigrateUtils {
    public static String xml2jsonString(FileInputStream in) throws JSONException, IOException {
        String xml = IOUtils.toString(in, "UTF-8");
        JSONObject jsonObject = XML.toJSONObject(xml);
        return jsonObject.toString();
    }
}