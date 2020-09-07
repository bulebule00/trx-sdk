package com.sunlight.trx.bean.bo;


import com.sunlight.trx.constant.AssetConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 转账类型(暂时分为原生资产 TRX 和 TRC10 资产)
 *
 * @author: sunlight
 * @date: 2020/7/28 17:30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferTypeBo {
    /**
     * 资产类型
     */
    private AssetConstant assetConstant;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 币种小数位
     */
    private Integer coinDecimal;

    public TransferTypeBo(AssetConstant assetConstant) {
        this.assetConstant = assetConstant;
    }
}
