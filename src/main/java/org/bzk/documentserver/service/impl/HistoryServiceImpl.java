package org.bzk.documentserver.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.bzk.documentserver.bean.*;
import org.bzk.documentserver.mapper.HistoryMapper;
import org.bzk.documentserver.service.HistoryService;
import org.bzk.documentserver.service.UserService;
import org.bzk.documentserver.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private HistoryMapper historyMapper;

    @Override
    public List<HistoryVo> history(String fileId) {
        Log.info("获取历史文档信息");
//        List<HistoryVo> result = new ArrayList<>();
//        List<HistoryDoc> list = historyService.list();
//        List<String> userIds = list.stream().map(HistoryDoc::getUserId).collect(Collectors.toList());
//        List<User> users = userService.listByIds(userIds);
//        Map<String, List<User>> collect = users.stream().collect(Collectors.groupingBy(User::getId));
//        for (HistoryDoc item : list) {
//            List<User> userList = collect.get(item.getUserId());
//            HistoryVo history = new HistoryVo();
//            history.setServerVersion(item.getServerVersion());
//            history.setCreated(item.getCreated());
//            history.setKey(item.getDocKey());
//            history.setVersion(item.getVersion());
//            history.setUser(userList.get(0));
//            result.add(history);
//        }
//        return result;
        List<HistoryVo> result = new ArrayList<>();
        List<HistoryDoc> list = historyService.listByFileId(fileId);
        for (HistoryDoc item : list) {
            HistoryVo history = new HistoryVo();
            User user = new User();
            user.setId(item.getUserId());
            user.setName(item.getUserName());
            Changes changes = new Changes();
            changes.setCreated(item.getCreated());
            changes.setUser(user);
            history.setServerVersion(item.getServerVersion());
            history.setCreated(item.getCreated());
            history.setKey(item.getDocKey());
            history.setVersion(item.getVersion());
            history.setUser(user);
            history.setChanges(changes);
            result.add(history);
        }
        return result;
    }

    @Override
    public HistoryDoc oneHistory(String fileId, String version) {
        HistoryDoc historyDoc = historyService.getOne(new QueryWrapper<HistoryDoc>().eq("version", version).eq("file_id",fileId));
        return historyDoc;
    }

    @Override
    public List<HistoryDoc> listByFileId(String fileId) {
        List<HistoryDoc> historyList =historyMapper.selectByFileId(fileId);
        return historyList;
    }

    @Override
    public ChangeUrl changeUrls(String version ,String fileId2) {
        HistoryDoc historyDoc = historyService.getOne(new QueryWrapper<HistoryDoc>().eq("version", version).eq("file_id",fileId2));;
        ChangeUrl changeUrl = new ChangeUrl();
        changeUrl.setUrl(historyDoc.getUrl());
        changeUrl.setKey(historyDoc.getDocKey());
        changeUrl.setVersion(historyDoc.getVersion());
        changeUrl.setChangesUrl(historyDoc.getChangesUrl());
        changeUrl.setFileType(historyDoc.getFileType());
        return changeUrl;
    }


}
