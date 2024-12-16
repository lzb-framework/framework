import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.pro.framework.api.util.JSONUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 一键 新建项目,复制项目
 */
public class CopyProjectTest {
    @SneakyThrows
    public static void main(String[] args) {
        copyProject();
    }

    /**
     * --- 复制项目 ---
     */
//    @Test
    @SneakyThrows
    public static void copyProject() {
        String fromProject = "Snowball";
        String toProject = "Homeparking";

        String sourcePath = "/Users/zubin/IdeaProjects/snowball";
        String destinationPath = "/Users/zubin/IdeaProjects/homeparking";

        String fromProjectLower = fromProject.toLowerCase();

        // 1.全量拷贝 文件,文件夹
        copyProject_1(sourcePath, destinationPath, fromProjectLower);

        // 2.修改文件夹名称,和文件内容
        changeFolder_3(destinationPath, fromProject, toProject);

//        Thread.sleep(1000);

        // 3.初始化 git
        executeCommand(destinationPath + "/platform", getInitGitCommands("git@github.com:lzb-homeparking/platform.git"));
        executeCommand(destinationPath + "/" + "ui-user", getInitGitCommands("git@github.com:lzb-homeparking/ui-user.git"));
        executeCommand(destinationPath + "/" + "ui-admin", getInitGitCommands("git@github.com:lzb-homeparking/ui-admin.git"));

        //        executeCommand(destinationPath + "/" + "ui-agent", getInitGitCommands("git@github.com:lzb-homeparking/ui-agent.git"));
    }

    /**
     * 1.全量拷贝 文件,文件夹
     */
    public static void copyProject_1(String sourcePath, String destinationPath, String fromProjectLower) {
        // 忽略的文件夹
        String[] relativeFoldersToIgnore = {
                "platform/.git",
                "ui-user/.git",
//                "ui-agent/.git",
                "ui-admin/.git",
        };
        String[] nameFoldersToIgnore = {"node_modules", "target", "logs", "dist", ".idea"};

        copyFolder(sourcePath, destinationPath, relativeFoldersToIgnore, nameFoldersToIgnore);
//        renameFilesAndFolders(destinationPath,fromProjectLower,)
    }
//    public static void main(String[] args) {
//            String basePath = "/path/to/your/directory";
//            String oldString = "ai";
//            String newString = "gym";
//
//            File baseDir = new File(basePath);
//            if (baseDir.exists() && baseDir.isDirectory()) {
//                renameFilesAndFolders(baseDir, oldString, newString);
//            } else {
//                System.out.println("The provided base path is not a directory or does not exist.");
//            }
//        }

