package cc.ryanc.halo.util;

import cc.ryanc.halo.model.domain.Post;
import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.model.dto.Theme;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Content;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;
import io.github.biezhi.ome.OhMyEmail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author : RYAN0UP
 * @date : 2017/12/22
 * @version : 1.0
 * description:常用的方法
 */
@Slf4j
public class HaloUtil {

    private final static Calendar NOW = Calendar.getInstance();

    public final static String YEAR = NOW.get(Calendar.YEAR)+"";

    public final static String MONTH = (NOW.get(Calendar.MONTH)+1)+"";

    private static ArrayList<String> FILE_LIST = new ArrayList<>();

    /**
     * 验证字符串是否为空
     * @param str str
     * @return boolean
     */
    public static boolean isNotNull(String str){
        return null !=str && ! "".equals(str.trim());
    }

    /**
     * 解压Zip文件
     * @param zipFilePath 压缩文件的路径
     * @param descDir 解压的路径
     */
    public static void unZip(String zipFilePath,String descDir){
        File zipFile=new File(zipFilePath);
        File pathFile=new File(descDir);
        if(!pathFile.exists()){
            pathFile.mkdirs();
        }
        ZipFile zip=null;
        InputStream in=null;
        OutputStream out=null;
        try {
            zip=new ZipFile(zipFile);
            Enumeration<?> entries=zip.entries();
            while(entries.hasMoreElements()){
                ZipEntry entry=(ZipEntry) entries.nextElement();
                String zipEntryName=entry.getName();
                in=zip.getInputStream(entry);

                String outPath=(descDir+"/"+zipEntryName).replace("\\*", "/");
                File file=new File(outPath.substring(0, outPath.lastIndexOf('/')));
                if(!file.exists()){
                    file.mkdirs();
                }
                if(new File(outPath).isDirectory()){
                    continue;
                }
                out=new FileOutputStream(outPath);
                byte[] buf=new byte[4*1024];
                int len;
                while((len=in.read(buf))>=0){
                    out.write(buf, 0, len);
                }
                in.close();
            }
        } catch (Exception e) {
            log.error("解压失败：{0}",e.getMessage());
        }finally{
            try {
                if(zip!=null)
                    zip.close();
                if(in!=null)
                    in.close();
                if(out!=null)
                    out.close();
            } catch (IOException e) {
                log.error("未知错误：{0}",e.getMessage());
            }
        }
    }

