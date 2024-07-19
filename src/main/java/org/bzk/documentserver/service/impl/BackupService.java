package org.bzk.documentserver.service.impl;


import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.io.File;
import java.util.Vector;
@Service
public class BackupService {

//    public void backupRemoteFileToLocal() {
//        // 在这里编写备份远程文件到本地磁盘的逻辑
//        // 使用SFTP或其他协议来获取远程文件并保存到本地
//    }
   @Value("${backup.host}")
    String host;  // 远程服务器IP地址
    @Value("${backup.username}")
    String username;     // SSH用户名
    @Value("${backup.password}")
    String password;  // SSH密码（如果使用密码登录）
    @Value("${backup.port}")
    int port;  // SSH端口
    @Value("${backup.localDirectoryData}")
    String localDirectoryData ;  // 服务器目录
    @Value("${backup.localDirectoryDataBak}")
    String localDirectoryDataBak;  // 本地目录


    @Value("${upload.path}")
    String uploadPath ;  // 服务器目录

    @Value("${upload.pathbak}")
    String uploadPathBak ;  // 服务器目录

   // 每天的固定时间执行备份任务
   //@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
  // @Scheduled(cron = "*/10 * * * * ?") // 每10s执行一次
    public void backupRemoteFileToLocal() {


//        String host = "106.54.209.197";  // 远程服务器IP地址
//        String username = "ubuntu";     // SSH用户名
//        String password = "!QAZxsw23edc";  // SSH密码（如果使用密码登录）
//        int port = 22;  // SSH端口
//        String remoteDirectory = "/home/ubuntu/data/documentServer/databak/";  // 服务器目录
//        String localDirectory = "D:\\workspace\\tempdownload\\data\\";  // 本地目录
//        String targetFileExtension = ".pdf";  // 新的文件后缀

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;
        File localDir = new File(localDirectoryData);
        if (!localDir.exists()) {
            localDir.mkdirs(); // 创建目录
        }
       System.out.println("定时器开始执行");
        try {
            // 建立SSH会话
            session = jsch.getSession(username, host, port);

            // 如果使用密码登录，设置密码
            session.setPassword(password);

            // 跳过密钥检查
            session.setConfig("StrictHostKeyChecking", "no");

            session.connect();

            // 打开SFTP通道
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // 更改远程服务器工作目录
            channelSftp.cd(uploadPath);

            // 获取远程目录中的文件列表
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls(".");

            // 遍历每个文件并将其下载到本地目录，并更改文件后缀
            for (ChannelSftp.LsEntry file : files) {
                if (file.getAttrs().isDir()) {
                    // 跳过目录
                    continue;
                }
                String sourceFileName = file.getFilename();

//                // 检查文件后缀是否为".0form"
//                if (!sourceFileName.endsWith(".oform")) {
//                    // 如果后缀不为".0form"，跳过该文件
//                    continue;
//                }
//                String fileNameWithoutExtension = sourceFileName.substring(0, sourceFileName.lastIndexOf(".oform"));
//                // String sourceFileName = file.getFilename();
//                String destinationFileName = fileNameWithoutExtension + targetFileExtension;

                // 下载文件
                try{
                channelSftp.get(sourceFileName, localDirectoryData + sourceFileName);
                    System.out.println(sourceFileName+",,,,"+localDirectoryData + sourceFileName);
                    //dcoxtoPDF(localDirectory + destinationFileName,localDirectory+destinationFileName.substring(0, destinationFileName.lastIndexOf(".docx")) + ".pdf");
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }


    }

    // 每天的固定时间执行备份任务
   // @Scheduled(cron = "0 0 4 * * ?") // 每天凌晨2点执行
   // @Scheduled(cron = "*/20 * * * * ?") // 每10s执行一次
    public void backupRemoteFileToLocal2() {

        System.out.println("定时器开始执行222");
//        String host = "106.54.209.197";  // 远程服务器IP地址
//        String username = "ubuntu";     // SSH用户名
//        String password = "!QAZxsw23edc";  // SSH密码（如果使用密码登录）
//        int port = 22;  // SSH端口
//        String remoteDirectory = "/home/ubuntu/data/documentServer/data/";  // 服务器目录
//        String localDirectory = "D:\\workspace\\tempdownload\\data\\";  // 本地目录
//        String targetFileExtension = ".pdf";  // 新的文件后缀

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;
        File localDir = new File(localDirectoryDataBak);
        if (!localDir.exists()) {
            localDir.mkdirs(); // 创建目录
        }

        try {
            // 建立SSH会话
            session = jsch.getSession(username, host, port);

            // 如果使用密码登录，设置密码
            session.setPassword(password);

            // 跳过密钥检查
            session.setConfig("StrictHostKeyChecking", "no");

            session.connect();

            // 打开SFTP通道
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // 更改远程服务器工作目录
            channelSftp.cd(uploadPathBak);

            // 获取远程目录中的文件列表
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls(".");

            // 遍历每个文件并将其下载到本地目录，并更改文件后缀
            for (ChannelSftp.LsEntry file : files) {
                if (file.getAttrs().isDir()) {
                    // 跳过目录
                    continue;
                }
                String sourceFileName = file.getFilename();

//                // 检查文件后缀是否为".0form"
//                if (!sourceFileName.endsWith(".oform")) {
//                    // 如果后缀不为".0form"，跳过该文件
//                    continue;
//                }
//                String fileNameWithoutExtension = sourceFileName.substring(0, sourceFileName.lastIndexOf(".oform"));
//                // String sourceFileName = file.getFilename();
//                String destinationFileName = fileNameWithoutExtension + targetFileExtension;

                // 下载文件
                try{
                    channelSftp.get(sourceFileName, localDirectoryDataBak + sourceFileName);
                    System.out.println(sourceFileName+",,,,"+localDirectoryDataBak + sourceFileName);
                    //dcoxtoPDF(localDirectory + destinationFileName,localDirectory+destinationFileName.substring(0, destinationFileName.lastIndexOf(".docx")) + ".pdf");
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }


    }


}

