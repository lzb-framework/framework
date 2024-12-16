package com.pro.framework.generator.main.copymodule;


import com.pro.framework.generator.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * 拷贝模块
 */
public class CopyFolder {


//    public static void main(String[] args) {
//        String moduleName = "SysMsg".toUpperCase();
//
//
//        String fromPath = "/Users/fa/parent_projects/ai/demo-ui-admin/src/parent-ui";
//        String toPath = "/Users/fa/parent_projects/store/store-ui-admin/src/parent-ui";
//        copyFiles(fromPath, toPath);
//    }

    private static void copyFiles(String fromPath, String toPath) {
        List<File> directorys = FileUtils.loopDirectory(new File(fromPath));

        //先复制目录,再复制文件
        for (File directory : directorys) {
                String toDirectoryName = (directory.getParent() + "/" + directory.getName()).replace(fromPath, toPath);
                boolean ok = new File(toDirectoryName).mkdirs();
                System.out.println("生成文件夹: " + ok + "|" + toDirectoryName);
//            }
        }
    }

}
