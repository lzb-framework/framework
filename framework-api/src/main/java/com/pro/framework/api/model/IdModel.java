package com.pro.framework.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
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

//    @Schema(description = "主键id")
    @Schema(description = "主键id")
//    @TableId
    protected Long id;
}
