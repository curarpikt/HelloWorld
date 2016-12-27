package com.chanapp.chanjet.customer.service.checkin;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRow;
import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRowSet;
import com.chanapp.chanjet.customer.businessobject.api.checkin.ICheckinHome;
import com.chanapp.chanjet.customer.businessobject.api.checkin.ICheckinRow;
import com.chanapp.chanjet.customer.businessobject.api.checkin.ICheckinRowSet;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.usersetting.IUserSettingRow;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.CI;
import com.chanapp.chanjet.customer.constant.metadata.CheckinMetaData;
import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.exporttask.VisitExportData;
import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.customer.service.usersetting.UserSettingServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.ICSPEnumField;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.usertype.DynamicEnum;
import com.chanjet.csp.common.base.usertype.GeoPoint;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class CheckinServiceImpl extends BoBaseServiceImpl<ICheckinHome, ICheckinRow, ICheckinRowSet>
        implements CheckinServiceItf {

    private List<Map<String, Object>> getDutyUsers(List<Long> userIds, String countDate, boolean isOnDuty) {
        String cqlQueryString = " select to_char(c.checkinTime, 'dd') as checkinday,c.checkinTime as checkinTime,u1.id as userid,c.status as status,c.abnormalTime as abnormalTime from "
                + getBusinessObjectId() + " c " + " left join c.owner u1 "
                + "where c.checkinTime || '-' || u1.id in  ( " + " select " + (isOnDuty ? "min" : "max")
                + "(c2.checkinTime)  || '-' ||  u2.id from " + getBusinessObjectId() + " c2 "
                + " left join c2.owner u2 "
                + " where (c2.isDeleted is null or c2.isDeleted = :isDeleted) and  c2.checkinTag = :checkinTag "
                + " and to_char(c2.checkinTime, 'yyyy-MM') = :countDate and u2.id in :userIds "
                + " group by u2.id,to_char(c2.checkinTime, 'yyyy-MM-dd') " + " ) ";
        IEntity entity = metaDataManager.getEntityByName(CheckinMetaData.EOName);
        ICSPEnumField field = (ICSPEnumField) entity.getField("checkinTag");
        DynamicEnum checkinTag = boDataAccessManager.createDynamicEnumValue(field.getCSPEnumName(),
                isOnDuty ? "ONDUTY" : "OFFDUTY");

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("isDeleted", false);
        params.put("userIds", userIds);
        params.put("countDate", countDate);
        params.put("checkinTag", checkinTag);

        List<Map<String, Object>> result = runCQLQuery(cqlQueryString, params);

        return result;
    }

    /**
     * <p>
     * 考勤统计-上班
     * </p>
     */
    private List<Map<String, Object>> getOndutyUsers(List<Long> userIds, String countDate) {
        return getDutyUsers(userIds, countDate, true);
    }

    /**
     * <p>
     * 考勤统计-下班
     * </p>
     * 
     * @param userIds
     * @param countDate
     * @return
     */
    public List<Map<String, Object>> getOffdutyUsers(List<Long> userIds, String countDate) {
        return getDutyUsers(userIds, countDate, false);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<Long, Object> getCheckinUsers(List<Long> userIds, String countDate) {
        Map<Long, Object> result = new HashMap<Long, Object>();
        List<Map<String, Object>> ondutys = getOndutyUsers(userIds, countDate);
        for (Map<String, Object> onduty : ondutys) {
            if (onduty != null) {
                Long userIdTmp = ConvertUtil.toLong(onduty.get("userid").toString());
                if (!result.containsKey(userIdTmp)) {
                    Map<String, Object> m = new HashMap<String, Object>();
                    m.put("userId", userIdTmp);
                    m.put("actualTimes", 0);// 实到次数
                    m.put("lateTimes", 0);// 迟到次数
                    m.put("leaveEarlyTimes", 0);// 早退次数
                    result.put(userIdTmp, m);
                }
                Map<String, Object> map = (Map) result.get(userIdTmp);
                String checkDay = (String) onduty.get("checkinday");
                if (!map.containsKey(checkDay)) {
                    Map<String, Object> ondutyMap = new HashMap<String, Object>();
                    map.put(checkDay, ondutyMap);
                    map.put("actualTimes", (Integer) (map.get("actualTimes")) + 1);// 计算实到次数
                }
                Map<String, Object> ondutyMap = (Map) map.get(checkDay);
                String onTimeStr = "";
                Long  tempTime = null;
                if(onduty.get("checkinTime")!=null){                
                	tempTime =  DateUtil.getDateTimeByString(onduty.get("checkinTime").toString()).getTime();
                }      
          
                if (tempTime != null) {
                    Timestamp onTime = new Timestamp(tempTime);
                    if(onTime!=null){
                    	  onTimeStr = DateUtil.formatTimeStamp(onTime, "HH:mm");	
                    }                 
                }
                ondutyMap.put("onTime", onTimeStr);
                ondutyMap.put("onStatus", onduty.get("status"));
                if (CI.STATUS_LATE.equals(ConvertUtil.toLong(onduty.get("status").toString()))) {// 计算迟到次数
                    map.put("lateTimes", (Integer) (map.get("lateTimes")) + 1);
                }
                ondutyMap.put("onAbnormalTime", onduty.get("abnormalTime"));
            }
        }
        List<Map<String, Object>> offdutys = getOffdutyUsers(userIds, countDate);
        for (Map<String, Object> offduty : offdutys) {
            if (offduty != null) {
                Long userIdTmp = ConvertUtil.toLong(offduty.get("userid").toString());
                if (!result.containsKey(userIdTmp)) {
                    Map<String, Object> m = new HashMap<String, Object>();
                    m.put("userId", userIdTmp);
                    m.put("actualTimes", 0);// 实到次数
                    m.put("lateTimes", 0);// 迟到次数
                    m.put("leaveEarlyTimes", 0);// 早退次数
                    result.put(userIdTmp, m);
                }
                Map<String, Object> map = (Map) result.get(userIdTmp);
                String checkDay = (String) offduty.get("checkinday");
                if (!map.containsKey(checkDay)) {
                    Map<String, Object> offdutyMap = new HashMap<String, Object>();
                    map.put(checkDay, offdutyMap);
                    map.put("actualTimes", (Integer) (map.get("actualTimes")) + 1);// 计算实到次数
                }
                Map<String, Object> offdutyMap = (Map) map.get(checkDay);
                String offTimeStr = "";                
                Timestamp offTime = (Timestamp) offduty.get("checkinTime");
                if (offTime != null) {
                    offTimeStr = DateUtil.formatTimeStamp(offTime, "HH:mm");
                }
                offdutyMap.put("offTime", offTimeStr);
                offdutyMap.put("offStatus", offduty.get("status"));
                if (CI.STATUS_EARLY.equals(ConvertUtil.toLong(offduty.get("status").toString()))) {// 计算早退次数
                    map.put("leaveEarlyTimes", (Integer) (map.get("leaveEarlyTimes")) + 1);
                }
                offdutyMap.put("offAbnormalTime", offduty.get("abnormalTime"));
            }
        }

        return result;
    }

    /**
     * 计算年初 postgres 周数问题 如果1月1号是周五、六、日 则后面所有周数都加1 1月的大于52周的为第1周
     * 
     * @param yyyyMm
     * @return
     */
    private int getWeekOffSetByYYYYMM(String yyyyMm) {
        try {
            String[] strs = yyyyMm.split("-");
            String yyyy = strs[0];
            // String mm = strs[1];
            SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sFormat.parse(yyyy + "-01-01"));
            // 周日-1 周一-2 。。。。。。周五-6 周六-5
            int dayInWeek = new Integer(calendar.get(Calendar.DAY_OF_WEEK));
            // 开年第一天是周五之前 就是正常的 否则 postgres 取得的周数就要+1 > 52的1月份要等于1
            if (dayInWeek == 6 || dayInWeek == 7 || dayInWeek == 1) {
                return 1;
            } else {
                return 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Long, Object> getCheckinVisit(List<Long> userIds, String countDate, String countType) {
        String mm = countDate.split("-")[1];
        int weekOffSet = 0;
        weekOffSet = getWeekOffSetByYYYYMM(countDate);

        Map<Long, Object> result = new LinkedHashMap<Long, Object>();
        String monthHql = " select  u2.id as userid ,count(c.id) as monthnum from " + getBusinessObjectId() + " c "
                + " left join c.owner u2 "
                + " where (c.isDeleted is null or c.isDeleted = :isDeleted)  and c.customerId > 0 and c.checkinTag = :checkinTag and to_char(c.checkinTime, 'yyyy-MM') = :countDate and u2.id in :userIds "
                + " group by u2.id " + " order by u2.id ";
        IEntity entity = metaDataManager.getEntityByName(CheckinMetaData.EOName);
        ICSPEnumField field = (ICSPEnumField) entity.getField("checkinTag");
        DynamicEnum checkinTag = boDataAccessManager.createDynamicEnumValue(field.getCSPEnumName(), "EGRESS");
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("checkinTag", checkinTag);
        params.put("isDeleted", false);
        params.put("userIds", userIds);
        params.put("countDate", countDate);
        List<Map<String, Object>> monthList = runCQLQuery(monthHql, params);

        String timeCondition = " ,extract(week from c.checkinTime)  ";
        if ("day".equals(countType)) {
            timeCondition = " ,to_char(c.checkinTime, 'yyyy-MM-dd') ";
        }
        //timeCondition="";
        String weekHql = " select  u2.id as userid ,count(c.id) as cnum "
                + (StringUtils.isEmpty(timeCondition) ? "" : timeCondition + " as www ") + " from "
                + getBusinessObjectId() + " c " + " left join c.owner u2 "
                + " where (c.isDeleted is null or c.isDeleted = :isDeleted)  and c.customerId > 0 and c.checkinTag = :checkinTag and to_char(c.checkinTime, 'yyyy-MM') = :countDate and u2.id in :userIds "
                + " group by u2.id " + timeCondition + " order by u2.id " + timeCondition;
        HashMap<String, Object> weekHqlparams = new HashMap<String, Object>();
        weekHqlparams.put("isDeleted", false);
        weekHqlparams.put("userIds", userIds);
        weekHqlparams.put("countDate", countDate);
        weekHqlparams.put("checkinTag", checkinTag);
        List<Map<String, Object>> weekList = runCQLQuery(weekHql, weekHqlparams);

        for (Map<String, Object> month : monthList) {
            if (month != null) {
                Long userId = (Long) month.get("userid");
                if (!result.containsKey(userId)) {
                    Map<String, Object> m = new LinkedHashMap<String, Object>();
                    m.put("userId", userId);
                    m.put("monthNum", month.get("monthnum"));// 月拜访客户数
                    result.put(userId, m);
                } else {
                    Map<String, Object> map = (Map<String, Object>) result.get(userId);
                    map.put("monthNum", month.get("monthnum"));// 月拜访客户数
                }
            }
        }
        for (Map<String, Object> week : weekList) {
            if (week != null) {
                Long userIdTmp = (Long) week.get("userid");
                Long weekNum = (Long) week.get("cnum");
                String weekDay = week.get("www") + "";

                int weekDayInt = Integer.parseInt(weekDay);
                weekDayInt = weekDayInt + weekOffSet;
                if ((mm.equals("01") || mm.equals("1")) && weekDayInt > 50) {
                    weekDayInt = 1;
                }

                if (!result.containsKey(userIdTmp)) {
                    Map<String, Object> m = new LinkedHashMap<String, Object>();
                    m.put("userId", userIdTmp);
                    m.put(weekDayInt + "", weekNum);// 周拜访客户数
                    result.put(userIdTmp, m);
                } else {
                    Map<String, Object> map = (Map<String, Object>) result.get(userIdTmp);
                    map.put(weekDayInt + "", weekNum);// 周拜访客户数
                }
            }
        }
        return result;
    }

    @Override
    public Integer getWeekOfYear(String date) {
        try {
            SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sFormat.parse(date));
            int k = new Integer(calendar.get(Calendar.DAY_OF_WEEK));
            int wk = calendar.get(Calendar.WEEK_OF_YEAR);

            if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER && wk == 1) {// 如果是12月的日期
                                                                               // 并且是第一周
                                                                               // 那么就跨年了。。。
                calendar.add(Calendar.DATE, -7);// 跨年就回退7天
                wk = calendar.get(Calendar.WEEK_OF_YEAR);
                wk = wk + 1;// 然后再加1
            }

            if (k == 1) {// 如果是周日
                return wk - 1;
            }
            return wk;

        } catch (ParseException e) {
            e.printStackTrace();
            throw new AppException("app.common.params.invalid");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> getCheckinVisitDetail(Long userId, Long startDate, Long endDate) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        String cHql = " select count(c.id) as visitnum,c.customerId as customerId from " + getBusinessObjectId() + " c "
                + " left join c.owner u2 "
                + " where (c.isDeleted is null or c.isDeleted = :isDeleted) and c.customerId > 0 and c.checkinTag = :checkinTag "
                + " and u2.id = :userId and c.checkinTime between :startDate and :endDate " + " group by c.customerId "
                + " order by count(c.id) desc,min(c.checkinTime) ";
        IEntity entity = metaDataManager.getEntityByName(CheckinMetaData.EOName);
        ICSPEnumField field = (ICSPEnumField) entity.getField("checkinTag");
        DynamicEnum checkinTag = boDataAccessManager.createDynamicEnumValue(field.getCSPEnumName(), "EGRESS");

        HashMap<String, Object> paraC = new HashMap<String, Object>();
        paraC.put("checkinTag", checkinTag);
        paraC.put("isDeleted", false);
        paraC.put("userId", userId);
        Timestamp start = new Timestamp(startDate);
        Timestamp end = new Timestamp(endDate);
        paraC.put("startDate", start);
        paraC.put("endDate", end);

        List<Map<String, Object>> coArrarList = runCQLQuery(cHql, paraC);
        List<Map<String, Object>> cList = new ArrayList<Map<String, Object>>();
        if (coArrarList != null) {
            for (int i = 0; i < coArrarList.size(); i++) {
                Map<String, Object> arrayTmp = coArrarList.get(i);
                cList.add(arrayTmp);
            }
        }

        String dHql = " select c.customerId as customerId,c.checkinTime as checkinTime,c.customerName as customerName from "
                + getBusinessObjectId() + " c " + " left join c.owner u2 "
                + " where (c.isDeleted is null or c.isDeleted = :isDeleted)  and c.customerId > 0 and c.checkinTag = :checkinTag "
                + " and u2.id = :userId and c.checkinTime between :startDate and :endDate "
                + " order by c.customerId,c.checkinTime ";
        HashMap<String, Object> paraD = new HashMap<String, Object>();
        paraD.put("checkinTag", checkinTag);
        paraD.put("isDeleted", false);
        paraD.put("userId", userId);
        paraD.put("startDate", start);
        paraD.put("endDate", end);
        List<Map<String, Object>> doArrarList = runCQLQuery(dHql, paraD);
        List<Map<String, Object>> dList = new ArrayList<Map<String, Object>>();
        if (doArrarList != null) {
            for (int i = 0; i < doArrarList.size(); i++) {
                Map<String, Object> arrayTmp = doArrarList.get(i);
                dList.add(arrayTmp);
            }
        }

        Map<Long, Object> detailMap = new LinkedHashMap<Long, Object>();
        Map<Long, Object> customerNameMap = new LinkedHashMap<Long, Object>();
        for (Map<String, Object> detail : dList) {
            if (detail != null) {
                Long customerId = (Long) detail.get("customerId");
                Timestamp checkinTime = (Timestamp) detail.get("checkinTime");
                customerNameMap.put(customerId, detail.get("customerName"));
                if (!detailMap.containsKey(customerId)) {
                    List<Timestamp> list = new ArrayList<Timestamp>();
                    list.add(checkinTime);
                    detailMap.put(customerId, list);
                } else {
                    List<Timestamp> list = (List<Timestamp>) detailMap.get(customerId);
                    list.add(checkinTime);
                }
            }
        }

        for (Map<String, Object> customer : cList) {
            if (customer != null) {
                Long num = (Long) customer.get("visitnum");
                Long customerId = (Long) customer.get("customerId");
                Map<String, Object> m = new LinkedHashMap<String, Object>();
                m.put("customerId", customerId);
                m.put("customerName", customerNameMap.get(customerId));
                m.put("visitNum", num);
                m.put("items", detailMap.get(customerId));
                result.add(m);
            }
        }

        return result;
    }

    private List<Map<String, Object>> getVisitUsers4ExportHql(List<Long> userIds, String countDate) {
        // 用户ID 用户名称 外勤签到时间 外勤签到时间字符串 客户id 客户名称
        String hql = "select u.id as userId,u.name as userName,c.checkinTime as checkinTime,to_char(c.checkinTime,'yyyy-MM-dd HH24:mi') as checkinTimeStr,c.customerId as customerId,c.customerName as customerName"
                + " FROM " + getBusinessObjectId() + " c left join c.owner u" + " WHERE 1=1"
                + " and (c.isDeleted is null or c.isDeleted = :isDeleted)"
                + " and to_char(c.checkinTime,'yyyy-MM')= :countDate" + " and u.id in :userIds" + " and c.customerId>0"
                + " and c.checkinTag = :checkinTag " + " order by u.id,c.customerId desc,c.checkinTime";

        IEntity entity = metaDataManager.getEntityByName(CheckinMetaData.EOName);
        ICSPEnumField field = (ICSPEnumField) entity.getField("checkinTag");
        DynamicEnum checkinTag = boDataAccessManager.createDynamicEnumValue(field.getCSPEnumName(), "EGRESS");

        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("checkinTag", checkinTag);
        param.put("isDeleted", false);
        param.put("userIds", userIds);
        param.put("countDate", countDate);

        List<Map<String, Object>> result = runCQLQuery(hql, param);
        List<Map<String, Object>> rs = new ArrayList<Map<String, Object>>();
        if (result != null) {
            for (int i = 0; i < result.size(); i++) {
                Map<String, Object> temp = result.get(i);
                rs.add(temp);
            }
        }
        return rs;
    }

    private List<Map<String, Object>> getVisitUsers4Export(List<Long> userIds, String countDate) {
        List<Map<String, Object>> hqlResult = getVisitUsers4ExportHql(userIds, countDate);
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        if (hqlResult != null) {
            for (Map<String, Object> hqlResultRow : hqlResult) {
                resultList.add(hqlResultRow);
            }
        }
        return resultList;
    }

    private List<Map<String, Object>> getVisitUsers4ExportGpOrderHql(List<Long> userIds, String countDate) {
        String hql = "select u.id as userId,t.customerId as customerId,count(t.customerId) as ccount from "
                + getBusinessObjectId() + " t left join t.owner u"
                + " where to_char(t.checkinTime,'yyyy-MM')= :countDate"
                + " and (t.isDeleted is null or t.isDeleted = :isDeleted)" + " and u.id in :userIds"
                + " and t.customerId>0" + " and t.checkinTag = :checkinTag" + " group by u.id,t.customerId"
                + " order by u.id,count(t.customerId) desc,min(t.checkinTime)";

        IEntity entity = metaDataManager.getEntityByName(CheckinMetaData.EOName);
        ICSPEnumField field = (ICSPEnumField) entity.getField("checkinTag");
        DynamicEnum checkinTag = boDataAccessManager.createDynamicEnumValue(field.getCSPEnumName(), "EGRESS");
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("checkinTag", checkinTag);
        param.put("isDeleted", false);
        param.put("userIds", userIds);
        param.put("countDate", countDate);

        List<Map<String, Object>> result = runCQLQuery(hql, param);
        List<Map<String, Object>> rs = new ArrayList<Map<String, Object>>();
        if (result != null) {
            for (int i = 0; i < result.size(); i++) {
                rs.add(result.get(i));
            }
        }
        return rs;
    }

    private List<Map<String, Object>> getVisitUsers4ExportGroupOrder(List<Long> userIds, String countDate) {
        List<Map<String, Object>> hqlResult = getVisitUsers4ExportGpOrderHql(userIds, countDate);
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        if (hqlResult != null) {
            for (Map<String, Object> hqlResultRow : hqlResult) {
                resultList.add(hqlResultRow);
            }
        }
        return resultList;
    }

    @Override
    public List<VisitExportData> getVisitCountExport(List<Long> userIds, String countDate) {
        List<Map<String, Object>> ondutys = getVisitUsers4Export(userIds, countDate);
        List<VisitExportData> datasOrigin = new ArrayList<VisitExportData>();
        VisitExportData data = null;
        for (Map<String, Object> onduty : ondutys) {
            if (onduty != null) {
                data = new VisitExportData();
                // u.id as userId,u.name as userName,c.checkinTime as
                // checkinTime,to_char(c.checkinTime,'yyyy-MM-dd HH24:mi') as
                // checkinTimeStr,c.customerId as customerId,c.customerName as
                // customerName
                Long userIdTmp = (Long) onduty.get("userId");
                data.setUserId(userIdTmp);
                data.setUserName((String) onduty.get("userName"));
                data.setCheckinTime((Date) onduty.get("checkinTime"));
                data.setCheckinTimeStr((String) onduty.get("checkinTimeStr"));
                data.setCustomerId((Long) onduty.get("customerId"));
                data.setCustomerName((String) onduty.get("customerName"));
                datasOrigin.add(data);
            }
        }

        // 用户ID
        // 客户id
        // 客户被拜访次数
        List<Map<String, Object>> ondutysGroupOrder = getVisitUsers4ExportGroupOrder(userIds, countDate);
        List<VisitExportData> datasGo = new ArrayList<VisitExportData>();
        data = null;
        for (Map<String, Object> onduty : ondutysGroupOrder) {
            if (onduty != null) {
                // u.id as userId,t.customerId as customerId,count(t.customerId)
                // as ccount
                data = new VisitExportData();
                Long userIdTmp = (Long) onduty.get("userId");
                data.setUserId(userIdTmp);
                data.setCustomerId((Long) onduty.get("customerId"));
                Long ccount = (Long) onduty.get("ccount");
                data.setCcount(ccount.intValue());
                datasGo.add(data);
            }
        }

        List<VisitExportData> datas = new ArrayList<VisitExportData>();
        sortVisitData(datas, datasOrigin, datasGo);

        return datas;
    }

    private void sortVisitData(List<VisitExportData> datas, List<VisitExportData> datasOrigin,
            List<VisitExportData> datasGo) {
        if (datasOrigin == null || datasGo == null) {
            return;
        }
        VisitExportData go = null;
        VisitExportData orign = null;
        for (int i = 0; i < datasGo.size(); i++) {

            List<VisitExportData> datas_i = new ArrayList<VisitExportData>();
            go = datasGo.get(i);
            for (int j = 0; j < datasOrigin.size(); j++) {
                orign = datasOrigin.get(j);
                if (go.getUserId().compareTo(orign.getUserId()) == 0
                        && go.getCustomerId().compareTo(orign.getCustomerId()) == 0) {
                    orign.setCcount(go.getCcount());
                    datas_i.add(orign);
                }
            }
            for (int j = 0; j < datas_i.size(); j++) {
                datas_i.get(j).setCustomerName(datas_i.get(datas_i.size() - 1).getCustomerName());
                datas.add(datas_i.get(j));
            }

        }
    }

    @Override
    public void deleteCheckin(Long id) {
        // 删除附件
        ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).deleteAttachmentByRelate("Checkin", id);
        logicDeleteByIdWithAuth(id, CheckinMetaData.EOName, BO.Checkin);

    }

    private void logicDeleteByIdWithAuth(Long id, String entityName, String boName) {
/*        this.checkDeleteAuthById(id);
        String json = "{\"Criteria\" : {\"FieldName\" : \"id\", \"Operator\" : \"eq\", \"Values\" : [" + id + "]}}";
        batchSetIsDeleted(json, true);*/
    	deleteRowWithRecycle(id);
        ServiceLocator.getInstance().lookup(OperationLogServiceItf.class).generate(id, entityName, "DELETE");
    }

    @Override
    public Row updateCheckin(LinkedHashMap<String, Object> value) {
        Assert.notNull(value, "app.checkin.object.required");
        Assert.notNull(value.get("id"), "app.checkin.object.required");

        ICheckinRow checkinDb = findByIdWithAuth(ConvertUtil.toLong(value.get("id").toString()));
        Assert.notNull(checkinDb, "app.checkin.object.notexist");
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        if (checkinDb.getOwner() != null && !userId.equals(checkinDb.getOwner())) {
            throw new AppException("app.privilege.user.invalid.invalidoper");
        }
        if (value.get("remark") != null) {
            checkinDb.setRemark(value.get("remark").toString());
            upsert(checkinDb);
        }
        Row row = BoRowConvertUtil.toRow(checkinDb);
        List<Long> checkinIds = new ArrayList<Long>();
        checkinIds.add(ConvertUtil.toLong(value.get("id").toString()));
        IAttachmentRowSet attachments = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class)
                .findRowSetByRelate(CheckinMetaData.EOName, checkinIds);
        if (attachments != null && attachments.getAttachmentRows() != null) {
            row.put("attachments", BoRowConvertUtil.toRowList(attachments.getAttachmentRows()));
        }
        return row;
    }

    private ICheckinRow findByOwnerAndLocalId(String localId, Long userId) {
        Criteria criteria = Criteria.AND().eq("localId", localId).eq("owner", userId);
        String jsonQuerySpec = JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec();
        ICheckinRowSet rowSet = query(jsonQuerySpec);
        if (rowSet != null && rowSet.getRows() != null && rowSet.getRows().size() > 0) {
            return rowSet.getRow(0);
        }
        return null;
    }

    private Long getRelWorkingTime(Long checkinTime, String time) {
        if (null != time) {
            String[] timeArr = time.split(":");
            if (timeArr.length == 2) {
                Calendar cd = Calendar.getInstance();
                cd.setTimeInMillis(checkinTime);
                Integer h = Integer.valueOf(timeArr[0]);
                Integer m = Integer.valueOf(timeArr[1]);
                cd.set(Calendar.HOUR_OF_DAY, h);
                cd.set(Calendar.MINUTE, m);
                cd.set(Calendar.SECOND, 0);
                return cd.getTimeInMillis();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void setCheckinRowSpecialField(ICheckinRow row, LinkedHashMap<String, Object> value) {
        Long customerId = value.containsKey("customerId") ? ConvertUtil.toLong(value.get("customerId").toString())
                : null;
        if (customerId != null) {
            try {
                ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                        .findByIdWithAuth(customerId);
                if (customer != null) {
                    row.setCustomerName(customer.getName());
                    row.setCustomerId(customerId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Long checkinTime = (Long) value.get("checkinTime");
        if (checkinTime != null && checkinTime != 0l) {
            Timestamp checkinTimeTmp = new Timestamp(checkinTime);
            row.setCheckinTime(checkinTimeTmp);
        } else {
            row.setCheckinTime(null);
        }
        Map<String, Object> coordinate = (Map<String, Object>) value.get("coordinate");
        if(coordinate!=null){
            Object longitude=coordinate.get("longitude");
            Object latitude=coordinate.get("latitude");
            if (longitude != null&&latitude!=null) {
                GeoPoint geoPoint = boDataAccessManager.createGeoPoint(Double.parseDouble(longitude.toString()),
                		Double.parseDouble(latitude.toString()));
                row.setCoordinate(geoPoint);
            } else {
                row.setCoordinate(null);
            }
        }
        String checkinTag = (String) value.get("checkinTag");

        if (StringUtils.isNotEmpty(checkinTag)) {
            row.setCheckinTag(boDataAccessManager.createDynamicEnumValue("CheckinTagEnum", checkinTag));
        } else {
            row.setCheckinTag(null);
        }

        if ("ONDUTY".equals(checkinTag) || "OFFDUTY".equals(checkinTag)) {
            IUserSettingRow workingHours = ServiceLocator.getInstance().lookup(UserSettingServiceItf.class)
                    .workingHoursRow();
            if (workingHours != null && workingHours.getStatus() == true) {
                String valueStr = workingHours.getValue();
                Map<String, Object> valueMap = dataManager.jsonStringToMap(valueStr);
                if ("ONDUTY".equals(checkinTag)) {
                    Long startTime = getRelWorkingTime(checkinTime, valueMap.get("startTime").toString());
                    startTime = startTime / (1000 * 60);
                    checkinTime = checkinTime / (1000 * 60);
                    if (checkinTime > startTime) {
                        row.setStatus(CI.STATUS_LATE);
                        row.setAbnormalTime((checkinTime - startTime));
                    } else {
                        row.setStatus(CI.STATUS_NORMAL);
                        row.setAbnormalTime(null);
                    }
                } else {
                    Long endTime = getRelWorkingTime(checkinTime, valueMap.get("endTime").toString());
                    endTime = endTime / (1000 * 60);
                    checkinTime = checkinTime / (1000 * 60);
                    if (endTime > checkinTime) {
                        row.setStatus(CI.STATUS_EARLY);
                        row.setAbnormalTime((endTime - checkinTime));
                    } else {
                        row.setStatus(CI.STATUS_NORMAL);
                        row.setAbnormalTime(null);
                    }
                }
            } else {
                row.setStatus(CI.STATUS_NORMAL);
                row.setAbnormalTime(null);
            }
        } else {
            row.setStatus(CI.STATUS_NORMAL);
            row.setAbnormalTime(null);
        }
    }

    private void checkCheckinRow(ICheckinRow row) {
        // 小于1971年1月1日
        if (row.getCheckinTime() == null || row.getCheckinTime().before(new Timestamp(31507200000l))) {
            throw new AppException("app.checkin.checkinTime.required");
        }
    }

    private ICheckinRow value2HomeRow(LinkedHashMap<String, Object> value) {
        ICheckinRow checkinRow = null;
        Long id = value.containsKey("id") ? ConvertUtil.toLong(value.get("id").toString()) : 0L;
        if (id != null && id > 0) { // 编辑
            ICheckinRow checkin = findByIdWithAuth(id);
            Assert.notNull(checkin, "app.checkin.object.notexist");
        } else { // 新增
        	String localId = value.get("localId")==null?null:value.get("localId").toString();
     
            if (localId!=null&&StringUtils.isNotEmpty(localId)) {
                Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
                checkinRow = findByOwnerAndLocalId(value.get("localId").toString(), userId);
            }
            if (checkinRow == null) {
                checkinRow = createRow();
            }
        }
        populateBORow(value, checkinRow);
        setCheckinRowSpecialField(checkinRow, value);
        checkCheckinRow(checkinRow);
        return checkinRow;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Row addCheckin(LinkedHashMap<String, Object> value) {
        ICheckinRow checkinRow = value2HomeRow(value);
        upsert(checkinRow);
        Row row = BoRowConvertUtil.toRow(checkinRow);
        List<Map<String, Object>> attachments = (List<Map<String, Object>>) value.get("attachments");
        ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).save(checkinRow.getId(),
                checkinRow.getDefinition().getPrimaryEO().getName(), attachments);
        row.put("attachments", attachments);
        return row;
    }
    
    @Override
	public  String addCheckInForH5(String payload){
		String tag = CheckinMetaData.EGRESS;
	    LinkedHashMap<String, Object> checkinParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);
	    checkinParam.put(CheckinMetaData.checkinTag, tag);
	    checkinParam.put("checkinTime", new Date().getTime());
	    
        ICheckinRow checkinRow = value2HomeRow(checkinParam);
        checkinRow.setCheckinTime(DateUtil.getNowDateTime());		
        upsert(checkinRow);     
        Row retRow = BoRowConvertUtil.toRow(checkinRow);
        List<Map<String, Object>> attachments = (List<Map<String, Object>>) checkinParam.get("attachments");
        ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).save(checkinRow.getId(),
                checkinRow.getDefinition().getPrimaryEO().getName(), attachments);
        retRow.put("attachments", attachments);
        //回写客户坐标
        Long customerId = checkinParam.containsKey("customerId")
                ? ConvertUtil.toLong(checkinParam.get("customerId").toString()) : null;
        String coordinateNote =   checkinParam.get("coordinateNote")==null ? null:  checkinParam.get("coordinateNote").toString();    
        if (customerId != null && customerId > 0) {
            ServiceLocator.getInstance().lookup(CustomerServiceItf.class).updateCustomerCoordinate(customerId,
                    (Map<String, Double>) checkinParam.get("coordinate"),
                    coordinateNote);
        }
		//只能删除和查询
        retRow.put("privilege", Integer.valueOf("101", 2));
		return AppWorkManager.getDataManager().toJSONString(retRow);
	}
    
    @Override
	public  String getCheckinDetail(Long id){
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		Criteria criteria = Criteria.AND();
		//criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
		criteria.eq(SC.id, id);
		String jsonStr = jsonQueryBuilder.addFields(SC.owner,SC.id,SC.lastModifiedDate,CheckinMetaData.remark,CheckinMetaData.coordinate,CheckinMetaData.coordinateNote,CheckinMetaData.customerId,CheckinMetaData.customerName)
		.addCriteria(criteria).toJsonQuerySpec();
		ICheckinRowSet rowSet = this.query(jsonStr);		
		if(rowSet!=null&&rowSet.getRows()!=null&&rowSet.getRows().size()>0){
			ICheckinRow row = rowSet.getRow(0);
			Row retRow = BoRowConvertUtil.toRow(row);
			Long ownerId = row.getOwner();
			if(EnterpriseContext.getCurrentUser().getUserLongId().equals(ownerId)){
				retRow.put("privilege", Integer.valueOf("101", 2));
			}else{
				retRow.put("privilege", Integer.valueOf("001", 2));	
			}
			AttachmentServiceItf atservice = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class);
			List<IAttachmentRow> attachments = atservice.findAttachmentListByRelate(CheckinMetaData.EOName,id);
			List<Map<String, Object>> attMaps = atservice.getAttachmentRows(attachments);
			retRow.put("attachments", attMaps);
		}
		return null;
		
	}

    @Override
	public  String getCheckinList(Integer first,Integer max,Map<String,Object> para){
		Map<String,Object> retData = new HashMap<String,Object>();
		Criteria criteria = Criteria.AND();
		criteria.eq(CheckinMetaData.checkinTag, CheckinMetaData.EGRESS);
		//criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
		if(para.get("keyWord")!=null&&StringUtils.isNotEmpty(para.get("keyWord").toString())){
			String searchtext =(String)para.get("keyWord");
			try {
				//+号 在decode后会变成空格
				searchtext = searchtext.replace("+", "%2B"); 
				//searchtext =  new String(searchtext.getBytes("ISO-8859-1"), "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}			
			CustomerServiceItf customerService =ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
			List<Long> customerIds = customerService.getCustomerIdsByKeyWord(searchtext);
			Criteria childCriteria=Criteria.OR();
			if(customerIds!=null&&customerIds.size()>0)
				childCriteria.in(CheckinMetaData.customerId, customerIds.toArray());
			childCriteria.like(CheckinMetaData.remark,searchtext);
			childCriteria.like(CheckinMetaData.coordinateNote,searchtext);
			criteria.addChild(childCriteria);
		}
			if(para.get("customerId")!=null){
				criteria.eq(CheckinMetaData.customerId, para.get("customerId"));
			}
			if(para.get("owner")!=null){
				String ownerStr	= (String)para.get("owner");
				List<Long> ownerIds = new ArrayList<Long>();
				String[] ids =ownerStr.split(",");
				for(String owner:ids){
					if(StringUtils.isEmpty(owner)){
						continue;
					}
					ownerIds.add(Long.parseLong(owner));
				}
				if(ownerIds.size()>0)
					criteria.in(SC.owner, ownerIds.toArray());										
			}
			JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
			String jsonStr = jsonQueryBuilder.addFields(SC.owner,SC.id,SC.lastModifiedDate,CheckinMetaData.remark,CheckinMetaData.coordinate,CheckinMetaData.coordinateNote,CheckinMetaData.customerId,CheckinMetaData.customerName)
			.setFirstResult(first).setMaxResult(max+1).addCriteria(criteria).addOrderDesc(SC.lastModifiedDate).toJsonQuerySpec();
			ICheckinRowSet checkinSet = this.query(jsonStr);
			List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
			boolean hasMore = false;
			int size = 0;
			for(ICheckinRow checkinRow :checkinSet.getCheckinRows()){
				size++;
				if(size==max+1){
					hasMore = true;
					break;
				}
				Row retRow = BoRowConvertUtil.toRow(checkinRow);
				HashMap userMap = (HashMap)checkinRow.getFieldValue(SC.owner);
				Long ownerId = (Long)userMap.get(SC.id);
				if(EnterpriseContext.getCurrentUser().getUserLongId().equals(ownerId)){
					retRow.put("privilege", Integer.valueOf("101", 2));
				}else{
					retRow.put("privilege", Integer.valueOf("001", 2));	
				}
				AttachmentServiceItf atservice = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class);
				List<IAttachmentRow> attachments = atservice.findAttachmentListByRelate(CheckinMetaData.EOName,checkinRow.getId());
				List<Map<String, Object>> attMaps = atservice.getAttachmentRows(attachments);
				retRow.put("attachments", attMaps);
				items.add(retRow);
			}
	
			retData.put("hasMore", hasMore);
			retData.put("items", items);		
		return AppWorkManager.getDataManager().toJSONString(retData);
	}
    
    
    @Override
    public boolean checkCheckinByCustomerId(Long customerId){
    	int count = 0;
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		Criteria criteria=Criteria.AND();
		criteria.eq(CheckinMetaData.customerId, customerId);
		jsonQueryBuilder.addCriteria(criteria);
		count = this.getRowCount(jsonQueryBuilder.toJsonQuerySpec());
		if(count>0){
			return true;
		}
    	return false;
    }


}
