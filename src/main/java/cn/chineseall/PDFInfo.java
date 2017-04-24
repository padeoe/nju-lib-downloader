package cn.chineseall;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;
import com.itextpdf.kernel.xmp.XMPException;

import java.io.IOException;
import java.util.List;

/**
 * Created by padeoe on 2017/4/11.
 */
public class PDFInfo {
    public static void main(String[] args) {
        addBookMark(new Book("10000328908","汉语大词典订补","汉语大词典订补","汉语大词典编纂处","2010-12-01","",""),"C:\\Users\\padeoe\\Desktop\\总.pdf","C:\\Users\\padeoe\\Desktop\\汉语大词典订补.pdf");
    }

    public static String getTitle(String src){
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(src));
            PdfDocumentInfo info = pdfDoc.getDocumentInfo();
            String title=info.getTitle();
            pdfDoc.close();
            return title;
        } catch (Exception e) {
            return null;
        }
    }

    public static void addBookMark(Book book,String src,String dest){
        PdfDocument pdfDoc = null;
        try {
            pdfDoc = new PdfDocument(new PdfReader(src), new PdfWriter(dest));
            PdfOutline root = pdfDoc.getOutlines(false);
            PdfDocumentInfo info=pdfDoc.getDocumentInfo();
            info.setTitle(book.getName());

            info.setAuthor(CoreService.baseUrl+"/book/"+book.getId());
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
