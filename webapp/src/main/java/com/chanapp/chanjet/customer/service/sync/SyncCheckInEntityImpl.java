package com.chanapp.chanjet.customer.service.sync;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.chanapp.chanjet.customer.constant.VF;
import com.chanapp.chanjet.customer.constant.metadata.CheckinMetaData;
import com.chanapp.chanjet.customer.service.checkin.CheckinServiceItf;
import com.chanapp.chanjet.customer.service.idmove.IdMoveService;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.reader.PropertiesReader;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.QueryLimitUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class SyncCheckInEntityImpl extends BaseSyncEntity {
    private List<Long> hierarchyOwners;
    private Long syncVersion;
    private final SyncOprationLog log;

    public SyncCheckInEntityImpl(List<Long> hierarchyOwners, Long syncVersion, SyncOprationLog log) {
        this.entityName = CheckinMetaData.EOName;
        this.boHome = ServiceLocator.getInstance().lookup(CheckinServiceItf.class).getBusinessObjectHome();
        this.hierarchyOwners = hierarchyOwners;
        this.syncVersion = syncVersion;
        this.log = log;
        filedName = getFieldsName(entityName);
        filedName.add(VF.privilege);
        initPara();
    }

    @Override
    public void setAll() {
        this.isAll = true;
        entitySet = getAllWithOutDelete();
        if (entitySet != null) {
            for (IBusinessObjectRow row : entitySet) {
                Long id = (Long) row.getFieldValue(SC.id);
                this.addIds.add(id);
            }
        }
		//IDMOVE
		IdMoveService idmoveService = new IdMoveService();
		Set<Long> movedIds = idmoveService.getMovedIds(this.entityName);
		if(movedIds!=null&&movedIds.size()>0){
			delIds.addAll(movedIds);
		}
    }
    
    @Override
    public void initPara() {
        if (syncVersion == 0) {
            setAll();
        } else {
            // 通用逻辑
            setIdList(hierarchyOwners, log);
            // 代办新增，修改不记日志，按时间戳下发新增
            addByTs(syncVersion, SC.lastModifiedBy, "desc");
        }
    }
    
    private List<IBusinessObjectRow> getAllWithOutDelete() {
        Criteria criteria = Criteria.AND();
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
       	String enterpiseId = AppWorkManager.getCloudEnterpriseId();
    	Long userId = AppWorkManager.getCurrAppUserId();
    	boolean isSuperUser = ServiceLocator.getInstance().lookup(UserServiceItf.class).isBoss(userId);
    	String targetID = PropertiesReader.getInstance("customer/enterpriseID.properties").getString("specialID");
    	if(enterpiseId.equals(targetID)&&isSuperUser){
    		Date startDate = DateUtil.getLastThreeMonthStart();
            criteria.gt(SC.lastModifiedDate, startDate.getTime());    		
    	}
       // List<Long> ids = new ArrayList<Long>();
        // criteria.ne(SysColumns.isdeleted, false);
        // String queryStr =
        // JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec();
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        //jsonQueryBuilder.addFields(SC.id);
        List<IBusinessObjectRow> rows = QueryLimitUtil.queryList(jsonQueryBuilder.toJsonQuerySpec(), this.boHome);
        return rows;
/*        if (rowset != null && rowset.getRows() != null) {
            for (IBusinessObjectRow row : rowset.getRows()) {
                ids.add((Long) row.getFieldValue(SC.id));
            }
        }
        return getEntityByIds(ids, false);*/
    }

}
