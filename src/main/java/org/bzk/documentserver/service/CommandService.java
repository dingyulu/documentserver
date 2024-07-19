package org.bzk.documentserver.service;

import org.bzk.documentserver.exception.DocumentServerException;

/**
 * @Author 2023/3/1 17:59 ly
 **/
public interface CommandService {

    String forceSave(String id) throws DocumentServerException;

    String drop(String id, String user) throws DocumentServerException;

    String info(String id) throws DocumentServerException;

    String license() throws DocumentServerException;

    String meta(String id) throws DocumentServerException;

    String version() throws DocumentServerException;
}
