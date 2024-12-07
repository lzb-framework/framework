package com.pro.framework.mtq.service.multiwrapper.dto;

import com.pro.framework.mtq.service.multiwrapper.constant.MultiConstant;
import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class MultiAggregateResultMap {
    private MultiConstant.MultiAggregateTypeEnum aggregateType;
    private String relationCode;
    private String propName;

    public MultiAggregateResultMap(String append) {
        String[] split = append.split("\\.");
        aggregateType = MultiConstant.MultiAggregateTypeEnum.MAP.get(split[0]);
        if (aggregateType == null) {
            aggregateType = MultiConstant.MultiAggregateTypeEnum.PROPS;
            propName = split[0];
            relationCode = null;
        } else {
            relationCode = split[1];
            propName = split[2];
        }
    }
}
