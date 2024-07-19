package org.bzk.documentserver.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.bzk.documentserver.bean.Document;
import org.bzk.documentserver.bean.EditorConfig;
import org.bzk.documentserver.constant.Error;
import org.bzk.documentserver.exception.DocumentServerException;
import org.bzk.documentserver.propertie.DocumentServerProperties;
import org.bzk.documentserver.service.CommandService;
import org.bzk.documentserver.service.DocumentServerService;
import org.bzk.documentserver.service.FileService;
import org.bzk.documentserver.utils.*;
import org.bzk.documentserver.utils.minio.MinioUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @Author 2023/2/27 9:07 ly
 **/
@SuppressWarnings("ALL")
@Service
public class DocumentServerServiceImpl implements DocumentServerService {


    @Resource
    private DocumentServerUtils documentServerUtils;

    @Resource
    private FileService fileService;
    @Resource
    private DocumentServerProperties properties;
    @Value("${upload.pathbak}")
    private String pathbak;

    @Value("${upload.path}")
    private String path;
    @Value("${fileType}")
    private String fileType;
    @Autowired
    private MinioUploadUtil minioUploadUtil;

    @Resource
    private CommandService commandApiService;


    @Override
    public Document buildDocument(String id) throws DocumentServerException {
        Map<String, Object> obj = fileService.getFileInfo(id);
        return buildDocument(id, (String) obj.get("path"), (String) obj.get("attach_show_name"),(String) obj.get("user"));
    }


    @Override
    public Document buildDocumentForTemplate(String templateId,String documentId) throws DocumentServerException {
        Map<String, Object> obj = fileService.getFileInfo(templateId);
        return buildDocument(templateId, (String) obj.get("path"), (String) obj.get("attach_show_name"),(String) obj.get("user"));
    }

    private Document buildDocument(String id, String path, String name,String user) throws DocumentServerException {

        String type = documentServerUtils.getType(path);
        Map<String, Object> obj = fileService.getFileId(id);
       String fileName = (String) obj.get("attach_file_name") ;
        System.out.println("fileName..........."+ id +" " + obj);

        // 如果指定了文件名，则需要校验和实体文件格式是否一致
        boolean bln0 = StringUtils.isNotBlank(name);
        if (bln0 && !type.equalsIgnoreCase(FilenameUtils.getExtension(name))) {
            throw DocumentServerException.build(Error.DOC_FILE_EXTENSION_NOT_MATCH);
        }

        File file ;
        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){

            file = new File(FilenameUtils.normalize(path));
        }
        else{

            String bucketName = "onlinedocument";
            if(id.startsWith("aabbcc")){
                bucketName ="annex";
            }
            file = minioUploadUtil.getFileContent(fileName, bucketName,true,fileName);

        }

        System.out.println("check path...."+path);
        String Key = "";
        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){
            Key = documentServerUtils.key(file);
        }
        else{
            String bucketName = "onlinedocument";
            if(id.startsWith("aabbcc")){
                bucketName ="annex";
            }
            Key = documentServerUtils.keyMinio( minioUploadUtil.downloadMinio(fileName, bucketName),fileName);
        }

            // 校验文件实体
        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")) {
            documentServerUtils.checkFile(file);
        }


           Document document = Document.builder()
                .fileType(type)
                .title(bln0 ? name : file.getName())
//                .storage(filePath)
                .key(Key)
                .url(documentServerUtils.downloadUrl(id))
                .application("bzk")
                .info(Document.Info.builder()
                        .owner(user)
                        .application("bzk").build())
                .permissions(Document.Permissions.builder()
                        .chat(true)
                        .comment(true).build())
                .build();

           return document;

    }


    @Override
    public EditorConfig buildEditorConfig(String id, String user,String viewMode,String value,Integer zoom) {
        if(StringUtils.isEmpty(viewMode) ||"undefined".equals(viewMode)||"null".equals(viewMode)){
            viewMode = "edit";
        }
        return EditorConfig.builder()
                .mode(viewMode)
                .defalut(value)
                .zoom(zoom)
                .coEditing(EditorConfig.CoEditing.builder()
                        .mode("strict")
                        .change(false)
                        .build())
                .callbackUrl(documentServerUtils.callbackUrl(id))
                .user(EditorConfig.User.builder()
                        .id("uid-"+user)
                        .name(user)
                        .build())
                .build();
    }

