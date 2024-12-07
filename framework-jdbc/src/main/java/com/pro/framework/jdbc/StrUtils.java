//package com.pro.framework.jdbc;
//
//public class StrUtils {
//
//    public static String toUnderlineCase(String camelCase) {
//        if (camelCase == null || camelCase.isEmpty()) {
//            return camelCase;
//        }
//
//        StringBuilder result = new StringBuilder();
//        result.append(Character.toLowerCase(camelCase.charAt(0))); // 首字母转为小写
//
//        for (int i = 1; i < camelCase.length(); i++) {
//            char currentChar = camelCase.charAt(i);
//            // 如果当前字符是大写字母，则在其前面添加下划线，并将其转换为小写字母
//            if (Character.isUpperCase(currentChar)) {
//                result.append('_').append(Character.toLowerCase(currentChar));
//            } else {
//                result.append(currentChar);
//            }
//        }
//
//        return result.toString();
//    }
//
//    public static String lowerFirst(String str) {
//         if (str == null || str.isEmpty()) {
//             return str;
//         }
//         char[] charArray = str.toCharArray();
//         charArray[0] = Character.toLowerCase(charArray[0]);
//         return new String(charArray);
//     }
//}
