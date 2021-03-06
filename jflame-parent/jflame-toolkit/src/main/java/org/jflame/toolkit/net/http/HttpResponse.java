package org.jflame.toolkit.net.http;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import org.jflame.toolkit.common.bean.CallResult;
import org.jflame.toolkit.net.http.handler.JsonResponseHandler;
import org.jflame.toolkit.net.http.handler.ResponseBodyHandler;
import org.jflame.toolkit.net.http.handler.TextResponseHandler;
import org.jflame.toolkit.net.http.handler.XmlResponseHandler;

import com.alibaba.fastjson.TypeReference;

/**
 * http请求返回结果
 * 
 * @author yucan.zhang
 */
public class HttpResponse extends CallResult<byte[]> {

    private static final long serialVersionUID = -8303137663872800766L;
    private Map<String,List<String>> headers;// http headers
    private String charset;

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public HttpResponse() {
    }

    public HttpResponse(int status) {
        super(status);
    }

    public HttpResponse(int status, String message) {
        super(status, message);
    }

    public Map<String,List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String,List<String>> headers) {
        this.headers = headers;
    }

    @Override
    public boolean success() {
        return getStatus() == HttpURLConnection.HTTP_OK;
    }

    /**
     * 请求结果作为字符串返回
     * 
     * @return
     */
    public String getResponseAsText() {
        return getResponse(new TextResponseHandler());
    }

    /**
     * 请求结果作为JSON字符串反序列化为java对象.<b>只适用于单一对象,List,Map等复杂对象请使用</b>{@link #getResponseAsJson(TypeReference)}}
     * 
     * @param entityClass java对象类型
     * @return
     * @see #getResponseAsJson(TypeReference)
     */
    public <T> T getResponseAsJson(Class<T> entityClass) {
        ResponseBodyHandler<T> responseHandler = new JsonResponseHandler<>(entityClass);
        return getResponse(responseHandler);
    }

    /**
     * 请求结果作为JSON字符串反序列化为java对象
     * 
     * @param typeReference
     * @return
     */
    public <T> T getResponseAsJson(TypeReference<T> typeReference) {
        ResponseBodyHandler<T> responseHandler = new JsonResponseHandler<>(typeReference);
        return getResponse(responseHandler);
    }

    /**
     * 请求结果作为XML字符串反序列化为java对象
     * 
     * @param entityClass
     * @return
     */
    public <T> T getResponseAsXml(Class<T> entityClass) {
        ResponseBodyHandler<T> responseHandler = new XmlResponseHandler<>(entityClass);
        return getResponse(responseHandler);
    }

    /**
     * 返回请求结果原始byte[]
     * 
     * @return
     */
    public byte[] getResponseBody() {
        return getData();
    }

    /**
     * 返回请求结果反序列化为CallResult&lt;T&gt;
     * 
     * @param entityClass CallResult data的类型
     * @return
     */
    public <T> CallResult<T> getResponseAsCallResult(Class<T> entityClass) {
        return getResponseAsJson(new TypeReference<CallResult<T>>() {
        });
    }

    /**
     * 返回请求结果反序列化为CallResult&lt;List&lt;T&gt;&gt;
     * 
     * @param listElementClass CallResult data集合元素的类型
     * @return
     */
    public <T> CallResult<List<T>> getResponseAsCallResultWithList(Class<T> listElementClass) {
        return getResponseAsJson(new TypeReference<CallResult<List<T>>>() {
        });
    }

    /**
     * 返回是json格式,转为map
     * 
     * @return
     */
    public Map<String,Object> getResponseAsJsonMap() {
        return getResponseAsJson(new TypeReference<Map<String,Object>>() {
        });
    }

    /**
     * @param handler
     * @return
     */
    public <T> T getResponse(ResponseBodyHandler<T> handler) {
        return handler.handle(this);
    }

    @Override
    public String toString() {
        return "HttpResponse [headers=" + headers + ", charset=" + charset + ", status=" + getStatus() + ", message="
                + getMessage() + ", data=" + getResponseAsText() + "]";
    }

}
