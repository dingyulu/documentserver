package org.bzk.documentserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.bzk.documentserver.bean.HistoryDoc;

@Mapper
public interface HistoryMapper extends BaseMapper<HistoryDoc> {

}
