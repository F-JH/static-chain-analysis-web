package com.hsf.tools.Utils;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;

import java.io.*;
import java.nio.file.NotDirectoryException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.hsf.tools.Config.Code.URL_SPLIT;

public class FileUtil {

    public static void copyDirectory(File src, File dst)throws IOException {
        if (src.isFile()){
            copyFile(src, dst);
        }else if (src.isDirectory()){
            dst.mkdirs();
            for (File sub : src.listFiles()){
                copyDirectory(sub, new File(dst,sub.getName()));
            }
        }
    }

    public static void copyFile(File src, File dst)throws IOException {
        InputStream is = new FileInputStream(src);
        OutputStream os = new FileOutputStream(dst);

        byte[] flush = new byte[1024];
        int len = 0;
        while(-1 != (len = is.read(flush))){
            os.write(flush, 0, len);
        }
        os.flush();

        os.close();
        is.close();
    }

    /**
     * 获取目录下所有文件的完整路径
     * @param dirPath
     * @return
     * @throws NotDirectoryException
     */
    public static List<String> scanForDirectory(String dirPath) throws NotDirectoryException {
        List<String> result = new ArrayList<>();
        File dirFile = new File(dirPath);
        if (!dirFile.exists() || !dirFile.isDirectory())
            throw new NotDirectoryException("「" + dirPath + "」不是文件夹");
        File[] files = dirFile.listFiles();
        if(files==null)
            return null;

        Stack<Object[]> stack = new Stack<>();
        for (File file : files)
            stack.push(new Object[]{new StringBuilder(), file});
        while (!stack.empty()){
            Object[] topItem = stack.pop();
            File fileNode = (File) topItem[1];
            StringBuilder relativePath = (StringBuilder) topItem[0];
            if (fileNode.isFile()){
                String fileName = fileNode.getName();
                if (fileName.substring(fileName.lastIndexOf('.')).equals(".java") || fileName.substring(fileName.lastIndexOf('.')).equals(".class")){
                    String topItemPath = relativePath.append(URL_SPLIT).append(fileName).toString();
                    result.add(dirPath + topItemPath);
                }
            }else {
                String dirName = fileNode.getName();
                String topItemPath = relativePath.append(URL_SPLIT).append(dirName).toString();
                File[] fs = fileNode.listFiles();
                for (File f : fs){
                    stack.push(new Object[]{new StringBuilder(topItemPath), f});
                }
            }
        }
        return result;
    }

    /**
        * 比较两个文件夹的目录结构
     * @param oldPath   旧路径，需要传入完整路径
     * @param newPath   新路径，需要传入完整路径
     * @return  只会记录新建的java文件、修改的java文件以及新建的文件夹，不会返回删除的内容
     * @throws NotDirectoryException
     */
    public static Map<String, List<String>> compireToPath(String oldPath, String newPath) throws NotDirectoryException {
        File oldRoot = new File(oldPath);
        File newRoot = new File(newPath);
        if(!oldRoot.exists() || !newRoot.exists() || !oldRoot.isDirectory() || !newRoot.isDirectory())
            throw new NotDirectoryException("需要传入完整路径");

        Map<String, List<String>> result = new HashMap<>();
        List<String> newFiles = new ArrayList<>();
        List<String> modifyFiles = new ArrayList<>();
        List<String> newDirectorys = new ArrayList<>();

        Stack<Object[]> newStack = new Stack<>();
        // 初始化Stack
        File[] newRootFiles = newRoot.listFiles();
        if(newRootFiles==null)
            return null;
        for(File f:newRootFiles)
            newStack.push(new Object[]{new StringBuilder(), f});
        while(!newStack.empty()){
            Object[] topItem = newStack.pop();
            File fileNode = (File) topItem[1];
            StringBuilder relativePath = (StringBuilder) topItem[0];
            if(fileNode.isFile()){
                // 文件类型只处理.java文件，其他的不管
                // 能来到这说明至少在oldPath中有同个文件夹
                String fileName = fileNode.getName();
                if(fileName.substring(fileName.lastIndexOf('.')).equals(".java")){
                    // 检查 oldPath 中是否有此java文件，有的话再对比byte是否有修改
                    String topItemPath = relativePath.append(URL_SPLIT).append(fileName).toString();
                    File oldFile = new File(oldPath + topItemPath);
                    if(oldFile.exists()){
                        if(!Arrays.equals(getFileBytes(oldFile), getFileBytes(fileNode)))
                            modifyFiles.add(topItemPath);
                    }else{
                        newFiles.add(topItemPath);
                    }
                }
            }else{
                // 如果是文件夹，先判断oldPath里是否存在，不存在就是新增的文件夹，直接添加到newDirectory中，不用遍历文件
                // 如果不是新增的，则把文件夹内的元素解开，添加到Stack中
                String dirName = fileNode.getName();
                String topItemPath = relativePath.append(URL_SPLIT).append(dirName).toString();
                File oldDir = new File(oldPath + topItemPath);
                if(oldDir.exists()){
                    File[] files = fileNode.listFiles();
                    for(File file:files)
                        newStack.push(new Object[]{new StringBuilder(topItemPath), file});
                }else{
                    newDirectorys.add(topItemPath);
                }
            }
        }

        result.put("newFiles", newFiles);
        result.put("modifyFiles", modifyFiles);
        result.put("newDirectorys", newDirectorys);
        return result;
    }

    public static Map<String, String> scanMybatisXml(String path) throws NotDirectoryException, DocumentException {
        Map<String, String> result = new HashMap<>();
        File dirFile = new File(path);
        if (!dirFile.exists() || !dirFile.isDirectory())
            throw new NotDirectoryException("「" + path + "」不是文件夹");
        File[] files = dirFile.listFiles();
        if(files==null)
            return null;
        Stack<Object[]> stack = new Stack<>();
        for(File file:files)
            stack.push(new Object[]{new StringBuilder(), file});
        while(!stack.empty()){
            Object[] topItem = stack.pop();
            File fileNode = (File) topItem[1];
            StringBuilder relativePath = (StringBuilder) topItem[0];
            if(fileNode.isFile()){
                String fileName = fileNode.getName();
                if(fileName.substring(fileName.lastIndexOf('.')).equals(".xml")){
                    XmlDiffUtil xmlDiffUtils = new XmlDiffUtil(fileNode);
                    if(xmlDiffUtils.isMapper()){
                        String topItemPath = relativePath.append(URL_SPLIT).append(fileName).toString();
                        result.put(xmlDiffUtils.getMapperNameSpace(), path + topItemPath);
                    }
                }
            }else{
                String dirName = fileNode.getName();
                String topItemPath = relativePath.append(URL_SPLIT).append(dirName).toString();
                File[] fs = fileNode.listFiles();
                for(File f:fs)
                    stack.push(new Object[]{new StringBuilder(topItemPath), f});
            }
        }
        return result;
    }

    private static byte[] getFileBytes(File file){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] b = md.digest(FileUtils.readFileToByteArray(file));
            return b;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
