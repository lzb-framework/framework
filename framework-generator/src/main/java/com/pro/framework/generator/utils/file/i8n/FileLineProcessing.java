package com.pro.framework.generator.utils.file.i8n;

import java.io.*;
import java.util.*;

public class FileLineProcessing {
//    public static void main(String[] args) {
//        String filePath = "/Users/fa/parent_projects/..."; // 替换为你的文件路径
////        String filePath = "/Users/fa/parent_projects/store/parent/framework/framework-generator/src/main/java/com/pro/utils/file/i8n/1.txt"; // 替换为你的文件路径
//
//        try {
//            List<String> lines = readAndProcessFile(filePath);
//
//            // 去重复并排序
//            Set<String> uniqueSortedLines = new TreeSet<>(lines);
//
//            // 打印结果
//            for (String line : uniqueSortedLines) {
//                System.out.println(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static List<String> readAndProcessFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 对每一行进行trim
                line = line.trim();
                if (!line.isEmpty()) { // 只添加非空行
                    lines.add(line);
                }
            }
        }

        return lines;
    }
}
