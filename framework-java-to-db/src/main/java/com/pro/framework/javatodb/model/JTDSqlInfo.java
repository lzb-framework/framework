package com.pro.framework.javatodb.model;

import com.pro.framework.javatodb.constant.JTDConst;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class JTDSqlInfo {
    private JTDConst.EnumSqlRunType runType;
    private String content;
    private String errorInfo;

    public JTDSqlInfo(JTDConst.EnumSqlRunType runType, String content) {
        this.runType = runType;
        this.content = content;
    }
}
