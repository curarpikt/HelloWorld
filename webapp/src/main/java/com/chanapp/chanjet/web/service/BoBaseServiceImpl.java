package com.chanapp.chanjet.web.service;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.service.recycle.RecyclableBin;
import com.chanapp.chanjet.customer.service.recycle.RecyclableBinImpl;
import com.chanapp.chanjet.customer.service.recycle.db.DatabaseObjectReference;
import com.chanapp.chanjet.customer.service.recycle.db.DatabaseObjectReferenceImpl;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.jsonquery.JsonQuery;
import com.chanapp.chanjet.web.util.QueryLimitUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectManager;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.bo.api.ReportingResult;
import com.chanjet.csp.ccs.api.cia.UserInfo;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.cmr.api.metadata.base.field.generated.FieldTypeEnum;
import com.chanjet.csp.cmr.api.metadata.userschema.type.businessObject.IBusinessObject;
import com.chanjet.csp.cmr.api.metadata.userschema.type.businessObject.field.IBOField;
import com.chanjet.csp.common.base.dataauth.DataAuthPrivilege;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.usertype.AppBaseEntity;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

/**
 * 所有BoService的基础实现类
 * @author tds
 */
@SuppressWarnings("unchecked")
public class BoBaseServiceImpl<H extends IBusinessObjectHome, R extends IBusinessObjectRow, S extends IBusinessObjectRowSet>
    extends BaseServiceImpl implements BoBaseServiceItf<H, R, S> {
  private static final Logger logger = LoggerFactory.getLogger(BoBaseServiceImpl.class);
  public static final int MAX_QUERY_RESULT = 500;

  private H businessObjectHome;

  public BoBaseServiceImpl() {
    String className = this.getClass().getSimpleName();
    String boName = className.substring(0, className.lastIndexOf("ServiceImpl"));
    init(boName);
  }

  public BoBaseServiceImpl(String boName) {
    init(boName);
  }

  private void init(String boName) {
    IBusinessObjectManager boManager = AppWorkManager.getBusinessObjectManager();
    businessObjectHome = (H) boManager.getPrimaryBusinessObjectHome(boName);
  }

  @Override
  public H getBusinessObjectHome() {
    return businessObjectHome;
  }

  @Override
  public String getBusinessObjectId() {
    return businessObjectHome.getDefinition().getId();
  }

  @Override
  public R createRow() {
    return (R) businessObjectHome.createRow(session());
  }

  @Override
  public S createRowSet() {
    return (S) businessObjectHome.createRowSet();
  }

  @Override
  public void upsert(R row) {
    BoSession session = session();
    R origRow = null;
    if (BoCloneRowRegister.isCloneOrigRow(businessObjectHome.getDefinition().getName()) && !isInsert(row, true)) {
      if (row.getFieldValue(SC.id) != null) {
        Long origId = (Long) row.getFieldValue(SC.id);
        origRow = (R) this.getBusinessObjectHome().query(session, origId);
      } else {
        origRow = (R) businessObjectHome.cloneBoRow(session, row);
      }
    }
    preUpsert(row, origRow);

    businessObjectHome.upsert(session, row);

    postUpsert(row, origRow);
  }

  public void preUpsert(R row, R origRow) {

  }

  public void postUpsert(R row, R origRow) {

  }

  @Override
  public void delete(R row) {
    preDelete(row);

    businessObjectHome.delete(session(), row);

    postDelete(row);
  }

  @Override
  public void delete(Long id) {
    R row = query(id);
    delete(row);
  }

  public void preDelete(R row) {

  }

  public void postDelete(R row) {

  }

  public boolean isInsert(R row, boolean preOperation) {
    if (row.getFieldValue("id") == null) return true;

    if (preOperation) return false;

    if (row.getFieldValue("lastModifiedDate") == null) return true;

    if (row.getFieldValue("createdDate") == null) return true;

    if (row.getFieldValue("lastModifiedDate").equals(row.getFieldValue("createdDate"))) return true;

    return false;
  }

  protected void setOwner(R row, Long owner) {
    Long userId = owner;
    if (null == userId) {
      HashMap<String, Object> ownerO = (HashMap<String, Object>) row.getFieldValue(SC.owner);
      if (null != ownerO) {
        userId = (Long) ownerO.get(SC.id);
      }
      if (null == userId) {
        UserInfo userInfo = EnterpriseContext.getCurrentUser();
        userId = userInfo.getUserLongId();
      } else {
        return;
      }
    }
    row.setFieldValue(session(),SC.owner, userId);
  }

  @Override
  public R query(Long id) {
    return (R) businessObjectHome.query(session(), id);
  }

  @Override
  public S query(String jsonQuerySpec) {
    return (S) QueryLimitUtil.query(jsonQuerySpec, businessObjectHome);
  }

  @Override
  public int batchDelete(List<Long> rowIds, boolean allOrNothing) {
    return businessObjectHome.batchDelete(session(), rowIds, allOrNothing);
  }

  @Override
  public int batchDelete(String jsonQuery) {
    return businessObjectHome.batchDelete(session(), jsonQuery);
  }

  @Override
  public int batchIncrementalUpdate(String jsonQuery, String[] fieldNames, Object[] values, boolean inc) {
    return businessObjectHome.batchIncrementalUpdate(session(), jsonQuery, fieldNames, values, inc);
  }

  @Override
  public int batchInsert(S rowSet) {
    return businessObjectHome.batchInsert(session(), rowSet);
  }

  @Override
  public int batchInsert(S rowSet, boolean isReload) {
    return businessObjectHome.batchInsert(session(), rowSet, isReload);
  }

  @Override
  public int batchUpdate(String jsonQuery, String[] updateExpressions) {
    return businessObjectHome.batchUpdate(jsonQuery, updateExpressions);
  }

  @Override
  public int batchUpdate(String jsonQuery, String[] fieldNames, Object[] values) {
    return businessObjectHome.batchUpdate(session(), jsonQuery, fieldNames, values);
  }

  private void addCustomValues(LinkedHashMap<String, Object> boRowDataMap) {
    for (Entry<String, Object> entry : boRowDataMap.entrySet()) {
      String fieldName = entry.getKey();
      if ("customValues".equals(fieldName)) {
        LinkedHashMap<String, Object> fieldValue = (LinkedHashMap<String, Object>) entry.getValue();
        boRowDataMap.putAll(fieldValue);
        boRowDataMap.remove("CustomValues");
        break;
      } else {
        continue;
      }
    }
  }

  public void populateBORow(LinkedHashMap<String, Object> boRowDataMap, R boRow) {
    if (boRowDataMap == null) {
      throw new NullPointerException("boRowDataMap不能为Null");
    }
    addCustomValues(boRowDataMap);
    if (boRow == null) {
      throw new NullPointerException("boRow不能为Null");
    }
    BoSession session = session();
    IBusinessObject bo = businessObjectHome.getDefinition();
    LinkedHashMap<String, Object> newBoRowDataMap = new LinkedHashMap<String, Object>();
    for (Entry<String, Object> entry : boRowDataMap.entrySet()) {
      String fieldName = entry.getKey();
      Object fieldValue = entry.getValue();
      IBOField boField = bo.getField(fieldName);
      if (boField != null) {
        if (boField.isSystemField() && !SC.owner.equals(fieldName)) {
          continue;
        }
        if (boField.getType() == FieldTypeEnum.FOREIGN_KEY) {
          Long fkId = 0L;
          if (fieldValue != null) {
            if ((fieldValue instanceof IBusinessObjectRow))
              fkId = (Long) ((IBusinessObjectRow) fieldValue).getFieldValue("id");
            else if ((fieldValue instanceof AppBaseEntity)) fkId = ((AppBaseEntity) fieldValue).getId();
            else if ((fieldValue instanceof Long)) fkId = (Long) fieldValue;
            else if ((fieldValue instanceof Integer)) fkId = Long.valueOf(((Integer) fieldValue).longValue());
            else if ((fieldValue instanceof Map)) {
              Object tempId = ((Map<?, ?>) fieldValue).get("id");
              if (tempId != null) {
                fkId = Long.parseLong(tempId.toString());
              } else {
                fkId = null;
              }

            } else if ((fieldValue instanceof String)) {
              if (!StringUtils.isEmpty((String) fieldValue)) {
                fkId = Long.parseLong((String) fieldValue);
              }
            }
          }
          LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
          if (fkId != null && fkId < 1) {
            fkId = null;
          }
          map.put("id", fkId);
          newBoRowDataMap.put(fieldName, map);
        } else if (boField.getType() == FieldTypeEnum.CSP_ENUM) {
          if (entry.getValue() != null && StringUtils.isEmpty(entry.getValue().toString().trim()))
            newBoRowDataMap.put(fieldName, null);
          else if (entry.getValue() instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> enumMap = (LinkedHashMap<String, Object>) entry.getValue();
            newBoRowDataMap.put(fieldName, enumMap.get("value"));
          } else {
            newBoRowDataMap.put(fieldName, entry.getValue());
          }

        } else if (boField.getType() == FieldTypeEnum.INTEGER) {
          if (entry.getValue() != null && StringUtils.isEmpty(entry.getValue().toString().trim()))
            newBoRowDataMap.put(fieldName, null);
          else {
            newBoRowDataMap.put(fieldName, entry.getValue());
          }
        } else if (boField.getType() == FieldTypeEnum.DATE) {
          if (entry.getValue() != null && StringUtils.isNotEmpty(entry.getValue().toString().trim())) {
            Timestamp datestamp = null;
            String date = (String) entry.getValue().toString();
            Date dateTmp = DateUtil.getDateTimeByStringUTC(date);
            if (null != dateTmp) {
              datestamp = new Timestamp(dateTmp.getTime());
              newBoRowDataMap.put(fieldName, datestamp);
            }
          } else if (entry.getValue() != null && StringUtils.isEmpty(entry.getValue().toString().trim())) {
            newBoRowDataMap.put(fieldName, null);
          } else {
            newBoRowDataMap.put(fieldName, entry.getValue());
          }

        } else {
          if (entry.getValue() != null && StringUtils.isEmpty(entry.getValue().toString().trim()))
            newBoRowDataMap.put(fieldName, null);
          else newBoRowDataMap.put(fieldName, entry.getValue());
        }
      }
    }
    IBusinessObjectRow iboRow = businessObjectHome.constructBORowForInsert(session, newBoRowDataMap);
    for (String fieldName : newBoRowDataMap.keySet()) {
      boRow.setFieldValue(session,fieldName, iboRow.getFieldValue(fieldName));
    }
  }

  /**
   * 校验权限
   */
  protected void checkAuthById(Long id, DataAuthPrivilege prvl) {
    try {
      if (null != id && 0 != id) {
        boDataAccessManager.getDataAuthorization().isBoAuthorized(session(),
          businessObjectHome.getDefinition().getName(), prvl, id);
      }
    } catch (Exception e) {
      String eoName = businessObjectHome.getDefinition().getPrimaryEO().getName();
      String cnName = businessObjectHome.getDefinition().getPrimaryEO().getLabel();
      Object[] arguments = new Object[1];
      arguments[0] = cnName;

      String errorId = prvl.name().equals("SELECT") ? "app.privilege.select.Error" : "app.privilege.delete.Error";
      logger.error("{}:{},{},{}", errorId, eoName, id, cnName);
      throw new AppException(errorId, arguments);
    }
  }

  /**
   * 校验SELECT权限
   */
  protected void checkSelectAuthById(Long id) {
    checkAuthById(id, DataAuthPrivilege.SELECT);
  }

  /**
   * 校验DELETE权限
   */
  protected void checkDeleteAuthById(Long id) {
    //checkAuthById(id, DataAuthPrivilege.UPDATE);
    checkAuthById(id, DataAuthPrivilege.DELETE);
  }

  @Override
  public R findByIdWithAuth(Long id) {
    if (id == null) {
      return null;
    }
    checkSelectAuthById(id);
    Criteria criteria = Criteria.AND();
   // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
    criteria.eq(SC.id, id);
    String queryStr = JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec();
    S rowset = query(queryStr);
    if (rowset != null && rowset.size() > 0) {
      return (R) rowset.getRow(0);
    }
    return null;
  }

  @Override
  public S queryAll(String jsonQuerySpec) {
    return query(jsonQuerySpec);
  }

  @Override
  public S queryAll(String jsonQuerySpec, boolean withDeleted) {
    if (withDeleted) {
      return queryAll(jsonQuerySpec);
    }
    JsonQuery jq = JsonQuery.getInstance(jsonQuerySpec);
    String cstr = jq.getCriteriaStr();
    if (StringUtils.isEmpty(cstr)) {
      cstr = " 1=1 ";
    } else {
      cstr = " (" + cstr + ") AND (" + SC.isDeleted + " is null OR " + SC.isDeleted + "='N')";
    }
    jq.setCriteriaStr(cstr);

    return queryAll(jq.toString());
  }

  @Override
  public int batchSetIsDeleted(String jsonQuery, boolean deleted) {
    return businessObjectHome.batchSetIsDeleted(session(), jsonQuery, deleted);
  }

  @Override
  public Integer getRowCount(String jsonQuerySpec) {
    return businessObjectHome.getRowCount(session(), jsonQuerySpec);
  }

  @Override
  public Integer privilegedGetRowCount(String jsonQuerySpec) {
    return businessObjectHome.privilegedGetRowCount(session(), jsonQuerySpec);
  }

  @Override
  public R privilegedQuery(long boId) {
    return (R) businessObjectHome.privilegedQuery(session(), boId);
  }

  @Override
  public S privilegedQuery(String jsonQuerySpec) {
    return (S) businessObjectHome.privilegedQuery(session(), jsonQuerySpec);
  }

  @Override
  public List<Object[]> privilegedQueryNoTransform(String jsonQuerySpec) {
    return businessObjectHome.privilegedQueryNoTransform(session(), jsonQuerySpec).getRawData();
  }



  @Override
  public R getRowByLocalId(String localId) {
    R baseRow = null;
    if (StringUtils.isEmpty(localId)) {
      baseRow = createRow();
      return baseRow;
    }
    baseRow = getByLocalId(localId);
    if (baseRow == null) {
      return createRow();
    }
    return baseRow;
  }

  private R getByLocalId(String localId) {
    Criteria criteria = Criteria.AND().eq("localId", localId);
/*    if (!withDeleted) {
      criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
    }*/
    String jsonQuerySpec =
        JsonQueryBuilder.getInstance().setFirstResult(0).setMaxResult(1).addCriteria(criteria).toJsonQuerySpec();
    S rowSet = query(jsonQuerySpec);
    if (rowSet != null && rowSet.getRows() != null && rowSet.getRows().size() > 0) {
      return (R) rowSet.getRow(0);
    }
    return null;
  }

  @Override
  public ReportingResult getReportData(String jsonQuerySpec, String jsonReportSpec) {
    return businessObjectHome.getReportData(session(), jsonQuerySpec, jsonReportSpec);
  }

  @Override
  public List<Map<String, Object>> runCQLQuery(String cqlQueryString) {
    return QueryLimitUtil.runCQLQuery(businessObjectHome, session(), cqlQueryString);
  }

  @Override
  public List<Map<String, Object>> runCQLQuery(String cqlQueryString, List<Object> posParameters) {
    return QueryLimitUtil.runCQLQuery(businessObjectHome, session(), cqlQueryString, posParameters);
  }

  @Override
  public List<Map<String, Object>> runCQLQuery(String cqlQueryString, HashMap<String, Object> namedParameters) {
    return QueryLimitUtil.runCQLQuery(businessObjectHome, session(), cqlQueryString, namedParameters);
  }

  @Override
  public List<Map<String, Object>> runCQLQuery(String cqlQueryString, int start, int pageCount) {
    return boDataAccessManager.runCQLQuery(businessObjectHome, session(), cqlQueryString, start, pageCount);
  }

  @Override
  public List<Map<String, Object>> runCQLQuery(String cqlQueryString, List<Object> posParameters, int start,
      int pageCount) {
    return boDataAccessManager.runCQLQuery(businessObjectHome, session(), cqlQueryString, posParameters, start,
      pageCount);
  }

  @Override
  public List<Map<String, Object>> runCQLQuery(String cqlQueryString, HashMap<String, Object> namedParameters,
      int start, int pageCount) {
    return boDataAccessManager.runCQLQuery(businessObjectHome, session(), cqlQueryString, namedParameters, start,
      pageCount);
  }

  @Override
  public void runCQLUpdate(String cqlQueryString) {
      boDataAccessManager.runCQLUpdate(businessObjectHome, session(), cqlQueryString);
  }
  
  public void deleteRowWithRecycle(Long id){
	  String eoName = businessObjectHome.getDefinition().getPrimaryEO().getName();
	  if(Arrays.asList(EO.NeedRecycles).contains(eoName)){
			RecyclableBin service = new RecyclableBinImpl();
			service.put(eoName, id);
	  }else if(Arrays.asList(EO.UnNeedRecycles).contains(eoName)){
			DatabaseObjectReference reference = new DatabaseObjectReferenceImpl(eoName,id);
			reference.delete(); 
	  }
	  else{
		  delete(id);
	  }

  }

}
