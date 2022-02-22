package com.ssm.demo.service.impl;

import com.ssm.demo.dao.AdminUserDao;
import com.ssm.demo.entity.AdminUser;
import com.ssm.demo.service.AdminUserService;
import com.ssm.demo.utils.*;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by 13 on 2018/7/4.
 */
@Service("adminUserService")
public class AdminUserServiceImpl implements AdminUserService {

    final static Logger logger = Logger.getLogger(AdminUserServiceImpl.class);

    @Autowired
    private AdminUserDao adminUserDao;

    @Override
    public PageResult getAdminUserPage(PageUtil pageUtil) {
        List<AdminUser> users = adminUserDao.findAdminUsers(pageUtil);
        int total = adminUserDao.getTotalAdminUser(pageUtil);
        PageResult pageResult = new PageResult(users, total, pageUtil.getLimit(), pageUtil.getPage());
        logger.info("getAdminUserPage()方法调用，参数为：" + pageUtil.toString());
        return pageResult;
    }

    @Override
    public AdminUser updateTokenAndLogin(String userName, String password) {
        AdminUser adminUser = adminUserDao.getAdminUserByUserNameAndPassword(userName, MD5Util.MD5Encode(password, "UTF-8"));
        if (adminUser != null) {
            //登录后即执行修改token的操作
            String token = getNewToken(System.currentTimeMillis() + "", adminUser.getId());
            if (adminUserDao.updateUserToken(adminUser.getId(), token) > 0) {
                //返回数据时带上token
                adminUser.setUserToken(token);
                logger.info("updateTokenAndLogin()方法调用成功，参数为userName:" + userName + ",password:" + password);
                return adminUser;
            }
        }
        logger.error("updateTokenAndLogin()方法调用失败，参数为userName:" + userName + ",password:" + password);
        return null;
    }

    /**
     * 获取token值
     *
     * @param sessionId
     * @param userId
     * @return
     */
    private String getNewToken(String sessionId, Long userId) {
        String src = sessionId + userId + NumberUtil.genRandomNum(4);
        return SystemUtil.genToken(src);
    }

    @Override
    public AdminUser getAdminUserByToken(String userToken) {
        return adminUserDao.getAdminUserByToken(userToken);
    }

    @Override
    public AdminUser selectById(Long id) {
        return adminUserDao.getAdminUserById(id);
    }

    @Override
    public AdminUser selectByUserName(String userName) {
        return adminUserDao.getAdminUserByUserName(userName);
    }

    @Override
    public int save(AdminUser user) {
        //密码加密
        user.setPassword(MD5Util.MD5Encode(user.getPassword(), "UTF-8"));
        logger.info("save()方法调用成功，参数为:" + user.toString());
        return adminUserDao.addUser(user);
    }

    @Override
    public int updatePassword(AdminUser user) {
        logger.info("updatePassword()方法调用");
        return adminUserDao.updateUserPassword(user.getId(), MD5Util.MD5Encode(user.getPassword(), "UTF-8"));
    }

    @Override
    public int deleteBatch(Integer[] ids) {
        logger.info("deleteBatch()方法调用，参数为:" + Arrays.toString(ids));
        return adminUserDao.deleteBatch(ids);
    }

    @Override
    public List<AdminUser> getUsersForExport() {
        return adminUserDao.getAllAdminUsers();
    }

    @Override
    public int importUsersByExcelFile(File file) {
        XSSFSheet xssfSheet = null;
        try {
            //读取file对象并转换为XSSFSheet类型对象进行处理
            xssfSheet = PoiUtil.getXSSFSheet(file);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        List<AdminUser> adminUsers = new ArrayList<>();
        //第一行是表头因此默认从第二行读取
        for (int rowNum = 1; rowNum <= xssfSheet.getLastRowNum(); rowNum++) {
            //按行读取数据
            XSSFRow xssfRow = xssfSheet.getRow(rowNum);
            if (xssfRow != null) {
                //实体转换
                AdminUser adminUser = convertXSSFRowToAdminUser(xssfRow);
                //用户验证 已存在或者为空则不进行insert操作
                if (!StringUtils.isEmpty(adminUser.getUserName()) && !StringUtils.isEmpty(adminUser.getPassword()) && selectByUserName(adminUser.getUserName()) == null) {
                    adminUsers.add(adminUser);
                }
            }
        }
        //判空
        if (!CollectionUtils.isEmpty(adminUsers)) {
            //adminUsers用户列表不为空则执行批量添加sql
            return adminUserDao.insertUsersBatch(adminUsers);
        }
        return 0;
    }

    /**
     * 方法抽取
     * 将解析的列转换为AdminUser对象
     *
     * @param xssfRow
     * @return
     */
    private AdminUser convertXSSFRowToAdminUser(XSSFRow xssfRow) {
        AdminUser adminUser = new AdminUser();
        //用户名
        XSSFCell userName = xssfRow.getCell(0);
        //密码
        XSSFCell orinalPassword = xssfRow.getCell(1);
        //设置用户名
        if (!StringUtils.isEmpty(userName)) {
            adminUser.setUserName(PoiUtil.getValue(userName));
        }
        //对读取的密码进行加密并设置到adminUser对象中
        if (!StringUtils.isEmpty(orinalPassword)) {
            adminUser.setPassword(MD5Util.MD5Encode(PoiUtil.getValue(orinalPassword), "UTF-8"));
        }
        return adminUser;
    }
}
