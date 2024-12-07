package com.pro.framework.generator.main.copymodule;

import cn.hutool.core.io.FileUtil;
import com.pro.framework.generator.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * 拷贝模块
 */
public class CopyModuleMultiple {


//    public static void main(String[] args) {
//        copy("poster");
//        copy("posterCategory");
//        copy("sysAddress");
//        copy("sysMsgChannelMerchant");
//        copy("sysMsgChannelTemplate");
//        copy("sysMsgRecord");
//        copy("translation");
//        copy("banner");
//        copy("country");
//        copy("customer");
//        copy("activity");
//        copy("admin");
//        copy("agent");
//        copy("authDict");
//        copy("authRole");
//        copy("authRoute");
//    }

    private static void copy(String toGroupName) {
        String moduleName = "payChannel".toUpperCase();
        String fromPath = "/Users/fa/projectnew/lottery/lottery-ui-admin/src/views/pay";
        String toPath = "/Users/fa/projectnew/lottery/lottery-ui-admin/src/views/sys";
        String fromGroupName = "payChannel";
        copyFiles(moduleName, fromGroupName, toGroupName, fromPath, toPath);
    }

    private static void copyFiles(String moduleName, String fromGroupName, String toGroupName, String fromPath, String toPath) {
        List<File> directorys = FileUtils.loopDirectory(new File(fromPath));
        List<File> files = FileUtils.loopFiles(new File(fromPath));

        //先复制目录,再复制文件
        for (File directory : directorys) {
            if (directory.getName().toUpperCase().contains(moduleName)) {
                String toDirectoryName = (directory.getParent() + "/" + directory.getName()).replace(fromPath, toPath).replaceAll(fromGroupName, toGroupName);
                boolean ok = new File(toDirectoryName).mkdirs();
                System.out.println("生成文件夹: " + ok + "|" + toDirectoryName);

            }
        }

        //复制文件和内容
        for (File file : files) {
            if (file.getName().toUpperCase().contains(moduleName)) {
                String toFilePath = (file.getParent() + "/" + file.getName()).replace(fromPath, toPath).replaceAll(fromGroupName, toGroupName);
                if (!toFilePath.contains("/target/") && !toFilePath.contains("/node_modules/")) {
                    FileUtil.writeUtf8String(FileUtil.readUtf8String(file).replace(fromGroupName, toGroupName), new File(toFilePath));
//                    System.out.println("生成文件: " + toFilePath);
                    System.out.println("生成文件: " + "未知" + "|" + toFilePath);
                }
            }
        }
    }


}
