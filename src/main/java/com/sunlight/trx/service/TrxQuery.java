package com.sunlight.trx.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunlight.trx.context.HttpContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * TRX查询工具类
 *
 * @author: sunlight
 * @date: 2020/7/28 11:04
 */
public class TrxQuery {

    /**
     * 查询最新区块数据
     * @return 数据
     * @throws Exception 异常
     */
    public static JSONObject getLatestBlockJsonData() throws Exception {
        HttpEntity httpEntity = new HttpEntity<>(HttpContext.standardHeaders);
        String route = "/wallet/getnowblock";
        ResponseEntity<String> responseEntity = HttpContext.restTemplate.exchange(SdkConfig.getInstance().getNodeServer() + route, HttpMethod.POST, httpEntity, String.class);
        return JSON.parseObject(responseEntity.getBody());
    }

}
