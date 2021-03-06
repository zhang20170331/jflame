package org.jflame.web.util.webfile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.InvalidFileNameException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.jflame.toolkit.codec.TranscodeHelper;
import org.jflame.toolkit.file.FileHelper;

/**
 * 文件上传下载工具类.
 * 
 * @author yucan.zhang
 */
public class UploadUtils {

    /**
     * 上传文件,文件名使用URL编码,如果存在同名文件或文件名太长将重命名.基于apache-commons-upload包
     * 
     * @param request HttpServletRequest
     * @param props 上传文件属性,如大小，保存地址等
     * @return Map key上文件的表单域名称, value是保存文件相对于props.savePath路径
     * @throws UploadDownException 上传文件不符合要求或保存出错
     */
    public static Map<String,List<String>> upload(HttpServletRequest request, UploadItem props)
            throws UploadDownException {
        return upload(request, props, null);
    }

    /**
     * 上传文件,文件名使用URL编码,如果存在同名文件或文件名太长将重命名.基于apache-commons-upload包
     * 
     * @param request HttpServletRequest
     * @param props 上传文件属性,如大小，保存地址等
     * @param handler 文件处理接口,单个文件上传保存后执行的操作，null为无操作
     * @return Map key上文件的表单域名称, value是保存文件相对于props.savePath路径
     * @throws UploadDownException 上传文件不符合要求或保存出错
     */
    public static Map<String,List<String>> upload(HttpServletRequest request, UploadItem props,
            UploadFileHandler handler) throws UploadDownException {
        if (!ServletFileUpload.isMultipartContent(request)) {
            return null;
        }
        if (StringUtils.isEmpty(props.getSavePath())) {
            throw new IllegalArgumentException("请指定文件保存路径");
        }
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload fileUpload = new ServletFileUpload(factory);
        // 设置单个文件的最大上传值
        if (props.getFileSize() > 0)
            fileUpload.setFileSizeMax(props.getFileSize());
        // 设置整个request的最大值
        if (props.getFileTotalSize() > 0)
            fileUpload.setSizeMax(props.getFileTotalSize());
        fileUpload.setHeaderEncoding(request.getCharacterEncoding());
        List<FileItem> items = null;
        try {
            items = fileUpload.parseRequest(request);
        } catch (FileUploadException e) {
            throw new UploadDownException("上传失败", e);
        }

        Map<String,List<String>> resultMap = new HashMap<>();
        String ext;// 扩展名
        String newName;// 最终文件名
        Path basePath = Paths.get(props.getSavePath());// 给定的根路径
        String savePath = props.createSavePath();// 最终保存路径
        int i = 1;
        
        for (FileItem item : items) {
            if (!item.isFormField()) {
                // 超出文件个数忽略后面文件
                if (i++ > props.getFileCount()) {
                    item.delete();
                    continue;
                }
                // 0尺寸文件忽略,可能存在表单file域未选择文件情况
                if (item.getSize() <= 0) {
                    continue;
                }
                /* 取文件名和扩展名 */
                try {
                    newName =item.getName();
                }catch (InvalidFileNameException e){
                    newName=e.getName();
                }
                ext = FileHelper.getExtension(newName, true);
                if (!props.checkFileType(ext.isEmpty()?ext:ext.substring(1))) {
                    throw new UploadDownException("不允许上传的文件类型:" + ext);
                }
                //urlencode,转换汉字和特殊字符
                newName=TranscodeHelper.urlencode(newName);
                // 存在同名文件,或编码后文件名太长，重命名
                if (FileHelper.existSameNameFile(savePath, newName)||newName.length()>50) {
                    newName =String.valueOf(newName.hashCode())+System.nanoTime() + ext;
                }
                /* 保存文件 */
                Path finalPath = Paths.get(savePath, newName);
                try {
                    item.write(finalPath.toFile());
                } catch (Exception e) {
                    throw new UploadDownException("上传保存出错" + item.getFieldName(), e);
                }
                //计算相对路径,统一分隔符为/
                String relativelyPath = FileHelper.separatorsToUnix(basePath.relativize(finalPath).toString());
                if (resultMap.containsKey(item.getFieldName())) {
                    resultMap.get(item.getFieldName()).add(relativelyPath);
                } else {
                    List<String> lst = new ArrayList<>();
                    lst.add(relativelyPath);
                    resultMap.put(item.getFieldName(), lst);
                }
                if (handler != null) {
                    handler.afterUpload(item.getFieldName(), finalPath.toFile());
                }
            }
        }
        return resultMap;
    }

}
