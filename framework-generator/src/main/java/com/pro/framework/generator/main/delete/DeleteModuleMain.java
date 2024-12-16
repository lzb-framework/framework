package com.pro.framework.generator.main.delete;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import com.pro.framework.generator.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * 拷贝模块
 */
public class DeleteModuleMain {


//    public static void main(String[] args) {
//        String moduleName = "spy.properties";
//
//
//        String fromPath = "/Users/fa/projectnew/snowball";
//        deleteFiles(moduleName, fromPath);
//
//    }

    private static void deleteFiles(String moduleName, String fromPath) {
        List<File> directorys = FileUtils.loopDirectory(new File(fromPath));
        //删除目录
        for (File directory : directorys) {
            if (directory.getName().equals(moduleName)) {
                FileUtil.del(directory);
                Console.log("删除了 " + directory);
            }
        }
        List<File> files = FileUtils.loopFiles(new File(fromPath));
        //删除文件
        for (File file : files) {
            if (file.getName().equals(moduleName)) {
                FileUtil.del(file);
                Console.log("删除了 " + file);
            }
        }
    }
}
