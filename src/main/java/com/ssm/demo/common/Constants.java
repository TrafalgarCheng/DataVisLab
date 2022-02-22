package com.ssm.demo.common;

/**
 * Created by 13 on 2017/6/26.
 */
public class Constants {

    public static final int RESULT_CODE_SUCCESS = 200;  // 成功处理请求
    public static final int RESULT_CODE_BAD_REQUEST = 412;  // 请求错误
    public static final int RESULT_CODE_NOT_LOGIN = 402;  // 未登录
    public static final int RESULT_CODE_PARAM_ERROR = 406;  // 传参错误
    public static final int RESULT_CODE_CHUNK_EXIST = 298;  // 自定义code 分片已存在
    public static final int RESULT_CODE_CHUNK_NOTEXIST = 299;  // 自定义code 分片不存在
    public static final int RESULT_CODE_SERVER_ERROR = 500;  // 服务器错误

    public final static int PAGE_SIZE = 10;//默认分页条数

    public final static String FILE_PRE_URL = "http://140.143.248.96:8080";//上传文件的默认url前缀，根据部署设置自行修改
}
