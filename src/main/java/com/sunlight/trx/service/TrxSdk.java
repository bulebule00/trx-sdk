package com.sunlight.trx.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunlight.trx.bean.bo.TransactionSendResultBo;
import com.sunlight.trx.bean.bo.TransferTypeBo;
import com.sunlight.trx.constant.CoinConstant;
import com.sunlight.trx.context.HttpContext;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Sha256Sm3Hash;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Protocol;
import org.tron.protos.contract.AssetIssueContractOuterClass;
import org.tron.protos.contract.BalanceContract;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 离线签名转账SDK
 *
 * @author: sunlight
 * @date: 2020/7/29 9:29
 */
@Data
@AllArgsConstructor
public class TrxSdk {

    /**
     * 转账（支持TRX和TRC10转账）
     *
     * @param senderPrivateKey 发送方的私钥，十六进制
     * @param receiverAddress  接收方地址，Base58
     * @param value            转账数量
     * @param transferTypeBo   转账类型实体类
     * @return 转账结果
     * @throws Exception 异常
     */
    public TransactionSendResultBo sendTransaction(String senderPrivateKey, String receiverAddress,
                                                   BigDecimal value, TransferTypeBo transferTypeBo) throws Exception {
        byte[] transactionBytes = null;
        switch (transferTypeBo.getAssetConstant()) {
            case TRX:
                transactionBytes = buildTrxTransactionBytes(senderPrivateKey, receiverAddress, value);
                break;
            case TRC10:
                transactionBytes = buildTrc10TransactionBytes(
                        senderPrivateKey,
                        receiverAddress,
                        value,
                        transferTypeBo.getAssetId(),
                        transferTypeBo.getCoinDecimal()
                );
                break;
            default:
                break;
        }
        byte[] signedTransactionBytes = signTransaction(transactionBytes, ByteArray.fromHexString(senderPrivateKey));
        return broadcast(signedTransactionBytes);
    }

    /**
     * 构建TRX交易数据
     *
     * @param senderPrivateKey 发送方的私钥，十六进制
     * @param receiverAddress  接收方地址，Base58
     * @param value            转账数量
     * @return 交易数据
     */
    private byte[] buildTrxTransactionBytes(String senderPrivateKey, String receiverAddress, BigDecimal value) throws Exception {
        byte[] privateBytes = ByteArray.fromHexString(senderPrivateKey);
        ECKey ecKey = ECKey.fromPrivate(privateBytes);
        byte[] from = ecKey.getAddress();
        byte[] to = AddressService.decodeFromBase58Check(receiverAddress);
        long amount = value.multiply(BigDecimal.valueOf(CoinConstant.TRX_DECIMAL)).longValue();
        Protocol.Transaction transaction = createTransferContractTransaction(from, to, amount);
        return transaction.toByteArray();
    }

    /**
     * 构建TRC10交易数据
     *
     * @param senderPrivateKey 发送方的私钥，十六进制
     * @param receiverAddress  接收方地址，Base58
     * @param value            转账数量
     * @param assetId          资产ID
     * @param coinDecimal      资产小数位
     * @return 交易数据
     * @throws Exception 异常
     */
    private byte[] buildTrc10TransactionBytes(String senderPrivateKey, String receiverAddress, BigDecimal value, String assetId, Integer coinDecimal) throws Exception {
        byte[] privateBytes = ByteArray.fromHexString(senderPrivateKey);
        ECKey ecKey = ECKey.fromPrivate(privateBytes);
        byte[] from = ecKey.getAddress();
        byte[] to = AddressService.decodeFromBase58Check(receiverAddress);
        long amount = value.multiply(BigDecimal.valueOf(coinDecimal)).longValue();
        Protocol.Transaction transaction = createTransferAssetContractTransaction(from, to, amount, assetId);
        return transaction.toByteArray();
    }

    /**
     * 创建普通转账交易（转账TRX）
     *
     * @param from   发送方
     * @param to     接收方
     * @param amount 金额
     * @return Protocol.Transaction
     */
    private Protocol.Transaction createTransferContractTransaction(byte[] from, byte[] to, long amount) throws Exception {
        BalanceContract.TransferContract.Builder transferContractBuilder = createTransferContractBuilder(from, to, amount);
        return createTransaction(transferContractBuilder, Protocol.Transaction.Contract.ContractType.TransferContract);
    }

    /**
     * 创建资产转账交易
     *
     * @param from    发送方
     * @param to      接收方
     * @param amount  金额
     * @param assetId 资产ID
     * @return Protocol.Transaction
     */
    private Protocol.Transaction createTransferAssetContractTransaction(
            byte[] from, byte[] to, long amount, String assetId) throws Exception {
        AssetIssueContractOuterClass.TransferAssetContract.Builder transferAssetContractBuilder =
                createTransferAssetContractBuilder(from, to, amount, assetId);
        return createTransaction(transferAssetContractBuilder, Protocol.Transaction.Contract.ContractType.TransferAssetContract);
    }

    /**
     * 创建普通转账交易的builder
     *
     * @param from   发送方
     * @param to     接收方
     * @param amount 金额
     * @return Builder
     */
    private BalanceContract.TransferContract.Builder createTransferContractBuilder(byte[] from, byte[] to, long amount) {
        BalanceContract.TransferContract.Builder transferContractBuilder = BalanceContract.TransferContract.newBuilder();
        transferContractBuilder.setAmount(amount);
        ByteString bsTo = ByteString.copyFrom(to);
        ByteString bsOwner = ByteString.copyFrom(from);
        transferContractBuilder.setToAddress(bsTo);
        transferContractBuilder.setOwnerAddress(bsOwner);
        return transferContractBuilder;
    }

