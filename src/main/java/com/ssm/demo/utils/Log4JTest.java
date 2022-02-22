package com.ssm.demo.utils;

import org.apache.log4j.Logger;

public class Log4JTest {
    static Logger log = Logger.getLogger(Log4JTest.class);//获取日志记录器

    public static void main(String[] arg0) {
        for (int i = 0; i < 100; i++) {
            log.info("Log4J:" + i);
        }
    }
}
