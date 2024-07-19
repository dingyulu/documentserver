package org.bzk.documentserver.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bzk.documentserver.utils.minio.MinioUploadUtil;
import org.hashids.Hashids;
import org.bzk.documentserver.constant.Error;
import org.bzk.documentserver.exception.DocumentServerException;
import org.bzk.documentserver.propertie.DocumentServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.awt.PeerEvent;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author 2023/2/28 11:29 ly
 **/
@Component

public class DocumentServerUtils {

    @Resource
    private DocumentServerProperties properties;
    @Autowired
    private MinioUploadUtil minioUploadUtil;

    public String callbackUrl(String id) {
        return String.format(properties.getCallback(), id);
    }

    public String downloadUrl(String id) {
        return String.format(properties.getDownload(), id);
    }

    public boolean canEdit(String ext) {
        if (ArrayUtils.contains(properties.getEditExt(), ext)) {
            return true;
        }
        return false;
    }

    public void assertCanView(String ext) throws DocumentServerException {
        if (!ArrayUtils.contains(properties.getViewExt(), ext)) {
            throw DocumentServerException.build(Error.FILETYPE_NOT_SUPPORT);
        }
    }

    /**
     * generate file key
     *
     * @param file
     * @return
     * @throws DocumentServerException
     */
    public String key(File file ) throws DocumentServerException {
        String fileMd5;
        try {
            fileMd5 = DigestUtils.md5Hex(new FileInputStream(file));
        } catch (IOException e) {
            throw DocumentServerException.build(Error.DOC_FILE_NOT_EXISTS);
        }
        if (StringUtils.isBlank(fileMd5)) {
            throw DocumentServerException.build(Error.DOC_FILE_MD5_ERROR);
        }
        Hashids hashids = new Hashids(properties.getHashkey());

        String key = hashids.encodeHex(String.format("%s%s", fileMd5, DigestUtils.md5Hex(file.getName())));

        if (StringUtils.isBlank(key)) {
            throw DocumentServerException.build(Error.DOC_FILE_KEY_ERROR);
        }
        return key;
    }


    public String keyMinio(InputStream stream , String objectName) throws DocumentServerException {
        String fileMd5;


        try {
            fileMd5 = DigestUtils.md5Hex(stream);


            if (StringUtils.isBlank(fileMd5)) {
                throw DocumentServerException.build(Error.DOC_FILE_MD5_ERROR);
            }


        Hashids hashids = new Hashids(properties.getHashkey());
        String key = hashids.encodeHex(String.format("%s%s", fileMd5, DigestUtils.md5Hex(objectName)));

        if (StringUtils.isBlank(key)) {
            throw DocumentServerException.build(Error.DOC_FILE_KEY_ERROR);
        }

        return key;
        } catch (IOException e) {
            System.out.println("minio key err+" +e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    public String getType(String s) throws DocumentServerException {
        if (StringUtils.isBlank(s)) {
            throw DocumentServerException.build(Error.DOC_FILE_NOT_EXISTS);
        }

        String s0 = StringUtils.lowerCase(FilenameUtils.getExtension(s));

        if (StringUtils.isBlank(s0)) {
            throw DocumentServerException.build(Error.DOC_FILE_NO_EXTENSION);
        }
        return s0;
    }

    public void assertSize(File file) throws DocumentServerException {
        if (file.length() > properties.getFileSize()) {
            Log.error("file is too large {} > {}", file.length(), properties.getFileSize());
            throw DocumentServerException.build(Error.DOC_FILE_OVERSIZE);
        }
    }

    public void assertType(File file) throws DocumentServerException {
        String ext = StringUtils.lowerCase(FilenameUtils.getExtension(file.getName()));
        if (!ArrayUtils.contains(properties.getViewExt(), ext)) {
            Log.error("not support type: {}", ext);
            throw DocumentServerException.build(Error.DOC_FILE_TYPE_UNSUPPORTED);
        }
    }

    public void assertCanRead(File file) throws DocumentServerException {
        if (!file.canRead()) {
            Log.error("file not read: {}", file.getName());
            throw DocumentServerException.build(Error.DOC_FILE_UNREADABLE);
        }
    }

    public void assertDirectory(File file) throws DocumentServerException {
        if (file.isDirectory()) {
            Log.error("file is directory: {}", file.getName());
            throw DocumentServerException.build(Error.DOC_FILE_EMPTY);
        }
    }

    public void assertExists(File file) throws DocumentServerException {
        if (!file.exists()) {
            Log.error("file file not exists");
            throw DocumentServerException.build(Error.DOC_FILE_NOT_EXISTS);
        }
    }

    public void checkFile(File file) throws DocumentServerException {
        assertExists(file);
        assertDirectory(file);
        assertCanRead(file);
        assertSize(file);
        assertType(file);
    }


}