//    @Override
//    public void download(String id, HttpServletResponse response) throws IOException {
//        Map<String, Object> obj = fileService.getFileInfo(id);
//
//        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){
//
//            FileUtils.getResource((String) obj.get("path"), (String) obj.get("attach_show_name"), response);
//        }
//        else{
//            minioUploadUtil.dowloadMinioFile((String)obj.get("attach_show_name"),"onlinedocument");
//        }
//    }

    @Override
    public void download(String id, HttpServletResponse response) throws IOException {
        Map<String, Object> obj = fileService.getFileId(id);

        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){

            FileUtils.getResource((String) obj.get("path"), (String) obj.get("attach_file_name"), response);
        }
        else{
            String bucketName = "onlinedocument";
            if(id.startsWith("aabbcc")){
                bucketName ="annex";
            }
            minioUploadUtil.dowloadMinioFile((String)obj.get("attach_file_name"),bucketName);
        }
    }

    @Override
    public void downloadPdf(String id, HttpServletResponse response) throws IOException, DocumentServerException {
        Map<String, Object> obj = fileService.getFileInfo(id);
        String fileName = fileService.getFileName(id);
        String sourcePath = (String) obj.get("path");


        String expensionName = sourcePath.substring(sourcePath.indexOf(".")+1,sourcePath.length());

        File file = new File(FilenameUtils.normalize(sourcePath));

        String fileIdName = fileService.getIdFileName(id);
        String  key = "";
        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){
            key =documentServerUtils.key(file);
        }
        else{

            String bucketName = "onlinedocument";
            if(id.startsWith("aabbcc")){
                bucketName ="annex";
            }
            key = documentServerUtils.keyMinio( minioUploadUtil.downloadMinio(fileIdName, bucketName),fileIdName);
        }

       // String key =documentServerUtils.key(file);
        String url = documentServerUtils.downloadUrl(id);
        JSONObject json = new JSONObject();
//        {
//            "async": false,
//                "filetype": "docx",
//                "key": "Khirz6zTPdfd7",
//                "outputtype": "pdf",
//                "title": "Example Document Title.docx",
//                "url": "https://example.com/url-to-example-document.docx"
//        }
        json.put("async",false);
        json.put("filetype",expensionName);
        json.put("key",key);
        json.put("outputtype","pdf");
        json.put("password","123456");
        // json.put("title","pdf");
        json.put("url",url);

        JSONObject documentLayout = new JSONObject();
        documentLayout.put("drawPlaceHolders",true);
        documentLayout.put("drawFormHighlight",false);
        documentLayout.put("isPrint",true);
        json.put("documentLayout",documentLayout);
//
//
//        String temp2 ="{\"spreadsheetLayout\":{\"ignorePrintArea\":true,\"orientation\":\"portrait\",\"fitToWidth\":\"0\",\"fitToHeight\":\"0\",\"scale\":\"100\",\"headings\":false,\"gridLines\":false,\"pageSize\":{\"width\":\"210mm\",\"height\":\"297mm\"},\"margins\":{\"left\":\"17.8mm\",\"right\":\"17.8mm\",\"top\":\"19.1mm\",\"bottom\":\"19.1mm\"}}}";
//        JSONObject spreadsheetLayout = JSONObject.parseObject(temp2);
//        json.put("spreadsheetLayout",spreadsheetLayout.getJSONObject("spreadsheetLayout"));

        // json.put("url","https://doc.baizhanke.com/api/download?id=20230926162728116");
        System.out.println("convert param: " + json);
        String post = HttpUtils.post(properties.getConverter(), JSON.toJSONString(json));
        System.out.println("convert result"+post);
        JSONObject temp = JSONObject.parseObject(post);
        String url2 = temp.getString("fileUrl");

