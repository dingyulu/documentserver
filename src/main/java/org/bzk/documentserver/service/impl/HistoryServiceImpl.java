package org.bzk.documentserver.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.bzk.documentserver.bean.HistoryDoc;
import org.bzk.documentserver.bean.HistoryVo;
import org.bzk.documentserver.bean.User;
import org.bzk.documentserver.mapper.HistoryMapper;
import org.bzk.documentserver.service.HistoryService;
import org.bzk.documentserver.service.UserService;
import org.bzk.documentserver.utils.Log;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HistoryServiceImpl extends ServiceImpl<HistoryMapper, HistoryDoc> implements HistoryService {

    @Resource
    private HistoryService historyService;
    @Resource
    private UserService userService;
    @Override
    public List<HistoryVo> history() {
        Log.info("获取历史文档信息");
        List<HistoryVo> result = new ArrayList<>();
        List<HistoryDoc> list = historyService.list();
        List<String> userIds = list.stream().map(HistoryDoc::getUserId).collect(Collectors.toList());
        List<User> users = userService.listByIds(userIds);
        Map<String, List<User>> collect = users.stream().collect(Collectors.groupingBy(User::getId));
        for (HistoryDoc item : list) {
            List<User> userList = collect.get(item.getUserId());
            HistoryVo history = new HistoryVo();
            history.setServerVersion(item.getServerVersion());
            history.setCreated(item.getCreated());
            history.setKey(item.getDocKey());
            history.setVersion(item.getVersion());
            history.setUser(userList.get(0));
            result.add(history);
        }
        return result;
    }
}
