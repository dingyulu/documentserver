package org.bzk.documentserver.utils;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfTextFormField;
import com.itextpdf.kernel.pdf.canvas.draw.ILineDrawer;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;

public class PdfFormToReadOnlyConverter {
    public static void convert(String inputPdfPath, String outputPdfPath) {

        try {
            // 加载待处理的 PDF
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputPdfPath), new PdfWriter(outputPdfPath));
            System.out.println("pdfdoc...."+inputPdfPath +" "+ outputPdfPath +" "+ pdfDoc);
            // 获取 PDF 表单
            PdfAcroForm pdfForm = PdfAcroForm.getAcroForm(pdfDoc, true);

            // 将所有表单字段设置为只读
            for (PdfFormField field : pdfForm.getFormFields().values()) {

                field.setReadOnly(true);

            }
           // Flatten the form so that form fields are no longer editable
          // pdfForm.flattenFields();

            // 关闭文档
            pdfDoc.close();

            System.out.println("转换完成！PDF 文件已生成：" + outputPdfPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 判断字段是否有下划线
    private static boolean hasUnderline(PdfTextFormField textField) {
      //获取字段的边框颜色

        float borderWidth = textField.getBorderWidth(); // 获取边框宽度

// 检查边框样式
// 这里以边框宽度大于0来表示存在下划线，你也可以根据实际情况进行判断
        if (borderWidth > 0) {
            return true;
            // 存在下划线的处理逻辑
        } else {
            return false;
            // 不存在下划线的处理逻辑
        }
    }
    private static void setHeaderFieldsReadOnly(PdfDocument pdfDoc, PdfAcroForm pdfForm) {
        int numPages = pdfDoc.getNumberOfPages();

        for (int pageNum = 1; pageNum <= numPages; pageNum++) {
            PdfDictionary pageDict = pdfDoc.getPage(pageNum).getPdfObject();
            PdfDictionary resources = pageDict.getAsDictionary(PdfName.Resources);

            // Check if there are XObject resources
            if (resources != null) {
                PdfDictionary xObject = resources.getAsDictionary(PdfName.XObject);

                // Iterate through XObject resources
                if (xObject != null) {
                    for (PdfName key : xObject.keySet()) {
                        PdfObject obj = xObject.get(key);

                        // Check if the XObject is a stream
                        if (obj.isStream()) {
                            PdfDictionary xObjDict = ((PdfStream) obj).getAsDictionary(PdfName.Resources);
                            PdfAcroForm xObjForm = PdfAcroForm.getAcroForm(pdfDoc, true);

                            // Check if there is an AcroForm in the XObject
                            if (xObjForm != null) {
                                // Iterate through form fields in the XObject and set them as read-only
                                for (PdfFormField field : xObjForm.getFormFields().values()) {

                                    field.setReadOnly(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
