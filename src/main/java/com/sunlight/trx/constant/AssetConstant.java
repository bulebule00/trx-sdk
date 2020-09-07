package com.sunlight.trx.constant;

/**
 * 资产类型枚举
 * @author: sunlight
 * @date: 2020/7/28 17:34
 */
public enum  AssetConstant {
    /**
     * TRX 原生资产类型
     */
    TRX("TRX", "原生资产类型"),

    /**
     * TRC10 资产类型
     */
    TRC10("TRC10", "TRC10 资产类型");

    private final String type;
    private final String description;

    AssetConstant(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
