package com.pro.framework.api.util;

public class ReplaceSpecialChars {
    /**
     * 把特殊字符替换为下划线
     * 方便快速选中一个单词一段话,快速翻译
     */
    public static String replaceSpecialToUnderline(String str) {
        return (str == null) ? null : str.replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5]", "_");
    }

    public static void main(String[] args) {
        System.out.println(replaceSpecialToUnderline("例如_^09\\d{9}$_09开头的11位数"));
    }
}
