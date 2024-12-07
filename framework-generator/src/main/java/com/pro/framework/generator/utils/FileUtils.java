package com.pro.framework.generator.utils;

import cn.hutool.core.util.ArrayUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    /**
     * 查出所有文件(包括自己和所有层级的下级)
     * @param file       文件/目录
     */
    public static List<File> loopFiles(File file) {
        return loopFiles(file, null);
    }

    /**
     * 查出所有文件(包括自己和所有层级的下级)
     * @param file       文件/目录
     * @param fileFilter 可以自顶向下过滤排除
     */
    public static List<File> loopFiles(File file, FileFilter fileFilter) {
        final List<File> fileList = new ArrayList<>();
        if (null == file || !file.exists()) {
            return fileList;
        }

        if (file.isDirectory()) {
            final File[] subFiles = file.listFiles();
            if (ArrayUtil.isNotEmpty(subFiles)) {
                for (File tmp : subFiles) {
                    fileList.addAll(loopFiles(tmp, fileFilter));
                }
            }
        } else {
            if (null == fileFilter || fileFilter.accept(file)) {
                fileList.add(file);
            }
        }

        return fileList;
    }

    /**
     * 查出所有目录(包括自己和所有层级的下级)
     * @param directory       目录
     */
    public static List<File> loopDirectory(File directory) {
        final List<File> fileList = new ArrayList<>();
        if (null == directory || !directory.exists()) {
            return fileList;
        }
        if (directory.isDirectory()) {
            fileList.add(directory);
            final File[] subFiles = directory.listFiles();
            if (ArrayUtil.isNotEmpty(subFiles)) {
                for (File tmp : subFiles) {
                    fileList.addAll(loopDirectory(tmp));
                }
            }
        }
        return fileList;
    }
}
