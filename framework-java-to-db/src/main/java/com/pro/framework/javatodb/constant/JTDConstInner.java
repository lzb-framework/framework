package com.pro.framework.javatodb.constant;

import com.pro.framework.javatodb.service.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JTDConstInner {
    public static final char CHAR_UNDERLINE = '_';

    public static final String EMPTY = "";

//    public static final Integer INT_1 = 1;



    @Getter
    @AllArgsConstructor
    public enum EnumSequenceType {
        /**
         * 索引类型
         */
        KEY("KEY"),
        UNIQUE_KEY("UNIQUE KEY"),
        ;
        private final String value;
        public static final Map<String, EnumSequenceType> MAP = Arrays.stream(values()).collect(Collectors.toMap(EnumSequenceType::getValue, o -> o));
    }

    @Getter
    @AllArgsConstructor
    public enum EnumTableMetaType {
        //表名
        tableName(null, Collections.singletonList("CREATE TABLE")),
        //表注释
        tableLabel(JTDTableMetaServiceLabelImpl.INSTANCE, Collections.singletonList(")")),
        //主键
        primaryKey(JTDTableMetaServicePrimaryKeyImpl.INSTANCE, Collections.singletonList("PRIMARY KEY")),
        //字段
        fields(JTDTableMetaServiceFieldImpl.INSTANCE, Collections.singletonList("`")),
        //索引
        sequenceInfos(JTDTableMetaServiceSequenceImpl.INSTANCE, Arrays.asList("UNIQUE", "KEY")),
        //暂时未知的元素
        unknown(null, Collections.singletonList("")),
        ;
        private final JTDTableMetaService metaService;
        private final List<String> lineStartStrings;

        public static EnumTableMetaType getTypeByLine(String line) {
            return Arrays.stream(EnumTableMetaType.values()).filter(e -> e.getLineStartStrings().stream().anyMatch(line::startsWith))
                    .findFirst().orElse(EnumTableMetaType.unknown);
        }
    }
}
