package org.bzk.documentserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.bzk.documentserver.bean.ChangeUrl;
import org.bzk.documentserver.bean.HistoryDoc;
import org.bzk.documentserver.bean.HistoryVo;

import java.util.List;


public interface HistoryService extends IService<HistoryDoc> {
    List<HistoryVo> history( String fileId);

    //根据文件idc查询历史记录
    List<HistoryDoc>  listByFileId(String fileId);

    ChangeUrl changeUrls(String version,String fileId2);
}
