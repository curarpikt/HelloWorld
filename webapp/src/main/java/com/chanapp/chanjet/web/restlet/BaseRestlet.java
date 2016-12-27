package com.chanapp.chanjet.web.restlet;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.web.annotation.NoTransaction;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoDataAccessManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.BoTransactionManager;
import com.chanjet.csp.common.base.rest.RestAttachment;
import com.chanjet.csp.common.base.util.TransactionTracker;
import com.chanjet.csp.rest.restlet.Restlet;

/**
 * 客户管家的Restlet基类
 * 
 * <ul>
 * <li>自定义的Restlet继承本类时，只需要实现run方法</li>
 * <li>自定义的 Restlet可以使用
 * {@link com.chanapp.chanjet.web.annotation.NoTransaction} 注解，将会不开启事务</li>
 * </ul>
 * 
 * @author tds
 *
 */
public abstract class BaseRestlet extends Restlet {
    protected static final BoDataAccessManager boDataAccessManager = AppWorkManager.getBoDataAccessManager();
    private static final Logger logger = LoggerFactory.getLogger(BaseRestlet.class);
    private MethodEnum method = null;

    private Map<String, String[]> queryParameters = null;
    private Long id = null;
    private String payloadType = null;
    private String payload = null;
    private List<RestAttachment> atts = null;

    /**
     * 执行业务逻辑
     * 
     * @return HttpResponse的响应结果
     */
    public abstract Object run();

    /**
     * 请求类型枚举
     */
    public static enum MethodEnum {
        GET, POST, PUT, DELETE, UPLOAD
    }

    private Object _invoke(MethodEnum method, Map<String, String[]> queryParameters, Long id, String payloadType,
            String payload, List<RestAttachment> atts) {
        logger.info("_invoke restlet:{}, method:{}", this.getClass().getName(), method);

        this.setMethod(method);
        this.setAtts(atts);
        this.setQueryParameters(queryParameters);
        this.setId(id);
        this.setPayloadType(payloadType);
        this.setPayload(payload);

        if (this.getClass().getAnnotation(NoTransaction.class) == null) {
            return _invokeWithTransaction();
        }

        return run();
    }

    protected BoSession session() {
        return AppContext.session();
    }

    private Object _invokeWithTransaction() {
        BoSession session = session();
        BoTransactionManager tranxManager = AppWorkManager.getBoTransactionManager();
        TransactionTracker tracker = null;

        try {
            tracker = tranxManager.beginTransaction(session);

            Object result = run();

            tranxManager.commitTransaction(session, tracker);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("_invokeWithTransaction Exception:" + e.getMessage());
            if (tracker != null && session != null && session.getTransaction()!=null&&session.getTransaction().isActive()) {
                tranxManager.rollbackTransaction(session);
            }
            throw e; // 原样抛出
        }
    }

    @Override
    public Object doGet(Map<String, String[]> queryParameters) {
        return _invoke(MethodEnum.GET, queryParameters, null, null, null, null);
    }

    @Override
    public Object doGetId(Map<String, String[]> queryParameters, Long id) {
        return _invoke(MethodEnum.GET, queryParameters, id, null, null, null);
    }

    @Override
    public Object doPost(Map<String, String[]> queryParameters, String payloadType, String payload) {
        return _invoke(MethodEnum.POST, queryParameters, null, payloadType, payload, null);
    }

    @Override
    // json
    public Object doPost(Map<String, String[]> queryParameters, String jsonString) {
        return _invoke(MethodEnum.POST, queryParameters, null, null, jsonString, null);
    }

    @Override
    public Object doPostId(Map<String, String[]> queryParameters, String payloadType, String payload, Long id) {
        return _invoke(MethodEnum.POST, queryParameters, id, payloadType, payload, null);
    }

    @Override
    // json
    public Object doPostId(Map<String, String[]> queryParameters, String payload, Long id) {
        return _invoke(MethodEnum.POST, queryParameters, id, null, payload, null);
    }

    @Override
    public Object doPut(Map<String, String[]> queryParameters, String payloadType, String payload) {
        return _invoke(MethodEnum.PUT, queryParameters, null, payloadType, payload, null);
    }

