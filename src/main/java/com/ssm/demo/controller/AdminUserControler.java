package com.ssm.demo.controller;

import com.ssm.demo.common.Constants;
import com.ssm.demo.common.Result;
import com.ssm.demo.common.ResultGenerator;
import com.ssm.demo.controller.annotation.TokenToUser;
import com.ssm.demo.entity.AdminUser;
import com.ssm.demo.service.AdminUserService;
import com.ssm.demo.utils.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

/**
 * Created by 13 on 2018/7/4.
 */
@RestController
@RequestMapping("/users")
public class AdminUserControler {

    final static Logger logger = Logger.getLogger(AdminUserControler.class);

    @Autowired
    private AdminUserService adminUserService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Result login(@RequestBody AdminUser user) {
        logger.info("请求登陆方法，参数为{" + user.toString() + "}");
        Result result = ResultGenerator.genFailResult("登录失败");
        if (StringUtils.isEmpty(user.getUserName()) || StringUtils.isEmpty(user.getPassword())) {
            logger.error("未填写登陆信息，登陆失败");
            result.setMessage("请填写登录信息！");
        }
        AdminUser loginUser = adminUserService.updateTokenAndLogin(user.getUserName(), user.getPassword());
        if (loginUser != null) {
            result = ResultGenerator.genSuccessResult(loginUser);
        }
        logger.info("登陆成功，用户名为：" + user.getUserName());
        return result;
    }

