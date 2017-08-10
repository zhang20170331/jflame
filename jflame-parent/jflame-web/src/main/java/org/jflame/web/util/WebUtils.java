package org.jflame.web.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jflame.toolkit.excel.ExcelCreator;
import org.jflame.toolkit.excel.IExcelEntity;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.net.IPAddressHelper;
import org.jflame.toolkit.util.StringHelper;
import org.jflame.web.config.WebConstant;

/**
 * web环境常用工具方法
 * 
 * @author yucan.zhang
 */
public class WebUtils {

    /**
     * 设置让浏览器弹出下载对话框的Header.
     * 
     * @param response HttpServletResponse
     * @param fileName 下载后的文件名.
     * @param fileSize 文件大小
     */
    public static void setFileDownloadHeader(HttpServletResponse response, String fileName, long fileSize) {
        String encodedfileName;
        try {
            encodedfileName = new String(fileName.getBytes("gbk"), "ISO8859-1");
        } catch (UnsupportedEncodingException e) {
            encodedfileName = "download_file" + FileHelper.getExtension(fileName, true);
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");
        response.setContentType(WebConstant.MIME_TYPE_STREAM);
        response.setHeader("Content-Length", String.valueOf(fileSize));
    }

    /**
     * 设置客户端缓存过期时间 的Header.
     * 
     * @param response HttpServletResponse
     * @param expiresSeconds 过期时间,秒
     */
    public static void setExpiresHeader(HttpServletResponse response, long expiresSeconds) {
        response.setDateHeader("Expires", System.currentTimeMillis() + expiresSeconds * 1000);
        response.setHeader("Cache-Control", "max-age=" + expiresSeconds);
    }

    /**
     * 设置禁止客户端缓存的Header.
     * 
     * @param response HttpServletResponse
     */
    public static void setDisableCacheHeader(HttpServletResponse response) {
        response.setDateHeader("Expires", 1L);
        response.addHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0");
    }

    /**
     * 判断请求是否是一个ajax请求
     * <p>
     * 请求头含x-requested-with=XMLHttpRequest
     * 
     * @param request HttpServletRequest
     * @return
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        if (WebConstant.AJAX_REQUEST_FLAG.value()
                .equalsIgnoreCase(request.getHeader(WebConstant.AJAX_REQUEST_FLAG.name()))) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否请求json格式数据,header accept带json或请求路径以json结尾
     * 
     * @param request
     * @return
     */
    public static boolean isJsonRequest(HttpServletRequest request) {
        String headAccept = request.getHeader("accept");
        String flag = "json";
        boolean yes = false;
        if (headAccept != null && headAccept.indexOf(flag) >= 0) {
            yes = true;
        } else if (request.getServletPath().endsWith(flag)) {
            yes = true;
        }
        return yes;
    }

    /**
     * 输出json字符串.注:未设置输出编码
     * 
     * @param response HttpServletResponse
     * @param jsonStr json字符串
     * @throws IOException IOException
     */
    public static void outJson(HttpServletResponse response, String jsonStr) throws IOException {
        setDisableCacheHeader(response);
        response.setContentType(WebConstant.MIME_TYPE_JSON);
        PrintWriter out = response.getWriter();
        out.print(jsonStr);
        out.close();
    }

    /**
     * 获取应用的绝对url
     * <p>
     * 如:http://www.xx.com:8080/webapp,https://www.xx.com
     * 
     * @param request HttpServletRequest
     * @return
     */
    public static String getApplicationPath(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath();

    }

    /**
     * 返回请求路径,不包含应用路径和查询参数
     * 
     * @param request HttpServletRequest
     * @return
     */
    public static String getRequestPath(HttpServletRequest request) {
        String url = request.getServletPath();
        if (request.getPathInfo() != null) {
            url += request.getPathInfo();
        }
        return url;
    }

    /**
     * 合并url，自动补充url分隔符/和纠正url
     * 
     * @param firstUrl 首个url，可以是绝对或相对路径,如果不以协议或/开头将补充/
     * @param relativeUrls 要合并的url，相对路径
     * @return
     */
    public static String mergeUrl(final String firstUrl, final String... relativeUrls) {
        if (StringHelper.isEmpty(firstUrl)) {
            throw new IllegalArgumentException("argument 'firstUrl' must not be null");
        }
        String fullUrl = "";
        final char urlSplit = '/';
        if (relativeUrls.length == 0) {
            if (isAbsoluteUrl(firstUrl) && urlSplit != firstUrl.charAt(0)) {
                return firstUrl;
            } else {
                return urlSplit + fullUrl;
            }
        }
        for (String url : relativeUrls) {
            if (url.charAt(0) != urlSplit) {
                fullUrl += urlSplit;
            }
            fullUrl += url;
        }
        fullUrl = fullUrl.replace('\\', urlSplit).replaceAll("/{2,}", "/");

        if (firstUrl.charAt(firstUrl.length() - 1) == urlSplit) {
            fullUrl = firstUrl + fullUrl.substring(1);
        } else {
            fullUrl = firstUrl + fullUrl;
        }
        if (!isAbsoluteUrl(fullUrl) && urlSplit != fullUrl.charAt(0)) {
            fullUrl = urlSplit + fullUrl;
        }

        return fullUrl;
    }

    /**
     * 判断是否是绝对路径的url.
     * 
     * @param url url
     * @return
     */
    public static boolean isAbsoluteUrl(String url) {
        if (StringHelper.isEmpty(url)) {
            return false;
        }
        return URI.create(url).isAbsolute();
    }

    /**
     * 获取客户端ip地址，获取失败返回"unknown"
     * 
     * @param request HttpServletRequest
     * @return
     */
    public static String getRemoteClientIP(HttpServletRequest request) {
        final String unknownip = "unknown";
        String ip = request.getHeader("x-forwarded-for");
        if (StringHelper.isEmpty(ip) || unknownip.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringHelper.isEmpty(ip) || unknownip.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (StringHelper.isEmpty(ip) || unknownip.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringHelper.isEmpty(ip) || unknownip.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (StringHelper.isEmpty(ip) || unknownip.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个ip取第一个
        if (ip.indexOf(",") >= 0) {
            String[] ips = ip.split(",");
            for (String addr : ips) {
                if (!unknownip.equalsIgnoreCase(addr)) {
                    ip = addr;
                    break;
                }
            }
        }
        ip = ip.trim();
        if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) {
            ip = IPAddressHelper.getHostIP();
        }
        return ip;
    }

    /**
     * 导出excel文件到客户端
     * 
     * @param data 数据集Map
     * @param propertyNames 要导出的Key
     * @param titles 标题名与propertyNames key顺序对应
     * @param fileName 文件名,浏览器要显示的文件名
     * @param response
     * @throws IOException
     */
    public static void exportExcel(final List<LinkedHashMap<String,Object>> data, String[] propertyNames,
            String[] titles, String fileName, HttpServletResponse response) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelCreator.export(data, propertyNames, titles, out);
        setFileDownloadHeader(response, fileName, out.size());
        out.writeTo(response.getOutputStream());
        out.close();
    }

    /**
     * 导出excel文件到客户端
     * 
     * @param data 数据集
     * @param fileName 文件名,浏览器要显示的文件名
     * @param response
     * @throws IOException
     */
    public static void exportExcel(final List<? extends IExcelEntity> data, String fileName,
            HttpServletResponse response) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelCreator.export(data, out);
        setFileDownloadHeader(response, fileName, out.size());
        out.writeTo(response.getOutputStream());
    }

    /**
     * 获取指定名称的cookie值
     * 
     * @param request HttpServletRequest
     * @param cookieName cookie名
     * @return cookie字符串值，不存在返回null
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie c : cookies) {
                if (c.getName().equals(cookieName)) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 设置cookie
     * 
     * @param response HttpServletResponse
     * @param cookieName cookie名
     * @param cookieValue cookie值
     */
    public static void addCooke(HttpServletResponse response, String cookieName, String cookieValue) {
        response.addCookie(new Cookie(cookieName, cookieValue));
    }

    /**
     * 设置cookie，并设置生存时间
     * 
     * @param response HttpServletResponse
     * @param cookieName cookie名
     * @param cookieValue cookie值
     * @param maxAge 生存时间,单位秒，0将删除该cookie
     */
    public static void addCooke(HttpServletResponse response, String cookieName, String cookieValue, int maxAge) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
