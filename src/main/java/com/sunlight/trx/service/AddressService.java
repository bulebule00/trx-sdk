package com.sunlight.trx.service;

import com.sunlight.trx.bean.bo.AddressBo;
import org.springframework.stereotype.Service;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Sha256Sm3Hash;
import org.tron.common.utils.Base58;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.config.Parameter;

/**
 * 地址业务类
 * @author: sunlight
 * @date: 2020/7/24 16:37
 */
@Service
public class AddressService {
    /**
     * 新建地址
     * @return 地址业务实体
     */
    public static AddressBo newAddress() {
        ECKey eCkey = new ECKey(Utils.getRandom());
        String privateKey = ByteArray.toHexString(eCkey.getPrivKeyBytes());
        byte[] addressBytes = eCkey.getAddress();
        String base58checkAddress = addressBytesEncode58Check(addressBytes);
        return new AddressBo(privateKey, base58checkAddress);
    }

    /**
     * 对地址执行Base58编码
     * @param addressBytes 地址字节数组
     * @return 地址
     */
    private static String addressBytesEncode58Check(byte[] addressBytes) {
        byte[] hash0 = Sha256Sm3Hash.hash(addressBytes);
        byte[] hash1 = Sha256Sm3Hash.hash(hash0);
        byte[] inputCheck = new byte[addressBytes.length + 4];
        System.arraycopy(addressBytes, 0, inputCheck, 0, addressBytes.length);
        System.arraycopy(hash1, 0, inputCheck, addressBytes.length, 4);
        return Base58.encode(inputCheck);
    }

    /**
     * 地址Base58解码成字节数组
     * @param addressBase58 地址Base58格式
     * @return 字节数组
     */
    public static byte[] decodeFromBase58Check(String addressBase58) {
        byte[] address = decode58Check(addressBase58);
        if (!addressValid(address)) {
            return null;
        }
        return address;
    }

    /**
     * Base58解码
     * @param input Base58字符串
     * @return 字节数组
     */
    private static byte[] decode58Check(String input) {
        byte[] decodeCheck = Base58.decode(input);
        if (decodeCheck.length <= 4) {
            return null;
        }
        byte[] decodeData = new byte[decodeCheck.length - 4];
        System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
        byte[] hash0 = Sha256Sm3Hash.hash(decodeData);
        byte[] hash1 = Sha256Sm3Hash.hash(hash0);
        if (hash1[0] == decodeCheck[decodeData.length]
                && hash1[1] == decodeCheck[decodeData.length + 1]
                && hash1[2] == decodeCheck[decodeData.length + 2]
                && hash1[3] == decodeCheck[decodeData.length + 3]) {
            return decodeData;
        }
        return null;
    }

    /**
     * 地址校验
     * @param address 地址Base58 解码后的字节数组
     * @return 校验结果
     */
    private static boolean addressValid(byte[] address) {
        if (address.length != Parameter.CommonConstant.ADDRESS_SIZE) {
            return false;
        }
        byte prefixByte = address[0];
        return prefixByte == Parameter.CommonConstant.ADD_PRE_FIX_BYTE_MAINNET;
    }
}
