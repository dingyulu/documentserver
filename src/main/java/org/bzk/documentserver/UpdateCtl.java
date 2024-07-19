package org.bzk.documentserver;

import lombok.Data;

@Data
public class UpdateCtl {
   private String id;
   private  String attach_show_name;
   private  String  defaultConfigIn;
   private String defaultConfigOut;
   private String category;
   private String tenantId;
   private String userId;

}
