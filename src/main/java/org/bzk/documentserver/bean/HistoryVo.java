package org.bzk.documentserver.bean;

import lombok.Data;

@Data
public class HistoryVo {

    private String serverVersion;
    private String created;
    private String key;
    private String version;
    private User user;
    private Changes changes;

}
