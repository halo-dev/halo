package run.halo.app.utils;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.*;

/**
 * 解析wordpress导出的xml文章数据为Map结果集
 * @author guqing
 * @date 2019-10-29 14:49
 */
public class XmlTransferMapUtils {

    /**
     * 存储在此集合中的节点名称都会被解析为一个List存储
     */
    private static final List<String> ARRAY_PROPERTY = Arrays.asList("channel", "item", "category", "postmeta", "comment");

    /**
     * 需要解析成map集合的xml文件对象
     */
    private File file;

    public XmlTransferMapUtils(File file) {
        this.file = file;
    }

    /**
     * 根据xml文件对象获取xml的根节点rootElement对象
     * @param file xml的文件对象
     * @return 返回根节点元素
     */
    private Element getRootElement(File file) {
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
     * 获取xml的映射结果集对象
     * @return 返回xml解析得到的Map映射结果集
     */
    public Map<String, Object> getResultSetMapping() {
        Element rootElement = getRootElement(file);
        return getResultSetMapping(rootElement);
    }


    /**
     * 根据根节点获取子节点元素集合递归遍历得到Map结果集
     * @param root xml的根节点元素对象
     * @return 返回解析xml得到的Map结果集
     */
    private Map<String, Object> getResultSetMapping(Element root) {
        Map<String, Object> result = new HashMap<String, Object>();

        try {
            // 获取根元素的所有子元素
            List<Element> children = root.elements();

            //递归遍历将xml节点数据解析为Map结果集
            result = transfer2Map(children,null);
        } catch (Exception e) {
            throw new RuntimeException("can not transfer xml file to map." + e.getMessage());
        }

        return result;
    }

    /**
     * 递归解析xml，实现N层解析
     * @param elements 所有子节点元素集，随着递归遍历而改变
     * @param list 存储中间遍历结果的容器
     * @return 返回递归完成后的Map结果集映射
     */
    private Map<String, Object> transfer2Map(List<Element> elements,List<Map<String,Object>> list){
        Map<String, Object> map = new HashMap<String, Object>();

        for(Element element : elements){
            // getName获取到的节点名称不带名称空间例如<wp:content/>获取到name为content
            String nameWithoutPrefix = element.getName();

            // 需要使用的真是name默认等于不带名称空间的,如果名称存在空间则name的形式为: 名称空间:名称
            String name = nameWithoutPrefix;
            String preifx = element.getNamespace().getPrefix();
            if(isNotBlack(preifx)) {
                name =  preifx + ":" +nameWithoutPrefix;
            }

            //判断节点是否在定义的数组中,如果存在将其创建成List集合,否则作为文本
            if(ARRAY_PROPERTY.contains(name)) {
                //继续递归循环
                List<Map<String,Object>> sublist = new ArrayList<Map<String,Object>>();

                Map<String,Object> subMap  = this.transfer2Map(element.elements(), sublist);

                //根据key获取是否已经存在
                Object object = map.get(name);
                //如果存在,合并
                if(object !=null ){
                    List<Map<String,Object>> olist = (List<Map<String,Object>>)object;
                    olist.add(subMap);
                    map.put(name, olist);
                }else{
                    //否则直接存入map
                    map.put(name, sublist);
                }
            }else {
                //单个值存入map
                map.put(name, element.getTextTrim());
            }
        }

        //存入list中
        if(list != null) {
            list.add(map);
        }

        //返回结果集合
        return map;
    }

    /**
     * 判断字符串是否为空,如果是空串返回 {@code true},否则 {@code false}
     * @param str 需要进行空串判断的字符串
     * @return 如果是空串返回true,否则返回false
     */
    private boolean isNotBlack(String str) {
        if(str != null && str.length() > 0 && str.trim().length() > 0) {
            return true;
        }

        return false;
    }
}
