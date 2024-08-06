package org.bzk.documentserver.controller;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.bzk.documentserver.UpdateCtl;
import org.bzk.documentserver.bean.Document;
import org.bzk.documentserver.bean.EditorConfig;
import org.bzk.documentserver.constant.Error;
import org.bzk.documentserver.exception.DocumentServerException;
import org.bzk.documentserver.service.CommandService;
import org.bzk.documentserver.service.DocumentServerService;
import org.bzk.documentserver.service.FileService;
import org.bzk.documentserver.utils.CustomMap;
import org.bzk.documentserver.utils.FileUtils;
import org.bzk.documentserver.utils.Log;
import org.bzk.documentserver.utils.minio.MinioUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLDecoder;
//
//String encodedString = "Hello%20World%21";
//        String decodedString = URLDecoder.decode(encodedString, "UTF-8");
//        System.out.println(decodedString); // 输出：Hello World!

/**
 * @Author 2023/2/27 9:02 ly
 **/
@Controller
public class DocumentServerController {

    @Resource
    private DocumentServerService documentService;
    @Resource
    private CommandService commandApiService;
    @Resource
    private FileService fileService;
    @Value("${upload.pathbak}")
    private String pathbak;

    @Value("${upload.path}")
    private String path;
    @Value("${fileType}")
    private String fileType;
    @Autowired
    private MinioUploadUtil minioUploadUtil;


    @PostMapping("/updateAttachName") //修改显示文件名
    @ResponseBody
    public Map updateAttachName(@RequestBody UpdateCtl updateCtl) {//SearchTemplateFlag "0" 不查模板 “1”查模板 2或为空 查所有
        fileService.update(updateCtl.getId(),"0",updateCtl.getAttach_show_name(), updateCtl.getDefaultConfigIn(),updateCtl.getDefaultConfigOut(),updateCtl.getCategory(),updateCtl.getTenantId(),updateCtl.getUserId());
        return CustomMap.build(2)
                .pu1("msg", "修改成功")
                .pu1("code", Error.SUCCESS.getCode())
                ;
    }


    @GetMapping("/pagination")
    @ResponseBody
    public Map pagination(String keyWord,String tenantId,String userId,String user,String SearchTemplateFlag,String fileName,String pageIndex ,String pageSize,String options) throws UnsupportedEncodingException {//SearchTemplateFlag "0" 不查模板 “1”查模板 2或为空 查所有

        if(StringUtils.isNotEmpty(keyWord)){
            if(keyWord.indexOf(".")!=-1){
                keyWord = keyWord.substring(1);
            }
        }
        JSONObject orderBy = new JSONObject();
        if(StringUtils.isNotEmpty(options)){

            // String encodedString = "Hello%20World%21";
            options = URLDecoder.decode(options, "UTF-8");
//            System.out.println(decodedString); // 输出：Hello World!
//            options = decodeURIComponent(options);
            orderBy = JSONObject.parseObject(options);
        }
        System.out.println("1. 获取文档列表信息");
        Log.info("1. 获取文档列表信息");
        if(StringUtils.isNotEmpty(pageIndex)){

            return CustomMap.build(2)
                    .pu1("data", fileService.pagination( keyWord, tenantId, userId, user,SearchTemplateFlag,fileName, Integer.valueOf(pageIndex)-1,Integer.valueOf(pageSize) ,  orderBy))
                    .pu1("cout", fileService.count( keyWord, tenantId, userId, user,SearchTemplateFlag,fileName))
                    .pu1("pageIndex",Integer.valueOf(pageIndex) )
                    .pu1("pageSize", Integer.valueOf(pageSize));
        }
        else{
            return CustomMap.build(2)
                    .pu1("data", fileService.pagination( keyWord, tenantId, userId, user,SearchTemplateFlag,fileName, Integer.valueOf(pageIndex)-1,Integer.valueOf(pageSize),  orderBy))
                    .pu1("cout", fileService.count( keyWord, tenantId, userId, user,SearchTemplateFlag,fileName))
                    .pu1("pageIndex",0)
                    .pu1("pageSize", 100);
        }
    }