//        String value="";
//        // 使用 String.format() 插入变量到正则表达式中
//        String regex = String.format("([^/]+)\\.%s$", expensionName);
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(sourcePath);
//        if (matcher.find()) {
//            value = matcher.group(1);
//            System.out.println(value); // 输出: 20230715111945115
//        }
//        String fileNameReal = fileService.getFileName(value);
        // fileName="报价2023092616.oform";
        int lastSlashIndex = fileName.lastIndexOf(".");
        System.out.println("filename"+ String.valueOf(lastSlashIndex) +"..."+fileName);
        String finalResult = fileName.substring(0, lastSlashIndex);
        String contetnType ="";
        if(obj.get("content_type")!=null){
            contetnType = (String) obj.get("content_type");
        }
        String finalContetnType = contetnType;
        HttpUtils.download(url2, (InputStream is) -> FileUtils.save(is, finalContetnType, pathbak +finalResult+"_1"+".pdf",  fileType,id,  minioUploadUtil));

       // String sourcePath = "/home/ubuntu/data/documentServer/data/20230925220537775.oform";
      //  String targetPath = pathbak + "/home/ubuntu/data/documentServer/databak/12333.pdf";
//        int lastSlashIndex = fileName.lastIndexOf(".");
//        System.out.println("filename"+ String.valueOf(lastSlashIndex) +"..."+fileName);
//        String finalResult = fileName.substring(0, lastSlashIndex)+".pdf";
        String targetPath = pathbak +finalResult+".pdf";
       // PdfFormToReadOnlyConverter.convert(pathbak +finalResult+"_1"+".pdf",targetPath);
       PdfFormToReadOnlyConverter.convert(pathbak +finalResult+"_1"+".pdf",targetPath);
        // 获取源文件的扩展名
       // String sourceExtension = sourcePath.substring(sourcePath.lastIndexOf("."));

        // 替换目标路径中的文件名
        //targetPath = targetPath.substring(0, targetPath.lastIndexOf("/") + 1) + "12333" + sourceExtension;
      //  DocxToPdfConverter.run(targetPath,pathbak +finalResult+".pdf");

        System.out.println("目标路径: " + targetPath);
        FileUtils.getResourcePdf(targetPath, finalResult+".pdf", response);
    }

    @Override
    public void downloadOform(String id, String templateCode, HttpServletResponse response,String tenantId,String userId,String user,String oformId,String config,String configOut,String category,String needClearId) throws IOException, DocumentServerException {
        Map<String, Object> obj = fileService.getFileInfo(id);
        String fileName = fileService.getFileName(id);
        String sourcePath = (String) obj.get("path");
        String expensionName = sourcePath.substring(sourcePath.indexOf(".")+1,sourcePath.length());

        File file = new File(FilenameUtils.normalize(sourcePath));


        String fileIdName = fileService.getIdFileName(id);
        String  key = "";
        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){
            key =documentServerUtils.key(file);
        }
        else{

            String bucketName = "onlinedocument";
            if(id.startsWith("aabbcc")){
                bucketName ="annex";
            }
            key = documentServerUtils.keyMinio( minioUploadUtil.downloadMinio(fileIdName, bucketName),fileIdName);
        }
       // String key =documentServerUtils.key(file);
        String url = documentServerUtils.downloadUrl(id);
        JSONObject json = new JSONObject();
//        {
//            "async": false,
//                "filetype": "docx",
//                "key": "Khirz6zTPdfd7",
//                "outputtype": "pdf",
//                "title": "Example Document Title.docx",
//                "url": "https://example.com/url-to-example-document.docx"
//        }
        json.put("async",false);
        json.put("filetype",expensionName);
        json.put("key",key);
        json.put("outputtype","oform");
        json.put("password","123456");
        // json.put("title","pdf");
        json.put("url",url);
        // json.put("url","https://doc.baizhanke.com/api/download?id=20230926162728116");
        System.out.println("convert param: " + json);
        String post = HttpUtils.post(properties.getConverter(), JSON.toJSONString(json));
        System.out.println("convert result"+post);
        JSONObject temp = JSONObject.parseObject(post);
        String url2 = temp.getString("fileUrl");

