package com.pro.framework.javatodb.util;

public class DatabaseUrlParser {

    public static String removeDatabaseNameFromUrl(String url) {
        // 使用正则表达式只去掉数据库名称部分（即 /lottery2）
        return url.replaceFirst("(/[^/?]+)(?=\\?)", "");
    }

    public static void main(String[] args) {
        String originalUrl = "jdbc:mysql://localhost:3306/lottery2?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        String modifiedUrl = removeDatabaseNameFromUrl(originalUrl);
        System.out.println("Modified URL: " + modifiedUrl);
        // 输出：jdbc:mysql://localhost:3306?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    }
}
