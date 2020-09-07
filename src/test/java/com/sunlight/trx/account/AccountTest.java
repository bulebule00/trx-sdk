package com.sunlight.trx.account;


import com.alibaba.fastjson.JSON;
import com.sunlight.trx.bean.bo.AddressBo;
import com.sunlight.trx.service.AddressService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * @author: sunlight
 * @date: 2020/7/24 15:59
 */

public class AccountTest {
    private static final Logger LOGGER= LoggerFactory.getLogger(AccountTest.class);

    @Test
    public void testNewAddress(){
        AddressBo addressBo= AddressService.newAddress();
        LOGGER.info(JSON.toJSONString(addressBo));
        AddressService.decodeFromBase58Check("TYCw2XSJuQKWtqNMme4XQJX8MgtZ1R4Xwb");
        assertNotNull(addressBo);
        assertNotNull(addressBo.getAddress());
        assertNotNull(addressBo.getPrivateKey());
    }
}
