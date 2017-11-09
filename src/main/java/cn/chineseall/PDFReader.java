package cn.chineseall;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.filter.TextRegionEventFilter;
import com.itextpdf.kernel.pdf.canvas.parser.listener.FilteredEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;
import com.itextpdf.test.annotations.type.SampleTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;

@Category(SampleTest.class)
public class PDFReader {
//    public static final String SRC = "C:\\Users\\padeo\\Desktop\\nameddestinations.pdf";
public static final String SRC = "C:\\Users\\padeo\\Desktop\\0081.pdf";

    @BeforeClass
    public static void main() throws IOException {
//        PdfReader pdfReader = new PdfReader(file);
//        PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);
//
//        strategy = parser.processContent(currentPage, new SimpleTextExtractionStrategy());
//        content = strategy.getResultantText();

        File file = new File(SRC);
        file.getParentFile().mkdirs();
    }

    @Test
    public void manipulatePdf() throws IOException {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(SRC));
        Rectangle rect = new Rectangle(36, 750, 523, 56);

        FontFilter fontFilter = new FontFilter(rect);
        FilteredEventListener listener = new FilteredEventListener();
        LocationTextExtractionStrategy extractionStrategy = listener.attachEventListener(new LocationTextExtractionStrategy(), fontFilter);
        new PdfCanvasProcessor(listener).processPageContent(pdfDoc.getFirstPage());

        String actualText = extractionStrategy.getResultantText();
        System.out.println(actualText);

        pdfDoc.close();


    }


    class FontFilter extends TextRegionEventFilter {
        public FontFilter(Rectangle filterRect) {
            super(filterRect);
        }

        @Override
        public boolean accept(IEventData data, EventType type) {
            return true;
//            if (type.equals(EventType.RENDER_TEXT)) {
//                TextRenderInfo renderInfo = (TextRenderInfo) data;
//
//                PdfFont font = renderInfo.getFont();
//                if (null != font) {
//                    String fontName = font.getFontProgram().getFontNames().getFontName();
//                    System.out.println(fontName);
//                    return fontName.equals("FZHTK-GBK1-0200020e4");
//                    //FZHTK-GBK1-0200020e4
//                    //return fontName.endsWith("Bold") || fontName.endsWith("Oblique");
//                }
//            }
//            return false;
        }
    }
}