package org.bzk.documentserver.service.impl;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.bzk.documentserver.constant.Error;
import org.bzk.documentserver.exception.DocumentServerException;
import org.bzk.documentserver.service.FileService;
import org.bzk.documentserver.utils.DocumentServerUtils;
import org.bzk.documentserver.utils.FileUtils;
import org.bzk.documentserver.utils.minio.MinioUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author 2023/3/1 18:02 ly
 **/
@Service
public class FileServiceImpl implements FileService {
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private DocumentServerUtils documentServerUtils;
    @Value("${upload.path}")
    private String path;

    @Value("${fileType}")
    private String fileType;
    @Autowired
    private MinioUploadUtil minioUploadUtil;

    @Override
    public Map<String, Object> getFileInfo(String id) {
        return jdbcTemplate.queryForMap("SELECT * FROM maintableattachfile a WHERE a.id=?", id);
    }

    @Override
    public String getFilePath(String id) {
        return jdbcTemplate.queryForObject("SELECT a.path FROM maintableattachfile a WHERE a.id=?", String.class, id);
    }

    @Override
    public String getIdFileName(String id) {
        return jdbcTemplate.queryForObject("SELECT a.attach_file_name FROM maintableattachfile a WHERE a.id=?", String.class, id);
    }

    @Override
    public String getFileName(String id) {
        return jdbcTemplate.queryForObject("SELECT a.attach_show_name FROM maintableattachfile a WHERE a.id=?", String.class, id);
    }

    @Override
    public Map<String, Object> getFileTempalte(String templateCode) {
        return jdbcTemplate.queryForMap("SELECT * FROM maintableattachfile a WHERE a.template_code=? AND  a.template_flag=? AND a.delete_flag=?", templateCode, "1", "0");
    }

    @Override
    public int getCountFileTempalte(String templateCode, String templateFlag) {
        if (StringUtils.isEmpty(templateFlag)) {
            templateFlag = "1";
        }
        return jdbcTemplate.queryForObject("SELECT count(*) FROM maintableattachfile a WHERE a.template_code=? AND a.template_flag=? AND a.delete_flag=?", Integer.class, templateCode, templateFlag, "0");
    }


    @Override
    public Map<String, Object> getFileByDocumentCode(String documentCode) {
        return jdbcTemplate.queryForMap("SELECT a.attach_show_name, a.path,a.id ,a.attach_file_name FROM maintableattachfile a WHERE a.document_code=?AND  a.delete_flag=?", documentCode, "0");
    }

