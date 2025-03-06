package com.pro.framework.api.util;

import cn.hutool.core.util.StrUtil;
import com.pro.framework.api.FrameworkConst;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StrUtils {
    public static final String EMPTY = "";

    public static String camelToUnderline(String param) {

        if (null == param || param.length() == 0) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append("_");
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

//    public static Boolean isEmpty(String str) {
//        return null == str || str.length() == 0;
//    }

//    public static String lowerFirst(String str) {
//        if (str == null || str.isEmpty()) {
//            return str;
//        }
//        char[] charArray = str.toCharArray();
//        charArray[0] = Character.toLowerCase(charArray[0]);
//        return new String(charArray);
//    }

    public static <T> boolean empty(T o) {
        if (null == o) {
            return true;
        } else if (o instanceof Collection) {
            return empty((Collection) o);
        } else if (o instanceof CharSequence) {
            return empty((CharSequence) o);
        } else if (o.getClass().isArray()) {
            return empty((Object[]) o);
        } else if (o instanceof Map) {
            return empty((Map) o);
        } else {
            return false;
        }
    }

    public static <T> boolean empty(T[] o) {
        return null == o || 0 == o.length;
    }

    public static boolean empty(Collection o) {
        return null == o || 0 == o.size();
    }

    public static boolean empty(CharSequence s) {
        return null == s || 0 == s.length();
    }

    public static boolean empty(Map map) {
        return null == map || map.isEmpty();
    }


    @Deprecated
    public static <T> boolean noEmpty(T o) {
        return !empty(o);
    }


    /**
     * 首字母转换小写
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String firstToLowerCase(String param) {
        if (null == param || param.length() == 0) {
            return FrameworkConst.Str.EMPTY;
        }
        return param.substring(0, 1).toLowerCase() + param.substring(1);
    }

    /**
     * 首字母转换小写
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String firstToUpperCase(String param) {
        if (null == param || param.length() == 0) {
            return FrameworkConst.Str.EMPTY;
        }
        return param.substring(0, 1).toUpperCase() + param.substring(1);
    }

    public static boolean isBlank(CharSequence str) {
        final int length;
        if ((str == null) || ((length = str.length()) == 0)) {
            return true;
        }

        for (int i = 0; i < length; i++) {
            // 只要有一个非空字符即为非空字符串
            if (!isBlankChar(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    /**
     * 是否空白符<br>
     */
    public static boolean isBlankChar(char c) {
        return isBlankChar((int) c);
    }

    /**
     * 是否空白符<br>
     */
    public static boolean isBlankChar(int c) {
        return Character.isWhitespace(c)
                || Character.isSpaceChar(c)
                || c == '\ufeff'
                || c == '\u202a'
                || c == '\u0000'
                // issue#I5UGSQ，Hangul Filler
                || c == '\u3164'
                // Braille Pattern Blank
                || c == '\u2800'
                // MONGOLIAN VOWEL SEPARATOR
                || c == '\u180e';
    }

    /**
     * 取出url中的参数map
     */
    public static Map<String, String> parseParameters(String query) {
        Map<String, String> parameters = new HashMap<>();
        if (query != null) {
            if (query.contains("?")) {
                query = query.substring(query.indexOf("?") + 1);
            }
            Arrays.stream(query.split("&"))
                    .map(kv -> kv.split("="))
                    .filter(kv -> kv.length == 2)
                    .forEach(kv -> parameters.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8)));
        }

        return parameters;
    }

    /**
     * 取第一个不为空对象
     */
    @SafeVarargs
    public static <T> T or(T... list) {
        return Arrays.stream(list).filter(StrUtils::noEmpty).findAny().orElse(list[list.length - 1]);
    }


    /**
     * 是否包含中文
     */
    public static boolean isContainsChinese(String str) {
        if (null == str) {
            return false;
        }
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }

//    /**
//     * 替换存在分隔的字符 ([opt+右][alt+右]没办法一次性跳过的,全选内容的字符)
//     */
//    public static String replaceSpecialStr(String str) {
//        if (str == null) {
//            return "";
//        }
//
//        StringBuilder sb = new StringBuilder();
//        for (char c : str.toCharArray()) {
//            if ('_' == c || Character.isLetterOrDigit(c) || Character.UnicodeBlock.of(
//                    c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
//                sb.append(c);
//            }
//        }
//        return sb.toString();
//    }


    public static <T> List<T> split(String str, Class<T> clazz) {
        return split(str, ",", clazz, true);
    }

    public static <T> List<T> split(String str, String split, Class<T> clazz, Boolean ignoreEmpty) {
        if (str == null || str.length() == 0) {
            return Collections.emptyList();
        }
        if (ignoreEmpty) {
            str = str.trim();
        }
        Stream<String> stream = Arrays.stream(str.split(split));
        if (ignoreEmpty) {
            stream = stream.filter(StrUtil::isNotEmpty);
        }
        return stream.peek(s -> {
            if (s.isEmpty()) {
                throw new RuntimeException("Multi-value configurations cannot have intermediate spaces.");
            }
        }).map(s -> BeanUtils.convert(s, clazz)).collect(Collectors.toList());
    }

    /**
     * 把特殊字符替换为下划线
     * 方便快速选中一个单词一段话,快速翻译
     */
    public static String replaceSpecialToUnderline(String str) {
        return (str == null) ? null : str.replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5]", "_");
    }
}
