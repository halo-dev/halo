package run.halo.app.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: halo
 * @description: processing strings Utlis
 * @author: coor.top
 * @create: 2020-07-14 01:24
 **/
public class StringUtils {

    private StringUtils() {
    }

    /**
     * html convert to string
     *
     * @param htmlStr
     */
    public static String htmlToString(String htmlStr) {
        htmlStr = htmlStr.trim().replaceAll("\"", "'"); //如果有双引号将其先转成单引号
        String regExScript = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
        String regExStyle = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
        String regExHtml = "<[^>]+>"; // 定义HTML标签的正则表达式

        Pattern pScript = Pattern.compile(regExScript, Pattern.CASE_INSENSITIVE);
        Matcher mScript = pScript.matcher(htmlStr);
        htmlStr = mScript.replaceAll(""); // 过滤script标签

        Pattern pStyle = Pattern.compile(regExStyle, Pattern.CASE_INSENSITIVE);
        Matcher mStyle = pStyle.matcher(htmlStr);
        htmlStr = mStyle.replaceAll(""); // 过滤style标签

        Pattern pHtml = Pattern.compile(regExHtml, Pattern.CASE_INSENSITIVE);
        Matcher mHtml = pHtml.matcher(htmlStr);
        htmlStr = mHtml.replaceAll(""); // 过滤html标签

        return htmlStr;
    }
}
