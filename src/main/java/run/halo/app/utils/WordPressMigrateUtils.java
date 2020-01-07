package run.halo.app.utils;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * 解析 WordPress 导出的 XML 数据为 Map 结果集
 *
 * @author guqing
 * @date 2019-10-29 14:49
 */
public class WordPressMigrateUtils {

    /**
     * 存储在此集合中的节点名称都会被解析为一个List存储
     */
    private static final List<String> ARRAY_PROPERTY = Arrays.asList("channel", "item", "wp:category", "wp:tag", "wp:term", "wp:postmeta", "wp:comment");

    /**
     * 根据xml文件对象获取xml的根节点rootElement对象
     *
     * @param file xml的文件对象
     * @return 返回根节点元素
     */
    public static Element getRootElement(File file) {
        try {
            SAXReader saxReader = new SAXReader();
            FileInputStream fileInputStream = new FileInputStream(file);
            Document document = saxReader.read(fileInputStream);

            return document.getRootElement();
        } catch (Exception e) {
            throw new RuntimeException("can not get root element");
        }
    }

    public static Element getRootElement(FileInputStream fileInputStream) {
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(fileInputStream);
            saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return document.getRootElement();
        } catch (Exception e) {
            throw new RuntimeException("can not get root element");
        }
    }

    /**
     * 获取 xml的 映射结果集对象
     *
     * @return 返回 xml 解析得到的 Map 映射结果集
     */
    public static Map<String, Object> getResultSetMapping(File file) {
        Element rootElement = getRootElement(file);
        return getResultSetMapping(rootElement);
    }

    /**
     * 根据根节点获取子节点元素集合递归遍历得到 Map 结果集
     *
     * @param root xml 的根节点元素对象
     * @return 返回解析 xml 得到的Map结果集
     */
    public static Map<String, Object> getResultSetMapping(Element root) {
        Map<String, Object> result = new HashMap<String, Object>();

        try {
            // 获取根元素的所有子元素
            List<Element> children = root.elements();

            // 递归遍历将 xml 节点数据解析为 Map 结果集
            result = transfer2Map(children, null);
        } catch (Exception e) {
            throw new RuntimeException("can not transfer xml file to map." + e.getMessage());
        }

        return result;
    }

    /**
     * 递归解析 xml，实现 N 层解析
     *
     * @param elements 所有子节点元素集，随着递归遍历而改变
     * @param list     存储中间遍历结果的容器
     * @return 返回递归完成后的Map结果集映射
     */
    private static Map<String, Object> transfer2Map(List<Element> elements, List<Map<String, Object>> list) {
        Map<String, Object> map = new HashMap<String, Object>();

        for (Element element : elements) {
            // getName 获取到的节点名称不带名称空间例如 <wp:content/> 获取到 name 为 content
            String nameWithoutPrefix = element.getName();

            // 需要使用的真是 name 默认等于不带名称空间的,如果名称存在空间则 name 的形式为: 名称空间:名称
            String name = nameWithoutPrefix;
            String prefix = element.getNamespace().getPrefix();
            if (isNotBlack(prefix)) {
                name = prefix + ":" + nameWithoutPrefix;
            }

            // 判断节点是否在定义的数组中,如果存在将其创建成 List 集合,否则作为文本
            if (ARRAY_PROPERTY.contains(name)) {
                //继续递归循环
                List<Map<String, Object>> sublist = new ArrayList<Map<String, Object>>();

                Map<String, Object> subMap = transfer2Map(element.elements(), sublist);

                // 根据 key 获取是否已经存在
                Object object = map.get(name);
                // 如果存在,合并
                if (object != null) {
                    List<Map<String, Object>> olist = (List<Map<String, Object>>) object;
                    olist.add(subMap);
                    map.put(name, olist);
                } else {
                    // 否则直接存入 map
                    map.put(name, sublist);
                }
            } else {
                map.put(name, element.getTextTrim());
            }
        }

        // 存入 list 中
        if (list != null) {
            list.add(map);
        }
        return map;
    }

    /**
     * 判断字符串是否为空,如果是空串返回 {@code true},否则 {@code false}
     *
     * @param str 需要进行空串判断的字符串
     * @return 如果是空串返回 true, 否则返回 false
     */
    private static boolean isNotBlack(String str) {
        return str != null && str.length() > 0 && str.trim().length() > 0;
    }
}
