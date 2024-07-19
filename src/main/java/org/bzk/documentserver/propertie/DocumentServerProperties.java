package org.bzk.documentserver.propertie;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author 2023/2/28 11:17 ly
 **/
@Data
@Component
@ConfigurationProperties(prefix = "tencoding.documentserver")
public class DocumentServerProperties {
    private String[] converExt;
    private String[] editExt;
    private String[] viewExt;
    private String[] documentExt;
    private String[] sheetExt;
    private String[] presentationExt;
    private Long fileSize;
    private String hashkey;
    private Long timeout;

    @Value("${tencoding.documentserver.host.url}${tencoding.documentserver.host.download}")
    private String download;
    @Value("${tencoding.documentserver.host.url}${tencoding.documentserver.host.callback}")
    private String callback;
    @Value("${tencoding.documentserver.app.url}${tencoding.documentserver.app.converter}")
    private String converter;
    @Value("${tencoding.documentserver.app.url}${tencoding.documentserver.app.command}")
    private String command;
    @Value("${tencoding.documentserver.app.url}${tencoding.documentserver.app.api}")
    private String api;
    @Value("${tencoding.documentserver.app.url}${tencoding.documentserver.app.preloader}")
    private String preloader;

    private String logoImage;
    private String logoImageEmbedded;
    private String logoUrl;

}
