package com.sunlight.trx.bean.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 交易广播结果
 * @author: sunlight
 * @date: 2020/7/27 17:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSendResultBo {
    /**
     * 交易Hash
     */
    private String hash;
    /**
     * 已签名数据
     */
    private String transactionSigned;
    /**
     * 广播结果
     */
    private Boolean result;
    /**
     * 广播状态
     */
    private String code;
    /**
     * 消息
     */
    private String message;
}
