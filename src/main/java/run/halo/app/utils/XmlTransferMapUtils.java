package run.halo.app.utils;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * 解析 WordPress 导出的 xml 文章数据为 Map 结果集
 *
 * @author guqing
 */
public class XmlTransferMapUtils {

    /**
     * 存储在此集合中的节点名称都会被解析为一个List存储
     */
    private static final List<String> ARRAY_PROPERTY = Arrays.asList("channel", "item", "category", "postmeta", "comment");

    /**
     * 根据xml文件对象获取 xml 的根节点 rootElement 对象
     *
     * @param file xml的文件对象
     * @return 返回根节点元素
     */
    public Element getRootElement(File file) {
        try {
            SAXReader saxReader = new SAXReader();
            FileInputStream fileInputStream = new FileInputStream(file);
            Document document = saxReader.read(fileInputStream);
            return document.getRootElement();
        } catch (Exception e) {
            throw new RuntimeException("can not get root element");
        }
    }


    /**
     * 根据根节点获取子节点元素集合递归遍历得到Map结果集
     *
     * @param root xml的根节点元素对象
     * @return 返回解析xml得到的Map结果集
     */
    public Map<String, Object> getResultSetMapping(Element root) {
        Map<String, Object> result;
        try {
            // 获取根元素的所有子元素
            List<Element> children = root.elements();

            //递归遍历将xml节点数据解析为Map结果集
            result = transfer2Map(children, null);
        } catch (Exception e) {
            throw new RuntimeException("can not transfer xml file to map." + e.getMessage());
        }

        return result;
    }

    /**
     * 递归解析xml，实现N层解析
     *
     * @param elements 所有子节点元素集，随着递归遍历而改变
     * @param list     存储中间遍历结果的容器
     * @return 返回递归完成后的Map结果集映射
     */
    private Map<String, Object> transfer2Map(List<Element> elements, List<Map<String, Object>> list) {
        Map<String, Object> map = new HashMap<String, Object>();

        for (Element element : elements) {
            String name = element.getName();
            //如果是定义成数组
            if (ARRAY_PROPERTY.contains(name)) {
                //继续递归循环
                List<Map<String, Object>> sublist = new ArrayList<Map<String, Object>>();

                Map<String, Object> subMap = this.transfer2Map(element.elements(), sublist);

                //根据key获取是否已经存在
                Object object = map.get(name);
                //如果存在,合并
                if (object != null) {
                    List<Map<String, Object>> olist = (List<Map<String, Object>>) object;
                    olist.add(subMap);
                    map.put(name, olist);
                } else {
                    //否则直接存入map
                    map.put(name, sublist);
                }
            } else {
                //单个值存入map
                map.put(name, element.getTextTrim());
            }
        }

        //存入list中
        if (list != null) {
            list.add(map);
        }
        //返回结果集合
        return map;
    }
}