//        String value="";
//        // 使用 String.format() 插入变量到正则表达式中
//        String regex = String.format("([^/]+)\\.%s$", expensionName);
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(sourcePath);
//        if (matcher.find()) {
//            value = matcher.group(1);
//            System.out.println(value); // 输出: 20230715111945115
//        }
//        String fileNameReal = fileService.getFileName(value);
        // fileName="报价2023092616.oform";
        int lastSlashIndex = fileName.lastIndexOf(".");
        System.out.println("filename"+ String.valueOf(lastSlashIndex) +"..."+fileName);
        String finalResult = fileName.substring(0, lastSlashIndex);
        String contetnType ="";
        if(obj.get("content_type")!=null){
            contetnType = (String) obj.get("content_type");
        }
        String finalContetnType = contetnType;
        HttpUtils.download(url2, (InputStream is) -> FileUtils.save(is,finalContetnType, pathbak +finalResult+".oform", fileType,  id,minioUploadUtil));
        Date now = new Date();
        String documentId = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now);
        if(StringUtils.isNotEmpty(oformId)){
            documentId = oformId;
        }
        File file1 = new File(pathbak, finalResult+".oform");
        Random rand = new Random();
        // 最小值是 10,000,000，最大值是 99,999,999。
        int min = 10000000;
        int randomNum = min + rand.nextInt(90000000);  // 90,000,000 是因为 99,999,999 - 10,000,000 = 90,000,000
        int randomNum1 = min + rand.nextInt(90000000);  // 90,000,000 是因为 99,999,999 - 10,000,000 = 90,000,000
        if(StringUtils.isEmpty(templateCode)){
            templateCode = String.valueOf(randomNum);
        }
        MultipartFile multipartFile = FileUtils.getMultipartFile(file1);
        fileService.upload(multipartFile,templateCode,String.valueOf(randomNum1),documentId,"1","",tenantId,userId,user, config, configOut,category);



//        String targetPath = pathbak +finalResult+".oform";
//
//
//        System.out.println("目标路径: " + targetPath);
//        FileUtils.getResource(targetPath, finalResult+".oform", response);
    }
    @Override
    public void downloadDocxf(String id, String tenantId,String userId,String user,String templateCode) throws IOException, DocumentServerException {
        Map<String, Object> obj = fileService.getFileInfo(id);
        String fileName = fileService.getFileName(id);
        String sourcePath = (String) obj.get("path");
        String expensionName = sourcePath.substring(sourcePath.indexOf(".")+1,sourcePath.length());

        File file = new File(FilenameUtils.normalize(sourcePath));
        String fileIdName = fileService.getIdFileName(id);
        String  key = "";
        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){
            key =documentServerUtils.key(file);
        }
        else{

            String bucketName = "onlinedocument";
            if(id.startsWith("aabbcc")){
                bucketName ="annex";
            }
            key = documentServerUtils.keyMinio( minioUploadUtil.downloadMinio(fileIdName, bucketName),fileIdName);
        }
        String url = documentServerUtils.downloadUrl(id);
        JSONObject json = new JSONObject();
//        {
//            "async": false,
//                "filetype": "docx",
//                "key": "Khirz6zTPdfd7",
//                "outputtype": "pdf",
//                "title": "Example Document Title.docx",
//                "url": "https://example.com/url-to-example-document.docx"
//        }
        json.put("async",false);
        json.put("filetype",expensionName);
        json.put("key",key);
        json.put("outputtype","docxf");
        json.put("password","123456");
        // json.put("title","pdf");
        json.put("url",url);
        // json.put("url","https://doc.baizhanke.com/api/download?id=20230926162728116");
        System.out.println("convert param: " + json);
        String post = HttpUtils.post(properties.getConverter(), JSON.toJSONString(json));
        System.out.println("convert result"+post);
        JSONObject temp = JSONObject.parseObject(post);
        String url2 = temp.getString("fileUrl");