    @Override
    // json
    public Object doPut(Map<String, String[]> queryParameters, String payload) {
        return _invoke(MethodEnum.PUT, queryParameters, null, null, payload, null);
    }

    @Override
    public Object doPutId(Map<String, String[]> queryParameters, String payloadType, String payload, Long id) {
        return _invoke(MethodEnum.PUT, queryParameters, id, payloadType, payload, null);
    }

    @Override
    // json
    public Object doPutId(Map<String, String[]> queryParameters, String payload, Long id) {
        return _invoke(MethodEnum.PUT, queryParameters, id, null, payload, null);
    }

    @Override
    public Object doDelete(Map<String, String[]> queryParameters, String payloadType, String payload) {
        return _invoke(MethodEnum.DELETE, queryParameters, null, payloadType, payload, null);
    }

    @Override
    // json
    public Object doDelete(Map<String, String[]> queryParameters, String payload) {
        return _invoke(MethodEnum.DELETE, queryParameters, null, null, payload, null);
    }

    @Override
    public Object doDeleteId(Map<String, String[]> queryParameters, String payloadType, String payload, Long id) {
        return _invoke(MethodEnum.DELETE, queryParameters, id, payloadType, payload, null);
    }

    @Override
    // json
    public Object doDeleteId(Map<String, String[]> queryParameters, String payload, Long id) {
        return _invoke(MethodEnum.DELETE, queryParameters, id, null, payload, null);
    }

    @Override
    public Object doUpload(Map<String, String[]> queryParameters, List<RestAttachment> atts) {
        return _invoke(MethodEnum.UPLOAD, queryParameters, null, null, null, atts);
    }

    /**
     * 获取http请求的类型
     * 
     * @return the method
     */
    protected MethodEnum getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    protected void setMethod(MethodEnum method) {
        this.method = method;
    }

    /**
     * 获取get参数
     * 
     * @return the queryParameters
     */
    protected Map<String, String[]> getQueryParameters() {
        return queryParameters;
    }

    /**
     * @param queryParameters the queryParameters to set
     */
    protected void setQueryParameters(Map<String, String[]> queryParameters) {
        this.queryParameters = queryParameters;
    }

    /**
     * 获取url中的ID
     * 
     * @return
     */
    protected Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取请求的content-type
     * 
     * @return
     */
    protected String getPayloadType() {
        return payloadType;
    }

    protected void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    /**
     * 获取post或put中的body
     * 
     * @return
     */
    protected String getPayload() {
        return payload;
    }

    protected void setPayload(String payload) {
        this.payload = payload;
    }

    /**
     * 获取上传的附件
     * 
     * @return
     */
    protected List<RestAttachment> getAtts() {
        return atts;
    }

    protected void setAtts(List<RestAttachment> atts) {
        this.atts = atts;
    }

    /**
     * 获取get参数值
     * 
     * @param queryParameters
     * @param paramName
     * @return
     */
    protected String getParam(String paramName) {
        return getParam(paramName, null);
    }

    /**
     * 获取get参数值，不存在时，使用defaultValue
     * 
     * @param paramName
     * @param defaultValue
     * @return
     */
    protected String getParam(String paramName, String defaultValue) {
        return toUTF8(getParam(paramName, 0, defaultValue));
    }

    /**
     * 根据下标获取get参数值，不存在时，使用defaultValue
     * 
     * @param paramName 参数名称
     * @param idx 数组下标
     * @param defaultValue 不存在时，返回的默认值
     * @return
     */
    protected String getParam(String paramName, int idx, String defaultValue) {
        if (queryParameters == null || !queryParameters.containsKey(paramName)) {
            return defaultValue;
        }
        if (queryParameters.get(paramName).length <= idx) {
            return defaultValue;
        }
        return queryParameters.get(paramName)[idx];
    }

    protected String getParam(String paramName, int idx) {
        return getParam(paramName, idx, null);
    }

    /**
     * 获取get参数值，并转换成Integer类型
     * 
     * @param paramName
     * @return
     */
    protected Integer getParamAsInt(String paramName) {
        return getParamAsInt(paramName, null);
    }

    /**
     * 获取get参数值，并转换成Integer类型，不存在时，使用defaultValue
     * 
     * @param paramName
     * @param defaultValue
     * @return
     */
    protected Integer getParamAsInt(String paramName, Integer defaultValue) {
        return ConvertUtil.toInt(getParam(paramName), defaultValue);
    }

