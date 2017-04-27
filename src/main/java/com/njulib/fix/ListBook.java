package com.njulib.fix;

import com.njulib.object.InfoReader;
import com.njulib.object.Book;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by padeoe on 2017/4/25.
 */
public class ListBook {
    private static int id = 1;
    private static StringBuffer output = new StringBuffer("<html>\n" +
            "<head><meta charset='UTF-8'></head>" +
            "<table border=\"1\">\n" +
            "<tr>\n" +
            "  <th>id</th>\n" +
            "  <th>编号</th>\n" +
            "  <th>书名</th>\n" +
            "  <th>作者</th>\n" +
            "  <th>出版年份</th>\n" +
            "  <th>分类</th>\n" +
            "</tr>\n");

    public static void main(String[] args) {
        getAllBooks(new File(args[0])).forEach(book -> output.append(getBookLineInTable(book)));
        output.append("</table>\n");
        output.append("</html>");
        FileWriter writer = null;
        try {
            writer = new FileWriter("out.html", false);
            writer.write(output.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static List<Book> getAllBooks(File rootDir) {
        List<Book> result = new LinkedList<>();
        for (File subDir : rootDir.listFiles()) {
            if (subDir.getName().startsWith("《")) {
                File infoFile = subDir.toPath().resolve("info.txt").toFile();
                if (infoFile.exists()) {
                    result.add(new InfoReader(infoFile.getPath()).read());
                }

            } else {
                result.addAll(getAllBooks(subDir));
            }
        }
        return result;
    }

    public static class BookAndDir {
        Book book;
        File Dir;

        public BookAndDir(Book book, File dir) {
            this.book = book;
            Dir = dir;
        }

        public Book getBook() {
            return book;
        }

        public void setBook(Book book) {
            this.book = book;
        }

        public File getDir() {
            return Dir;
        }

        public void setDir(File dir) {
            Dir = dir;
        }
    }

    /**
     * 获取目录下所有书籍
     *
     * @param rootDir
     * @return
     */
    public static Stream<BookAndDir> getAllBooksAndDir(File rootDir) {
        Stream<File> inputFileStream = Arrays.stream(rootDir.listFiles());
        return inputFileStream.flatMap(subDir -> {
            if (subDir.getName().startsWith("《")) {
                File infoFile = subDir.toPath().resolve("info.txt").toFile();
                if (infoFile.exists()) {
                    return Arrays.stream(new BookAndDir[]{new BookAndDir(new InfoReader(infoFile.getPath()).read(), subDir)});
                }
                return null;
            } else {
                return getAllBooksAndDir(subDir);
            }
        }).filter(bookAndDir -> bookAndDir.getBook() != null);
    }

    private static String getBookLineInTable(Book book) {
        if (book != null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("<tr>\n");
            stringBuffer.append(getLine(id + ""));
            id++;
            stringBuffer.append(getAttr(Book::getId, book));
            stringBuffer.append(getAttr(Book::getName, book));
            stringBuffer.append(getAttr(Book::getAuthor, book));
            stringBuffer.append(getAttr(Book::getPublishDate, book));
            stringBuffer.append(getAttr(Book::getDetailBookClass, book));
            stringBuffer.append("</tr>");
            return stringBuffer.toString();
        } else {
            return null;
        }

    }

    private static String getAttr(Function<Book, String> attrGetter, Book book) {
        return getLine(attrGetter.apply(book));
    }

    private static String getLine(String content) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<td>");
        stringBuffer.append(content);
        stringBuffer.append("</td>\n");
        return stringBuffer.toString();
    }
}
