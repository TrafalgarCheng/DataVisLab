package com.ssm.demo.utils;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class FileUtil {

    /**
     * 转换MultipartFile对象为java.io.File类型
     *
     * @param multipartFile
     * @return
     */
    public static File convertMultipartFileToFile(MultipartFile multipartFile) {
        File result = null;
        try {
            result = File.createTempFile(UUID.randomUUID().toString(), null);
            multipartFile.transferTo(result);
            result.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据url获取文件对象
     *
     * @param fileUrl
     * @return
     */
    public static File downloadFile(String fileUrl) {
        File result = null;
        try {
            result = File.createTempFile(UUID.randomUUID().toString(), null);
            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(3000);
            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(result));
            byte[] car = new byte[1024];
            int l = 0;
            while ((l = bis.read(car)) != -1) {
                bos.write(car, 0, l);
            }
            bis.close();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param request
     * @return
     */
    public static String getRealPath(HttpServletRequest request) {
        ServletContext sc = request.getSession().getServletContext();
        String uploadDir = sc.getRealPath("/upload");
        return uploadDir;
    }

    public static boolean saveFile(String savePath, String fileFullName, MultipartFile file) throws IOException {
        File uploadFile = new File(savePath + fileFullName);
        FileUtils.writeByteArrayToFile(new File(savePath, fileFullName), file.getBytes());
        return uploadFile.exists();
    }

    public static String mergeFile(int chunksNumber, String ext, String uploadFolderPath,
                                   HttpServletRequest request) {
        //合并分片流
        String mergePath = uploadFolderPath;
        String destPath = getRealPath(request);// 文件路径
        String newName = System.currentTimeMillis() + ext;// 文件新名称
        SequenceInputStream s;
        InputStream s1;
        try {
            s1 = new FileInputStream(mergePath + 0 + ext);
            String tempFilePath;
            InputStream s2 = new FileInputStream(mergePath + 1 + ext);
            s = new SequenceInputStream(s1, s2);
            for (int i = 2; i < chunksNumber; i++) {
                tempFilePath = mergePath + i + ext;
                InputStream s3 = new FileInputStream(tempFilePath);
                s = new SequenceInputStream(s, s3);
            }
            //分片文件存储到/upload/chunked目录下
            StringBuilder filePath = new StringBuilder();
            filePath.append(destPath).append(File.separator).append("chunked").append(File.separator);
            saveStreamToFile(s, filePath.toString(), newName);
            // 删除保存分块文件的文件夹
            deleteFolder(mergePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newName;
    }

    private static boolean deleteFolder(String mergePath) {
        File dir = new File(mergePath);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return dir.delete();
    }

    private static void saveStreamToFile(SequenceInputStream inputStream, String filePath, String newName)
            throws Exception {
        File fileDirectory = new File(filePath);
        synchronized (fileDirectory) {
            if (!fileDirectory.exists()) {
                if (!fileDirectory.mkdir()) {
                    throw new Exception("文件夹创建失败,路径为：" + fileDirectory);
                }
            }
            if (!fileDirectory.exists()) {
                if (!fileDirectory.mkdir()) {
                    throw new Exception("文件夹创建失败,路径为：" + fileDirectory);
                }
            }
        }
        OutputStream outputStream = new FileOutputStream(filePath + newName);
        byte[] buffer = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

}

