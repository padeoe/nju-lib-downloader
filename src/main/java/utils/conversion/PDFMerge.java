package utils.conversion;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PDFMerge {

    public static void mergePDFs(File[] pdfs, Path outFilePath) throws IOException {
        PDFMergerUtility PDFmerger = new PDFMergerUtility();
        PDFmerger.setDestinationFileName(outFilePath.toString());
        for (File file : pdfs) PDFmerger.addSource(file);
        PDFmerger.mergeDocuments(MemoryUsageSetting.setupMixed(1024 * 1024 * 500));
    }

    public static void compressPDF(Path originPDF, Path outfilePath) throws IOException {
        PdfReader pdfReader = new PdfReader(originPDF.toString());
        PdfDocument inputPdfDoc = new PdfDocument(pdfReader);
        File outputPDF = new File(outfilePath.toString());
        PdfDocument outPdfDoc = new PdfDocument(new PdfWriter(outputPDF.getPath()
        ).setSmartMode(true));

        int size = inputPdfDoc.getNumberOfPages();
        inputPdfDoc.copyPagesTo(1, size, outPdfDoc);
        outPdfDoc.close();
        inputPdfDoc.close();
    }


}
