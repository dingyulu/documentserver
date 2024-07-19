package org.bzk.documentserver.exception;

import org.bzk.documentserver.constant.Error;

/**
 * @Author 2023/2/27 9:10 ly
 **/
public class DocumentServerException extends Exception {
    private String code;
    public DocumentServerException(Error error) {
        this(error.getCode(), error.getMsg());
    }

    public DocumentServerException(String code, String msg) {
        super(msg);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static DocumentServerException build(Error errorCode) {
        return DocumentServerException.build(errorCode);
    }

    public static DocumentServerException format(Error errorCode, Object... params) {
        return new DocumentServerException(errorCode.getCode(), String.format(errorCode.getMsg(), params));
    }
}
