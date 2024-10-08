package org.bzk.documentserver.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("history")
public class HistoryDoc {
    /**主键*/
    @TableId(type = IdType.AUTO)
    private Long id;
    /**服务器版本*/
    private String serverVersion;
    /**创建时间*/
    private String created;
    /**用户主键*/
    private String userId;
    /**文档key*/
    private String docKey;
    /**版本号*/
    private String version;
    /**文件下载地址*/
    private String url;
    /**文件id*/
    private String fileId;
    /**用户名*/
    private String userName;
    /**文件后缀*/
    private String fileType;
    /**文档编辑数据的文件url*/
    private String changesUrl;
}
