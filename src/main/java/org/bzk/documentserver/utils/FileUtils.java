package org.bzk.documentserver.utils;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

import org.apache.commons.fileupload.FileItem;


import org.apache.commons.lang3.StringUtils;
import org.bzk.documentserver.constant.Error;
import org.bzk.documentserver.exception.DocumentServerException;
import org.bzk.documentserver.service.impl.MinoServer;
import org.bzk.documentserver.utils.minio.MinioUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;


import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
//import com.jcraft.jsch.*;
@Service
public class FileUtils {
//    private final MinoServer minoServer;
//    @Autowired
//    public FileUtils(MinoServer minoServer) {
//        this.minoServer = minoServer;
//    }
    public static void save(MultipartFile file, String path) throws DocumentServerException {
        try {
            IOUtils.copy(file.getInputStream(), new FileOutputStream(path));
        } catch (Exception e) {
            e.printStackTrace();
            throw DocumentServerException.build(Error.FILE_ERROR);
        }
    }

    public static void save(InputStream is, String ContentType,String path,   String fileType,String id, MinioUploadUtil minioUploadUtil) throws DocumentServerException {


        System.out.println("save fileType......." +fileType+" "+minioUploadUtil+" " +ContentType);
        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){

            try {
                IOUtils.copy(is, new FileOutputStream(path));
            } catch (Exception e) {
                e.printStackTrace();
                throw DocumentServerException.build(Error.FILE_ERROR);
            }
        }
        else{

              //  MultipartFile multipartFile = FileUtils.inputStreamToMultipartFile(is, getFileName(path));
            try {
                IOUtils.copy(is, new FileOutputStream(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(path.contains("databak")){
                return;
            }
            try {


                File file = new File(path);
                MultipartFile multipartFile = FileUtils.getMultipartFile(file);
                String bucketName = "onlinedocument";
                if(id.startsWith("aabbcc")){
                    bucketName ="annex";
                }
                minioUploadUtil.uploadFile(multipartFile, bucketName, getFileName(path));
                file.delete();
                file.deleteOnExit();
            }catch (Exception e){
                e.printStackTrace();
                System.out.println(e.getMessage());
            }

        }





    }

    public static String getFileName(String path) {
        Path p = Paths.get(path);
        return p.getFileName().toString();
    }
    public static File copy( String source, String target) throws DocumentServerException {

        String sourceFilePath =source;// "C:\\example\\test.txt";
           String targetFilePath = target;//"C:\\example\\output.txt";
           File sourceFile = new File(sourceFilePath);
           File targetFile = new File(targetFilePath);
           try {

               InputStream inputStream = new FileInputStream(sourceFile);
               OutputStream outputStream = new FileOutputStream(targetFile);
                IOUtils.copy(inputStream, outputStream);
               inputStream.close();
               outputStream.close();
            } catch (IOException e) {
               e.printStackTrace();
               throw DocumentServerException.build(Error.FILE_ERROR);
            }

           return targetFile;
       }

    public static MultipartFile inputStreamToMultipartFile(InputStream inputStream, String ContentType,String originalFilename) throws IOException {
        // 读取 inputStream 的内容到 byte 数组中
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);

        // 获取原始文件的内容类型
        // 获取文件的扩展名
        String fileExtension = getFileExtension(originalFilename);

        // 根据扩展名设置文件的内容类型
        String contentType = "application/octet-stream"; // 默认值为application/octet-stream

        if (fileExtension != null) {
            switch (fileExtension.toLowerCase()) {
                case "jpg":
                case "jpeg":
                    contentType = "image/jpeg";
                    break;
                case "png":
                    contentType = "image/png";
                    break;
                case "json":
                    contentType = "application/json";
                    break;
                // 添加更多的扩展名和相应的内容类型
            }
        }


        // 创建 MultipartFile 对象
        MultipartFile multipartFile = new MockMultipartFile(originalFilename, originalFilename, contentType, buffer);
        return multipartFile;
    }

    public static String getFileExtension(String filename) {
        if (filename != null && filename.lastIndexOf(".") != -1) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }
    public static MultipartFile getMultipartFile(File file) {
        FileItem item = new DiskFileItemFactory().createItem("file"
                //, MediaType.MULTIPART_FORM_DATA_VALUE
                , MediaType.APPLICATION_OCTET_STREAM_VALUE
                , true
                , file.getName());
        try (InputStream input = new FileInputStream(file);
             OutputStream os = item.getOutputStream()) {
            // 流转移
            IOUtils.copy(input, os);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid file: " + e, e);
        }

        return new CommonsMultipartFile(item);
    }

    public static MultipartFile getMultipartFileAndName(File file,String name) {
        FileItem item = new DiskFileItemFactory().createItem("file"
                , MediaType.MULTIPART_FORM_DATA_VALUE
                , true
                ,name);
        try (InputStream input = new FileInputStream(file);
             OutputStream os = item.getOutputStream()) {
            // 流转移
            IOUtils.copy(input, os);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid file: " + e, e);
        }

        return new CommonsMultipartFile(item);
    }



//    import java.io.*;
//import org.apache.commons.io.IOUtils;
//
//    public class CopyFileExample {
//        public static void main(String[] args) {
//            String sourceFilePath = "C:\\example\\test.txt";
//            String targetFilePath = "C:\\example\\output.txt";
//            File sourceFile = new File(sourceFilePath);
//            File targetFile = new File(targetFilePath);
//            try {
//                InputStream inputStream = new FileInputStream(sourceFile);
//                OutputStream outputStream = new FileOutputStream(targetFile);
//                IOUtils.copy(inputStream, outputStream);
//                inputStream.close();
//                outputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }



    /**
     * 前端下载文件方法
     *
     * @param filepath
     * @param response
     * @throws IOException
     */
    public static void getResource(String filepath, String filename, HttpServletResponse response) throws IOException {
        File file = new File(filepath);
        FileInputStream fis = new FileInputStream(file);
        // 清空response
        response.reset();
        // 设置response的Header
        response.setCharacterEncoding(CharEncoding.UTF_8);
        //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
        //attachment表示以附件方式下载   inline表示在线打开   "Content-Disposition: inline; filename=文件名.mp3"
        // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + URLEncoder.encode(filename, CharEncoding.UTF_8));
        // 告知浏览器文件的大小
        response.addHeader(HttpHeaders.CONTENT_LENGTH, "" + file.length());
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        outputStream.write(buffer);
        IOUtils.copy(fis, response.getOutputStream());
        fis.close();
        response.getOutputStream().flush();
    }

    public static void getResourcePdf(String filepath, String filename, HttpServletResponse response) throws IOException {
        File file = new File(filepath);
        FileInputStream fis = new FileInputStream(file);
        // 清空response
        response.reset();
        // 设置response的Header
        response.setCharacterEncoding(CharEncoding.UTF_8);
        //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
        //attachment表示以附件方式下载   inline表示在线打开   "Content-Disposition: inline; filename=文件名.mp3"
        // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + URLEncoder.encode(filename, CharEncoding.UTF_8));
        // 告知浏览器文件的大小
        response.addHeader(HttpHeaders.CONTENT_LENGTH, "" + file.length());
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/pdf");
//        outputStream.write(buffer);
        IOUtils.copy(fis, response.getOutputStream());
        fis.close();
        response.getOutputStream().flush();
    }



    public static void dcoxtoPDF( String docxFilePath,  String pdfFilePath ){
        File file = new File(pdfFilePath);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //Address是将要被转化的word文档
        Document doc = null;
        try {
            doc = new Document(docxFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF, EPUB, XPS, SWF 相互转换
        try {
            doc.save(os, SaveFormat.PDF);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
