package org.bzk.documentserver.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author 2023/2/28 16:20 ly
 **/
@Slf4j
public class Log {

    private final static String LOG_PREFIX = "[tencodingoffice ducomentserver]# ";

    public static void error(String msg, Object... s) {
        log.error(LOG_PREFIX + msg, s);
    }

    public static void info(String msg, Object... s) {
        log.info(LOG_PREFIX + msg, s);
    }
}
