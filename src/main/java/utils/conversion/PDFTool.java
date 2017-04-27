package utils.conversion;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;
import com.njulib.object.Book;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用于处理前期图片产物，压制成pdf，并给pdf添加书本信息
 * Created by padeoe on 2017/4/26.
 */
public class PDFTool {

    /**
     * 将图片合成为一个PDF
     * @param inputImage 图片，格式为图片格式
     * @param outputPDF 输出文件
     * @throws FileNotFoundException
     * @throws MalformedURLException
     */
    public static void generatePDFFromImage(File[] inputImage,File outputPDF) throws FileNotFoundException, MalformedURLException {
        List<Image>images=new LinkedList<>();
        for(File file:inputImage){
            images.add(new Image(ImageDataFactory.create(file.getPath())));
        }
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputPDF.getPath()));

        images.forEach(image -> pdfDoc.addNewPage(new PageSize(new Rectangle(image.getImageScaledWidth(), image.getImageScaledHeight()))));
        BackgroundEventHandler handler = new BackgroundEventHandler(images);
        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, handler);
        pdfDoc.close();
    }

    public static void generatePDFFromImage(File[] inputImage,File outputPDF,Book book) throws FileNotFoundException{
        List<Image>images=new LinkedList<>();
        boolean hasException=false;
        for(File file:inputImage){
            try {
                images.add(new Image(ImageDataFactory.create(file.getPath())));
            } catch (MalformedURLException e) {
                System.err.println(file.getPath());
                e.printStackTrace();
            } catch (com.itextpdf.io.IOException eee){
                hasException=true;
            }
        }
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputPDF.getPath()));
        PdfDocumentInfo info=pdfDoc.getDocumentInfo();
        if(book.getName()!=null&&!book.getName().equals("null")){
            info.setTitle(book.getName());
        }
        if(book.getAuthor()!=null&&!book.getAuthor().equals("null")){
            info.setAuthor(book.getAuthor());
        }
        if(book.getTheme()!=null&&!book.getTheme().equals("null")){
            info.setSubject(book.getTheme());
        }
        StringBuffer keyword=new StringBuffer();
        if(book.getPublishDate()!=null&&!book.getPublishDate().equals("null")){
            keyword.append("出版时间:"+book.getPublishDate()+"\n");
        }
        if(book.getBookClass()!=null&&!book.getBookClass().equals("null")){
            keyword.append("分类:"+book.getDetailBookClass().replaceAll("图书馆",""));
        }
        info.setKeywords(keyword.toString());
        if(hasException){
            System.err.println(book.getName()+" 图片格式异常");
            info.setCreator("exception");
        }

        images.forEach(image -> pdfDoc.addNewPage(new PageSize(new Rectangle(image.getImageScaledWidth(), image.getImageScaledHeight()))));
        BackgroundEventHandler handler = new BackgroundEventHandler(images);
        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, handler);
        pdfDoc.close();
    }

    private static class BackgroundEventHandler implements IEventHandler {
        protected List<Image> images;
        protected int offset=0;

        public BackgroundEventHandler(List<Image> images) {
            this.images = images;
        }
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(),
                    page.getResources(), pdfDoc);
            Rectangle area = page.getPageSize();
            new Canvas(canvas, pdfDoc, area)
                    .add(images.get(offset));
            offset++;
        }
    }
}
