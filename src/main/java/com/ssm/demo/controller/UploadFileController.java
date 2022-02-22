package com.ssm.demo.controller;

import com.ssm.demo.common.Result;
import com.ssm.demo.common.ResultGenerator;
import com.ssm.demo.controller.enums.UploadFileTypeEnum;
import com.ssm.demo.utils.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static com.ssm.demo.common.Constants.FILE_PRE_URL;

/**
 * Created by 13 on 2017/7/17.
 */
@Controller
@RequestMapping("/upload")
public class UploadFileController {

    final static Logger logger = Logger.getLogger(UploadFileController.class);

    /**
     * 通用 文件上传接口 (可以上传图片、视频、excel等文件，具体格式可在UploadFileTypeEnum中进行配置)
     *
     * @return
     */
    @RequestMapping(value = "/file", method = RequestMethod.POST)
    @ResponseBody
    public Result uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        ServletContext sc = request.getSession().getServletContext();
        String type = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1, file.getOriginalFilename().length());
        String fileName = null;
        UploadFileTypeEnum uploadFileTypeEnum = UploadFileTypeEnum.getFileEnumByType(type);
        if (uploadFileTypeEnum == UploadFileTypeEnum.ERROR_TYPE) {
            //格式错误则不允许上传，直接返回错误提示
            return ResultGenerator.genFailResult("请检查文件格式！");
        } else {
            //生成文件名称通用方法
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Random r = new Random();
            StringBuilder tempName = new StringBuilder();
            tempName.append(sdf.format(new Date())).append(r.nextInt(100)).append(".").append(type);
            fileName = tempName.toString();
        }
        try {
            String dir = sc.getRealPath("/upload");
            FileUtils.writeByteArrayToFile(new File(dir, fileName), file.getBytes());
        } catch (IOException e) {
            //文件上传异常
            return ResultGenerator.genFailResult("文件上传失败！");
        }
        Result result = ResultGenerator.genSuccessResult();
        //返回文件的全路径
        StringBuilder fileUrl = new StringBuilder();
        fileUrl.append(FILE_PRE_URL).append("/upload/").append(fileName);
        result.setData(fileUrl.toString());
        return result;
    }

    /**
     * @param chunks 当前所传文件的分片总数
     * @param chunk  当前所传文件的当前分片数
     * @return
     * @Description: 大文件上传前分片检查
     * @author: 13
     */
    @ResponseBody
    @RequestMapping(value = "/checkChunk")
    public Result checkChunk(HttpServletRequest request, String guid, Integer chunks, Integer chunk, String fileName) {
        try {
            String uploadDir = FileUtil.getRealPath(request);
            String ext = fileName.substring(fileName.lastIndexOf("."));
            // 判断文件是否分块
            if (chunks != null && chunk != null) {
                //文件路径
                StringBuilder tempFileName = new StringBuilder();
                tempFileName.append(uploadDir).append(File.separator).append("temp").append(File.separator).append(guid).append(File.separator).append(chunk).append(ext);
                File tempFile = new File(tempFileName.toString());
                //是否已存在分片,如果已存在分片则返回SUCCESS结果
                if (tempFile.exists()) {
                    return ResultGenerator.genSuccessResult("分片已经存在！跳过此分片！");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResultGenerator.genFailResult("error");
        }
        return ResultGenerator.genNullResult("不存在分片");
    }

    /**
     * @param chunks 当前所传文件的分片总数
     * @param chunk  当前所传文件的当前分片数
     * @return
     * @Description: 大文件分片上传
     * @author: 13
     */
    @ResponseBody
    @RequestMapping(value = "/files")
    public Result upload(HttpServletRequest request, String guid, Integer chunks, Integer chunk, String name, MultipartFile file) {
        String filePath = null;
        //上传存储路径
        String uploadDir = FileUtil.getRealPath(request);
        //后缀名
        String ext = name.substring(name.lastIndexOf("."));
        StringBuilder tempFileName = new StringBuilder();
        //等价于 uploadDir + "\\temp\\" + guid + "\\" + chunk + ext
        tempFileName.append(uploadDir).append(File.separator).append("temp").append(File.separator).append(guid).append(File.separator).append(chunk).append(ext);
        File tempFile = new File(tempFileName.toString());
        // 判断文件是否分块
        if (chunks != null && chunk != null) {
            //根据guid 创建一个临时的文件夹
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            try {
                //保存每一个分片
                file.transferTo(tempFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //如果当前是最后一个分片，则合并所有文件
            if (chunk == (chunks - 1)) {
                StringBuilder tempFileFolder = new StringBuilder();
                //等价于 uploadDir + "\\temp\\" + guid + File.separator
                tempFileFolder.append(uploadDir).append(File.separator).append("temp").append(File.separator).append(guid).append(File.separator);
                String newFileName = FileUtil.mergeFile(chunks, ext, tempFileFolder.toString(), request);
                filePath = "upload/chunked/" + newFileName;
            }
        } else {
            //不用分片的文件存储到files文件夹中
            StringBuilder destPath = new StringBuilder();
            destPath.append(uploadDir).append(File.separator).append("files").append(File.separator);
            String newName = System.currentTimeMillis() + ext;// 文件新名称
            try {
                FileUtil.saveFile(destPath.toString(), newName, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            filePath = "upload/files/" + newName;
        }
        Result result = ResultGenerator.genSuccessResult();
        result.setData(filePath);
        return result;
    }
}
