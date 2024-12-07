package com.pro.framework.generator.utils.file.css;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

public class CSSFileSplitter {
    /**
     * 这个方法用于将包含[data-v-xxxxxxx]后缀的CSS规则分组并保存到对应的data-v-xxxxxxx.css文件，
     * 而不包含[data-v-xxxxxxx]后缀的样式则保存到common.css文件中。
     */
//    public static void main(String[] args) {
//        String path = "/Users/fa/parent_projects/...";
////        String path = "/Users/fa/parent_projects/store/parent/framework/framework-generator/src/main/java/com/pro/utils/file/css/";
//
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(path + "input.css"));
//            Map<String, StringBuilder> groupedCss = new HashMap<>();
//            StringBuilder commonCss = new StringBuilder();
//            boolean isInsideBrackets = false;
//            StringBuilder currentBlock = new StringBuilder();
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                if (line.contains("[data-v-")) {
//                    isInsideBrackets = true;
//                    currentBlock.append(line).append("\n");
//                } else if (isInsideBrackets) {
//                    currentBlock.append(line).append("\n");
//                    if (line.trim().endsWith("}")) {
//                        isInsideBrackets = false;
//                        Pattern pattern = Pattern.compile("\\[data-v-(.*?)\\]");
//                        Matcher matcher = pattern.matcher(currentBlock.toString());
//                        if (matcher.find()) {
//                            String dataVValue = matcher.group(1);
//                            if (!groupedCss.containsKey(dataVValue)) {
//                                groupedCss.put(dataVValue, new StringBuilder());
//                            }
//                            groupedCss.get(dataVValue).append(currentBlock);
//                        } else {
//                            commonCss.append(currentBlock);
//                        }
//                        currentBlock.setLength(0);
//                    }
//                } else {
//                    commonCss.append(line).append("\n");
//                }
//            }
//
//            // Write the grouped CSS rules to data-v-xxxxxxx.css files
//            for (Map.Entry<String, StringBuilder> entry : groupedCss.entrySet()) {
//                String dataVValue = entry.getKey();
//                String fileName = path + "data-v-" + dataVValue + ".css";
//                BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
//                writer.write(entry.getValue().toString());
//                writer.close();
//            }
//
//            // Write the common CSS rules to common.css
//            BufferedWriter commonCssWriter = new BufferedWriter(new FileWriter(path + "common.css"));
//            commonCssWriter.write(commonCss.toString());
//            commonCssWriter.close();
//
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
