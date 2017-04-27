package utils;

import com.njulib.fix.ListBook;
import com.njulib.object.InfoReader;
import com.njulib.spider.BookDownloader;
import utils.conversion.PDFTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by padeoe on 2017/4/27.
 */
public class ImageMeger {
    private String rootDir;
    private String outputDir;

    public static void main(String[] args) {
      //  args=new String[]{"G:\\Book","G:\\BookPDF"};
        ImageMeger imageMeger = new ImageMeger(args[0], args[1]);
        imageMeger.start();
    }

    public void start() {
        final int[] i = {1};
        ListBook.getAllBooksAndDir(new File(rootDir)).filter(
                bookAndDir-> !(Paths.get(outputDir,bookAndDir.getDir().getName()+".pdf").toFile().exists())
        ).forEach(
                bookAndDir -> {
                    try {
                        PDFTool.generatePDFFromImage(
                                Arrays.stream(
                                        bookAndDir.getDir().listFiles()).filter(
                                        file -> file.getName().endsWith(".png") || file.getName().endsWith("jpg")
                                ).toArray(File[]::new),
                                Paths.get(outputDir, bookAndDir.getDir().getName() + ".pdf").toFile(),
                                bookAndDir.getBook()
                        );
                        String bookName=bookAndDir.getBook().getName();
                        String output="\r"+i[0] + " "+bookName;
                        StringBuffer spaces=new StringBuffer();
                        for(int k=0;k<80-output.length();k++){
                            spaces.append(" ");
                        }
                        System.out.print(output+spaces.toString());
                        i[0]++;

                           BookDownloader.deleteDir(bookAndDir.getDir());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    public ImageMeger(String rootDir, String outputDir) {
        this.rootDir = rootDir;
        this.outputDir = outputDir;
    }

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}
