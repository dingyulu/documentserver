package org.bzk.documentserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.bzk.documentserver.bean.HistoryDoc;
import org.bzk.documentserver.bean.HistoryVo;

import java.util.List;


public interface HistoryService extends IService<HistoryDoc> {
    List<HistoryVo> history();
}