//        String value="";
//        // 使用 String.format() 插入变量到正则表达式中
//        String regex = String.format("([^/]+)\\.%s$", expensionName);
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(sourcePath);
//        if (matcher.find()) {
//            value = matcher.group(1);
//            System.out.println(value); // 输出: 20230715111945115
//        }
//        String fileNameReal = fileService.getFileName(value);
        // fileName="报价2023092616.oform";
        int lastSlashIndex = fileName.lastIndexOf(".");
        System.out.println("filename"+ String.valueOf(lastSlashIndex) +"..."+fileName);
        String finalResult = fileName.substring(0, lastSlashIndex);
        String contetnType ="";
        if(obj.get("content_type")!=null){
            contetnType = (String) obj.get("content_type");
        }
        String finalContetnType = contetnType;
        HttpUtils.download(url2, (InputStream is) -> FileUtils.save(is,finalContetnType, pathbak +finalResult+".docxf", fileType,id,  minioUploadUtil));
        Date now = new Date();
        String documentId = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now);
        File file1 = new File(pathbak, finalResult+".docxf");
        Random rand = new Random();
        // 最小值是 10,000,000，最大值是 99,999,999。
        int min = 10000000;
        int randomNum = min + rand.nextInt(90000000);  // 90,000,000 是因为 99,999,999 - 10,000,000 = 90,000,000
        int randomNum1 = min + rand.nextInt(90000000);  // 90,000,000 是因为 99,999,999 - 10,000,000 = 90,000,000
        MultipartFile multipartFile = FileUtils.getMultipartFile(file1);
        if(StringUtils.isEmpty(templateCode)){
            templateCode =String.valueOf(randomNum);

        }
        fileService.upload(multipartFile,templateCode,String.valueOf(randomNum1),documentId,"2","",tenantId,userId,user, "", "","");



//        String targetPath = pathbak +finalResult+".oform";
//
//
//        System.out.println("目标路径: " + targetPath);
//        FileUtils.getResource(targetPath, finalResult+".oform", response);
    }

    @Override
    public void downloadPng(String id, String height,String width,String allPage,HttpServletResponse response) throws IOException, DocumentServerException {
        Integer h =100;
        Integer w= 100;
        Boolean firstPage = true;
        if(StringUtils.isNotEmpty(height)){
           h= Integer.valueOf(height);
       }
        if(StringUtils.isNotEmpty(width)){
            w= Integer.valueOf(width);
        }
        if(StringUtils.isNotEmpty(allPage)){
           if("2".equals(allPage)){
               firstPage =false;
           }
        }
        Map<String, Object> obj = fileService.getFileInfo(id);
        String fileName = fileService.getFileName(id);
        String sourcePath = (String) obj.get("path");
        String expensionName = sourcePath.substring(sourcePath.indexOf(".")+1,sourcePath.length());

        File file = new File(FilenameUtils.normalize(sourcePath));
        String fileIdName = fileService.getIdFileName(id);
        String  key = "";
        if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){
            key =documentServerUtils.key(file);
        }
        else{
            String bucketName = "onlinedocument";
            if(id.startsWith("aabbcc")){
                bucketName ="annex";
            }
            key = documentServerUtils.keyMinio( minioUploadUtil.downloadMinio(fileIdName, bucketName),fileIdName);
        }
        String url = documentServerUtils.downloadUrl(id);
        JSONObject json = new JSONObject();
