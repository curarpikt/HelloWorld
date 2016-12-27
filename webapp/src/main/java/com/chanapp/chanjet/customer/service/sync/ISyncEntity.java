package com.chanapp.chanjet.customer.service.sync;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ISyncEntity {

    public Map<Long, List<Long>> getNoDeletedIds();

    public Set<Long> getDeletedData();

    /**
     * <p>
     * 获取下发数据
     * </p>
     * 
     * @return
     *
     * @author : lf
     * @date : 2015年9月7日
     */
    public List<Map<String, Object>> getEntityData();

    /**
     * <p>
     * 获取实体名称
     * </p>
     * 
     * @return
     *
     * @author : lf
     * @date : 2015年9月7日
     */
    public String getEntityName();

    /**
     * <p>
     * 获取下发实体字段
     * </p>
     * 
     * @return
     *
     * @author : lf
     * @date : 2015年9月7日
     */
    public Set<String> getFieldSet();
}
