package org.bzk.documentserver.utils.minio;


import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * web 压缩解压工具
 */
public class WebCompressUtil {

    private static final int BUFFER_SIZE = 2048;


    public static List<String> unTar(MultipartFile file, MinioClient minioClient, String bucketName, String parentDir) {
        List<String> fileNames = new ArrayList<>();
        try {
            TarArchiveInputStream tarIn = new TarArchiveInputStream(file.getInputStream(), BUFFER_SIZE);
            TarArchiveEntry entry = null;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                if (entry.isFile()) {
                    //minioClient.putObject(bucketName, parentDir + entry.getName(), String.valueOf(tarIn), entry.getSize(), null, null, null);

                    minioClient.putObject(
                            PutObjectArgs.builder().bucket(bucketName).object(parentDir + entry.getName()).stream(
                                            tarIn, entry.getSize(), -1)
                                    .contentType(file.getContentType())
                                    .build());

                    fileNames.add(parentDir + entry.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileNames;
    }


    public static List<String> unZip(MultipartFile file, MinioClient minioClient, String bucketName, String parentDir) {
        List<String> fileNames = new ArrayList<>();
        try {
            // 创建客户端
            ZipArchiveInputStream is = new ZipArchiveInputStream(new BufferedInputStream(file.getInputStream(), BUFFER_SIZE));
            ZipArchiveEntry entry = null;
            while ((entry = is.getNextZipEntry()) != null) {
                if (!entry.isDirectory()) {
                    try {
                        // 使用putObject上传一个文件到存储桶中。


                     //   minioClient.putObject(bucketName, parentDir + entry.getName(), is, entry.getSize(), null, null, null);
                        String temp = entry.getName();
                        String temp1 = parentDir;

                        minioClient.putObject(
                                PutObjectArgs.builder().bucket(bucketName).object(parentDir + entry.getName()).stream(
                                                is, entry.getSize(), -1)
                                        .contentType(file.getContentType())
                                        .build());


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    fileNames.add(parentDir + entry.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileNames;
    }


    /**
     * 解压方法
     *
     * @param file 解压文件
     * @return
     * @throws Exception
     */
    public static List<String> unCompress(MultipartFile file, MinioClient minioClient, String bucketName, String parentDir) {
        List<String> ret = new ArrayList<>();
        try {
            String fileName = file.getOriginalFilename();
            String upperName = fileName.toUpperCase();
            if (upperName.endsWith(".ZIP")) {
                ret = unZip(file, minioClient, bucketName, parentDir);
            } else if (upperName.endsWith(".TAR")) {
                ret = unTar(file, minioClient, bucketName, parentDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


}