//        {
//            "async": false,
//                "filetype": "docx",
//                "key": "Khirz6zTPdfd7",
//                "outputtype": "pdf",
//                "title": "Example Document Title.docx",
//                "url": "https://example.com/url-to-example-document.docx"
//        }
//
//        "thumbnail": {
//            "aspect": 0,
//                    "first": true,
//                    "height": 150,
//                    "width": 100
//        },
        JSONObject temp1 =new JSONObject();
        temp1.put( "aspect", 0);
        temp1.put( "first", firstPage);
        temp1.put( "height", h);
        temp1.put( "width", w);
        json.put("thumbnail",temp1);


        json.put("async",false);
        json.put("filetype",expensionName);
        json.put("key",key);
        json.put("outputtype","png");
        json.put("password","123456");
        // json.put("title","pdf");
        json.put("url",url);
        // json.put("url","https://doc.baizhanke.com/api/download?id=20230926162728116");
        System.out.println("convert param: " + json);
        String post = HttpUtils.post(properties.getConverter(), JSON.toJSONString(json));
        System.out.println("convert result"+post);
        JSONObject temp = JSONObject.parseObject(post);
        String url2 = temp.getString("fileUrl");

//        String value="";
//        // 使用 String.format() 插入变量到正则表达式中
//        String regex = String.format("([^/]+)\\.%s$", expensionName);
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(sourcePath);
//        if (matcher.find()) {
//            value = matcher.group(1);
//            System.out.println(value); // 输出: 20230715111945115
//        }
//        String fileNameReal = fileService.getFileName(value);
        // fileName="报价2023092616.oform";
        int lastSlashIndex = fileName.lastIndexOf(".");
        System.out.println("filename"+ String.valueOf(lastSlashIndex) +"..."+fileName);
        String finalResult = fileName.substring(0, lastSlashIndex);
        String contetnType ="";
        if(obj.get("content_type")!=null){
            contetnType = (String) obj.get("content_type");
        }

        String targetPath = pathbak +finalResult+".png";
        String returnfFileName = finalResult+".png";
        if(firstPage ==false){
            targetPath = pathbak +finalResult+".zip";
            returnfFileName = finalResult+".zip";
        }
        String finalContetnType = contetnType;
        String finaltargetPath = targetPath;
        HttpUtils.download(url2, (InputStream is) -> FileUtils.save(is,finalContetnType, finaltargetPath, fileType, id, minioUploadUtil));

        // String sourcePath = "/home/ubuntu/data/documentServer/data/20230925220537775.oform";
        //  String targetPath = pathbak + "/home/ubuntu/data/documentServer/databak/12333.pdf";
