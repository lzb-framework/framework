package com.pro.framework.generator.main.copymodule;

import cn.hutool.core.io.FileUtil;
import com.pro.framework.generator.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * 拷贝模块
 */
public class CopyModuleToOld {


//    public static void main(String[] args) {
//        String moduleName = "UserMoneyWait".toUpperCase();
//
//
//        String toPath = "/Users/fa/projects/yungou/yungou-platform";
//        String fromPath = "/Users/fa/parent_projects/ai/parent/common";
//        String toGroupName = "yungou";
//        String fromGroupName = "common";
//        copyFiles(moduleName, fromGroupName, toGroupName, fromPath, toPath);
//
//        toPath = "/Users/fa/projects/yungou/yungou-ui-user";
//        fromPath = "/Users/fa/parent_projects/ai/lottery-ui-user";
//        toGroupName = "yungou";
//        fromGroupName = "ai";
//        copyFiles(moduleName, fromGroupName, toGroupName, fromPath, toPath);
//
//
//        toPath = "/Users/fa/projects/yungou/yungou-ui-admin";
//        fromPath = "/Users/fa/parent_projects/ai/lottery-ui-admin";
//        toGroupName = "yungou";
//        fromGroupName = "ai";
//
//        copyFiles(moduleName, fromGroupName, toGroupName, fromPath, toPath);
//
//
//        toPath = "/Users/fa/projects/yungou/yungou-ui-agent";
//        fromPath = "/Users/fa/parent_projects/ai/lottery-ui-agent";
//        toGroupName = "yungou";
//        fromGroupName = "ai";
//        copyFiles(moduleName, fromGroupName, toGroupName, fromPath, toPath);
//
//    }

    private static void copyFiles(String moduleName, String fromGroupName, String toGroupName, String fromPath, String toPath) {
        List<File> directorys = FileUtils.loopDirectory(new File(fromPath));
        List<File> files = FileUtils.loopFiles(new File(fromPath));

        //先复制目录,再复制文件
        for (File directory : directorys) {
            if (directory.getName().toUpperCase().contains(moduleName)) {
                String toDirectoryName = (directory.getParent() + "/" + directory.getName()).replace(fromPath, toPath)
                        .replaceAll(fromGroupName + "-common", "##temp###")
                        .replaceAll(fromGroupName, toGroupName)
                        .replaceAll("##temp###", toGroupName + "-common");
                boolean ok = new File(toDirectoryName).mkdirs();
                System.out.println("生成文件夹: " + ok + "|" + toDirectoryName);

            }
        }

        //复制文件和内容
        for (File file : files) {
            if (file.getName().toUpperCase().contains(moduleName)) {
                String toFilePath = (file.getParent() + "/" + file.getName()).replace(fromPath, toPath)
                        .replaceAll(fromGroupName + "-common", "##temp###")
                        .replaceAll(fromGroupName, toGroupName)
                        .replaceAll("##temp###", toGroupName + "-common")
                        ;
                if (!toFilePath.contains("/target/") && !toFilePath.contains("/node_modules/")) {
                    FileUtil.writeUtf8String(FileUtil.readUtf8String(file).replace(fromGroupName, toGroupName), new File(toFilePath));
//                    System.out.println("生成文件: " + toFilePath);
                    System.out.println("生成文件: " + "|" + toFilePath);
                }
            }
        }
    }
}
