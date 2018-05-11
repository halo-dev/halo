package cc.ryanc.halo.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/5/11
 * Zip压缩工具类
 */
@Slf4j
public class ZipUtils {

    /**
     * 解压Zip文件到指定目录
     *
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

    /**
     * 压缩目录
     * https://github.com/otale/tale/blob/master/src/main/java/com/tale/utils/ZipUtils.java
     *
     * @param srcFolder 目录路径
     * @param destZipFile 输出路径
     * @throws Exception
     */
    public static void zipFolder(String srcFolder, String destZipFile) throws Exception {
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;

        fileWriter = new FileOutputStream(destZipFile);
        zip = new ZipOutputStream(fileWriter);

        addFolderToZip("", srcFolder, zip);
        zip.flush();
        zip.close();
    }

    /**
     * 压缩文件
     * https://github.com/otale/tale/blob/master/src/main/java/com/tale/utils/ZipUtils.java
     *
     * @param filePath 文件路径
     * @param zipPath 输出路径
     * @throws Exception
     */
    public static void zipFile(String filePath, String zipPath) throws Exception{
        byte[] buffer = new byte[1024];
        FileOutputStream fos = new FileOutputStream(zipPath);
        ZipOutputStream zos = new ZipOutputStream(fos);
        ZipEntry ze= new ZipEntry("spy.log");
        zos.putNextEntry(ze);
        FileInputStream in = new FileInputStream(filePath);
        int len;
        while ((len = in.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        }
        in.close();
        zos.closeEntry();
        //remember close it
        zos.close();
    }

    /**
     * 添加文件到Zip压缩文件
     * https://github.com/otale/tale/blob/master/src/main/java/com/tale/utils/ZipUtils.java
     *
     * @param path path
     * @param srcFile srcFile
     * @param zip zip
     * @throws Exception
     */
    public static void addFileToZip(String path, String srcFile, ZipOutputStream zip)
            throws Exception {

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

    /**
     * 添加目录到zip压缩文件
     * https://github.com/otale/tale/blob/master/src/main/java/com/tale/utils/ZipUtils.java
     *
     * @param path path
     * @param srcFolder srcFolder
     * @param zip zip
     * @throws Exception
     */
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
}