//        int lastSlashIndex = fileName.lastIndexOf(".");
//        System.out.println("filename"+ String.valueOf(lastSlashIndex) +"..."+fileName);
//        String finalResult = fileName.substring(0, lastSlashIndex)+".pdf";


        // 获取源文件的扩展名
        // String sourceExtension = sourcePath.substring(sourcePath.lastIndexOf("."));

        // 替换目标路径中的文件名
        //targetPath = targetPath.substring(0, targetPath.lastIndexOf("/") + 1) + "12333" + sourceExtension;

        System.out.println("目标路径: " + targetPath);
        FileUtils.getResource(targetPath, returnfFileName, response);
    }

    @Override
    public void callBack(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = request.getParameter("id");
        System.out.println("callback....."+id);
        String filePath = fileService.getFilePath(id);


        Scanner scanner = new Scanner(request.getInputStream()).useDelimiter("\\A");
        String body = scanner.hasNext() ? scanner.next() : "";
        JSONObject js = JSON.parseObject(body);
        System.out.println("前置保存。。。。js。"+js);
       // js。{"key":"QDD8rEAMxyuy3ABn3Ne9uPdzQgO6dMIkrr7OXWBwS4NPrwJJJzcd6OX","status":7,"users":["uid-undefined"],"lastsave":"2023-05-18T12:52:43.000Z","forcesavetype":0}


        PrintWriter writer = response.getWriter();

        String error = "0";
        switch (js.getInteger("status")) {
            // 1 - document is being edited
            case 1:
                Log.info("action: EDITING");
                System.out.println("action: EDITING");
                break;
            // 2 - document is ready for saving
            case 2:
                Log.info("action: SAVE");
                System.out.println("action: SAVE"+js);
                resourceAcquisitionAndSave(js.getString("url"), filePath,js.getString("key"),id);
                break;
            // 3 - document saving error has occurred
            case 3:
                System.out.println("action: CORRUPTED");
                Log.info("action: CORRUPTED");
                break;
            case 4:
                System.out.println("action: NOT MODIFY"+js);
                Log.info("action: NOT MODIFY");
                break;
            // MUST_FORCE_SAVE 6 - document is being edited, but the current document state is saved
            case 6:
                System.out.println("action: MUST_FORCE_SAVE"+js);
                Log.info("action: MUST_FORCE_SAVE");
                resourceAcquisitionAndSave(js.getString("url"), filePath,js.getString("key"),id);
                break;
            // 7 - error has occurred while force saving the document
            case 7:
                System.out.println("action: CORRUPTED_FORCE_SAVE"+js);
                Log.info("action: CORRUPTED_FORCE_SAVE");
                resourceAcquisitionAndSave(js.getString("url"), filePath,js.getString("key"),id);
                break;
            default:
                Log.info("action: OTHER");
                System.out.println("action: OTHER"+js);
                error = "1";
                break;
        }
        Log.info(js.toString());
//        FileUtil.deleteTempFile(filePath);
        writer.write("{\"error\":" + error + "}");
    }

    private void resourceAcquisitionAndSave(String url, String path,String key,String id) {
        System.out.println("下载参数。。。。"+ url +","+path);
        Map<String, Object> obj = fileService.getFileInfo(id);
        String contetnType ="";
        if(obj.get("content_type")!=null){
            contetnType = (String) obj.get("content_type");
        }
        String finalContetnType = contetnType;
      HttpUtils.download(url, (InputStream is) -> FileUtils.save(is,finalContetnType, path, fileType,id, minioUploadUtil));


        boolean endsWithOform = path.endsWith(".oform");

        if (endsWithOform) {
            System.out.println("字符串以 .oform 结尾");
        } else {
            System.out.println("字符串不以 .oform 结尾");
            return;
        }
        JSONObject json = new JSONObject();
//        {
//            "async": false,
//                "filetype": "docx",
//                "key": "Khirz6zTPdfd7",
//                "outputtype": "pdf",
//                "title": "Example Document Title.docx",
//                "url": "https://example.com/url-to-example-document.docx"
//        }
        json.put("async",false);
        json.put("filetype","oform");
        json.put("key",key);
        json.put("outputtype","pdf");
       // json.put("title","pdf");
        json.put("url",url);
       // json.put("url","https://doc.baizhanke.com/api/download?id=20230926162728116");
        String post = HttpUtils.post(properties.getConverter(), JSON.toJSONString(json));
        System.out.println("convert result"+post);
        JSONObject temp = JSONObject.parseObject(post);
        String url2 = temp.getString("fileUrl");
       // String path = "/home/ubuntu/data/documentServer/data/20230715111945115.oform";
//        int lastSlashIndex = path.lastIndexOf("/");
//        String result="";
//        if (lastSlashIndex != -1) {
//             result = path.substring(0, lastSlashIndex-1);
//            System.out.println(result); // 输出: /home/ubuntu/data/documentServer/data/
//        } else {
//            // 没有斜杠，无需去掉内容
//            System.out.println(path);
//        }
        String value="";
        Pattern pattern = Pattern.compile("([^/]+)\\.oform$");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
             value = matcher.group(1);
            System.out.println(value); // 输出: 20230715111945115
        }
        String fileName = fileService.getFileName(value);
       // fileName="报价2023092616.oform";
        int lastSlashIndex = fileName.lastIndexOf(".");
        System.out.println("filename"+ String.valueOf(lastSlashIndex) +"..."+fileName);
        String finalResult = fileName.substring(0, lastSlashIndex);

        HttpUtils.download(url2, (InputStream is) -> FileUtils.save(is,finalContetnType, pathbak +finalResult+".pdf", fileType, id, minioUploadUtil));
    }



}
