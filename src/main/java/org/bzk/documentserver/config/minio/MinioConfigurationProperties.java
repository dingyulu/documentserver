package org.bzk.documentserver.config.minio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

/**
 * Minio属性配置
 *
 * @author bzk开发平台组
 * @version V3.1.0
 * @copyright 百展客信息技术有限公司（https://www.baizhanke.com）
 * @date 2021-06-07
 */
@ConfigurationProperties(prefix = "bzk.minio")
@Service
public class MinioConfigurationProperties {
    /**
     * 服务端地址
     */
    @Value("${bzk.minio.endpoint}")
    private String endpoint;
    /**
     * 账号
     */
    @Value("${bzk.minio.accessKey}")
    private String accessKey;
    /**
     * 密码
     */
    @Value("${bzk.minio.secretKey}")
    private String secretKey;
    @Value("${bzk.minio.fileHost}")
    private String fileHost;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getFileHost() {
        return fileHost;
    }

    public void setFileHost(String fileHost) {
        this.fileHost = fileHost;
    }
}
