package com.pro.framework.generator.utils;

import cn.hutool.core.util.StrUtil;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class ReplacerRegular {

//    public static void main(String[] args) {
//        String fullStr = "insert into t_role(role_id,role_name,"
//                + "role_level,org_id,role_type,role_status,add_user_id,"
//                + "add_time,upd_user_id,upd_time) values(#{id},"
//                + "${role_name},${role_level},${org_id},${role_type},"
//                + "${role_status},"
//                + "#E{accId},#{dateInt},#{accId},#E{dateInt})";
//        Pattern pattern = Pattern.compile("#E\\{[\\w]*\\}");
//        System.out.println(matchReplace(fullStr, pattern, matchStr ->
//                //去掉 #E{accId} 中的 #E{}
//                matchStr.substring(3, matchStr.length() - 1)
//        ));
//    }

    private static String matchReplace(String str, Pattern pattern, Function<String, String> matchStrConsume) {
        String[] strs = {str};
        matchDo(strs[0], pattern, matchGroup0 -> {
            //进行替换
            strs[0] = StrUtil.replace(strs[0], matchGroup0.getSubStrStart(), matchGroup0.getSubStr(), matchStrConsume.apply(matchGroup0.getSubStr()), false);
            // strs[0] = strs[0].replace(matchGroup0.getSubStr(), matchStrConsume.apply(matchGroup0.getSubStr()));
        });
        return strs[0];
    }

    private static void matchDo(String fullStr, Pattern pattern, Consumer<MatchGroup>... matchStrConsume) {
        assert null != matchStrConsume && matchStrConsume.length > 0;
        Matcher matcher = pattern.matcher(fullStr);
        //循环，字符串中有多少个符合的，就循环多少次
        while (matcher.find()) {
            int groupCount = matcher.groupCount();
            System.out.println("groupCount:" + groupCount);
            IntStream.range(0, groupCount + 1).forEach(i -> {
                //每一个符合正则的字符串
                String group = matcher.group(i);
                int start = matcher.start(i);
                int end = matcher.end(0);
                String subStr = fullStr.substring(start, end);
                System.out.println("匹配字符串:" + subStr + " group规则表达式:" + group + " 开始位置:" + start + " 结束位置:" + end);
                matchStrConsume[i].accept(new MatchGroup(group, subStr, start, end));
            });
        }
    }
}
