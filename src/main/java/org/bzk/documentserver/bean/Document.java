package org.bzk.documentserver.bean;

import lombok.Builder;
import lombok.Data;

/**
 * @Author 2023/2/27 9:04 ly
 **/

@Data
@Builder
public class Document {

    private String key;
    /** 【必需】文档名称 */
    private String title;
    /** 【必需】文档后缀 */
    private String fileType;
    /** mimeType 应该先校验文件是否可以打开(非api必须字段) */
    private String storage;
    /** 【必需】文件实体下载地址 */
    private String url;
    private String application;

    private  Info info;

    private Permissions permissions;

    @Data
    @Builder
    public static class Info {
        private String owner;
        private String application;

    }


    @Data
    @Builder
    public static class Permissions {
        private Boolean chat =true;
        private Boolean comment =true;


    }



}

//    var docEditor = new DocsAPI.DocEditor("placeholder", {
//            "document": {
//        "permissions": {
//        "chat": true,
//        "comment": true,
//        "commentGroups": {
//        "edit": ["Group2", ""],
//        "remove": [""],
//        "view": ""
//        },
//        "copy": true,
//        "deleteCommentAuthorOnly": false,
//        "download": true,
//        "edit": true,
//        "editCommentAuthorOnly": false,
//        "fillForms": true,
//        "modifyContentControl": true,
//        "modifyFilter": true,
//        "print": true,
//        "protect": true,
//        "review": true,
//        "reviewGroups": ["Group1", "Group2", ""],
//        "userInfoGroups": ["Group1", ""]
//        },
//        ...
//        },
//        ...
//        });

//   "info": {
//           "owner": "王重阳", //文件创建者名称
//           "sharingSettings": [ //文件对应用户的操作权限配置
//           {
//           "permissions": "Full Access", // 完全操作权限-Full Access,只读权限-Read Only 拒绝访问-Deny Access
//           "user": "林朝英" //有次权限的用户
//           },
//           {
//           "permissions": "Read Only",
//           "user": "周伯通"
//           },
//           ],
//           "uploaded": "2010-07-07 3:46 PM" //文件创建时间
//           }
