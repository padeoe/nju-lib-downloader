package cn.chineseall;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by padeoe on 2017/4/23.
 */
public class FixWhiteSpace {
    public static void main(String[] args) {
        long begin = System.currentTimeMillis();
        List<File> allDir = getAllDir(new File(args[0]));
        System.out.println("总数本数 "+allDir.size());
        allDir.parallelStream().forEach(file -> handleDir(file));
        System.out.println((System.currentTimeMillis() - begin) );
    }

    public static List<File> getAllDir(File rootDir){
        if(rootDir.isFile()){
            return null;
        }
        List<File> result=new LinkedList<>();
        for (File subDir:rootDir.listFiles()){
            if(subDir.getName().startsWith("《")){
                result.add(subDir);
            }
            else {
                result.addAll(getAllDir(subDir));
            }
        }
        return result;
    }


    public static void handleDir(File dir){
        File []files=dir.listFiles();
        if(files.length>0){
            if(!files[0].getName().endsWith(".txt")){
                if(files[0].length()%1024==0){
                    System.out.println(dir.getName());
                    fixDir(dir);
                }
            }
            else{
                if(files[1].length()%1024==0){
                    System.out.println(dir.getName());
                    fixDir(dir);
                }
            }
        }
        else{
            System.out.println("空文件夹"+dir.getName());
        }

    }
    public static void fixDir(File dir){
        for(File file:dir.listFiles()){
            try {
                byte[]imageByte=Files.readAllBytes(file.toPath());
                int length=imageByte.length;
                for(int i=imageByte.length-1;i>-1;i--){
                    if(imageByte[i]!=0){
                        length=i+1;
                        break;
                    }
                }
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.setLength(length);
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