    @ResponseBody
    @PostMapping(value = "upload")
    public Map upload(@RequestParam("file") MultipartFile file, String templateCode, String documentCode,String tenantId,String userId,
                      String user,String defaultConfigIn,String defaultConfigOut,String category, String templateFlag ) {

        System.out.println("2. 上传文档列表信息");
        Log.info("2. 上传文档列表信息");
        String id="";
        try {
            if(StringUtils.isEmpty(templateFlag)){

                templateFlag = "1";
            }
            if(StringUtils.isEmpty(templateCode)){
                templateFlag="0";
            }
            if("null".equals( templateCode)) templateCode="";
            if("null".equals(documentCode)) documentCode="";
            id = fileService.upload(file,templateCode,documentCode,"",templateFlag,"",tenantId,userId,user, defaultConfigIn, defaultConfigOut,category); //templateCode 如果为空，表示不是模板
            if("2".equals(templateFlag))
            {  documentService.downloadDocxf(id,tenantId,userId,user,templateCode);
                commandApiService.drop(id, user);
                fileService.update(id,"1","","","","",tenantId,userId);

            }
        } catch (DocumentServerException e) {
            e.printStackTrace();
            return CustomMap.build(2)
                    .pu1("msg", e.getMessage())
                    .pu1("code", e.getCode())
                    ;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return CustomMap.build(2)
                .pu1("msg", Error.SUCCESS.getMsg())
                .pu1("code", Error.SUCCESS.getCode())
                .pu1("id",id)
                ;
    }


//    @PostMapping("/edit")
//    public String editDocFile1(@RequestParam String id, String user, Model model) {
//        System.out.println("3. 编辑文档");
//        Log.info("3. 编辑文档");
//        Document document;
//        try {
//            document = documentService.buildDocument(id);
//        } catch (DocumentServerException e) {
//            e.printStackTrace();
//            model.addAttribute("error", e.getMessage());
//            return "/error";
//        }
//
//        EditorConfig editorConfig = documentService.buildEditorConfig(id, user);
//        model.addAttribute("document", document);
//        model.addAttribute("editorConfig", editorConfig);
//
//        return "/editor";
//    }



    //查表单模板，并且下载模板 成一个新文件，类似
    //id 有值 则documentCode 、tempalteCode 为空，否则 documentCode 、tempalteCode必须有值

    @PostMapping("/edit")
    public String editDocFile( String id,String documentCode,String templateCode, String user,String tenantId,String userId,String viewMode, Model model,String defaultValue,String zoom,String fileNameSuffix) {
        System.out.println("31. 查询并编辑文档");
        Log.info("31. 查询并编辑文档");
        String  value ="{}";
        if(defaultValue!=null){
            value =defaultValue;
        }
        Integer zoom_in =-2;
        if(StringUtils.isNotEmpty(zoom)){
            zoom_in = Integer.valueOf(zoom);
        }
        Document document;
        //1、首先看文档id是否有值，如果有值的话，其他的就不用了，首先查询文档编号是否存在
//         id ="";
//        documentCode ="d123456";
//        templateCode ="t123456";
        if("null".equals( templateCode)||"undefined".equals( templateCode)) templateCode="";
        if("null".equals(documentCode)||"undefined".equals(documentCode)) documentCode="";
        if(StringUtils.isNotEmpty(id)){
            Log.info("312. 查询并编辑文档,id不为空" +id);
            try {
                document = documentService.buildDocument(id);
            } catch (DocumentServerException e) {
                e.printStackTrace();
                model.addAttribute("error", e.getMessage());
                return "/error";
            }

            EditorConfig editorConfig = documentService.buildEditorConfig(id, user,viewMode,value,zoom_in);
            model.addAttribute("document", document);
            model.addAttribute("editorConfig", editorConfig);
            System.out.println("edit.0..."+document+"...\n"+editorConfig);
            return "/editor";
        }



        //2、查询文档编号是否存在，也即任务号，先处理两个都存在的情况
        if(StringUtils.isNotEmpty(documentCode)&&StringUtils.isNotEmpty(templateCode)){
            //首先查询数据库，是否存在，如果不存在 则需要保存到数据库，如果模板也不存在则返回错误
            Log.info("313. 查询并编辑文档,id为空" +id+" "+documentCode +" "+templateCode);
            int fileCount = fileService.getCountFileByDocumentCode(documentCode);

            if(fileCount==0){


                int objCount = fileService.getCountFileTempalte(templateCode,"1");

                if(objCount==0){

                    model.addAttribute("error", "模板不存在");
                    return "/errorblank";
                }
                Map<String, Object> obj = fileService.getFileTempalte(templateCode);
                String templateId =(String) obj.get("id");
                String category = (String) obj.get("category");
                Date now = new Date();
                String s1 =  new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now);
                String sourcePath = (String) obj.get("path");

                // /home/ubuntu/data/docementServer/data/20230513181236046.xlsx


                String regex = "(?<=/)[0-9]+(?=\\.)";

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(sourcePath);
                String attach_show_file = (String)obj.get("attach_show_name");
                String temp=attach_show_file.substring(0,attach_show_file.lastIndexOf(".")); //attach_show_file.split(".");
                if(StringUtils.isNotEmpty(fileNameSuffix)){
                    temp =temp+ "_"+fileNameSuffix;
                }
                System.out.println("attach_show_file..."+attach_show_file);
                System.out.println("attach_show_file..2."+temp);

                // String deltemp=  temp[0].replace("模板","");
                String s2 =  new SimpleDateFormat("yyyyMMddHH").format(now);
                String fname = temp+s2;
                if(fname.contains("模板")||fname.contains("模版")){
                    fname= fname.replaceAll("模板","");
                    fname= fname.replaceAll("模版","");
                }

                //为熊猫汉达改文件名
                if(documentCode.contains("|")){

                    String [] MES = documentCode.split("\\|");
                    if(MES.length ==6||MES.length ==7||MES.length ==5){
                        fname = MES[4]+"_"+MES[1] +"_"+MES[3];
                    }
                    System.out.println("fname................."+fname);
                }
                String output = matcher.replaceAll(fname);

                System.out.println("output................."+output);

                //创建数据库，并进行文档构建,拷贝一份

                try {
//                  File file = FileUtils.copy(sourcePath,output);
                    File file ;
                    if(StringUtils.isEmpty(fileType)||!fileType.equals("minio")){

                        file = FileUtils.copy(sourcePath,output);
                    }
                    else{
                        Map<String, Object> obj2 = fileService.getFileId(templateId);
                        String fileName = (String) obj2.get("attach_file_name") ;
                        // String orgfileName =(String) obj2.get("attach_show_name") ;

                        String orgfileName =fname+ "."+extractExtension((String) obj2.get("attach_show_name")) ;
                        String bucketName = "onlinedocument";
                        if(s1.startsWith("aabbcc")){
                            bucketName ="annex";
                        }
                        file = minioUploadUtil.getFileContent(fileName, bucketName,false,orgfileName);

                    }


                    //MultipartFile multipartFile =FileUtils.getMultipartFile(file);


                    fileService.upload(FileUtils.getMultipartFile(file),templateCode,documentCode,s1,"0",templateId,tenantId,userId,user,"","",category);
                    file.delete();
                    file.deleteOnExit();
                } catch (DocumentServerException e) {
                    e.printStackTrace();
                    throw new RuntimeException("文件创建错误");
                }


                try {
                    System.out.println("copyid...."+s1);
                    document = documentService.buildDocument(s1);
                } catch (DocumentServerException e) {
                    e.printStackTrace();
                    model.addAttribute("error", e.getMessage());
                    return "/error";
                }
                System.out.println("copyid..3.."+s1);
                EditorConfig editorConfig = documentService.buildEditorConfig(s1, user,viewMode,value,zoom_in);
                model.addAttribute("document", document);
                model.addAttribute("editorConfig", editorConfig);
                System.out.println("edit...."+document+"...\n"+editorConfig);
                return "/editor";


            }
            else{

                Map<String, Object> fileobj = fileService.getFileByDocumentCode(documentCode);
                System.out.println("fileObj.."+fileobj);
                //直接构建已经存在的文档，下载id用模板的
                id = (String) fileobj.get("id");
                try {
                    document = documentService.buildDocument(id);
                } catch (DocumentServerException e) {
                    e.printStackTrace();
                    model.addAttribute("error", e.getMessage());
                    return "/error";
                }

                EditorConfig editorConfig = documentService.buildEditorConfig(id, user,viewMode,value,zoom_in);
                model.addAttribute("document", document);
                model.addAttribute("editorConfig", editorConfig);
                System.out.println("edit.2..."+document+"...\n"+editorConfig);
                return "/editor";
            }





        }

        model.addAttribute("error", "传递参数错误 ");
        return "/errorblank";
        //return "/error";
        // return "/editor";
    }

