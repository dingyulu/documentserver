package org.bzk.documentserver.bean;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class EditorConfig {
    /** 当前打开编辑页面的用户信息 */
    private User user;
    /** tencodingoffice在编辑时请求的回调地址,必选项 */
    private String callbackUrl;
    private CoEditing coEditing;
    private String mode;
    private  String defalut;//存放缺省字段的json字符串
    private  Integer zoom;

//  "coEditing": {
//        "mode": "strict",
//                "change": false
//    },
//            "mode": "view"
    @Data
    @Builder
    public static class User {
        private String id;
        private String name;
        private String tenant;
    }

    @Data
    @Builder
    public static class CoEditing {
        private String mode;
        private Boolean change;

    }
}
