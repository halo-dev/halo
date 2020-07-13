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
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
        String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式

        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); // 过滤script标签

        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); // 过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); // 过滤html标签

        return htmlStr;
    }
}
