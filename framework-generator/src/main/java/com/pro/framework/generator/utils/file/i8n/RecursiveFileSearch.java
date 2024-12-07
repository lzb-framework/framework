package com.pro.framework.generator.utils.file.i8n;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecursiveFileSearch {

//    public static void main(String[] args) {
//        String directoryPath = "/Users/fa/projects/exchange/exchange-ui-user/src"; // 你的目标文件夹路径
//        String searchTerm = "\\$t\\((.*?)\\)"; // 正则表达式用于匹配括号内的内容
//
//        searchFiles(directoryPath, searchTerm);
//    }

    public static void searchFiles(String directoryPath, String searchTerm) {
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            System.out.println("目标文件夹不存在: " + directoryPath);
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    searchFiles(file.getAbsolutePath(), searchTerm); // 递归调用以遍历子文件夹
                } else {
//                    if (file.getName().endsWith(".txt")) { // 你可以根据需要更改文件扩展名
                        searchInFile(file, searchTerm);
//                    }
                }
            }
        }
    }

    public static void searchInFile(File file, String searchTerm) {
        try (Scanner scanner = new Scanner(file)) {
            Pattern pattern = Pattern.compile(searchTerm);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
//                    System.out.println("文件: " + file.getAbsolutePath());
//                    System.out.println("匹配内容: " + matcher.group(1)); // 括号内的内容
                    System.out.println(matcher.group(1)); // 括号内的内容
                }
            }
        } catch (IOException e) {
            System.err.println("处理文件时发生错误: " + e.getMessage());
        }
    }
}
