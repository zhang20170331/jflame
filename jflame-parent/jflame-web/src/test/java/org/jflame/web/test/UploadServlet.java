package org.jflame.web.test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jflame.toolkit.util.JsonHelper;
import org.jflame.web.util.WebUtils;
import org.jflame.web.util.webfile.UploadItem;
import org.jflame.web.util.webfile.UploadUtils;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/upload")
// @MultipartConfig(maxFileSize=1024*1024*10,location="d:\\")
public class UploadServlet extends HttpServlet {

    // @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UploadItem uploadItem = new UploadItem();
        uploadItem.setSavePath("d:\\datacenter");
        uploadItem.setFileCount(3);
        uploadItem.setFileSize(30000);
        uploadItem.setFileTotalSize(90000);
        uploadItem.setCreateDateDir(true);
        uploadItem.setAllowedFiles(WebUtils.IMAGE_EXTS);
        Map<String,List<String>> mp = UploadUtils.upload(req, uploadItem);
        for (Entry<String,List<String>> kv : mp.entrySet()) {
            System.out.println(kv.getKey() + ":" + JsonHelper.toJson(kv.getValue()));
        }
    }

    // servlet3方式 上传
    // void doUploadServlet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // Part part=req.getPart("file");
    // part.write("d:\\datacenter");
    // }
}
