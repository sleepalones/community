package com.brotherming.community.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    //生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //md5加密
    public static String md5(String key){
        if (StrUtil.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String,Object> data) {
        Map<String,Object> map = new HashMap<>();
        map.put("code",code);
        map.put("msg",msg);
        if (CollUtil.isNotEmpty(data)) {
            map.put("data",data);
        }
        return JSONUtil.toJsonStr(map);
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code,msg,null);
    }

}
