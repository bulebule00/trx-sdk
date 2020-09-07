package com.sunlight.trx.sdk;

import com.alibaba.fastjson.JSON;
import com.sunlight.trx.bean.bo.TransactionSendResultBo;
import com.sunlight.trx.bean.bo.TransferTypeBo;
import com.sunlight.trx.constant.AssetConstant;
import com.sunlight.trx.service.SdkConfig;
import com.sunlight.trx.service.TrxSdk;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * @author: sunlight
 * @date: 2020/7/29 10:43
 */
public class SendTransactionTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendTransactionTest.class);
    private String senderPrivateKey = "";
    private String receiverAddress = "";
    private BigDecimal value = BigDecimal.ONE;

    @Test
    public void testSendTransaction() throws Exception {
        SdkConfig.getInstance().setNodeServer("http://3.225.171.164:8090");
        TrxSdk trxSdk = new TrxSdk();
        //转波场
        TransferTypeBo transferTypeBo = new TransferTypeBo(AssetConstant.TRX);
        TransactionSendResultBo resultBo = trxSdk.sendTransaction(senderPrivateKey, receiverAddress, value, transferTypeBo);
        LOGGER.info("resultBo:" + JSON.toJSONString(resultBo));

        //转TRC10
        String assetId = "31303032303030";
        Integer coinDecimal = 1000000;
        transferTypeBo = new TransferTypeBo(AssetConstant.TRC10, assetId, coinDecimal);
        resultBo = trxSdk.sendTransaction(senderPrivateKey, receiverAddress, value, transferTypeBo);
        LOGGER.info("resultBo:" + JSON.toJSONString(resultBo));
    }
}
