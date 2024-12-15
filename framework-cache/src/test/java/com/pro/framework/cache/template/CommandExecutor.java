package com.pro.framework.cache.template;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CommandExecutor {
    public static void main(String[] args) {
        // 定义命令列表
        List<Command> commands = new ArrayList<>();

//        commands.add(new Command("git -C \"/Users/zubin/IdeaProjects/lottery-github/framework\" pull || git clone \"https://github.com/lzb-framework/framework.git\" \"/Users/zubin/IdeaProjects/lottery-github\""));
//        commands.add(new Command("git -C \"/Users/zubin/IdeaProjects/lottery-github/parent\" pull || git clone \"https://github.com/lzb-parent/parent.git\" \"/Users/zubin/IdeaProjects/lottery-github\""));
//        commands.add(new Command("git -C \"/Users/zubin/IdeaProjects/lottery-github/platform\" pull || git clone \"https://github.com/lzb-lottery/platform.git\" \"/Users/zubin/IdeaProjects/lottery-github\""));
//        commands.add(new Command("mvn -f /Users/zubin/IdeaProjects/lottery-github/parent/pom.xml -DskipTests=true -DannotationProcessorPaths=true clean install"));
        commands.add(new Command("sshpass -p \"F13lbYuMkQro8fAgqBdO0dVHUtRs668qlnQszCDmnIiTy6MVsGjfgbLlPw2OF2akjwib52So8JO1dLHQ\" scp -o StrictHostKeyChecking=no -q /Users/zubin/IdeaProjects/lottery-github/platform/lottery-user/target/lottery-user.jar root@111.230.10.171:/project/lottery/lottery-user.jar"));
        commands.add(new Command("ssh -i aaaaaaaaa root@192.168.1.1 << 'EOF' \n pkill -f lottery-user.jar || true \n nohup java -jar /project/lottery/lottery-user.jar & \n EOF"));

        // 遍历并执行命令
        for (Command command : commands) {
            executeCommand(command);
        }
    }

    private static void executeCommand(Command command) {
        try {
            System.out.println("执行命令: " + command.getCommand());

            // 使用 ProcessBuilder 执行命令
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command.getCommand());
            processBuilder.redirectErrorStream(true); // 合并标准错误和标准输出

            Process process = processBuilder.start();

            // 读取命令输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("命令执行成功");
            } else {
                System.err.println("命令执行失败，退出码: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 命令类
    static class Command {
        private final String command;

        public Command(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }
    }
}
