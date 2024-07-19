package org.bzk.documentserver.constant;

/**
 * @Author 2023/2/27 9:12 ly
 **/
public enum Error {

    SUCCESS("0", "success"),

    DOC_TEMPLATE_CODE_EXISTS("2001", "文档模板已经存在"),
    DOC_FILE_CODE_EXISTS("2002", "文档编号已经存在"),
    DOC_FILE_NOT_EXISTS("1001", "文档不存在"),
    DOC_FILE_EMPTY("1002", "文档或目录是空文件"),
    DOC_FILE_UNREADABLE("1003", "文档不可读"),
    DOC_FILE_OVERSIZE("1004", "文档大小超过限制%s"),
    DOC_FILE_TYPE_UNSUPPORTED("1005", "文档格式不正确"),
    DOC_FILE_MD5_ERROR("1006", "文档md5校验失败"),
    DOC_FILE_MIME_ERROR("1007", "文档MIME检查失败"),
    DOC_FILE_NO_EXTENSION("1008", "文件路径不包含扩展名"),
    DOC_FILE_EXTENSION_NOT_MATCH("1009", "文件路径和名称后缀不匹配"),
    DOC_FILE_KEY_ERROR("1010", "文档key计算失败"),
    FILE_EMPTY("1011", "上传文件为空"),
    FILE_ERROR("1012", "上传文件错误"),
    FILETYPE_NOT_SUPPORT("1013", "文件类型不支持"),

    DOC_CACHE_ERROR("1101", "文档信息缓存失败"),
    DOC_CACHE_NOT_EXISTS("1102", "从缓存中获取文档信息失败"),

    UNSUPPORTED_REQUEST_METHOD("1201", "不支持的请求类型"),

    SYSTEM_UNKNOWN_ERROR("-1", "系统繁忙，请稍后再试...");

    private String code;
    private String msg;

    Error(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "ErrorCodeEnum{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }

    public boolean isSuccessful() {
        return this.code == Error.SUCCESS.getCode();
    }

    public boolean isFailed() {
        return !isSuccessful();
    }
}