    public static void renameFilesAndFolders(File dir, String oldString, String newString) {
        // List all files and directories in the current directory
        File[] files = dir.listFiles();

        // Traverse subdirectories first
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    renameFilesAndFolders(file, oldString, newString);
                }
            }

            // Rename files
            for (File file : files) {
                if (file.isFile() && file.getName().contains(oldString)) {
                    String newName = file.getName().replace(oldString, newString);
                    File newFile = new File(file.getParent(), newName);
                    if (!file.renameTo(newFile)) {
                        System.out.println("Failed to rename file: " + file.getAbsolutePath());
                    }
                }
            }

            // Rename directories
            for (File file : files) {
                if (file.isDirectory() && file.getName().contains(oldString)) {
                    String newName = file.getName().replace(oldString, newString);
                    File newDir = new File(file.getParent(), newName);
                    if (!file.renameTo(newDir)) {
                        System.out.println("Failed to rename directory: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }


    /**
     * 3.修改文件夹名称,和文件内容
     */
    public static void changeFolder_3(String destinationPath, String fromProject, String toProject) {

        Set<String> excludePaths = new HashSet<>(Arrays.asList(".idea", ".git", "parent", "parent-ui", "node_modules"));

        List<Reg> regs = List.of(
                new Reg("", "Mapper"),
                new Reg("", "Dao"),
                new Reg("", "Service"),
                new Reg("", "Controller"),
                new Reg("", "Vo"),
                new Reg("", "Request"),
                new Reg("", "Common"),

                new Reg("User", ""),
                new Reg("UserMoney", ""),
                new Reg("UserAmountTotal", ""),
                new Reg("UserLevelConfig", ""),
                new Reg("EnumAuthRoute", ""),
                new Reg("Enum", "Product"),
                new Reg("Agent", "")
        );


        // 遍历所有目录,文件,修改名字,路径
        Map<String, String> replaceNameMap = getReplaceNameMap(fromProject, toProject, regs);
        changeFileRecursively(new File(destinationPath), excludePaths, f -> rename(f, replaceNameMap));

        // 遍历所有文件,修改内容
        Map<String, String> replaceContentMap = getReplaceContentMap(fromProject, toProject, regs);
        changeFileRecursively(new File(destinationPath), excludePaths, f -> replaceContent(f, replaceContentMap));
    }

    private static Map<String, String> getReplaceNameMap(String fromProject, String toProject, List<Reg> regs) {
        Map<String, String> replaceNameMap = new HashMap<>();
        regs
                .forEach(reg -> reg.addToMap(replaceNameMap, fromProject, toProject));
        List.of(new Reg("", ""))
                .forEach(reg -> reg.addToMap(replaceNameMap, fromProject, toProject));
        List.of(new Reg("", ""))
                .forEach(reg -> reg.addToMapLowerProject(replaceNameMap, fromProject, toProject));
        return replaceNameMap;
    }

    private static Map<String, String> getReplaceContentMap(String fromProject, String toProject, List<Reg> regs) {
        Map<String, String> replaceContentMap = new HashMap<>();
        regs
                .forEach(reg -> reg.addToMap(replaceContentMap, fromProject, toProject));
        regs
                .forEach(reg -> reg.addToMapLowerFirstPrefix(replaceContentMap, fromProject, toProject));
        List.of(
                        new Reg("", "\\-"),
                        new Reg("_", " "),
                        new Reg(">", "<"),
                        new Reg("\\.", "\\."),
                        new Reg("_", " "),
                        new Reg("_", " "),
                        new Reg("3306/", "")
                )
                .forEach(reg -> reg.addToMapLowerProject(replaceContentMap, fromProject, toProject));

        List.of(
                        new Reg("", " "),
                        new Reg("level", ""),
                        new Reg(" ", "")
                )
                .forEach(reg -> reg.addToMap(replaceContentMap, fromProject, toProject));

        replaceContentMap.put("Enum([a-zA-Z]+)" + fromProject, "Enum$1" + toProject);
        replaceContentMap.put("enum([a-zA-Z]+)" + fromProject, "enum$1" + toProject);
        replaceContentMap.put(fromProject + "([a-zA-Z]+)", toProject + "$1");
        return replaceContentMap;
    }

    private static void changeFileRecursively(File file, Set<String> excludePaths, Consumer<File> consumer) {
        String name = file.getName();
        if (!excludePaths.contains(name)) {
            if (file.isDirectory()) {
                // 子目录,子文件
                File[] files = file.listFiles();
                if (files != null) {
                    for (File subFile : files) {
                        changeFileRecursively(subFile, excludePaths, consumer);
                    }
                }
                //父目录
                consumer.accept(file);
            }
            // 底层文件
            else {
                consumer.accept(file);
            }
        }
    }

    private static void replaceContent(File file, Map<String, String> replaceMap) {
        if (file.isFile()) {
            String rs = FileUtil.readUtf8String(file);
            String rsInit = rs;
            List<String> keys = new ArrayList<>();
            for (String key : replaceMap.keySet()) {
                String rsInit0 = rs;
                String value = replaceMap.get(key);
                rs = rs.replaceAll(key, value);
                if (!rsInit0.equals(rs)) {
                    keys.add(key);
                }
            }
            if (!rsInit.equals(rs)) {
                FileUtil.writeUtf8String(rs, file);
                System.out.println("修改文件内容" + JSONUtil.toJsonStr(keys) + file);
            }
        }
    }

    private static void rename(File file, Map<String, String> replaceMap) {
        String name = file.getName();
        // 文件夹
        if (file.isDirectory()) {
            replaceMap.forEach((key, value) -> {
                if (name.startsWith(key) || name.endsWith(key)) {
                    String newName = name.replace(key, value);
                    if (!newName.equals(name)) {
                        File newFile = new File(file.getParent(), newName);
                        // 文件夹 重命名
                        renameFolderWithFiles(file.getAbsolutePath(), newFile.getAbsolutePath());
                    }
                }
            });
        }
        // 文件
        else {
            String innerName = name.lastIndexOf(".") >= 0 ? name.substring(0, name.lastIndexOf(".")) : name;
            replaceMap.forEach((key, value) -> {
                if (innerName.startsWith(key) || innerName.endsWith(key)) {
                    String newName = name.replace(key, value);
                    if (!newName.equals(name)) {
                        File newFile = new File(file.getParent(), newName);
                        // 文件 重命名
                        boolean flag = file.renameTo(newFile);
                        System.out.println(flag ? "Renamed file: " + name + " to " + newName : "Failed to rename file: " + name);
                    }
                }
            });
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void renameFolderWithFiles(String sourcePath, String destinationPath) {
        File sourceFolder = new File(sourcePath);
        File destinationFolder = new File(destinationPath);

        if (sourceFolder.exists() && sourceFolder.isDirectory()) {
            // 创建目标文件夹
            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }

            // 获取源文件夹下的所有文件和子文件夹
            File[] files = sourceFolder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        // 对文件进行重命名
                        renameFile(file, destinationPath);
                    } else if (file.isDirectory()) {
                        // 递归处理子文件夹
                        renameFolderWithFiles(file.getAbsolutePath(), destinationPath + File.separator + file.getName());
                    }
                }

                // 对文件夹进行重命名
//                sourceFolder.renameTo(destinationFolder);
                sourceFolder.delete();
            }
        }
    }

    private static void renameFile(File file, String destinationPath) {
        String newName = file.getName().replace("oldText", "newText"); // 替换为你的重命名逻辑
        File newFile = new File(destinationPath, newName);

        if (file.renameTo(newFile)) {
            System.out.println("Renamed file: " + file.getName() + " to " + newName);
        } else {
            System.err.println("Failed to rename file: " + file.getName());
        }
    }


    /**
     * 执行系统指令
     */
    @SneakyThrows
    private static void executeCommand(String workingDirectory, String[][] commandss) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        Map<String, String> environment = processBuilder.environment();
        // 确保环境中有正确的 PATH
        environment.put("PATH", "/usr/bin:/usr/local/bin:" + System.getenv("PATH"));
        File directory = new File(workingDirectory);
        System.out.println("Current PATH: " + environment.get("PATH"));
        processBuilder.directory(directory);
        for (String[] commands : commandss) {
//            String[] commandStr = splitCommand(command);
            processBuilder.command(commands);
            Process process = processBuilder.start();
            System.out.println((process.waitFor() == 0 ? "exe: " : "== exe-fail: ") + String.join(" ", commands));
        }
    }

    private static String[] splitCommand(String gitCommand) {
        // Split by space, but consider double-quoted sections as a whole
        return gitCommand.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    }

    /**
     * 2.执行
     */
    @SneakyThrows
    public static void copyFolder(String sourcePath, String destinationPath, String[] relativeFoldersToIgnore, String[] nameFoldersToIgnore) {
        Path source = Paths.get(sourcePath);
        Path destination = Paths.get(destinationPath);

        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relativePath = source.relativize(dir);

                // Check if the relative path contains any folder to ignore
                if (shouldIgnore(relativePath.toString(), relativeFoldersToIgnore)) {
                    return FileVisitResult.SKIP_SUBTREE; // Skip the entire directory
                }

                // Check if the folder name should be ignored
                for (String folderToIgnore : nameFoldersToIgnore) {
                    if (dir.endsWith(folderToIgnore)) {
                        return FileVisitResult.SKIP_SUBTREE; // Skip the entire directory
                    }
                }

                Path targetDir = destination.resolve(relativePath);
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = source.relativize(file);

                // Check if the relative path contains any folder to ignore
                if (shouldIgnore(relativePath.toString(), relativeFoldersToIgnore)) {
                    return FileVisitResult.CONTINUE; // Skip the individual file
                }

                // Check if the file name should be ignored
                for (String fileToIgnore : nameFoldersToIgnore) {
                    if (file.getFileName().toString().equals(fileToIgnore)) {
                        return FileVisitResult.CONTINUE; // Skip the individual file
                    }
                }

                Path targetFile = destination.resolve(relativePath);
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static boolean shouldIgnore(String path, String[] ignoreList) {
        for (String item : ignoreList) {
            if (path.contains(item)) {
                return true;
            }
        }
        return false;
    }


    private static String[][] getInitGitCommands(String gitUrl) {

        // 初始化git
        return new String[][]{
                {"/bin/sh", "-c", "git init"},
                {"/bin/sh", "-c", "git remote add origin " + gitUrl},
                {"/bin/sh", "-c", "git remote set-url origin " + gitUrl},
                {"/bin/sh", "-c", "git push -u origin --all "},
                {"/bin/sh", "-c", "git push origin --tags "},
        };
    }

    @Data
    @AllArgsConstructor
    public static class Reg {
        String prefix;
        String suffix;
        Boolean lowercase;
        /**
         * String fromProject = "snowball";
         * String toProject = "Demo";
         * 返回指定的 正则/内容
         */
        BiFunction<String, String, String> fun;

        public Reg(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public void addToMap(Map<String, String> replaceMap, String fromProject, String toProject) {
            addToMap(replaceMap, fromProject, toProject, false, false);
        }

        public void addToMapLowerFirstPrefix(Map<String, String> replaceMap, String fromProject, String toProject) {
            addToMap(replaceMap, fromProject, toProject, true, false);
        }

        public void addToMapLowerProject(Map<String, String> replaceMap, String fromProject, String toProject) {
            addToMap(replaceMap, fromProject, toProject, false, true);
        }

        /**
         * 填充过滤map
         *
         * @param replaceNameMap   外部map
         * @param fromProject      来源项目单词
         * @param toProject        目标项目单词
         * @param lowerFirstPrefix 前缀后缀是否转小写
         * @param lowerProject     项目单词是否转小写
         */
        public void addToMap(Map<String, String> replaceNameMap, String fromProject, String toProject, Boolean lowerFirstPrefix, Boolean lowerProject) {
            String prefix = lowerFirstPrefix ? StrUtil.lowerFirst(this.prefix) : this.prefix;
            String suffix = lowerFirstPrefix ? StrUtil.lowerFirst(this.suffix) : this.suffix;
            String fromProject1 = lowerProject ? fromProject.toLowerCase() : fromProject;
            String toProject1 = lowerProject ? toProject.toLowerCase() : toProject;
            String key = (null == prefix ? "" : prefix) + fromProject1 + (null == suffix ? "" : suffix);
            String value = (null == prefix ? "" : prefix) + toProject1 + (null == suffix ? "" : suffix);
            replaceNameMap.put(key, value);
        }
    }

}
