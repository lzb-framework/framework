package com.pro.framework.cache.template;

import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class SSHCommandExecutor {
    public static void main(String[] args) {
        String host = "111.230.10.171";  // 远程服务器地址
        String user = "root";            // 用户名
        String password = "F13lbYuMkQro8fAgqBdO0dVHUtRs668qlnQszCDmnIiTy6MVsGjfgbLlPw2OF2akjwib52So8JO1dLHQ";  // 密码
        String command1 = "pkill -f lottery-user.jar || true";  // 要执行的命令1
        String command2 = "nohup java -Xms1024m -Xmx1024m  -Dloader.path=/project/lottery/lib -jar /project/lottery/lottery-user.jar --spring.profiles.active=prod > /project/lottery/lottery-user.log 2>&1 &";  // 要执行的命令2
//        java -Xms1024m -Xmx1024m -Djava.net.preferIPv4Stack=true -Dloader.path=/usr/code/lib -Dserver.port=7777 -jar /usr/code/ai-user.jar --spring.profiles.active=prod,common-prod
//        java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005 -Xms1024m -Xmx1024m -Djava.net.preferIPv4Stack=true -Dloader.path=/usr/code/lib -Dserver.port=7777 -jar /usr/code/ai-user.jar --spring.profiles.active=prod,common-prod

        try {
            // 创建 JSch 实例
            JSch jsch = new JSch();

            // 设置调试输出
            jsch.setLogger(new MyLogger());  // 自定义日志

            // 创建 Session 对象
            Session session = jsch.getSession(user, host);

            // 设置密码认证
            session.setPassword(password);

            // 设置一些配置，避免第一次连接时提示是否信任主机
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(60000); // 设置连接超时为60秒

            // 连接到远程服务器
            session.connect();
            if (!session.isConnected()) {
                System.out.println("SSH 连接失败!");
                return;
            }

            // 打开一个执行命令的通道
            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            // 执行第一个命令：pkill
            channel.setCommand(command1);

            // 获取命令的输入输出流
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            // 执行命令1
            channel.connect();

            // 使用 BufferedReader 读取标准输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(channel.getErrStream()));

            // 输出所有的标准输出内容
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("标准输出: " + line);  // 打印到控制台
            }

            // 输出所有的错误输出内容
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println("pkill 错误输出: " + errorLine);  // 打印到错误输出
            }

            // 等待命令执行完成
            while (!channel.isClosed()) {
                Thread.sleep(100);
            }

            // 获取命令1的退出状态
            int exitStatus1 = channel.getExitStatus();
            System.out.println("pkill 退出码: " + exitStatus1);

            if (exitStatus1 == 0) {
                System.out.println("pkill 命令执行成功！");
            } else {
                System.out.println("pkill 执行失败！退出码: " + exitStatus1);
            }

            // 执行第二个命令：nohup
            channel.setCommand(command2);

            // 执行命令2
            channel.connect();

            // 使用 BufferedReader 读取标准输出
            reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(channel.getErrStream()));

            // 输出所有的标准输出内容
            while ((line = reader.readLine()) != null) {
                System.out.println("标准输出: " + line);  // 打印到控制台
            }

            // 输出所有的错误输出内容
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println("nohup 错误输出: " + errorLine);  // 打印到错误输出
            }

            // 等待命令执行完成
            while (!channel.isClosed()) {
                Thread.sleep(100);
            }

            // 获取命令2的退出状态
            int exitStatus2 = channel.getExitStatus();
            System.out.println("nohup 退出码: " + exitStatus2);

            if (exitStatus2 == 0) {
                System.out.println("nohup 命令执行成功！");
            } else {
                System.out.println("nohup 执行失败！退出码: " + exitStatus2);
            }

            // 关闭通道和 session
            channel.disconnect();
            session.disconnect();

        } catch (JSchException e) {
            System.err.println("SSH连接失败：" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO异常：" + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("线程中断：" + e.getMessage());
            e.printStackTrace();
        }
    }

    // 自定义日志类
    public static class MyLogger implements com.jcraft.jsch.Logger {
        public boolean isEnabled(int level) {
            return true;  // 启用所有级别的日志
        }

        public void log(int level, String message) {
            System.out.println("JSCH日志 - Level: " + level + " - " + message);
        }
    }
}
