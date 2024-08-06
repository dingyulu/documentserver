package org.bzk.documentserver.bean;

import lombok.*;


/**
 * Defines the changes from the history object returned after saving the document.
 */
@Data
public class Changes {

    /**
     * Defines the document version creation date.
     */
    private String created;

    /**
     * Defines the user who is the author of the document version.
     */
    private User user;
}