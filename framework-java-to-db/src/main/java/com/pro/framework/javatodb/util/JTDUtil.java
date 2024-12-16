package com.pro.framework.javatodb.util;

import com.pro.framework.api.model.FrameworkException;
import com.pro.framework.api.structure.Tuple2;
import com.pro.framework.api.util.AssertUtil;
import com.pro.framework.javatodb.constant.JTDConstInner;
import lombok.Cleanup;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 工具类
 *
 * @author administrator
 * @date 2022-01-20
 */
public class JTDUtil {

    public static final String EMPTY = "";

    public static String format(String format, Object keyValue) {
        return null != keyValue && JTDUtil.isNotBlank(keyValue.toString()) ? MessageFormat.format(format, keyValue) : "";
    }

    /**
     * @param errMsg 例如 name={0}, age={1}
     */
    @SneakyThrows
    public static void assertTrue(Boolean flag, String errMsg, String... params) {
        if (null == flag || !flag) {
            throw new JTDException(MessageFormat.format(errMsg, (Object[]) params));
        }
    }

    @SneakyThrows
    public static String or(String... ss) {
        if (ss == null) {
            return null;
        }
        return Arrays.stream(ss).filter(JTDUtil::isNotBlank).findFirst().orElse(ss[ss.length - 1]);
    }

    @SafeVarargs
    @SneakyThrows
    public static <T> T or(T... ss) {
        if (ss == null) {
            return null;
        }
        return Arrays.stream(ss).filter(Objects::nonNull).findFirst().orElse(ss[ss.length - 1]);
    }

