package org.bzk.documentserver.utils;

import org.bzk.documentserver.exception.DocumentServerException;

import java.io.InputStream;

/**
 * @Author 2023/3/1 15:07 ly
 **/
public interface Saver {

    void processing(InputStream is) throws DocumentServerException;

//    void processing(InputStream is, String path) throws DocumentServerException;
}