    public  String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
    @GetMapping("/download")
    public void download(@RequestParam String id, HttpServletResponse response) {
        System.out.println("4. 前端下载文件"+id);
        Log.info("4. 前端下载文件"+id);
        try {
            documentService.download(id, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @GetMapping("/downloadPdf")
    public void downloadPdf(@RequestParam String id, HttpServletResponse response) {
        System.out.println("43. 前端下载PDF文件"+id);
        Log.info("43. 前端下载PDF文件"+id);
        try {
            documentService.downloadPdf(id, response);
        } catch (IOException | DocumentServerException e) {
            e.printStackTrace();
        }
    }


    @GetMapping("/downloadOform") //ifDownloadLocal 0 不下载到本地， 1 直接存为模板文档 2 下载到本地同时转为线上模板
    public void downloadOform(@RequestParam String id,String templateCode,String tenantId,String userId,String user,String ifDownloadLocal,HttpServletResponse response) {
        System.out.println("48. 前端转成Oform文件"+id);
        Log.info("48. 前端转成Oform文件"+id);
        try {
            String  oformId="";
            String config ="";
            String configOut ="";
            String category="";
            String needClearId="0";
            if(StringUtils.isNotEmpty(templateCode)){
                int countFileTempalte = fileService.getCountFileTempalte(templateCode,"1");
                if(countFileTempalte>0){

                    Map<String, Object> fileTempalte = fileService.getFileTempalte(templateCode);
                    if(fileTempalte!=null&&fileTempalte.size()>0){

                        commandApiService.drop((String)fileTempalte.get("id"), user);
                        fileService.update((String)fileTempalte.get("id"),"2","","","","",tenantId,userId);

                        oformId = (String)fileTempalte.get("id");
                        config = (String)fileTempalte.get("default_config_in");
                        category = (String)fileTempalte.get("category");
                        configOut = (String)fileTempalte.get("default_config_out");
                    }
                }
                else{
                    needClearId="1";
                }
            }
            documentService.downloadOform(id, templateCode,response,tenantId, userId, user,oformId,config,configOut,category,needClearId);
        } catch (IOException | DocumentServerException e) {
            e.printStackTrace();
        }
    }

    //allPage 1 生成首页缩略图，2 生成所有页缩略图，但是以zip形式返回
    @GetMapping("/downloadPng")
    public void downloadPng(@RequestParam String id, String height,String width,String allPageFlag, HttpServletResponse response) {
        System.out.println("43. 前端下载PDF文件"+id);
        Log.info("43. 前端下载PDF文件"+id);
        try {
            documentService.downloadPng(id,height,width,allPageFlag,response);
        } catch (IOException | DocumentServerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 编辑文档时回调接口
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @PostMapping("/callback")
    public void callback(HttpServletRequest request, HttpServletResponse response) {

        System.out.println("5. 编辑文档时回调"+request.getParameter("id"));
        Log.info("5. 编辑文档时回调"+request.getParameter("id"));
        //处理编辑回调逻辑
        try {
            documentService.callBack(request, response);
        } catch (DocumentServerException | IOException e) {
            e.printStackTrace();
        }
    }




@GetMapping("/templateCode/count")
@ResponseBody
public CustomMap getTemplateByCode(@RequestParam String code,String templateFlag) throws DocumentServerException {
//         if(StringUtils.isEmpty(code)){
//             return  CustomMap.build(2)
//                     .pu1("msg", Error.SUCCESS.getMsg())
//                     .pu1("code", Error.SUCCESS.getCode())
//                     ;
//         }
    int count = fileService.getCountFileTempalte(code,templateFlag );
    if(count>0){
        return CustomMap.build(2)
                .pu1("msg", Error.DOC_TEMPLATE_CODE_EXISTS.getMsg())
                .pu1("code", Error.DOC_TEMPLATE_CODE_EXISTS.getCode())
                ;
    }

    return  CustomMap.build(2)
            .pu1("msg", Error.SUCCESS.getMsg())
            .pu1("code", Error.SUCCESS.getCode())
            ;

}

@GetMapping("/documentCode/count")
@ResponseBody
public CustomMap getDocumentByCode(@RequestParam String code) throws DocumentServerException {
    if(StringUtils.isEmpty(code)||"null".equals(code)){
        return  CustomMap.build(2)
                .pu1("msg", Error.SUCCESS.getMsg())
                .pu1("code", Error.SUCCESS.getCode())
                ;
    }
    int count = fileService.getCountFileByDocumentCode(code);
    if(count>0){
        return CustomMap.build(2)
                .pu1("msg", Error.DOC_FILE_CODE_EXISTS.getMsg())
                .pu1("code", Error.DOC_FILE_CODE_EXISTS.getCode())
                ;
    }

    return  CustomMap.build(2)
            .pu1("msg", Error.SUCCESS.getMsg())
            .pu1("code", Error.SUCCESS.getCode())
            ;

}

@GetMapping("/document/count")
@ResponseBody
public CustomMap getDocumentByTenantId(@RequestParam String tenantId) throws DocumentServerException {
//        if(StringUtils.isEmpty(code)||"null".equals(code)){
//            return  CustomMap.build(2)
//                    .pu1("msg", Error.SUCCESS.getMsg())
//                    .pu1("code", Error.SUCCESS.getCode())
//                    ;
//        }
    int count = fileService.getCountFileByDocumentCode(tenantId);
    if(count>0){
        return CustomMap.build(2)
                .pu1("msg", Error.DOC_FILE_CODE_EXISTS.getMsg())
                .pu1("code", Error.DOC_FILE_CODE_EXISTS.getCode())
                ;
    }

    return  CustomMap.build(2)
            .pu1("msg", Error.SUCCESS.getMsg())
            .pu1("code", Error.SUCCESS.getCode())
            ;

}

@GetMapping("/force-save")
@ResponseBody
public Map<String, Object> forceSave(@RequestParam String id) throws DocumentServerException {
    System.out.println("6. 强制保存文件");
    Log.info("6. 强制保存文件");

    return CustomMap.build(1).pu1("data", commandApiService.forceSave(id));
}

@GetMapping("/drop")
@ResponseBody
public Map drop(@RequestParam String id, String user,String tenantId,String userId) throws DocumentServerException {
    System.out.println("7. 删除文件"+id);
    //把delete_flage 改为1
    fileService.update(id,"1","","","","",tenantId,userId);
    return CustomMap.build(1).pu1("data", commandApiService.drop(id, user));
}

@GetMapping("/fileId")
@ResponseBody
public Map infoFile(@RequestParam String id) throws DocumentServerException {
    System.out.println("8. 获取文件信息");
    return CustomMap.build(1).pu1("data", fileService.getFileId(id));
}

@GetMapping("/info")
@ResponseBody
public Map info(@RequestParam String id) throws DocumentServerException {
    System.out.println("8. 获取文件信息");
    return CustomMap.build(1).pu1("data", commandApiService.info(id));
}

@GetMapping("/license")
@ResponseBody
public Map license() throws DocumentServerException {
    System.out.println("9. 获取文件license信息");
    return CustomMap.build(1).pu1("data", commandApiService.license());
}

@GetMapping("/meta")
@ResponseBody
public Map meta(@RequestParam String id) throws DocumentServerException {
    System.out.println("10. 获取文件原始信息");
    return CustomMap.build(1).pu1("data", commandApiService.meta(id));
}

@GetMapping("/version")
@ResponseBody
public Map version() throws DocumentServerException {
    System.out.println("11. 获取文件版本信息");
    return CustomMap.build(1).pu1("data", commandApiService.version());
}



}
