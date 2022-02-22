package com.ssm.demo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 13
 * @date 2017-7-31
 */
public class DateUtil {

    /**
     * 格式化date
     *
     * @param date
     * @return
     */
    public static String getDateString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
}
