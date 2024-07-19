package org.bzk.documentserver.service;

import com.alibaba.fastjson2.JSONObject;
import org.bzk.documentserver.exception.DocumentServerException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @Author 2023/3/1 18:02 ly
 **/
public interface FileService {
    Map<String, Object> getFileInfo(String id);

    String getFilePath(String id);
    String getFileName(String id);
    public String getIdFileName(String id);
    Object pagination(String keyWord, String tenantId, String userId, String user, String SearchTemplateFlag, String fileName, int pageIndex, int pageSize,JSONObject options);
    int count(String keyWord,String tenantId,String userId,String user,String SearchTemplateFlag,String fileName);

    String upload(MultipartFile file,String templateCode,String documentCode,String id,String templateFlag,String templateId,
                  String tenantId,String userId,String user,String defaultConfigIn,String defaultConfigOut,String category) throws DocumentServerException;
    void save(MultipartFile file,String templateCode,String documentCode) throws DocumentServerException;

    Map<String, Object> getFileTempalte(String templateCode);
    int getCountFileTempalte(String templateCode,String templateFlag);

    Map<String, Object> getFileByDocumentCode(String documentCode);



    int getCountFileByDocumentCode(String documentCode);
    int getCountFileByTenantId(String tenantId);
    Map<String, Object> getFileByDocumentCodeAndTemplate(String templateCode,String documentCode);
    int getCountFileByDocumentCodeAndTemplate(String templateCode,String documentCode);
    public  Map<String, Object> getFileId(String id);
    void update(String id,String deleteFlag,String attachName,String  defaultConfigIn,String defaultConfigOut,String category,String tenantId,String userId);
}
