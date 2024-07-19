package org.bzk.documentserver.manager;

import com.alibaba.fastjson2.JSON;
import org.bzk.documentserver.propertie.DocumentServerProperties;
import org.bzk.documentserver.utils.CustomMap;
import org.bzk.documentserver.utils.HttpUtils;
import org.bzk.documentserver.utils.Log;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

import static org.bzk.documentserver.constant.CommandInstruct.*;


/**
 * @Author 2023/3/1 16:38 ly
 **/
@Component
public class CommandManager {
    @Resource
    private DocumentServerProperties properties;

    public String forceSave(String key, String userdata) {
        Log.info("instruct: forcesave");
        System.out.println("强制保存2"+key);
        Map<String, Object> map = CustomMap.build(3)
                .pu1("c", FORCE_SAVE)
                .pu1("key", key)
                .pu1("users", userdata);

        Log.info("instruct: forcesave url "+ properties.getCommand());
        String s0 = HttpUtils.post(properties.getCommand(), JSON.toJSONString(map));
        Log.info(s0);
        return s0;
    }

    public String drop(String key, String[] users) {
        Log.info("instruct: drop, users: " + Arrays.toString(users));
        Map<String, Object> map = CustomMap.build(3)
                .pu1("c", DROP)
                .pu1("key", key)
                .pu1("users", users);
        String s0 = HttpUtils.post(properties.getCommand(), JSON.toJSONString(map));
        Log.info(s0);
        return s0;
    }

    public String info(String key) {
        Log.info("instruct: info");
        Map<String, Object> map = CustomMap.build(2)
                .pu1("c", INFO)
                .pu1("key", key);
        String s0 = HttpUtils.post(properties.getCommand(), JSON.toJSONString(map));
        Log.info(s0);
        return s0;
    }

    public String license() {
        Log.info("instruct: license");
        Map<String, Object> map = CustomMap.build(1)
                .pu1("c", LICENSE);
        String s0 = HttpUtils.post(properties.getCommand(), JSON.toJSONString(map));
        Log.info(s0);
        return s0;
    }

    public String meta(String key, Map mate) {
        Log.info("instruct: meta");
        Map<String, Object> map = CustomMap.build(3)
                .pu1("c", META)
                .pu1("key", key)
                .pu1("meta", mate);
        String s0 = HttpUtils.post(properties.getCommand(), JSON.toJSONString(map));
        Log.info(s0);
        return s0;
    }

    public String version() {
        Log.info("instruct: version");
        Map<String, Object> map = CustomMap.build(1)
                .pu1("c", VERSION);
        String s0 = HttpUtils.post(properties.getCommand(), JSON.toJSONString(map));
        Log.info(s0);
        return s0;
    }
}