    /**
     * 列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result list(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("page")) || StringUtils.isEmpty(params.get("limit"))) {
            logger.error("请求用户列表错误，参数异常！");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_PARAM_ERROR, "参数异常！");
        }
        //查询列表数据
        PageUtil pageUtil = new PageUtil(params);
        logger.info("请求用户列表成功，参数为 page:" + params.get("page").toString() + ",limit:" + params.get("limit").toString());
        return ResultGenerator.genSuccessResult(adminUserService.getAdminUserPage(pageUtil));
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public Result save(@RequestBody AdminUser user, @TokenToUser AdminUser loginUser) {
        if (loginUser == null) {
            logger.error("请求添加用户失败，未登录");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_NOT_LOGIN, "未登录！");
        }
        if (StringUtils.isEmpty(user.getUserName()) || StringUtils.isEmpty(user.getPassword())) {
            logger.error("请求添加用户失败，参数异常");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_PARAM_ERROR, "参数异常！");
        }
        AdminUser tempUser = adminUserService.selectByUserName(user.getUserName());
        if (tempUser != null) {
            logger.error("请求添加用户失败，用户已存在");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_PARAM_ERROR, "用户已存在勿重复添加！");
        }
        if ("admin".endsWith(user.getUserName().trim())) {
            logger.error("请求添加用户失败，不能添加admin用户");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_PARAM_ERROR, "不能添加admin用户！");
        }
        if (adminUserService.save(user) > 0) {
            logger.info("请求添加用户成功 " + user.toString());
            return ResultGenerator.genSuccessResult();
        } else {
            logger.error("请求添加用户失败 " + user.toString());
            return ResultGenerator.genFailResult("添加失败");
        }
    }

    /**
     * 修改
     */
    @RequestMapping("/updatePassword")
    public Result update(@RequestBody AdminUser user, @TokenToUser AdminUser loginUser) {
        if (loginUser == null) {
            logger.error("请求修改用户失败，未登录");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_NOT_LOGIN, "未登录！");
        }
        if (StringUtils.isEmpty(user.getPassword())) {
            logger.error("请求修改用户失败，未输入密码");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_PARAM_ERROR, "请输入密码！");
        }
        AdminUser tempUser = adminUserService.selectById(user.getId());
        if (tempUser == null) {
            logger.error("请求修改用户失败，无此用户");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_PARAM_ERROR, "无此用户！");
        }
        if ("admin".endsWith(tempUser.getUserName().trim())) {
            logger.error("请求修改用户失败，不能修改admin用户");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_PARAM_ERROR, "不能修改admin用户！");
        }
        tempUser.setPassword(user.getPassword());
        if (adminUserService.updatePassword(user) > 0) {
            logger.info("请求修改用户成功 " + user.toString());
            return ResultGenerator.genSuccessResult();
        } else {
            logger.info("请求修改用户失败 " + user.toString());
            return ResultGenerator.genFailResult("添加失败");
        }
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public Result delete(@RequestBody Integer[] ids, @TokenToUser AdminUser loginUser) {
        if (loginUser == null) {
            logger.error("请求删除用户失败，未登录");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_NOT_LOGIN, "未登录！");
        }
        if (ids.length < 1) {
            logger.error("请求删除用户失败，参数异常");
            return ResultGenerator.genErrorResult(Constants.RESULT_CODE_PARAM_ERROR, "参数异常！");
        }
        if (adminUserService.deleteBatch(ids) > 0) {
            logger.info("请求删除用户成功 " + Arrays.toString(ids));
            return ResultGenerator.genSuccessResult();
        } else {
            logger.error("请求删除用户失败 " + Arrays.toString(ids));
            return ResultGenerator.genFailResult("删除失败");
        }
    }

    /**
     * 批量导入用户V1
     * <p>
     * 批量导入用户(直接导入)
     */
    @RequestMapping(value = "/importV1", method = RequestMethod.POST)
    public Result saveByExcelFileV1(@RequestParam("file") MultipartFile multipartFile) {
        File file = FileUtil.convertMultipartFileToFile(multipartFile);
        if (file == null) {
            logger.error("上传文件为空，importV1导入失败");
            return ResultGenerator.genFailResult("导入失败");
        }
        int importResult = adminUserService.importUsersByExcelFile(file);
        if (importResult > 0) {
            Result result = ResultGenerator.genSuccessResult();
            result.setData(importResult);
            logger.info("importV1用户导入成功");
            return result;
        } else {
            logger.error("上传文件为空，importV1导入失败");
            return ResultGenerator.genFailResult("导入失败");
        }
    }

    /**
     * 批量导入用户V2
     * <p>
     * 批量导入用户(根据文件url导入)
     */
    @RequestMapping(value = "/importV2", method = RequestMethod.POST)
    public Result saveByExcelFileV2(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            logger.error("fileUrl为空，importV2导入失败");
            return ResultGenerator.genFailResult("fileUrl不能为空");
        }
        File file = FileUtil.downloadFile(fileUrl);
        if (file == null) {
            logger.error("文件不存在，importV2导入失败");
            return ResultGenerator.genFailResult("文件不存在");
        }
        int importResult = adminUserService.importUsersByExcelFile(file);
        if (importResult > 0) {
            Result result = ResultGenerator.genSuccessResult();
            result.setData(importResult);
            logger.info("importV2用户导入成功");
            return result;
        } else {
            logger.error("importV2导入失败");
            return ResultGenerator.genFailResult("导入失败");
        }
    }


    /**
     * 文件导出
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void exportUsers(HttpServletRequest request, HttpServletResponse response) {
        List<AdminUser> userList = adminUserService.getUsersForExport();
        //单元格表头
        String[] excelHeader = {"用户id", "用户名", "账号状态", "添加时间"};
        //字段名称
        String[] fileds = {"userId", "userName", "status", "createTime"};
        //单元格宽度内容格式
        int[] formats = {4, 2, 1, 1};
        //单元格宽度
        int[] widths = {256 * 14, 512 * 14, 256 * 14, 512 * 14};
        try {
            List<Map<String, Object>> excelData = new ArrayList<Map<String, Object>>();
            if (CollectionUtils.isNotEmpty(userList)) {
                for (AdminUser user : userList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", user.getId());
                    map.put("userName", user.getUserName());
                    map.put("status", user.getIsDeleted() == 0 ? "正常账号" : "废弃账号");
                    map.put("createTime", DateUtil.getDateString(user.getCreateTime()));
                    excelData.add(map);
                }
            }
            String excelName = "用户数据_" + System.currentTimeMillis();
            logger.info("用户数据导出");
            PoiUtil.exportFile(excelName, excelHeader, fileds, formats, widths, excelData, request, response);
        } catch (Exception e) {
            logger.error("用户数据导出失败" + e.toString());
        }
    }
}