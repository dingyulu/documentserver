package org.bzk.documentserver.utils;

import java.io.*;

import com.aspose.words.Document;
import com.aspose.words.License;
import com.aspose.words.FontSettings;
import com.aspose.words.SaveFormat;

public class DocxToPdfConverter {


    public static boolean run(String inPath, String outputFilePath) {
//        if (!getLicense()) { // 验证License 若不验证则转化出的pdf文档会有水印产生
//            return false;
//        }
//        String inPath = "D:\\GoogleDownload\\ExportWord_230803_032555.docx";
//        String outputFilePath = "C:\\Users\\WPC\\Desktop\\test.pdf";
        FileOutputStream os = null;

        try {
            long old = System.currentTimeMillis();
            File file = new File(outputFilePath); // 新建一个空白pdf文档
            os = new FileOutputStream(file);
            System.out.println("inPath.............."+inPath);
            com.aspose.words.Document doc = new Document(inPath); // Address是将要被转化的word文档
            //Linux 下使用字体库
//            FontSettings fontSettings = new FontSettings();
//            fontSettings.setFontsFolder("/usr/share/fonts/windows"+File.separator,true);
//            doc.setFontSettings(fontSettings);
            doc.save(os, SaveFormat.PDF);// 全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF,
            // EPUB, XPS, SWF 相互转换
            long now = System.currentTimeMillis();
            System.out.println("pdf转换成功，共耗时：" + ((now - old) / 1000.0) + "秒"); // 转化用时
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
    public static boolean getLicense() {
        boolean result = false;
        try {
            InputStream is = DocxToPdfConverter.class.getClassLoader().getResourceAsStream("\\license.xml"); // license.xml应放在资源路径下
            License aposeLic = new License();
            aposeLic.setLicense(is);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}




