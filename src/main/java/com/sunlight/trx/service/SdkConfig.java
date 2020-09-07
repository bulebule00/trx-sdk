package com.sunlight.trx.service;

import lombok.Data;

/**
 * 单例配置类
 * @author: sunlight
 * @date: 2020/7/29 13:42
 */
@Data
public class SdkConfig {
    /**
     * 节点服务地址
     */
    private String nodeServer;

    private SdkConfig(){}

    private static class Holder{
        private static SdkConfig instance=new SdkConfig();
    }

    public static  SdkConfig getInstance(){
        return Holder.instance;
    }
}