    @Override
    public int getCountFileByDocumentCode(String documentCode) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM maintableattachfile a WHERE a.document_code=? AND a.delete_flag=?", Integer.class, documentCode, "0");
    }

    @Override
    public int getCountFileByTenantId(String tenantId) {
        if (StringUtils.isEmpty(tenantId) || "undefined".equals(tenantId)) {

            return jdbcTemplate.queryForObject("SELECT count(*) FROM maintableattachfile a WHERE a.delete_flag=?", Integer.class, "0");
        }
        return jdbcTemplate.queryForObject("SELECT count(*) FROM maintableattachfile a WHERE a.F_TenantId=? AND a.delete_flag=?", Integer.class, tenantId, "0");
    }

    @Override
    public Map<String, Object> getFileByDocumentCodeAndTemplate(String templateCode, String documentCode) {
        return jdbcTemplate.queryForMap("SELECT a.attach_show_name, a.path,a.id ,a.attach_file_name FROM maintableattachfile a WHERE a.document_code=? and a.template_code=? AND  a.delete_flag=?", documentCode, templateCode, "0");
    }


    @Override
    public Map<String, Object> getFileId(String id) {
        return jdbcTemplate.queryForMap("SELECT * FROM maintableattachfile a WHERE a.id=?  AND  a.delete_flag=?", id, "0");
    }


    @Override
    public int getCountFileByDocumentCodeAndTemplate(String templateCode, String documentCode) {
        return jdbcTemplate.queryForObject("SELECT a.attach_show_name, a.path,a.id ,a.attach_file_name FROM maintableattachfile a WHERE a.document_code=? and a.template_code=? and a.delete_flag=?", Integer.class, documentCode, templateCode, "0");
    }

    @Override
    public List<Map<String, Object>> pagination(String keyWord, String tenantId, String userId, String user, String searchTemplateFlag, String fileName, int pageIndex, int pageSize, JSONObject options) {
        //
//        SELECT * FROM table_name ORDER BY column_name ASC; 升
//
//        按照多个列进行排序：
//
//        SELECT * FROM table_name ORDER BY column_name1 ASC, column_name2 DESC;
        String order = "ORDER BY a.attach_show_name ASC,";
        //String order ="ORDER BY CONVERT(a.attach_show_name USING gbk) ASC,";

        if (options != null) {
            String order1 = "ORDER BY  ";
            for (String key : options.keySet()) {
                if (options.getInteger(key) == 1) {
                    //order1 = order1 +" "+"a."+ key+" " + "ASC,";
                    order1 = order1 + " " + "CONVERT(" + "a." + key + " "+"USING gbk)" + " " + "ASC,";
                } else {
                    order1 = order1 + " " + "a." + key + " " + "DESC,";
                }
            }
            if (order1.length() == 10) {

            } else {
                order = order1;
            }
            order = order.substring(0, order.length() - 1);
        }


        if (StringUtils.isEmpty(searchTemplateFlag) || "2".equals(searchTemplateFlag)) {


            if (StringUtils.isEmpty(keyWord) && (StringUtils.isEmpty(tenantId))) {
                if (StringUtils.isNotEmpty(fileName)) {
                    String sql = "SELECT * FROM maintableattachfile a WHERE (a.delete_flag=? AND a.attach_show_name  like ?  )     %s LIMIT %d,%d ";
                    sql = String.format(sql, order, pageIndex, pageSize);
                    //return jdbcTemplate.queryForList("SELECT a.id, a.attach_show_name FROM maintableattachfile a WHERE a.delete_flag=? AND a.attach_show_name +like ? ' ", "0",'%'+fileName+"%");
                    return jdbcTemplate.queryForList(sql, "0", '%' + fileName + "%");
                } else {
                    String sql = "SELECT * FROM maintableattachfile a WHERE a.delete_flag=?     %s LIMIT %d,%d ,order";
                    sql = String.format(sql, order, pageIndex, pageSize);
                    return jdbcTemplate.queryForList(sql, "0");
                }
            } else {
                if (!StringUtils.isEmpty(keyWord) && !StringUtils.isEmpty(tenantId)) {
                    if (StringUtils.isNotEmpty(fileName)) {
                        String sql = "SELECT * FROM maintableattachfile a WHERE (a.delete_flag=? AND ( a.F_TenantId=? )) AND (a.user like ? OR a.document_type like ? OR  a.document_code like ? OR  a.template_code like ? OR  a.category like ? OR a.attach_show_name like ?)  AND a.attach_show_name  like ?     %s LIMIT %d,%d ";
                        sql = String.format(sql, order, pageIndex, pageSize);
                        return jdbcTemplate.queryForList(sql, "0", tenantId, "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + fileName + "%");
                    } else {
                        String sql = "SELECT * FROM maintableattachfile a WHERE (a.delete_flag=? AND  ( a.F_TenantId=? )) AND (a.user=? OR a.document_type=? OR  a.category like ?)     %s LIMIT %d,%d ";
                        sql = String.format(sql, order, pageIndex, pageSize);
                        return jdbcTemplate.queryForList(sql, "0", tenantId, keyWord, keyWord, keyWord);
                    }
                } else {
                    if (!StringUtils.isEmpty(keyWord)) {
                        String sql = "SELECT * FROM maintableattachfile a WHERE (a.delete_flag=?) AND (a.user like ? OR a.document_type like ? OR  a.document_code like ?  OR  a.category like ? OR  a.template_code like ?  OR a.attach_show_name like ?)     %s LIMIT %d,%d ";
                        sql = String.format(sql, order, pageIndex, pageSize);
                        return jdbcTemplate.queryForList(sql, "0", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%");
                    }

                    if (!StringUtils.isEmpty(tenantId)) {
                        String sql = "SELECT * FROM maintableattachfile a WHERE (a.delete_flag=? ) AND (a.F_TenantId=?)     %s LIMIT %d,%d ";
                        sql = String.format(sql, order, pageIndex, pageSize);
                        return jdbcTemplate.queryForList(sql, "0", tenantId);
                    }
                }
            }
        } else if (!StringUtils.isEmpty(searchTemplateFlag) && "1".equals(searchTemplateFlag)) { //只查模板
            if (StringUtils.isEmpty(keyWord) && (StringUtils.isEmpty(tenantId))) {
                if (StringUtils.isNotEmpty(fileName)) {
                    String sql = "SELECT * FROM maintableattachfile a WHERE a.delete_flag=? And a.template_flag=? AND a.attach_show_name like ?     %s LIMIT %d,%d ";
                    sql = String.format(sql, order, pageIndex, pageSize);
                    return jdbcTemplate.queryForList(sql, "0", "1", "%" + fileName + "%");
                } else {
                    String sql = "SELECT * FROM maintableattachfile a WHERE a.delete_flag=? And a.template_flag=?     %s LIMIT %d,%d ";
                    sql = String.format(sql, order, pageIndex, pageSize);
                    return jdbcTemplate.queryForList(sql, "0", "1");
                }
            } else {
                if (!StringUtils.isEmpty(keyWord) && !StringUtils.isEmpty(tenantId)) {
                    if (StringUtils.isNotEmpty(fileName)) {
                        String sql = "SELECT * FROM maintableattachfile a WHERE (a.delete_flag=? AND ( a.F_TenantId=? OR  a.F_TenantId=?) And a.template_flag=?) AND (a.user like ? OR a.document_type like ? OR  a.document_code like ? OR  a.template_code like ? OR  a.category like ?   OR a.attach_show_name like ?) AND a.attach_show_name like ?     %s LIMIT %d,%d ";
                        sql = String.format(sql, order, pageIndex, pageSize);
                        return jdbcTemplate.queryForList(sql, "0", tenantId, "sys", "1", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + fileName + "%");
                    } else {
                        String sql = "SELECT * FROM maintableattachfile a WHERE (a.delete_flag=? AND  ( a.F_TenantId=? OR  a.F_TenantId=?) And a.template_flag=?) AND (a.user=? OR a.document_type=? OR  a.category like ? )     %s LIMIT %d,%d ";
                        sql = String.format(sql, order, pageIndex, pageSize);
                        return jdbcTemplate.queryForList(sql, "0", tenantId, "sys", "1", keyWord, keyWord, keyWord);
                    }

                } else {
                    if (!StringUtils.isEmpty(keyWord)) {
                        String sql = "SELECT * FROM maintableattachfile a WHERE (a.delete_flag=? AND  a.template_flag=?) AND (a.user like ? OR a.document_type like ? OR  a.document_code like ?  OR  a.category like ? OR  a.template_code like ?  OR a.attach_show_name like ?)     %s LIMIT %d,%d ";
                        sql = String.format(sql, order, pageIndex, pageSize);
                        return jdbcTemplate.queryForList(sql, "0", "1", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%");
                    }

                    if (!StringUtils.isEmpty(tenantId)) {
                        String sql = "SELECT * FROM maintableattachfile a WHERE (a.delete_flag=? AND   a.template_flag=?) AND (a.F_TenantId=? OR  a.F_TenantId=?)     %s LIMIT %d,%d ";
                        sql = String.format(sql, order, pageIndex, pageSize);
                        return jdbcTemplate.queryForList(sql, "0", "1", tenantId, "sys");
                    }
                }
            }
        } else {
            if (StringUtils.isEmpty(keyWord) && (StringUtils.isEmpty(tenantId))) {

                if (StringUtils.isNotEmpty(fileName)) {

                    String sql = "SELECT * FROM maintableattachfile a WHERE a.delete_flag=? And a.template_flag=? AND a.attach_show_name like ?     %s LIMIT %d,%d ";

                    sql = String.format(sql, order, pageIndex, pageSize);
                    return jdbcTemplate.queryForList(sql, "0", "0", "%" + fileName + "%");
                } else {
                    String sql = "SELECT * FROM maintableattachfile a WHERE a.delete_flag=? And a.template_flag=?     %s LIMIT %d,%d ";
                    sql = String.format(sql, order, pageIndex, pageSize);
                    return jdbcTemplate.queryForList(sql, "0", "0");
                }
            } else {
                if (!StringUtils.isEmpty(keyWord) && !StringUtils.isEmpty(tenantId)) {
                    if (StringUtils.isNotEmpty(fileName)) {
                        String sql = "SELECT * FROM maintableattachfile a WHERE (a.delete_flag=? AND  ( a.F_TenantId=?) And a.template_flag=? AND a.attach_show_name like ? ) AND  (a.user like ? OR a.document_type like ? OR  a.document_code like ? OR  a.template_code like ?  OR a.attach_show_name like ?  OR  a.category like ?)     %s LIMIT %d,%d ";
                        sql = String.format(sql, order, pageIndex, pageSize);
                        return jdbcTemplate.queryForList(sql, "0", tenantId, "0", "%" + fileName + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%");
                    } else {
                        String sql = "SELECT * FROM maintableattachfile a WHERE (a.delete_flag=? AND  ( a.F_TenantId=? ) And a.template_flag=?) AND (a.user like ? OR a.document_type like ? OR  a.document_code like ? OR  a.template_code like ?   OR  a.category like ? OR a.attach_show_name like ?)     %s LIMIT %d,%d ";
                        sql = String.format(sql, order, pageIndex, pageSize);
                        return jdbcTemplate.queryForList(sql, "0", tenantId, "0", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%");
                    }
                } else {
                    if (!StringUtils.isEmpty(keyWord)) {
                        String sql = "SELECT * FROM maintableattachfile a WHERE (a.delete_flag=? AND   a.template_flag=?) AND (a.user like ? OR a.document_type like ? OR  a.document_code like ?   OR  a.category like ? OR  a.template_code like ?  OR a.attach_show_name like ?)     %s LIMIT %d,%d ";
                        sql = String.format(sql, order, pageIndex, pageSize);
                        return jdbcTemplate.queryForList(sql, "0", "0", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%");
                    }

                    if (!StringUtils.isEmpty(tenantId)) {
                        String sql = "SELECT * FROM maintableattachfile a WHERE (a.delete_flag=? AND   a.template_flag=?) AND (a.F_TenantId=?)     %s LIMIT %d,%d ";
                        sql = String.format(sql, order, pageIndex, pageSize);
                        return jdbcTemplate.queryForList(sql, "0", "0", tenantId);
                    }
                }
            }
        }
        System.out.println("query....ee");
        return jdbcTemplate.queryForList("SELECT * FROM maintableattachfile a WHERE a.delete_flag=?", "0");
    }


    @Override
    public int count(String keyWord, String tenantId, String userId, String user, String searchTemplateFlag, String fileName) {
        //


        if (StringUtils.isEmpty(searchTemplateFlag) || "2".equals(searchTemplateFlag)) {


            if (StringUtils.isEmpty(keyWord) && (StringUtils.isEmpty(tenantId))) {
                if (StringUtils.isNotEmpty(fileName)) {
                    String sql = "SELECT count(*) FROM maintableattachfile a WHERE (a.delete_flag=? AND a.attach_show_name  like ?  ) ";

                    //return jdbcTemplate.queryForList("SELECT a.id, a.attach_show_name FROM maintableattachfile a WHERE a.delete_flag=? AND a.attach_show_name +like ? ' ", "0",'%'+fileName+"%");
                    return jdbcTemplate.queryForObject(sql, Integer.class, "0", '%' + fileName + "%");
                } else {
                    String sql = "SELECT count(*) FROM maintableattachfile a WHERE a.delete_flag=? ";

                    return jdbcTemplate.queryForObject(sql, Integer.class, "0");
                }
            } else {
                if (!StringUtils.isEmpty(keyWord) && !StringUtils.isEmpty(tenantId)) {
                    if (StringUtils.isNotEmpty(fileName)) {
                        String sql = "SELECT count(*) FROM maintableattachfile a WHERE (a.delete_flag=? AND  ( a.F_TenantId=? )) AND (a.user like ? OR a.document_type like ? OR  a.document_code like ? OR  a.template_code like ?   OR  a.category like ? OR a.attach_show_name like ?)  AND a.attach_show_name  like ? ";

                        return jdbcTemplate.queryForObject(sql, Integer.class, "0", tenantId, "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + fileName + "%");
                    } else {
                        String sql = "SELECT count(*) FROM maintableattachfile a WHERE (a.delete_flag=? AND  ( a.F_TenantId=? )) AND (a.user=? OR a.document_type=? OR  a.category like ?)";

                        return jdbcTemplate.queryForObject(sql, Integer.class, "0", tenantId, keyWord, keyWord, keyWord);
                    }
                } else {
                    if (!StringUtils.isEmpty(keyWord)) {
                        String sql = "SELECT count(*) FROM maintableattachfile a WHERE (a.delete_flag=? ) AND (a.user like ? OR a.document_type like ? OR  a.document_code like ? OR  a.template_code like ?  OR  a.category like ? OR a.attach_show_name like ?) ";

                        return jdbcTemplate.queryForObject(sql, Integer.class, "0", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%");

                    }

                    if (!StringUtils.isEmpty(tenantId)) {
                        String sql = "SELECT count(*) FROM maintableattachfile a WHERE (a.delete_flag=? AND  ( a.F_TenantId=? )) )";

                        return jdbcTemplate.queryForObject(sql, Integer.class, "0", tenantId);
                    }
                }
            }
        } else if (!StringUtils.isEmpty(searchTemplateFlag) && "1".equals(searchTemplateFlag)) { //只查模板
            if (StringUtils.isEmpty(keyWord) && (StringUtils.isEmpty(tenantId))) {
                if (StringUtils.isNotEmpty(fileName)) {
                    String sql = "SELECT count(*) FROM maintableattachfile a WHERE a.delete_flag=? And a.template_flag=? AND a.attach_show_name like ? ";

                    return jdbcTemplate.queryForObject(sql, Integer.class, "0", "1", "%" + fileName + "%");
                } else {
                    String sql = "SELECT count(*) FROM maintableattachfile a WHERE a.delete_flag=? And a.template_flag=?";

                    return jdbcTemplate.queryForObject(sql, Integer.class, "0", "1");
                }
            } else {
                if (!StringUtils.isEmpty(keyWord) && !StringUtils.isEmpty(tenantId)) {
                    if (StringUtils.isNotEmpty(fileName)) {
                        String sql = "SELECT count(*) FROM maintableattachfile a WHERE (a.delete_flag=? AND ( a.F_TenantId=? OR  a.F_TenantId=?) And a.template_flag=?) AND (a.user like ? OR a.document_type like ? OR  a.document_code like ? OR  a.template_code like ?   OR  a.category like ?  OR a.attach_show_name like ?) AND a.attach_show_name like ? ";

                        return jdbcTemplate.queryForObject(sql, Integer.class, "0", tenantId, "sys", "1", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + fileName + "%");
                    } else {
                        String sql = "SELECT count(*) FROM maintableattachfile a WHERE (a.delete_flag=? AND  ( a.F_TenantId=? OR  a.F_TenantId=?) And a.template_flag=?) AND (a.user=? OR a.document_type=? OR  a.category like ?)";

                        return jdbcTemplate.queryForObject(sql, Integer.class, "0", tenantId, "sys", "1", keyWord, keyWord, keyWord);
                    }

                } else {
                    if (!StringUtils.isEmpty(keyWord)) {
                        String sql = "SELECT count(*) FROM maintableattachfile a WHERE (a.delete_flag=?  And a.template_flag=?) AND (a.user like ? OR a.document_type like ?  OR  a.category like ? OR  a.document_code like ? OR  a.template_code like ?  OR a.attach_show_name like ?) ";

                        return jdbcTemplate.queryForObject(sql, Integer.class, "0", "1", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%");

                    }

                    if (!StringUtils.isEmpty(tenantId)) {
                        String sql = "SELECT count(*) FROM maintableattachfile a WHERE (a.delete_flag=?  And a.template_flag=?) AND ( a.F_TenantId=? OR  a.F_TenantId=?) ";

                        return jdbcTemplate.queryForObject(sql, Integer.class, "0", "1", tenantId, "sys");

                    }
                }
            }
        } else {
            if (StringUtils.isEmpty(keyWord) && (StringUtils.isEmpty(tenantId))) {

                if (StringUtils.isNotEmpty(fileName)) {

                    String sql = "SELECT count(*) FROM maintableattachfile a WHERE a.delete_flag=? And a.template_flag=? AND a.attach_show_name like ?";


                    return jdbcTemplate.queryForObject(sql, Integer.class, "0", "0", "%" + fileName + "%");
                } else {
                    String sql = "SELECT count(*) FROM maintableattachfile a WHERE a.delete_flag=? And a.template_flag=? ";

                    return jdbcTemplate.queryForObject(sql, Integer.class, "0", "0");
                }
            } else {
                if (!StringUtils.isEmpty(keyWord) && !StringUtils.isEmpty(tenantId)) {
                    if (StringUtils.isNotEmpty(fileName)) {
                        String sql = "SELECT count(*) FROM maintableattachfile a WHERE (a.delete_flag=? AND  ( a.F_TenantId=?) And a.template_flag=? AND a.attach_show_name like ? ) " +
                                "AND  (a.user like ? OR a.document_type like ? OR  a.document_code like ? OR  a.template_code like ?  OR  a.category like ? OR a.attach_show_name like ?)";

                        return jdbcTemplate.queryForObject(sql, Integer.class, "0", tenantId, "0", "%" + fileName + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%");
                    } else {
                        String sql = "SELECT count(*) FROM maintableattachfile a WHERE (a.delete_flag=? AND  ( a.F_TenantId=?) And a.template_flag=?) AND (a.user like ? OR a.document_type like ? OR  a.category like ? OR  a.document_code like ? OR  a.template_code like ?  OR a.attach_show_name like ?) ";

                        return jdbcTemplate.queryForObject(sql, Integer.class, "0", tenantId, "0", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%");
                    }
                } else {
                    if (!StringUtils.isEmpty(keyWord)) {
                        String sql = "SELECT count(*) FROM maintableattachfile a WHERE (a.delete_flag=?  And a.template_flag=?) AND (a.user like ? OR a.document_type like ? OR  a.document_code like ?  OR  a.category like ? OR  a.template_code like ?  OR a.attach_show_name like ?) ";

                        return jdbcTemplate.queryForObject(sql, Integer.class, "0", "0", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%", "%" + keyWord + "%");

                    }

                    if (!StringUtils.isEmpty(tenantId)) {
                        String sql = "SELECT count(*) FROM maintableattachfile a WHERE (a.delete_flag=?  And a.template_flag=?) AND ( a.F_TenantId=? ) ";

                        return jdbcTemplate.queryForObject(sql, Integer.class, "0", "0", tenantId);

                    }
                }
            }
        }

        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM maintableattachfile a WHERE a.delete_flag=?", Integer.class, "0");
    }

    @Override
    public String upload(MultipartFile file, String templateCode, String documentCode, String id, String templateFlag, String templateId, String tenantId, String userId, String user, String defaultConfigIn, String defaultConfigOut, String category) throws DocumentServerException {
        if (file.isEmpty()) {
            throw DocumentServerException.build(Error.FILE_EMPTY);
        }
        String s0 = StringUtils.lowerCase(FilenameUtils.getExtension(file.getOriginalFilename()));
        System.out.println("upload s0..." + s0);
        documentServerUtils.assertCanView(s0);
        Date now = new Date();
        String s1 = "";//= new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now);
        if (StringUtils.isEmpty(id)) {
            s1 = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now);
        } else {
            s1 = id;
        }
        String s2 = s1 + "." + s0;
        String s3 = FilenameUtils.normalize(path + s2);
        System.out.println("fileType................................" + fileType);
        if (StringUtils.isEmpty(fileType) || !fileType.equals("minio")) {

            FileUtils.save(file, s3);
        } else {

            String bucketName = "onlinedocument";
            if (id.startsWith("aabbcc")) {
                bucketName = "annex";
            }
            minioUploadUtil.uploadFile(file, bucketName, s2);
        }


        if (StringUtils.isEmpty(templateCode)) {
            templateCode = "";
        }


        if (StringUtils.isEmpty(documentCode)) {
            documentCode = "";
        }

        System.out.println("upload s1..." + s3);
        jdbcTemplate.update("INSERT INTO " +
                        "maintableattachfile(id, attach_file_name, path, attach_show_name, attach_time, attach_size,delete_flag,template_flag,template_code,document_code,template_id,document_type,user,F_TenantId,F_UserId, default_config_in, default_config_out,category,content_type,F_Type,F_BucketName,F_ParentId,F_CreatorUserId) " +
                        "VALUE(?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                s1,
                s2,
                s3,
                file.getOriginalFilename(),
                now,
                file.getSize(),
                "0",
                templateFlag,
                templateCode,
                documentCode,
                templateId,
                s0,
                user,
                tenantId,
                userId,
                defaultConfigIn,
                defaultConfigOut,
                category,
                file.getContentType(),
                1,
                "onlinedocument",
                category,
                userId

        );

        return s1;
    }

    @Override
    public void save(MultipartFile file, String templateCode, String documentCode) throws DocumentServerException {

    }

    @Override
    public void update(String id, String deleteFlag, String attachName, String defaultConfigIn, String defaultConfigOut, String category, String tenantId, String userId) {
        if (StringUtils.isNotEmpty(deleteFlag) && "1".equals(deleteFlag)) {

            String sql = "UPDATE " + "maintableattachfile" + " SET " + "delete_flag" + " = ? WHERE id = ? AND F_TenantId=?";

            int re = jdbcTemplate.update(sql, deleteFlag, id, tenantId);
            System.out.println("update ..." + re + " ...  " + sql);
        }

        if (StringUtils.isNotEmpty(deleteFlag) && "2".equals(deleteFlag)) {
//            Date now = new Date();
//            String documentId = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now);
//            String sql = "UPDATE " + "maintableattachfile" + " SET " + "id = ? ," +"delete_flag" + " = ?  WHERE id = ? AND F_TenantId=?";
//
//            int re=  jdbcTemplate.update(sql,documentId, deleteFlag, id,tenantId);
            String sql = "DELETE FROM maintableattachfile WHERE id = ? AND F_TenantId = ?";

            int re = jdbcTemplate.update(sql, id, tenantId);


            System.out.println("update ..." + re + " ...  " + sql);
        }
        if (StringUtils.isNotEmpty(attachName)) {

            String sql = "UPDATE " + "maintableattachfile" + " SET " + "attach_show_name" + " = ? WHERE id = ?  AND F_TenantId=?";
            jdbcTemplate.update(sql, attachName, id, tenantId);
        }
        if (StringUtils.isNotEmpty(category)) {

            String sql = "UPDATE " + "maintableattachfile" + " SET " + "category" + " = ? WHERE id = ?  AND F_TenantId=?";
            jdbcTemplate.update(sql, category, id, tenantId);
        }
        if (StringUtils.isNotEmpty(defaultConfigIn)) {

            String sql = "UPDATE " + "maintableattachfile" + " SET " + "default_config_in" + " = ? WHERE id = ?  AND F_TenantId=?";
            jdbcTemplate.update(sql, defaultConfigIn, id, tenantId);
            System.out.println("update ..." + sql);
        }
        if (StringUtils.isNotEmpty(defaultConfigOut)) {

            String sql = "UPDATE " + "maintableattachfile" + " SET " + "default_config_out" + " = ? WHERE id = ?  AND F_TenantId=?";
            jdbcTemplate.update(sql, defaultConfigOut, id, tenantId);
        }


    }

}
