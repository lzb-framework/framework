package com.pro.framework.cache;

import java.io.IOException;
import java.util.List;

public class CommandExecutor {

    private static final String BASE_PATH = "/Users/zubin/IdeaProjects/lottery-github/";

    public static void main(String[] args) {
        List<Command> commands = List.of(
            new Command("checkout", "https://github.com/lzb-lottery/platform"),
            new Command("checkout", "https://github.com/lzb-lottery/platform"),
            new Command("checkout", "https://github.com/lzb-lottery/platform"),
            new Command("install", "@/parent"),
            new Command("cmd", "sshpass -p \"aaaaaaaaa\" scp -o StrictHostKeyChecking=no -q @/platform/lottery-user/target/lottery-user.jar root@192.168.1.1:/project/lottery/lottery-user.jar"),
            new Command("cmd", "ssh -i aaaaaaaaa root@192.168.1.1 << 'EOF' pkill -f lottery-user.jar || true; nohup java -jar /project/lottery/lottery-user.jar & EOF")
        );

        for (Command command : commands) {
            try {
                executeCommand(command);
            } catch (Exception e) {
                System.err.println("Error executing command: " + command);
                e.printStackTrace();
            }
        }
    }

    private static void executeCommand(Command command) throws IOException, InterruptedException {
        String resolvedCommand = resolvePaths(command);
        System.out.println("Executing: " + resolvedCommand);

        Process process = new ProcessBuilder()
            .command("/bin/bash", "-c", resolvedCommand)
            .inheritIO()
            .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command failed with exit code " + exitCode);
        }
    }

    private static String resolvePaths(Command command) {
        return command.getParams().replace("@/", BASE_PATH);
    }

    static class Command {
        private final String type;
        private final String params;

        public Command(String type, String params) {
            this.type = type;
            this.params = params;
        }

        public String getType() {
            return type;
        }

        public String getParams() {
            return params;
        }

        @Override
        public String toString() {
            return "Command{" +
                "type='" + type + '\'' +
                ", params='" + params + '\'' +
                '}';
        }
    }
}
