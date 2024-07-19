package org.bzk.documentserver.service;

import org.bzk.documentserver.bean.Document;
import org.bzk.documentserver.bean.EditorConfig;
import org.bzk.documentserver.exception.DocumentServerException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author 2023/2/27 9:06 ly
 **/
public interface DocumentServerService {

    Document buildDocument(String id) throws DocumentServerException;
    Document buildDocumentForTemplate(String templateId,String documentId) throws DocumentServerException;

    /**
     * 构建文档编辑参数 对象
     * @return
     */
    EditorConfig buildEditorConfig(String id, String user,String viewMode,String value,Integer zoom);


    void callBack(HttpServletRequest request, HttpServletResponse response) throws IOException, DocumentServerException;

    void download(String id, HttpServletResponse response) throws IOException;
    void downloadPdf(String id, HttpServletResponse response) throws IOException, DocumentServerException;
    void downloadPng(String id,String height,String width,String allPageFlag, HttpServletResponse response) throws IOException, DocumentServerException;
     void downloadOform(String id, String templateCode,  HttpServletResponse response,String tenantId,String userId,String user,String oformId,String config,String configOut,String category,String needClearId)throws IOException, DocumentServerException ;
    public void downloadDocxf(String id, String tenantId,String userId,String user,String templateCode) throws IOException, DocumentServerException;
}