    public static boolean isBlank(String str) {
        return null == str || EMPTY.equals(str.trim());
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String append(CharSequence delimiter, String... strs) {
        return Arrays.stream(strs).filter(JTDUtil::isNotBlank).collect(Collectors.joining(delimiter));
    }

//    public static Set<Class<?>> getClasses(String pack) {
//
//        // 第一个class类的集合
//        Set<Class<?>> classes = new LinkedHashSet<>();
//        // 获取包的名字 并进行替换
//        String packageName = pack;
//        String packageDirName = packageName.replace('.', '/');
//        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
//        Enumeration<URL> dirs;
//        try {
//            dirs = Thread.currentThread().getContextClassLoader().getResources(
//                    packageDirName);
//            // 循环迭代下去
//            while (dirs.hasMoreElements()) {
//                // 获取下一个元素
//                URL url = dirs.nextElement();
//                // 得到协议的名称
//                String protocol = url.getProtocol();
//                // 如果是以文件的形式保存在服务器上
//                if ("file".equals(protocol)) {
//                    // 获取包的物理路径
//                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
//                    // 以文件的方式扫描整个包下的文件 并添加到集合中
//                    findAndAddClassesInPackageByFile(packageName, filePath,
//                            true, classes);
//                } else if ("jar".equals(protocol)) {
//                    // 如果是jar包文件 定义一个JarFile
//                    JarFile jar;
//                    try {
//                        // 获取jar
//                        jar = ((JarURLConnection) url.openConnection())
//                                .getJarFile();
//                        // 从此jar包 得到一个枚举类
//                        Enumeration<JarEntry> entries = jar.entries();
//                        // 同样的进行循环迭代
//                        while (entries.hasMoreElements()) {
//                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
//                            JarEntry entry = entries.nextElement();
//                            String name = entry.getName();
//                            // 如果是以/开头的
//                            if (name.charAt(0) == '/') {
//                                // 获取后面的字符串
//                                name = name.substring(1);
//                            }
//                            // 如果前半部分和定义的包名相同
//                            if (name.startsWith(packageDirName)) {
//                                int idx = name.lastIndexOf('/');
//                                // 如果以"/"结尾 是一个包
//                                if (idx != -1) {
//                                    // 获取包名 把"/"替换成"."
//                                    packageName = name.substring(0, idx)
//                                            .replace('/', '.');
//                                }
//                                // 如果可以迭代下去 并且是一个包
//                                // 如果是一个.class文件 而且不是目录
//                                if (name.endsWith(".class")
//                                        && !entry.isDirectory()) {
//                                    // 去掉后面的".class" 获取真正的类名
//                                    String className = name.substring(
//                                            packageName.length() + 1, name
//                                                    .length() - 6);
//                                    try {
//                                        // 添加到classes
//                                        classes.add(Class
//                                                .forName(packageName + '.'
//                                                        + className));
//                                    } catch (ClassNotFoundException e) {
//                                        // log
//                                        // .error("添加用户自定义视图类错误 找不到此类的.class文件");
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
//                        }
//                    } catch (IOException e) {
//                        // log.error("在扫描用户定义视图时从jar包获取文件出错");
//                        e.printStackTrace();
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return classes;
//    }

    /**
     * 以文件的形式来获取包下的所有Class
     */
    public static void findAndAddClassesInPackageByFile(
            String packageName,
            String packagePath, final boolean recursive, Set<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
        File[] dirfiles = dir.listFiles(file -> (recursive && file.isDirectory())
                || (file.getName().endsWith(".class")));
        // 循环所有文件
        if (dirfiles != null) {
            for (File file : dirfiles) {
                // 如果是目录 则继续扫描
                if (file.isDirectory()) {
                    findAndAddClassesInPackageByFile(packageName + "."
                                    + file.getName(), file.getAbsolutePath(), recursive,
                            classes);
                } else {
                    // 如果是java类文件 去掉后面的.class 只留下类名
                    String className = file.getName().substring(0,
                            file.getName().length() - 6);
                    try {
                        // 添加到集合中去
                        //classes.add(Class.forName(packageName + '.' + className));
                        //经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
                        classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                    } catch (ClassNotFoundException e) {
                        // log.error("添加用户自定义视图类错误 找不到此类的.class文件");
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    /**
     * 将驼峰式命名的字符串转换为下划线方式。如果转换前的驼峰式命名的字符串为空，则返回空字符串。<br>
     * 例如：
     *
     * <pre>
     * HelloWorld=》hello_world
     * Hello_World=》hello_world
     * HelloWorld_test=》hello_world_test
     * </pre>
     *
     * @param str 转换前的驼峰式命名的字符串，也可以为下划线形式
     * @return 转换后下划线方式命名的字符串
     */
    public static String toUnderlineCase(CharSequence str) {
        return toSymbolCase(str, JTDConstInner.CHAR_UNDERLINE);
    }

    /**
     * 首字母转换小写
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String firstToUpperCase(String param) {
        if (null == param || param.length() == 0) {
            return JTDConstInner.EMPTY;
        }
        return param.substring(0, 1).toUpperCase() + param.substring(1);
    }

    /**
     * 首字母转换小写
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String firstToLowerCase(String param) {
        if (null == param || param.length() == 0) {
            return JTDConstInner.EMPTY;
        }
        return param.substring(0, 1).toLowerCase() + param.substring(1);
    }


    /**
     * 字符串下划线转驼峰格式
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String underlineToCamel(String param) {
        if (null == param || param.length() == 0) {
            return JTDConstInner.EMPTY;
        }
        String temp = param.toLowerCase();
        int len = temp.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = temp.charAt(i);
            if (c == JTDConstInner.CHAR_UNDERLINE) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(temp.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将驼峰式命名的字符串转换为使用符号连接方式。如果转换前的驼峰式命名的字符串为空，则返回空字符串。<br>
     *
     * @param str    转换前的驼峰式命名的字符串，也可以为符号连接形式
     * @param symbol 连接符
     * @return 转换后符号连接方式命名的字符串
     * @since 4.0.10
     */
    public static String toSymbolCase(CharSequence str, char symbol) {
        if (str == null) {
            return null;
        }

        final int length = str.length();
        final StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < length; i++) {
            c = str.charAt(i);
            final Character preChar = (i > 0) ? str.charAt(i - 1) : null;
            if (Character.isUpperCase(c)) {
                // 遇到大写字母处理
                final Character nextChar = (i < str.length() - 1) ? str.charAt(i + 1) : null;
                if (null != preChar && Character.isUpperCase(preChar)) {
                    // 前一个字符为大写，则按照一个词对待
                    sb.append(c);
                } else if (null != nextChar && Character.isUpperCase(nextChar)) {
                    // 后一个为大写字母，按照一个词对待
                    if (null != preChar && symbol != preChar) {
                        // 前一个是非大写时按照新词对待，加连接符
                        sb.append(symbol);
                    }
                    sb.append(c);
                } else {
                    // 前后都为非大写按照新词对待
                    if (null != preChar && symbol != preChar) {
                        // 前一个非连接符，补充连接符
                        sb.append(symbol);
                    }
                    sb.append(Character.toLowerCase(c));
                }
            } else {
                if (sb.length() > 0 && Character.isUpperCase(sb.charAt(sb.length() - 1)) && symbol != c) {
                    // 当结果中前一个字母为大写，当前为小写，说明此字符为新词开始（连接符也表示新词）
                    sb.append(symbol);
                }
                // 小写或符号
                sb.append(c);
            }
        }
        return sb.toString();
    }


    /**
     * 读取文件所有数据<br>
     * 文件的长度不能超过 {@link Integer#MAX_VALUE}
     *
     * @return 字节码
     */
    @SneakyThrows
    public static byte[] readBytes(File file) {
        long len = file.length();
        if (len >= Integer.MAX_VALUE) {
            throw new JTDException("File is larger then max array size");
        }

        byte[] bytes = new byte[(int) len];
        @Cleanup FileInputStream in = new FileInputStream(file);
        int readLength = in.read(bytes);
        if (readLength < len) {
            throw new IOException(MessageFormat.format("File length is [{0}] but read [{1}]!", len, readLength));
        }
        return bytes;
    }


//    /**
//     * 读取文件内容
//     */
//    public static String readString(File file, Charset charset) {
//        return new String(readBytes(file), charset);
//    }

    /**
     * 读取文件内容
     */
    public static String readString(File file) {
        return new String(readBytes(file), StandardCharsets.UTF_8);
    }

    public static boolean notEmpty(Collection<?> list) {
        return !isEmpty(list);
    }

    public static boolean isEmpty(Collection<?> list) {
        return null == list || list.isEmpty();
    }

//    public static List<Field> getAllFieldsChildCoverParent(Class<?> clazz) {
//        return getClassMetas(clazz, c -> {
//            Field[] fields = c.getDeclaredFields();
//            Map<String, Field> fieldMap = Arrays.stream(fields).collect(Collectors.toMap(Field::getName, f -> f, (f1, f2) -> f1));
//            fields = fieldMap.values().toArray(new Field[0]);
//            return Arrays.stream(fields).filter(f -> !Modifier.isStatic(f.getModifiers()) && !Modifier.isFinal(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())).collect(Collectors.toList());
//        });
//    }

    public static List<Field> getAllFields(Class<?> clazz) {
        Function<Class<?>, List<Field>> getFieldsFun = c -> Arrays.stream(c.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers()) && !Modifier.isFinal(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())).collect(Collectors.toList());
        return getClassMetas(clazz, getFieldsFun, Field::getName);
    }

    private static <T> List<T> getClassMetas(Class<?> clazz, Function<Class<?>, List<T>> getOneClassMetaFun) {
        return getClassMetas(clazz, getOneClassMetaFun, o -> String.valueOf(o.hashCode()));
    }

    private static <T> List<T> getClassMetas(Class<?> clazz, Function<Class<?>, List<T>> getOneClassMetaFun, Function<T, String> metaKeyFun) {
        Map<String, T> map = new LinkedHashMap<>(64);
        Class<?> currClazz = clazz;
        while (!currClazz.equals(Object.class)) {
            List<T> list = getOneClassMetaFun.apply(currClazz);
            Collections.reverse(list);
            list.forEach(f -> map.putIfAbsent(metaKeyFun.apply(f), f));
            currClazz = currClazz.getSuperclass();
        }
        ArrayList<T> list = new ArrayList<>(map.values());
        Collections.reverse(list);
        return list;
    }


    /**
     * list 转 map
     */
    public static <T, KEY> Map<KEY, T> listToMap(Collection<T> list, Function<T, KEY> keyFun) {
        return listToMap(list, keyFun, o -> o, true);
    }


//    /**
//     * list 转 map
//     */
//    public static <T, KEY, Val> Map<KEY, Val> listToMap(Collection<T> list, Function<T, KEY> keyFun, Function<T, Val> valFun) {
//        return listToMap(list, keyFun, valFun, true);
//    }

    /**
     * list 转 map
     * 例如   Map<String, User> userMap = listToMap(new ArrayList<User>(), User::getUsername);
     * 把   [{id:1,username:'u1'},{id:2,username:'u2'},{id:3,username:'u3'}]
     * 转成 {1:{id:1,username:'u1'},2:{id:2,username:'u2'},3:{id:3,username:'u3'}}
     */
    @SneakyThrows
    public static <T, KEY, Val> Map<KEY, Val> listToMap(Collection<T> list, Function<T, KEY> keyFun, Function<T, Val> valFun, boolean repeatReplace) {
        Map<KEY, Val> map = new LinkedHashMap<>();
        if (null != list) {
            for (T t : list) {
                if (null != t) {
                    KEY key = keyFun.apply(t);
                    if (map.containsKey(key) && !repeatReplace) {
                        throw new JTDException("listToMap不能存在重复键值:" + key);
                    }
                    map.put(key, valFun.apply(t));
                }
            }
        }
        return map;
    }

    /**
     * 删掉尾数为0的字符,结尾如果是小数点，则去掉
     */
    public static String shortNum(String str) {
        if (str == null) {
            return null;
        }
        if (str.indexOf(".") > 0) {
            str = str.replaceAll("0+?$", "");
            str = str.replaceAll("[.]$", "");
        }
        return str;
    }

    public static String sub(String str, int size) {
        if (str == null) {
            return null;
        }
        return str.substring(0, Math.min(str.length(), size));
    }

    private static final Pattern pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*");

    public static boolean isNum(String str) {
        return pattern.matcher(str).matches();
    }

    public static void copyProperties(Object source, Object target) {
        Map<String, Field> targetFieldMap = Arrays.stream(target.getClass().getDeclaredFields())
                .collect(Collectors.toMap(Field::getName, Function.identity()));
        int i = 0;
        Arrays.stream(source.getClass().getDeclaredFields())
                .filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                .forEach(sourceField -> {
                    Field targetField = targetFieldMap.get(sourceField.getName());
                    if (targetField != null) {
                        sourceField.setAccessible(true);
                        targetField.setAccessible(true);
                        try {
                            targetField.set(target, sourceField.get(source));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //"jdbc:mysql://localhost:3306/demo?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
    //转成
    //"jdbc:mysql://localhost:3306?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
    //和 snowball2
    public static Tuple2<String, String> dbUrlExtractor(String originalUrl) {

        // 使用正则表达式匹配数据库名称
        String regex = "jdbc:mysql://[^:]+:([0-9]+)/([^?]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(originalUrl);

        if (matcher.find()) {
            String dbName = matcher.group(2); // 数据库名称
            String urlWithoutDbName = originalUrl.substring(0, originalUrl.indexOf("/" + dbName)) + originalUrl.substring(originalUrl.indexOf("?")); // 没有数据库名称的URL

            // 输出结果
            System.out.println("URL: " + urlWithoutDbName);
            System.out.println("Database Name: " + dbName);
            return new Tuple2<>(dbName, urlWithoutDbName);
        } else {
            System.out.println("No match found.");
            throw new FrameworkException("db-url error:" + originalUrl);
//            return null;
        }
    }
}
