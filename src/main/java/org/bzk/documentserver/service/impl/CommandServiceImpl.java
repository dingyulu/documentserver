package org.bzk.documentserver.service.impl;

import lombok.SneakyThrows;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.bzk.documentserver.exception.DocumentServerException;
import org.bzk.documentserver.manager.CommandManager;
import org.bzk.documentserver.service.CommandService;
import org.bzk.documentserver.service.FileService;
import org.bzk.documentserver.utils.CustomMap;
import org.bzk.documentserver.utils.DocumentServerUtils;
import org.bzk.documentserver.utils.minio.MinioUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.net.URLDecoder;

/**
 * @Author 2023/3/1 18:00 ly
 **/
@Service
public class CommandServiceImpl implements CommandService {

    @Resource
    private DocumentServerUtils documentServerUtils;
    @Resource
    private CommandManager commandManager;

    @Resource
    private FileService fileService;
    @Value("${fileType}")
    private String fileType;
    @Autowired
    private MinioUploadUtil minioUploadUtil;
    @Override
    public String forceSave(String id) throws DocumentServerException {
        String filePath = fileService.getFilePath(id);
        String fileName = fileService.getIdFileName(id);
        String  Key = "";
        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){
            Key = documentServerUtils.key(new File(filePath));
        }
        else{
            String bucketName = "onlinedocument";
            if(id.startsWith("aabbcc")){
                bucketName ="annex";
            }
            Key = documentServerUtils.keyMinio( minioUploadUtil.downloadMinio(fileName, bucketName),fileName);
        }
        System.out.println("强制保存1"+filePath+".,"+id);

        return commandManager.forceSave(Key, "simple data");
    }

    @SneakyThrows
    @Override
    public String drop(String id, String user) throws DocumentServerException {
        String filePath = fileService.getFilePath(id);
        String fileName = fileService.getIdFileName(id);
        String  Key = "";
        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){
            Key = documentServerUtils.key(new File(filePath));
        }
        else{
            String bucketName = "onlinedocument";
            if(id.startsWith("aabbcc")){
                bucketName ="annex";
            }
            Key = documentServerUtils.keyMinio( minioUploadUtil.downloadMinio(fileName, bucketName),fileName);
        }
        return commandManager.drop(Key, new String[]{"uid-" + URLDecoder.decode(user, CharEncoding.UTF_8)});
    }

    @Override
    public String info(String id) throws DocumentServerException {
        String filePath = fileService.getFilePath(id);
        String fileName = fileService.getIdFileName(id);
        String  Key = "";
        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){
            Key = documentServerUtils.key(new File(filePath));
        }
        else{

            String bucketName = "onlinedocument";
            if(id.startsWith("aabbcc")){
                bucketName ="annex";
            }
            Key = documentServerUtils.keyMinio( minioUploadUtil.downloadMinio(fileName, bucketName),fileName);
        }
        return commandManager.info(Key);
    }

    @Override
    public String license() {
        return commandManager.license();
    }

    @Override
    public String meta(String id) throws DocumentServerException {
        String filePath = fileService.getFilePath(id);
        String fileName = fileService.getIdFileName(id);
        String  Key = "";
        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){
            Key = documentServerUtils.key(new File(filePath));
        }
        else{


            String bucketName = "onlinedocument";
            if(id.startsWith("aabbcc")){
                bucketName ="annex";
            }
            Key = documentServerUtils.keyMinio( minioUploadUtil.downloadMinio(fileName, bucketName),fileName);
        }
        return commandManager.meta(Key, CustomMap.build(1).pu1("title", "test meta.docx"));
    }

    @Override
    public String version() {
        return commandManager.version();
    }
}
