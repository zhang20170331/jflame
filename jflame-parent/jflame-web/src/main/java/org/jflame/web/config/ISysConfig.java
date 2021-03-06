package org.jflame.web.config;

import org.jflame.toolkit.exception.BusinessException;

/**
 * 系统配置参数接口
 * 
 * @author yucan.zhang
 */
public interface ISysConfig {

    /**
     * 加载所有配置参数
     * 
     * @throws BusinessException
     */
    void loadConfig() throws BusinessException;

    /**
     * 重载所有配置参数
     * 
     * @throws BusinessException
     */
    void reloadConfig() throws BusinessException;


    /**
     * 根据参数名取得参数值
     * 
     * @param paramKey 配置参数名
     * @return
     */
    Object getParam(String paramKey);

    /**
     * 取得参数文本值
     * 
     * @param paramKey 配置参数名
     * @return
     */
    String getTextParam(String paramKey);

}
