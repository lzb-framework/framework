package com.pro.framework.generator.main.copymodule;

import cn.hutool.core.io.FileUtil;
import com.pro.framework.generator.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * 拷贝模块
 */
public class CopyModuleOldToOld {


//    public static void main(String[] args) {
//        String moduleName = "TradePrice".toUpperCase();
//
//
//        String fromPath = "/Users/fa/projects/trade/trade-platform";
//        String toPath = "/Users/fa/projects/exchange/exchange-platform";
//        String fromGroupName = "trade";
//        String toGroupName = "exchange";
//        copyFiles(moduleName, fromGroupName, toGroupName, fromPath, toPath);
//
//        fromPath = "/Users/fa/projects/trade/trade-ui-user";
//        toPath = "/Users/fa/projects/exchange/exchange-ui-user";
//        fromGroupName = "trade";
//        toGroupName = "exchange";
//        copyFiles(moduleName, fromGroupName, toGroupName, fromPath, toPath);
//
//        fromPath = "/Users/fa/projects/trade/trade-ui-admin";
//        toPath = "/Users/fa/projects/exchange/exchange-ui-admin";
//        fromGroupName = "trade";
//        toGroupName = "exchange";
//        copyFiles(moduleName, fromGroupName, toGroupName, fromPath, toPath);
//
//        fromPath = "/Users/fa/projects/trade/trade-ui-agent";
//        toPath = "/Users/fa/projects/exchange/exchange-ui-agent";
//        fromGroupName = "trade";
//        toGroupName = "exchange";
//        copyFiles(moduleName, fromGroupName, toGroupName, fromPath, toPath);
//
//    }

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
