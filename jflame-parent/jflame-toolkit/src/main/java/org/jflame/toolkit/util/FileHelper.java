package org.jflame.toolkit.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

/**
 * 文件操作工具类.
 * 
 * @see java.nio.Files
 * @author zyc
 */
public final class FileHelper {

    /**
     * windows系统文件路径分隔符
     */
    public static final String WIN_PATH_SEPARATOR = "\\";
    /**
     * unix系统文件路径分隔符
     */
    public static final String UNIX_PATH_SEPARATOR = "/";

    /**
     * 返回文件路径的目录部分
     * 
     * @param filePath 文件路径
     * @return
     */
    public static String getDir(String filePath) {
        int i = filePath.lastIndexOf(UNIX_PATH_SEPARATOR);
        if (i < 0) {
            i = filePath.lastIndexOf(WIN_PATH_SEPARATOR);
        }
        if (i < 0) {
            return filePath;
        } else {
            if (filePath.indexOf('.', i) == -1) {
                return filePath;
            }
            return filePath.substring(0, i + 1);
        }
    }

    /**
     * 取得文件扩展名,小写
     * 
     * @param filename 文件名
     * @param includePoint 返回的扩展名是否包含.号
     * @return 无扩展名将返回空字符串
     */
    public static String getExtension(String filename, boolean includePoint) {
        if (StringHelper.isNotEmpty(filename)) {
            int i = filename.lastIndexOf('.');

            if ((i > 0) && (i < (filename.length() - 1))) {
                return includePoint ? (filename.substring(i)).toLowerCase() : (filename.substring(i + 1)).toLowerCase();
            }
        }
        return "";
    }

    /**
     * 在指定的文件夹下创建当前以年/月命名的子文件夹，如果存在则直接返回路径,不存在则创建.
     * <p>
     * 如:当前时间为2011年3月,rootDir=/home/user,返回路径为/home/user/2011/3
     *
     * @param rootDir 指定的根目录.如果是相对路径仍然会创建，空字符串则相对于项目路径
     * @param isCreateMonthDir 是否创建当前月份名文件夹
     * @return 返回全路径
     */
    public static String createDateDir(String rootDir, boolean isCreateMonthDir) {
        if (rootDir == null) {
            throw new IllegalArgumentException("参数错误 rootDir不能为null");
        }
        Calendar now = Calendar.getInstance();
        Path newPath = null;
        if (isCreateMonthDir) {
            newPath = Paths.get(rootDir, String.valueOf(now.get(Calendar.YEAR)),
                    String.valueOf(now.get(Calendar.MONTH) + 1));
        } else {
            newPath = Paths.get(rootDir, String.valueOf(now.get(Calendar.YEAR)));
        }

        File todayFile = newPath.toFile();
        if (!todayFile.exists()) {
            todayFile.mkdirs();
        }

        return newPath.toString();
    }

    /**
     * 一次性读取文件内容作为字符串返回
     * 
     * @param filePath 文件路径
     * @param charset 字符集
     * @return
     * @throws InvalidPathException 路径不正确
     * @throws IOException 文件不存在或i/o异常
     */
    public static String readString(String filePath, String charset) throws IOException {
        try (InputStream stream = Files.newInputStream(Paths.get(filePath))) {
            return IOHelper.readString(stream, charset);
        }
    }

    /**
     * 复制文件.如果目标文件存在则替换
     * 
     * @see java.nio.Files#copy(Path source, Path target, CopyOption... options)
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @param isReplaceOnExit 如果目标文件存在是否替换
     * @throws IOException 读写或替换文件异常
     */
    public static void copyFile(File sourceFile, File targetFile, boolean isReplaceOnExit) throws IOException {
        if (isReplaceOnExit) {
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.copy(sourceFile.toPath(), targetFile.toPath());
        }
    }

    /**
     * 复制文件.如果目标文件存在则替换
     * 
     * @see java.nio.Files#copy(Path source, Path target, CopyOption... options)
     * @param sourceFile 源文件路径
     * @param targetFile 目标文件路径
     * @param isReplaceOnExit 如果目标文件存在是否替换
     * @throws IOException 读写或替换文件异常
     */
    public static void copyFile(String sourceFile, String targetFile, boolean isReplaceOnExit) throws IOException {
        if (isReplaceOnExit) {
            Files.copy(Paths.get(sourceFile), Paths.get(targetFile), StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.copy(Paths.get(sourceFile), Paths.get(targetFile));
        }
    }

    /**
     * 检测指定目录下是否已存在同名文件,如果有返回一个随机新名称
     * 
     * @param dir 待检测目录
     * @param fileName 文件名
     */
    public static String detectSameNameFile(String dir, String fileName) {
        File file = Paths.get(dir, fileName).toFile();
        if (file.exists()) {
            return StringHelper.noHyphenUUID() + getExtension(fileName, true);
        }
        return null;
    }

    /**
     * 清空一个文件夹里的文件，但不删除文件夹本身
     *
     * @param directory 要清空的文件夹
     * @throws IOException in case cleaning is unsuccessful
     */
    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) { // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * 删除文件，如果文件是目录同时删除下面的文件.
     * <p>
     * 如果其中一个文件没有被删除会抛出异常
     *
     * @param file file or directory to delete, must not be <code>null</code>
     * @throws NullPointerException if the directory is <code>null</code>
     * @throws FileNotFoundException if the file was not found
     * @throws IOException in case deletion is unsuccessful
     */
    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent) {
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                String message = "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    /**
     * 递归删除目录.
     *
     * @param directory 要删除的目录
     * @throws IOException in case deletion is unsuccessful
     */
    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        if (!Files.isSymbolicLink(directory.toPath())) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            String message = "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * 静默删除文件,不抛出异常. 如果文件是目录同时删除下面的文件.
     * 
     * @param file file or directory to delete, can be <code>null</code>
     * @return <code>true</code> 成功删除, 否则 <code>false</code>
     */
    public static boolean deleteQuietly(File file) {
        if (file == null) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                cleanDirectory(file);
            }
        } catch (Exception ignored) {
            // 忽略异常
            ignored.printStackTrace();
        }

        try {
            return file.delete();
        } catch (Exception ignored) {
            return false;
        }
    }

    // public static void main(String[] args) {
    /*
     * System.out.println(getDir("E:\\abc")); System.out.println(getDir("E:\\abc\\ab.jpg"));
     * System.out.println(getDir("/data")); System.out.println(getDir("/data/abc.jpg"));
     * System.out.println(getExtension("/data/abc.jpg",false)); System.out.println(getExtension("/data/abc.jpg",true));
     */
    // Path fPath=Paths.get("E:/abc", "2015/2/1","abx.jsp");
    // Path fPath1=Paths.get("2015", "2","3");
    // Path fPath2=fPath1.resolve("xxx.jpg");
    // String x=getTodayDir("e:\\abc");
    // System.out.println(x);
    // System.out.println(getTodayDir("/data"));
    // Path fPath=Paths.get("e:\\2015",x);
    // Path fPath1=Paths.get("e:\\");
    // System.out.println(fPath1.relativize(fPath));
    // System.out.println(getDir("/data/abc.jpg"));
    // }
}
