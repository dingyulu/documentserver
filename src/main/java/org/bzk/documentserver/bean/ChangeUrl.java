package org.bzk.documentserver.bean;

import lombok.Data;

@Data
public class ChangeUrl {

    private String version;
    private String url;
    private String key;
    private String changesUrl;
    private String fileType;

}
