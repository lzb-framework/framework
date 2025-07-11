package com.pro.framework.javatodb.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Data
@ApiModel(description = "表信息对象")
public class JTDTableInfoRequest {
    private String urlTemplate =  "/common/jtd/{option}/{entityName}";
}
