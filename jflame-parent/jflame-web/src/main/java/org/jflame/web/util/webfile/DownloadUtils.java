package org.jflame.web.util.webfile;

import java.io.File;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.util.IOHelper;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.web.util.WebUtils;

/**
 * 文件下载工具类
 * 
 * @author yucan.zhang
 */
public final class DownloadUtils {

    /**
     * 文件下载,下载完成关闭response输出流
     * 
     * @param response HttpServletResponse
     * @param filePath 等下载文件
     * @param fileShowName 文件下载名
     * @throws UploadDownException 文件不存在或I/O异常
     */
    public static void download(HttpServletResponse response, String filePath, String fileShowName)
            throws UploadDownException {
        ServletOutputStream out = null;
        try {
            File downFile = new File(filePath);
            if (downFile.exists() && downFile.isFile()) {
                if (StringHelper.isEmpty(fileShowName)) {
                    fileShowName = FileHelper.getFilename(filePath);
                }
                WebUtils.setFileDownloadHeader(response, fileShowName, downFile.length());
                out = response.getOutputStream();
                IOHelper.copy(downFile, out);
                out.flush();
            } else {
                throw new UploadDownException("下载文件不存在或不是可下载文件" + filePath);
            }
        } catch (Exception e) {
            throw new UploadDownException(e);
        } finally {
            IOHelper.closeQuietly(out);
        }

    }
}
