package org.bzk.documentserver.service.impl;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.*;
import java.util.zip.*;

import com.itextpdf.kernel.pdf.*;
@SpringBootApplication
public class ZipToPdfConverter  {

//    public static void main(String[] args) {
//        SpringApplication.run(ZipToPdfConverter.class, args);
//    }
//
//    @Override
    public void run(String... args) throws Exception {
        // 读取ZIP文件并解压缩
        File zipFile = new ClassPathResource("input.zip").getFile();
        File tempDir = new File("temp");
        tempDir.mkdirs();
        Unzip(zipFile, tempDir);

        // 创建PDF文档对象
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("output.pdf"));
        document.open();

        // 读取解压缩后的PNG图像文件
        File[] pngFiles = tempDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (pngFiles != null) {
            for (File pngFile : pngFiles) {
                if (pngFile.isFile()) {
                    // 创建PDF页面，并添加图像
                    document.newPage();
                    com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(pngFile.getAbsolutePath());
                    image.scaleToFit(document.getPageSize());
                    document.add(image);
                }
            }
        }

        // 关闭PDF文档
        document.close();
        writer.close();

        // 删除临时文件夹
        DeleteDirectory(tempDir);

        System.out.println("转换完成！PDF文件已生成：output.pdf");
    }

    private void Unzip(File zipFile, File destDir) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry = zis.getNextEntry();
        while (entry != null) {
            String fileName = entry.getName();
            File newFile = new File(destDir.getAbsolutePath() + File.separator + fileName);
            new File(newFile.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zis.closeEntry();
            entry = zis.getNextEntry();
        }
        zis.close();
    }

    private void DeleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    DeleteDirectory(file);
                }
            }
        }
        dir.delete();
    }
}
