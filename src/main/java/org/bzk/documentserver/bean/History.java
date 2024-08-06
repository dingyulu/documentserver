package org.bzk.documentserver.bean;

import lombok.Data;

import java.util.List;

@Data
public class History {

    /**
     * Defines the current server version number.
     */
    private String serverVersion;

    /**
     * Defines the changes from the history object returned after saving the document.
     */
    private List<Changes> changes;
}