    public static void zipFolder(String folder,String outPutFile){
        ZipOutputStream zip = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(outPutFile);
            zip = new ZipOutputStream(fileOutputStream);
            addFolderToZip("", folder, zip);
            zip.flush();
            zip.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void addFileToZip(String path,String srcFile,ZipOutputStream zip) throws Exception{
        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip);
        } else {
            byte[] buf = new byte[1024];
            int len;
            FileInputStream in = new FileInputStream(srcFile);
            zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
        }
    }

    public static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
        File folder = new File(srcFolder);
        if (null != path && folder.isDirectory()) {
            for (String fileName : folder.list()) {
                if ("".equals(path)) {
                    addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
                } else {
                    addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
                }
            }
        }
    }

    /**
     * 截取图片
     * @param src 输入路径
     * @param dest 输出路径
     * @param w 宽度
     * @param h 长度
     * @param suffix 后缀
     * @throws IOException
     */
    public static void cutCenterImage(String src,String dest,int w,int h,String suffix){
        try{
            Iterator iterator = ImageIO.getImageReadersByFormatName(suffix);
            ImageReader reader = (ImageReader)iterator.next();
            InputStream in=new FileInputStream(src);
            ImageInputStream iis = ImageIO.createImageInputStream(in);
            reader.setInput(iis, true);
            ImageReadParam param = reader.getDefaultReadParam();
            int imageIndex = 0;
            Rectangle rect = new Rectangle((reader.getWidth(imageIndex)-w)/2, (reader.getHeight(imageIndex)-h)/2, w, h);
            param.setSourceRegion(rect);
            BufferedImage bi = reader.read(0,param);
            ImageIO.write(bi, suffix, new File(dest));
        }catch (Exception e){
            log.error("剪裁失败，图片本身尺寸小于需要修剪的尺寸：{0}",e.getMessage());
        }
    }

    /**
     * 获取所有附件
     * @param filePath filePath
     * @return Map
     */
    public static ArrayList<String> getFiles(String filePath){
        try{
            //获取项目根路径
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            //获取目标路径
            File targetPath = new File(basePath.getAbsolutePath(),filePath);
            File[] files = targetPath.listFiles();
            //遍历文件
            for(File file:files){
                if(file.isDirectory()){
                    getFiles(filePath+"/"+file.getName());
                }else{
                    String abPath = file.getAbsolutePath().substring(file.getAbsolutePath().indexOf("/upload"));
                    FILE_LIST.add(abPath);
                }
            }
        }catch (Exception e){
            log.error("未知错误：{0}",e.getMessage());
        }
        return FILE_LIST;
    }

    /**
     * 获取所有主题
     * @return list
     */
    public static List<Theme> getThemes(){
        List<Theme> themes = new ArrayList<>();
        try {
            //获取项目根路径
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            //获取主题路径
            File themesPath = new File(basePath.getAbsolutePath(),"templates/themes");
            File[] files = themesPath.listFiles();
            if(null!=files) {
                Theme theme = null;
                for (File file : files) {
                    if (file.isDirectory()) {
                        theme = new Theme();
                        theme.setThemeName(file.getName());
                        File optionsPath = new File(themesPath.getAbsolutePath(), file.getName() + "/module/options.ftl");
                        if (optionsPath.exists()) {
                            theme.setHasOptions(true);
                        } else {
                            theme.setHasOptions(false);
                        }
                        themes.add(theme);
                    }
                }
            }
        }catch (Exception e){
            log.error("主题获取失败：{0}",e.getMessage());
        }
        return themes;
    }

    /**
     * 获取主题下的模板文件名
     * @param theme theme
     * @return list
     */
    public static List<String> getTplName (String theme){
        List<String> tpls = new ArrayList<>();
        try{
            //获取项目根路径
            File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            //获取主题路径
            File themesPath = new File(basePath.getAbsolutePath(),"templates/themes/"+theme);
            File modulePath = new File(themesPath.getAbsolutePath(),"module");
            File[] baseFiles = themesPath.listFiles();
            File[] moduleFiles = modulePath.listFiles();
            if(null!=moduleFiles) {
                for (File file : moduleFiles) {
                    if (file.isFile()) {
                        if (file.getName().endsWith(".ftl")) {
                            tpls.add("module/" + file.getName());
                        }
                    }
                }
            }
            if(null!=baseFiles){
                for (File file:baseFiles){
                    if(file.isFile()) {
                        if (file.getName().endsWith(".ftl")) {
                            tpls.add(file.getName());
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("未知错误：{0}",e.getMessage());
        }
        return tpls;
    }

    /**
     * 获取文件内容
     * @param filePath filePath
     * @return string
     */
    public static String getFileContent(String filePath){
        File file = new File(filePath);
        Long fileLength = file.length();
        byte[] fileContent = new byte[fileLength.intValue()];
        try{
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.read(fileContent);
            inputStream.close();
            return new String(fileContent,"UTF-8");
        }catch (Exception e){
            log.error("读取模板文件错误：{0}",e.getMessage());
        }
        return null;
    }

    /**
     * 移除文件
     * @param fileName fileName
     * @return true or false
     */
    public static boolean removeFile(String fileName){
        File file = new File(fileName);
        if(file.exists() && file.delete()){
            return true;
        }
        return false;
    }

    /**
     * 获取当前时间
     * @return 字符串
     */
    public static String getStringDate(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String dateString = formatter.format(new Date());
        return dateString;
    }

    /**
     * 获取当前时间
     * @return 日期类型
     */
    public static Date getDate() {
        Date date = new Date();
        return date;
    }

    /**
     * 提取html中的文字
     * @param html html
     * @return string
     */
    public static String htmlToText(String html) {
        if (!"".equals(html)) {
            return html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
        }
        return "";
    }

    /**
     * 提取文章摘要
     * @param html html
     * @param summary summary
     * @return string
     */
    public static String getSummary(String html,Integer summary){
        return htmlToText(html).substring(0,summary);
    }

    /**
     * md5加密字符串
     * @param str str
     * @return MD5
     */
    public static String getMD5(String str) {
        String md5 = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageByte = str.getBytes("UTF-8");
            byte[] md5Byte = md.digest(messageByte);
            md5 = bytesToHex(md5Byte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5;
    }


    /**
     * 2进制转16进制
     * @param bytes bytes
     * @return string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer hexStr = new StringBuffer();
        int num;
        for (int i = 0; i < bytes.length; i++) {
            num = bytes[i];
            if(num < 0) {
                num += 256;
            }
            if(num < 16){
                hexStr.append("0");
            }
            hexStr.append(Integer.toHexString(num));
        }
        return hexStr.toString().toLowerCase();
    }

    /**
     * 获取客户端ip地址
     * @param request request
     * @return string
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
            if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 备份数据库
     * @param hostIp ip
     * @param userName 用户名
     * @param password 密码
     * @param savePath 保存路径
     * @param fileName 文件名
     * @param databaseName 数据库名
     * @return boolean
     * @throws InterruptedException InterruptedException
     */
    public static boolean exportDatabase(String hostIp,String userName,String password,String savePath,String fileName,String databaseName) throws InterruptedException{
        File saveFile = new File(savePath);
        if(!saveFile.exists()){
            saveFile.mkdirs();
        }
        if(!savePath.endsWith(File.separator)){
            savePath = savePath+File.separator;
        }

        PrintWriter printWriter = null;
        BufferedReader bufferedReader = null;
        try{
            printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(savePath+fileName),"utf-8"));
            Process process = Runtime.getRuntime().exec(" mysqldump -h" + hostIp + " -u" + userName + " -p" + password + " --set-charset=UTF8 " + databaseName);
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(),"utf-8");
            bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while((line = bufferedReader.readLine())!=null){
                printWriter.println(line);
            }
            printWriter.flush();
            if(process.waitFor()==0){
                return true;
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                if(bufferedReader != null){
                    bufferedReader.close();
                }
                if(printWriter!=null){
                    printWriter.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void dbToFile(String data,String filePath,String fileName){
        try{
            File file =new File(filePath);
            if(!file.exists()){
                file.mkdirs();
            }
            //true = append file
            FileWriter fileWritter = new FileWriter(file.getAbsoluteFile()+"/"+fileName,true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(data);
            bufferWritter.close();
            System.out.println("Done");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 生成rss
     * @param posts posts
     * @return string
     * @throws FeedException
     */
    public static String getRss(List<Post> posts) throws FeedException {
        Channel channel = new Channel("rss_2.0");
        if(null==HaloConst.OPTIONS.get("site_title")){
            channel.setTitle("");
        }else{
            channel.setTitle(HaloConst.OPTIONS.get("site_title"));
        }
        if(null==HaloConst.OPTIONS.get("site_url")){
            channel.setLink("");
        }else {
            channel.setLink(HaloConst.OPTIONS.get("site_url"));
        }
        if(null==HaloConst.OPTIONS.get("seo_desc")){
            channel.setDescription("");
        }else{
            channel.setDescription(HaloConst.OPTIONS.get("seo_desc"));
        }
        channel.setLanguage("zh-CN");
        List<Item> items = new ArrayList<>();
        for(Post post : posts){
            Item item = new Item();
            item.setTitle(post.getPostTitle());
            Content content = new Content();
            String value = post.getPostContent();
            char[] xmlChar = value.toCharArray();
            for (int i = 0; i < xmlChar.length; ++i) {
                if (xmlChar[i] > 0xFFFD) {
                    xmlChar[i] = ' ';
                } else if (xmlChar[i] < 0x20 && xmlChar[i] != 't' & xmlChar[i] != 'n' & xmlChar[i] != 'r') {
                    xmlChar[i] = ' ';
                }
            }
            value = new String(xmlChar);
            content.setValue(value);
            item.setContent(content);
            item.setLink(HaloConst.OPTIONS.get("site_url")+"/article/"+post.getPostUrl());
            item.setPubDate(post.getPostDate());
            items.add(item);
        }
        channel.setItems(items);
        WireFeedOutput out = new WireFeedOutput();
        return out.outputString(channel);
    }

    /**
     * 获取sitemap
     * @param posts posts
     * @return string
     */
    public static String getSiteMap(List<Post> posts){
        String head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">";
        String urlBody="";
        String urlItem;
        String urlPath = HaloConst.OPTIONS.get("site_url")+"/article/";
        for(Post post:posts){
            urlItem = "<url><loc>"+urlPath+post.getPostUrl()+"</loc><lastmod>"+post.getPostDate()+"</lastmod>"+"</url>";
            urlBody+=urlItem;
        }
        return head+urlBody+"</urlset>";
    }

    /**
     * 配置邮件
     * @param smtpHost smtpHost
     * @param userName 邮件地址
     * @param password 密码
     */
    public static void configMail(String smtpHost,String userName,String password){
        Properties properties = OhMyEmail.defaultConfig(false);
        properties.setProperty("mail.smtp.host",smtpHost);
        OhMyEmail.config(properties,userName,password);
    }

//    public static String importMarkdowns(String filePath) throws Exception{
//        File file = new File(filePath);
//        FileReader reader = new FileReader(file);
//        BufferedReader bufferedReader = new BufferedReader(reader);
//        StringBuffer stringBuffer = new StringBuffer();
//        String s = "";
//        while ((s = bufferedReader.readLine())!=null){
//            stringBuffer.append(s+"\n");
//        }
//        bufferedReader.close();
//        String str = stringBuffer.toString();
//        return str;
//    }
//
//    public static void main(String[] args) throws Exception{
//        String content = importMarkdowns("/Users/ryan0up/Desktop/hello-hexo.md");
//        String matter = StringUtils.substringBetween(content,"---","---");
//        String[] strs =  matter.split("\n");
//        for(String str:strs){
//            System.out.println(StringUtils.substringBetween("title","\n","\n"));
//        }
//    }

    /**
     * 访问路径获取json数据
     * @param url
     * @return
     */
    public static String getHttpResponse(String enterUrl) {
        BufferedReader in = null;
        StringBuffer result = null;
        try {
            URI uri = new URI(enterUrl);
            URL url = uri.toURL();
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Charset", "utf-8");
            connection.connect();
            result = new StringBuffer();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }
}
