package com.pro.framework.api.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IdModel implements IModel{
    private static final long serialVersionUID = 100L;

    @ApiModelProperty(value = "主键id")
//    @TableId
    protected Long id;
}
