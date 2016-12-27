package com.chanapp.chanjet.customer.service.recover;

import java.util.List;

import com.chanapp.chanjet.web.service.BaseServiceItf;

public interface RecoverServiceItf extends BaseServiceItf {
    /**
     * <p>
     * 恢复删除实体
     * </p>
     * 
     * @param entityName
     * @param ids
     */
    public void recoveryEntity(String entityName, List<Long> ids);

    public void preRec(String entityName, List<Long> ids);

    public void postRec(String entityName, List<Long> ids);

    public void addRecovery(String entityName, Long id);

    void reCoverEntity(String entityName, List<Long> ids);
}