    /**
     * 创建资产转账交易的builder
     *
     * @param from    发送方
     * @param to      接收方
     * @param amount  金额
     * @param assetId 资产ID
     * @return Builder
     */
    private AssetIssueContractOuterClass.TransferAssetContract.Builder createTransferAssetContractBuilder(
            byte[] from, byte[] to, long amount, String assetId) {
        AssetIssueContractOuterClass.TransferAssetContract.Builder transferAssetContractBuilder = AssetIssueContractOuterClass.TransferAssetContract.newBuilder();
        transferAssetContractBuilder.setAmount(amount);
        ByteString bsTo = ByteString.copyFrom(to);
        ByteString bsOwner = ByteString.copyFrom(from);
        transferAssetContractBuilder.setToAddress(bsTo);
        transferAssetContractBuilder.setOwnerAddress(bsOwner);
        transferAssetContractBuilder.setAssetName(ByteString.copyFrom(ByteArray.fromHexString(assetId)));
        return transferAssetContractBuilder;
    }

    /**
     * 创建交易
     *
     * @param builder      不同交易类型的builder
     * @param contractType 合约类型
     * @return Protocol.Transaction
     */
    private Protocol.Transaction createTransaction(
            Message.Builder builder, Protocol.Transaction.Contract.ContractType contractType) throws Exception {
        Protocol.Transaction.Builder transactionBuilder = Protocol.Transaction.newBuilder();
        Protocol.Transaction.Contract.Builder contractBuilder = Protocol.Transaction.Contract.newBuilder();
        try {
            Any any = Any.pack(builder.build());
            contractBuilder.setParameter(any);
        } catch (Exception e) {
            return null;
        }
        contractBuilder.setType(contractType);
        //获取最新区块的相关信息
        JSONObject latestBlockData = TrxQuery.getLatestBlockJsonData();
        JSONObject rawData = latestBlockData.getJSONObject("block_header").getJSONObject("raw_data");
        Long latestBlockTimestamp = rawData.getLong("timestamp");
        long blockHeight = rawData.getLong("number");
        byte[] blockHash = ByteArray.fromHexString(latestBlockData.getString("blockID"));
        transactionBuilder.getRawDataBuilder().addContract(contractBuilder)
                .setTimestamp(System.currentTimeMillis())
                .setExpiration(latestBlockTimestamp + 10 * 60 * 60 * 1000);
        Protocol.Transaction transaction = transactionBuilder.build();
        return setReference(transaction, blockHeight, blockHash);
    }

    /**
     * 设置交易的引用
     *
     * @param transaction 交易
     * @return Protocol.Transaction
     */
    private Protocol.Transaction setReference(Protocol.Transaction transaction, long blockHeight, byte[] blockHash) {
        byte[] refBlockNum = ByteArray.fromLong(blockHeight);
        Protocol.Transaction.raw rawData = transaction.getRawData().toBuilder()
                .setRefBlockHash(ByteString.copyFrom(ByteArray.subArray(blockHash, 8, 16)))
                .setRefBlockBytes(ByteString.copyFrom(ByteArray.subArray(refBlockNum, 6, 8)))
                .build();
        return transaction.toBuilder().setRawData(rawData).build();
    }

    /**
     * 交易签名
     *
     * @param transactionBytes 待签名数据
     * @param privateKey       交易创建者私钥
     * @return 签名后的数据
     * @throws InvalidProtocolBufferException 异常
     */
    private byte[] signTransaction(byte[] transactionBytes, byte[] privateKey) throws InvalidProtocolBufferException {
        Protocol.Transaction transaction = Protocol.Transaction.parseFrom(transactionBytes);
        byte[] rawData = transaction.getRawData().toByteArray();
        byte[] hash = Sha256Sm3Hash.hash(rawData);
        ECKey ecKey = ECKey.fromPrivate(privateKey);
        byte[] sign = ecKey.sign(hash).toByteArray();
        return transaction.toBuilder().addSignature(ByteString.copyFrom(sign)).build().toByteArray();
    }

    /**
     * 调用节点Http接口广播交易
     *
     * @param signedTransactionBytes 交易数据
     * @return 交易广播结果
     */
    private TransactionSendResultBo broadcast(byte[] signedTransactionBytes) {
        String signedDataStr = ByteArray.toHexString(signedTransactionBytes);
        Map<String, String> params = new HashMap<>(1);
        params.put("transaction", signedDataStr);
        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(params, HttpContext.standardHeaders);
        String route = "/wallet/broadcasthex";
        ResponseEntity<String> responseEntity = HttpContext.restTemplate.exchange(SdkConfig.getInstance().getNodeServer() + route, HttpMethod.POST, httpEntity, String.class);
        JSONObject responseBody = JSON.parseObject(responseEntity.getBody());
        String hash = responseBody.getString("txid");
        Boolean result = responseBody.getBoolean("result");
        String code = responseBody.getString("code");
        String message = responseBody.getString("message");
        return new TransactionSendResultBo(hash, signedDataStr, result, code, message);
    }
}
