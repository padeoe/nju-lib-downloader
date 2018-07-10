package cn.chineseall;

import com.njulib.spider.BookDownloader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;
import utils.conversion.PDFMerge;
import utils.network.MyHttpRequest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by padeoe on 2017/4/10.
 */
public class Downloader {
    private Book book;
    private CoreService coreService;
    private Path path = Paths.get(System.getProperty("user.dir"));
    private int threadNumber = 10;
    private int total = -1;
    private AtomicInteger needDownload = new AtomicInteger(1);
    private Path directory;
    private String normalizedBookName;
    private String cookie;
    private int retryTime = 10;
    private boolean hasError = false;
    private Path tmpPathDir = Paths.get(System.getProperty("user.dir")).resolve("tmp");
    boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") != -1;

    public Downloader(String bookId, CoreService coreService) {
        book = new Book(bookId);
        this.coreService = coreService;
    }

    public Downloader(Book book, CoreService coreService) {
        this.book = book;
        this.coreService = coreService;
    }

    public static void testCmd() {
        File inputFile_win = new File("C:\\Users\\padeoe\\Desktop\\输入 (输入)\\0005.pdf");
        File outFile_win = new File("C:\\Users\\padeoe\\Desktop\\输出 (输出)\\out.pdf");
        File inputFile_linux = new File("/mnt/c/Users/padeoe/Desktop/输入 (输入)/0005.pdf");
        File outFile_linux = new File("/mnt/c/Users/padeoe/Desktop/输出 (输出)/out.pdf");
        File bashFile = new File("/mnt/c/Users/padeoe/Desktop/输入 (输入)/qpdf.sh");
        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            if (System.getProperty("os.name").toLowerCase().indexOf("win") == -1) {
                if (!inputFile_linux.exists()) {
                    System.out.println("输入文件不存在");
                }
/*                String command="qpdf --password=\"\" -decrypt " + parsePathWithWhiteSpace(inputFile_linux.getPath()) + " " + parsePathWithWhiteSpace(outFile_linux.toPath().toString());
                FileWriter fileWriter = new FileWriter(bashFile);
                fileWriter.write(command);
                fileWriter.close();
                process = runtime.exec(new String[]{"bash",bashFile.getPath()});*/

                process = runtime.exec(new String[]{"pdftk", inputFile_linux.getPath(), "output", outFile_linux.getPath()});
            } else {
                if (!inputFile_win.exists()) {
                    System.out.println("输入文件不存在");
                }
                //  process = runtime.exec(new String[]{"qpdf","--password=\"\"","-decrypt",inputFile_win.getPath(),outFile_win.getPath()});
                process = runtime.exec(new String[]{"pdftk", inputFile_win.getPath(), "output", outFile_win.getPath()});
            }

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bf = new BufferedReader(isr);
            String line;
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
            }
            is = process.getErrorStream();
            isr = new InputStreamReader(is);
            bf = new BufferedReader(isr);
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
            }
            int a = process.waitFor();
            System.out.println("返回值" + a);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        args = new String[]{"hCiVg"};
        if (args != null && args.length > 0) {
            Downloader bookDownloader = new Downloader(args[0], new CoreService("Maskeney", "147258"));
            bookDownloader.setThreadNumber(8);
            //   bookDownloader.setTmpPathDir(Paths.get("/mnt/f/tmp"));
            //  bookDownloader.setPath(Paths.get("/mnt/f/TP"));
            long begin = System.currentTimeMillis();
            bookDownloader.downloadBook();
            System.out.println((System.currentTimeMillis() - begin) / 1000);
        } else {
            System.out.println("需要一个参数：书本id");
        }
    }

    /**
     * 初始化书的下载元信息。包括书本id，书的名称等。
     *
     * @return
     */
    public boolean initBookPara() {
        String result = null;
        IOException exception = null;
        for (int i = 0; i < retryTime; i++) {
            try {
                result = viewBookPageWeb(1);
                break;
            } catch (IOException e) {
                exception = e;
            }
        }
        if (exception == null) {
            int index = result.indexOf("共有");
            if (index != -1) {
                index += 3;
                int start = index - 1;
                while (result.charAt(index) > 47 && result.charAt(index) < 58) {
                    index++;
                }
                int end = index;
                total = Integer.parseInt(result.substring(start, end));
            }

            Document doc = Jsoup.parse(result);
            Elements idIntNode = doc.select("[id=bookId]");
            String idInt = idIntNode.attr("value");
            book.setIdInt(idInt);
            Elements nameNode = doc.select("[href=/v3/book/detail/" + book.getId() + "]");
            if (nameNode != null && nameNode.size() > 0)
                book.setName(nameNode.get(0).text());
            else
                return false;
            setDirectory(book.getId());
            return true;
        } else {
            return false;
        }
    }

    public String getM(int page) throws IOException {
        String result = viewBookPageWeb(page);
        Document doc = Jsoup.parse(result);

        Elements infoNode = doc.select("input[name=m]");
        if (infoNode != null) {
            String m = infoNode.get(0).attr("value");
            return m;
        }
        return null;
    }

    public String viewBookPageWeb(int page) throws IOException {
        if (cookie == null) {
            cookie = coreService.getSession();
        }
        String url = CoreService.baseUrl + "/v3/book/read/" + book.getId() + "/PDF/" + page;
        String result = MyHttpRequest.getWithCookie(url, null, cookie, "UTF-8", 2000);
        return result;
    }

    public boolean downloadBook() {
        if (!initBookPara()) {
            return false;
        }
        //判断书本pdf是否已经存在
        File expected = path.resolve(normalizedBookName + ".pdf").toFile();
        return expected.exists() ? handleExistPDFFile(expected) : downloadBookFromMkdir();//若不存在，则立即下载;若存在，则进一步判断是否需要下载
    }

    /**
     * 检测已存在的PDF查看是否是预期下载的
     *
     * @param existFile
     * @return
     */
    private boolean handleExistPDFFile(File existFile) {
        String title = PDFInfo.getTitle(existFile.getPath());
        if (title != null && title.equals(book.getName())) {
            System.out.println(book.getName() + " 已存在，跳过");
            return true;
        } else {
            existFile.delete();
            return downloadBookFromMkdir();
        }
    }

    private boolean downloadBookFromMkdir() {
        File downloadTmpDir = directory.toFile();
        if (downloadTmpDir.exists()) {
            return handleExistDir(downloadTmpDir);
        } else {
            if (downloadTmpDir.mkdirs()) {
                return downloadandMerge(IntStream.range(1, total + 1).mapToObj(i -> i).collect(Collectors.toList()));
            } else {
                System.out.println("文件夹创建失败");
                return false;
            }
        }
    }

    public boolean downloadandMerge(List<Integer> pageNumbers) {

        if (pageNumbers.size() > 0) {
            System.out.println("开始下载" + book.toString());
        }
        downloadPages(pageNumbers);
        if (!hasError) {
            try {
                mergePDF();
                System.out.println(book.getName() + "的PDF合成结束");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;

    }

    private boolean handleExistDir(File existDir) {
        //检查缺页情况
        List<Integer> existPages = Arrays.asList(existDir.listFiles())
                .parallelStream()
                .map(file -> file.getName())
                .filter(filename -> filename.endsWith(".pdf"))
                .map(filename -> filename.split("\\.")[0])
                .filter(name -> name.matches("^[0-9]\\d*$"))
                .map(name -> Integer.parseInt(name))
                .collect(Collectors.toList());
        List<Integer> allPages = IntStream.range(1, total + 1)
                .mapToObj(i -> i)
                .collect(Collectors.toList());
        allPages.removeAll(existPages);
        return downloadandMerge(allPages);


    }

    public void downloadPages(List<Integer> pageNumbers) {
        final int pageSize = pageNumbers.size();
        AtomicInteger tobeDownloadIndex = new AtomicInteger(0);
        ArrayList<Thread> threadArrayList = new ArrayList<>();
        Object lock = new Object();
        for (int i = 0; i < threadNumber; i++) {
            threadArrayList.add(new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (true) {
                        int downloadingIndex = tobeDownloadIndex.getAndIncrement();
                        if (downloadingIndex < pageSize) {
                            try {
                                downloadPage(pageNumbers.get(downloadingIndex));
                                synchronized (lock) {
                                    System.out.print("\r" + (tobeDownloadIndex.get() <= pageSize ? tobeDownloadIndex : pageSize) + "/" + pageSize + "    ");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                setHasError(true);
                            }
                        } else {
                            break;
                        }
                    }
                }
            });
        }
        for (Thread thread : threadArrayList) {
            thread.start();
        }
        for (Thread thread : threadArrayList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    private static String parsePathWithWhiteSpace(String input) {
        return input.indexOf(" ") != -1 ? "\"" + input + "\"" : input;
    }


    private static String parsePathWithWhiteSpace(Path path) {
        StringBuffer output = new StringBuffer();
        output.append(path.getRoot());
        Iterator<Path> pathIterator = path.iterator();
        while (pathIterator.hasNext()) {
            output.append(System.getProperty("file.separator"));
            output.append(parsePathWithWhiteSpace(pathIterator.next().toString()));
        }
        return output.toString();
    }

    public void mergePDF() throws MergeException{
        File inputFileArray[] = directory.toFile().listFiles();
        Arrays.sort(inputFileArray, Comparator.comparing(File::getName));
        inputFileArray = Arrays.asList(inputFileArray).stream().filter(file -> file.isFile() && file.getName().endsWith(".pdf")).toArray(File[]::new);
        System.out.println("将PDF合并成一个文件");
        Path tmpFile = tmpPathDir.resolve(book.getId() + "-tmp.pdf");
        Path outFile = tmpPathDir.resolve(book.getId() + ".pdf");
        try {
            PDFMerge.mergePDFs(inputFileArray, tmpFile);
            PDFMerge.compressPDF(tmpFile, outFile);
            PDFInfo.addBookMark(book, outFile.toString(), path.resolve(book.getName().replaceAll("[/\\\\:\"*?<>|]", " ") + ".pdf").toString());
            tmpFile.toFile().delete();
            outFile.toFile().delete();
        } catch (IOException e) {
            throw new MergeException();
        }
    }

    @Deprecated
    public void mergePDF_Old() throws IOException, InterruptedException, MergeException, DecryptFail {
        File inputFileArray[] = directory.toFile().listFiles();
        List<File> sorted = Arrays.asList(inputFileArray);
        Collections.sort(sorted, Comparator.comparing(File::getName));
        inputFileArray = sorted.toArray(new File[]{});
        inputFileArray = Arrays.asList(inputFileArray).stream()
                .filter(file -> file.isFile() && file.getName().endsWith(".pdf"))
                .toArray(File[]::new);
        int size = inputFileArray.length;
        try {

            System.out.println(book.getName() + "合成开始[GhostScript]");
            File outputFile = tmpPathDir.resolve(book.getId() + ".pdf").toFile();
            combinePDF_GhostScript(inputFileArray, 0, size, outputFile);
            PDFInfo.addBookMark(book, outputFile.getPath(), path.resolve(book.getName().replaceAll("[/\\\\:\"*?<>|]", " ") + ".pdf").toString());
            outputFile.delete();
            //BookDownloader.deleteDir(directory.toFile());
        } catch (MergeException e) {
            System.out.println(book.getName() + "合成开始[QPDF+PDFTK]");
            mergePDF2();
        }

    }

    @Deprecated
    public void mergePDF2() throws IOException, InterruptedException, DecryptFail, MergeException {
        Path decryptDir = directory.resolve("decrypt");
        if (!decryptDir.toFile().exists()) {
            decryptDir.toFile().mkdir();
        }
        File inputFileArray[] = directory.toFile().listFiles();
        //首先解密
        boolean hasFailure = Arrays.asList(inputFileArray)
                .parallelStream().filter(file -> file.getName().endsWith(".pdf"))
                .map(file -> decryptPDF(file, decryptDir.resolve(file.getName()).toFile()))
                .anyMatch(success -> success == false);

        if (hasFailure) {
            throw new DecryptFail();
        }
        File[] decryptedFiles = decryptDir.toFile().listFiles();
        File outputFile = tmpPathDir.resolve(tmpPathDir.resolve(book.getId() + ".pdf")).toFile();
        combinePDF_PDFTK(decryptedFiles, 0, decryptedFiles.length, outputFile);
        BookDownloader.deleteDir(decryptDir.toFile());
        PDFInfo.addBookMark(book, outputFile.getPath(), path.resolve(book.getName().replaceAll("[/\\\\:\"*?<>|]", " ") + ".pdf").toString());
        outputFile.delete();
    }


    private static void RunProcess(String[] commands) throws Exception {
        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            process = runtime.exec(commands);
            InputStream is = process.getErrorStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bf = new BufferedReader(isr);
            String line;
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
                if (line.toLowerCase().indexOf("error") != -1) {
                    process.destroy();
                    throw new Exception();
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new Exception();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    public void combinePDF_PDFTK(File inputFileArray[], int start, int end, File outputFile) throws MergeException {
        //获取输入文件列表
        StringBuffer inputPDFList = new StringBuffer();

        for (int i = start; i < end; i++) {
            File file = inputFileArray[i];
            inputPDFList.append(parsePathWithWhiteSpace(file.getAbsolutePath()));
            inputPDFList.append(" ");
        }

        String command = ("pdftk " + inputPDFList + "output \"" + outputFile.getPath() + "\"");
        File bashFile = directory.resolve(book.getId() + ".sh").toFile();
        //开始合成
        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            if (!isWindows) {
                FileWriter fileWriter = new FileWriter(bashFile);
                fileWriter.write(command);
                fileWriter.close();
                command = "bash " + parsePathWithWhiteSpace(bashFile.toPath());
                process = runtime.exec(new String[]{"bash", bashFile.getPath()});
            } else {
                process = runtime.exec(command);
            }

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bf = new BufferedReader(isr);
            String line;
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
            }
            is = process.getErrorStream();
            isr = new InputStreamReader(is);
            bf = new BufferedReader(isr);
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
            }
            int a = process.waitFor();
            bashFile.delete();
            if (a != 0) {
                throw new MergeException();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new MergeException();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new MergeException();
        } finally {
            bashFile.delete();
        }
    }


    private boolean decryptPDF(File inputFile, File outputFile) {
        Runtime runtime = Runtime.getRuntime();
        Process process;
        String command = "qpdf --password=\"\" -decrypt " + parsePathWithWhiteSpace(inputFile.getPath()) + " " + parsePathWithWhiteSpace(outputFile.toPath().toString());
        File bashFile = inputFile.toPath().getParent().resolve(inputFile.getName() + "-decrypt.sh").toFile();
        try {
            if (!isWindows) {
                //TODO
                FileWriter fileWriter = new FileWriter(bashFile);
                fileWriter.write(command);
                fileWriter.close();
                command = "bash " + parsePathWithWhiteSpace(bashFile.toPath());
                process = runtime.exec(new String[]{"bash", bashFile.getPath()});
            } else {
                process = runtime.exec(command);
            }
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bf = new BufferedReader(isr);
            String line;
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
            }
            is = process.getErrorStream();
            isr = new InputStreamReader(is);
            bf = new BufferedReader(isr);
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
            }
            int a = process.waitFor();
            if (a != 0) {
                System.out.println("返回值" + a);
            }
            return a == 0 ? true : false;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bashFile.delete();
        }
        return false;
    }


    public void combinePDF_GhostScript(File inputFileArray[], int start, int end, File outputFile) throws MergeException {
        LinkedList<String> commands = new LinkedList<>();
        commands.add(isWindows ? "gswin64c" : "gs");
        Arrays.asList(new String[]{"-dBATCH", "-dNOPAUSE", "-q", "-sDEVICE=pdfwrite", "-dPDFSETTINGS=/prepress"}).forEach(arg -> commands.add(arg));
        commands.add("-sOutputFile=" + outputFile.getPath());
        Arrays.asList(inputFileArray).forEach(file -> commands.add(file.getPath()));
        try {
            RunProcess(commands.toArray(new String[commands.size()]));
        } catch (Exception e) {
            e.printStackTrace();
            throw new MergeException();
        }

/*
          Runtime runtime = Runtime.getRuntime();
        Process process;
  try {
            process = runtime.exec(commands.toArray(new String[commands.size()]));
            InputStream is = process.getErrorStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bf = new BufferedReader(isr);
            String line;
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
                if (line.indexOf("ERROR") != -1) {
                    throw new MergeException();
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new MergeException();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new MergeException();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new MergeException();
        }*/
        /*//获取输入文件列表
        StringBuffer inputPDFList = new StringBuffer();
        for (int i = start; i < end; i++) {
            File file = inputFileArray[i];
            inputPDFList.append(parsePathWithWhiteSpace(file.getAbsolutePath()));
            inputPDFList.append(" ");
        }
        String command = () + " -dBATCH -dNOPAUSE -q -sDEVICE=pdfwrite -dPDFSETTINGS=/prepress -sOutputFile=" + outputFile.getPath() + "\" " + inputPDFList;
        File bashFile = directory.resolve(book.getId() + ".sh").toFile();
        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            if (!isWindows) {
                FileWriter fileWriter = new FileWriter(bashFile);
                fileWriter.write(command);
                fileWriter.close();
                command = "bash " + bashFile.getPath();
                System.out.println("[command]:" + command);
                process = runtime.exec(new String[]{"bash",bashFile.getPath()});
            }
            else {
                System.out.println("[command]:" + command);
                process = runtime.exec(command);
            }


            InputStream is = process.getErrorStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bf = new BufferedReader(isr);
            String line;
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
                if (line.indexOf("ERROR") != -1) {
                    throw new MergeException();
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new MergeException();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new MergeException();
        } catch (IOException e) {
            e.printStackTrace();
            throw new MergeException();
        } finally {
            bashFile.delete();
        }*/
    }

    public void mergePDF(File inputFileArray[], int start, int end, File outputFile) throws IOException, InterruptedException {
        //获取输入文件列表
        StringBuffer inputPDFList = new StringBuffer();

        for (int i = start; i < end; i++) {
            File file = inputFileArray[i];
            inputPDFList.append(parsePathWithWhiteSpace(file.getAbsolutePath()));
            inputPDFList.append(" ");
        }
/*        for (File file : inputFileArray) {
            if (file.getName().endsWith(".pdf")) {
*//*                inputPDFList.append(file.toPath().getRoot());

                Iterator<Path>pathIterator=file.toPath().iterator();
                String firstPath=pathIterator.next().toString();
                if(firstPath!=null){
                    inputPDFList.append(firstPath);
                }
                while(pathIterator.hasNext()){
                    inputPDFList.append(System.getProperty("file.separator"));
                    inputPDFList.append(parsePathWithWhiteSpace(pathIterator.next().toString()));
                }*//*
                inputPDFList.append(parsePathWithWhiteSpace(file.getAbsolutePath()));
                inputPDFList.append(" ");
            }
        }*/
        // File outputFile = path.resolve(book.getId() +"-"+partID+ ".pdf").toFile();
        String command = (isWindows ? "gswin64c" : "gs") + " -dBATCH -dNOPAUSE -q -sDEVICE=pdfwrite -dPDFSETTINGS=/prepress -sOutputFile=\"" + outputFile.getPath() + "\" " + inputPDFList;

        try {
            if (isWindows) {
                new ProcessExecutor().command(command.split(" "))
                        .redirectOutput(Slf4jStream.of(getClass()).asInfo())
                        .readOutput(true).execute().outputUTF8();
            } else {
                File bashFile = path.resolve(book.getId() + (isWindows ? ".bat" : ".sh")).toFile();
                FileWriter fileWriter = new FileWriter(bashFile);
                fileWriter.write(command);
                fileWriter.close();
                new ProcessExecutor().command("bash", parsePathWithWhiteSpace(bashFile.getName()))
                        .redirectOutput(Slf4jStream.of(getClass()).asInfo())
                        .readOutput(true).execute().outputUTF8();
                bashFile.delete();
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置文件夹名
     *
     * @param directoryString 文件夹名
     */
    public void setDirectory(String directoryString) {
        normalizedBookName = directoryString.replaceAll("[/\\\\:\"*?<>|]", " ");
        directory = tmpPathDir.resolve(normalizedBookName);
    }

    public void downloadPage(int page) throws IOException {
        IOException exception = null;
        for (int i = 0; i < retryTime; i++) {
            try {
                downloadPageWithoutRetry(page);
                return;
            } catch (IOException e) {
                exception = e;
            }
        }
        throw exception;
    }

/*    public void downloadPageWithoutRetry(int page) throws IOException {
        String m = getM(page);
        // Map<String,String> attr=new HashMap<>();
        //   attr.put("Referer","http://sxnju.chineseall.cn/book/"+bookId+"/1/"+page);
        ReturnData returnData = MyHttpRequest.action_returnbyte("POST", "bookId=" + book.getId() + "&page=" + page + "&type=1&m=" + m, "http://sxnju.chineseall.cn/book/readPdf.jsps",
                null, cookie,
                "UTF-8", 3000);
        File file = new File(directory.resolve(String.format("%04d", page) + ".pdf").toString());

        if (returnData.getData().length == 0) {
            throw new IOException();
        } else {
            BufferedOutputStream bf = new BufferedOutputStream(new FileOutputStream(file));
            bf.write(returnData.getData(), 0, returnData.getData().length);
            bf.close();
        }
    }*/

    public void downloadPageWithoutRetry(int page) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://sxqh.chineseall.cn/v3/book/content/" + book.getId() + "/pdf/" + page).openConnection();
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Cookie", cookie);
        connection.setRequestProperty("Pragma", "no-cache");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
        connection.connect();
        String location = connection.getHeaderField("Location");
        String cookie = connection.getHeaderField("Set-Cookie");
        cookie = cookie.substring(0, cookie.indexOf(';'));

        connection = (HttpURLConnection) new URL(location).openConnection();
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Cookie", cookie);
        connection.setRequestProperty("Pragma", "no-cache");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
        try (InputStream is = connection.getInputStream()) {

            utils.network.MyByteArray myByteArray = new utils.network.MyByteArray();
            while (true) {
                myByteArray.ensureCapacity(4096);
                int len = is.read(myByteArray.getBuffer(), myByteArray.getOffset(), 4096);
                if (len == -1) {
                    break;
                }
                myByteArray.addOffset(len);
            }
            byte[] bytes = new byte[myByteArray.getSize()];
            System.arraycopy(myByteArray.getBuffer(), 0, bytes, 0, bytes.length);

            //byte[] fileData = is.readAllBytes();
            byte[] fileData = bytes;
            //System.out.println(new String(fileData));
            File file = new File(directory.resolve(String.format("%04d", page) + ".pdf").toString());
            if (fileData.length == 0) {
                throw new IOException();
            } else {
                BufferedOutputStream bf = new BufferedOutputStream(new FileOutputStream(file));
                bf.write(fileData, 0, fileData.length);
                bf.close();
            }
        }


    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
        if (!path.toFile().exists()) {
            path.toFile().mkdirs();
        }
    }

    public int getThreadNumber() {
        return threadNumber;
    }

    public void setThreadNumber(int threadNumber) {
        this.threadNumber = threadNumber;
    }

    /**
     * 获取单页下载失败的最大重试次数
     *
     * @return
     */
    public int getRetryTime() {
        return retryTime;
    }

    /**
     * 设置单页下载失败的最大重试次数
     *
     * @param retryTime 最大重试次数
     */
    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    private int getTotal() {
        return total;
    }

    class DecryptFail extends Exception {

    }

    class MergeException extends Exception {

    }

    public Path getTmpPathDir() {
        return tmpPathDir;
    }

    public void setTmpPathDir(Path tmpPathDir) {
        this.tmpPathDir = tmpPathDir;
        if (!tmpPathDir.toFile().exists() || !tmpPathDir.toFile().isDirectory()) {
            tmpPathDir.toFile().mkdirs();
        }
    }
}