    /**
     * 获取get参数值，并转换成Long类型
     * 
     * @param paramName
     * @return
     */
    protected Long getParamAsLong(String paramName) {
        return getParamAsLong(paramName, null);
    }

    /**
     * 获取get参数值，并转换成Long类型，不存在时，使用defaultValue
     * 
     * @param paramName
     * @param defaultValue
     * @return
     */
    protected Long getParamAsLong(String paramName, Long defaultValue) {
        return ConvertUtil.toLong(getParam(paramName), defaultValue);
    }

    /**
     * 获取get参数值，并转换成BigDecimal类型
     * 
     * @param paramName
     * @return
     */
    protected BigDecimal getParamAsBigDecimal(String paramName) {
        return getParamAsBigDecimal(paramName, null);
    }

    /**
     * 获取get参数值，并转换成BigDecimal类型，不存在时，使用defaultValue
     * 
     * @param paramName
     * @param defaultValue
     * @return
     */
    protected BigDecimal getParamAsBigDecimal(String paramName, BigDecimal defaultValue) {
        return ConvertUtil.toBigDecimal(getParam(paramName), defaultValue);
    }

    /**
     * 获取get参数值，并转换成Double类型
     * 
     * @param paramName
     * @return
     */
    protected Double getParamAsDouble(String paramName) {
        return getParamAsDouble(paramName, null);
    }

    /**
     * 获取get参数值，并转换成Double类型，不存在时，使用defaultValue
     * 
     * @param paramName
     * @param defaultValue
     * @return
     */
    protected Double getParamAsDouble(String paramName, Double defaultValue) {
        return ConvertUtil.toDouble(getParam(paramName), defaultValue);
    }

    /**
     * 获取get参数值，并转换成Float类型
     * 
     * @param paramName
     * @return
     */
    protected Float getParamAsFloat(String paramName) {
        return getParamAsFloat(paramName, null);
    }

    /**
     * 获取get参数值，并转换成Float类型，不存在时，使用defaultValue
     * 
     * @param paramName
     * @param defaultValue
     * @return
     */
    protected Float getParamAsFloat(String paramName, Float defaultValue) {
        return ConvertUtil.toFloat(getParam(paramName), defaultValue);
    }

    /**
     * 获取get参数值，并转换成Boolean类型
     * 
     * @param paramName
     * @return
     */
    protected Boolean getParamAsBoolean(String paramName) {
        return getParamAsBoolean(paramName, null);
    }

    /**
     * 获取get参数值，并转换成Boolean类型，不存在时，使用defaultValue
     * 
     * @param paramName
     * @param defaultValue
     * @return
     */
    protected Boolean getParamAsBoolean(String paramName, Boolean defaultValue) {
        return ConvertUtil.toBoolean(getParam(paramName), defaultValue);
    }

    /**
     * 获取get参数值，并转换成Date类型
     * 
     * @param paramName
     * @return
     */
    protected Date getParamAsDate(String paramName) {
        return getParamAsDate(paramName, null);
    }

    /**
     * 获取get参数值，并转换成Date类型，不存在时，使用defaultValue
     * 
     * @param paramName
     * @param defaultValue
     * @return
     */
    protected Date getParamAsDate(String paramName, Date defaultValue) {
        return ConvertUtil.toDate(getParam(paramName), defaultValue);
    }

    /**
     * 获取get参数值，并转换成Timestamp类型
     * 
     * @param paramName
     * @return
     */
    protected Timestamp getParamAsTimestamp(String paramName) {
        return getParamAsTimestamp(paramName, null);
    }

    /**
     * 获取get参数值，并转换成Timestamp类型，不存在时，使用defaultValue
     * 
     * @param paramName
     * @param defaultValue
     * @return
     */
    protected Timestamp getParamAsTimestamp(String paramName, Timestamp defaultValue) {
        return ConvertUtil.toTimestamp(getParam(paramName), defaultValue);
    }
    
    public static String toUTF8(String str) {
    	if(str==null)
    		return str;
        try {
            return new String(str.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.warn("{} toUTF8 error :{}", str, e.getMessage());
        }
        return str;
    }
}
