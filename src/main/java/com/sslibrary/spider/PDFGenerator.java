package com.sslibrary.spider;

import cn.chineseall.Node;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;
import com.sslibrary.object.Book;

import java.io.*;
import java.util.List;

/**
 * Created by padeoe on 2017/4/24.
 */
public class PDFGenerator {
    static String converComamndLocation="D:\\ImageMagick-7.0.5-Q16\\convert.exe";
    File sourceDir;
    File outputDir;
    Book book;

    public PDFGenerator(File sourceDir, File outputDir, Book book) {
        this.sourceDir = sourceDir;
        this.outputDir = outputDir;
        this.book = book;
    }

    public void make(){
        String outputPath=outputDir.toPath().resolve(sourceDir.getName()).toString();
        String[] commands = new String[]{converComamndLocation, "-density","300","-units","PixelsPerInch",sourceDir.getPath()+System.getProperty("file.separator")+"*p*", outputPath+"-tmp.pdf"};
        Runtime runtime = Runtime.getRuntime();
        Process process;
        System.out.println(book.getName()+"开始合成pdf");
        try {
            process = runtime.exec(commands);
            InputStream is = process.getErrorStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bf = new BufferedReader(isr);
            String line;
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();
            addBookMark(book,outputPath+"-tmp.pdf",outputPath+".pdf");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addBookMark(book,outputPath+"-tmp.pdf",outputPath+".pdf");

    }

    public static void addBookMark(Book book,String src,String dest){
        PdfDocument pdfDoc = null;
        try {
            pdfDoc = new PdfDocument(new PdfReader(src), new PdfWriter(dest));
            PdfOutline root = pdfDoc.getOutlines(false);
            PdfDocumentInfo info=pdfDoc.getDocumentInfo();
            info.setTitle(book.getName());
            info.setAuthor(book.getAuthor());
            List<Node> nodes = book.getOutline();
            addOutline(nodes, root, pdfDoc);
            pdfDoc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void addOutline(List<Node> nodes, PdfOutline root, PdfDocument pdfDocument) {
        for (Node node : nodes) {
            PdfOutline child = root.addOutline(node.getTitle());
            child.addAction(PdfAction.createGoTo(
                    PdfExplicitDestination.createFitH(pdfDocument.getPage(node.getPage()),
                            pdfDocument.getPage(node.getPage()).getPageSize().getTop())));
            addOutline(node.getChildren(), child, pdfDocument);

        }
    }


}
