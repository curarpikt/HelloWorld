package com.chanapp.chanjet.customer.service.report;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;

import com.chanapp.chanjet.customer.cache.CSPEnum;
import com.chanapp.chanjet.customer.cache.CSPEnumValue;
import com.chanapp.chanjet.customer.cache.CustomerLayout;
import com.chanapp.chanjet.customer.cache.CustomerMetaData;
import com.chanapp.chanjet.customer.cache.MetadataCacheBuilder;
import com.chanapp.chanjet.customer.constant.CT;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.constant.SRU;
import com.chanapp.chanjet.customer.layout.LayoutManager;
import com.chanapp.chanjet.customer.service.checkin.CheckinServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.importrecordnew.ImportUtil;
import com.chanapp.chanjet.customer.service.layout.LayoutServiceItf;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.CsvUtil;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.util.PinyinUtil;
import com.chanapp.chanjet.customer.util.ReportUtil;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.cmr.api.metadata.base.field.generated.FieldTypeEnum;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.IField;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.usertype.DynamicEnum;
import com.chanjet.csp.common.base.util.StringUtils;

public class ReportServiceImpl extends BaseServiceImpl implements ReportServiceItf {
    private void sort(List<Row> rows, final String sort) {
        Collections.sort(rows, new Comparator<Row>() {
            public int compare(Row a, Row b) {
                int ret = 0;
                String aSimpleSpell = a.getString("simpleSpell");
                String bSimpleSpell = b.getString("simpleSpell");
                if (null == aSimpleSpell) {
                    aSimpleSpell = "";
                }
                if (null == bSimpleSpell) {
                    bSimpleSpell = "";
                }
                aSimpleSpell = aSimpleSpell.toLowerCase();
                bSimpleSpell = bSimpleSpell.toLowerCase();
                if ("desc".equals(sort)) {// 倒序
                    ret = aSimpleSpell.compareTo(bSimpleSpell);
                } else {
                    ret = bSimpleSpell.compareTo(aSimpleSpell);
                }
                return ret;
            }
        });
    }

    @Override
    public RowSet getAddWeekCustomerAnalysisData(String params) {
        Map<String, Object> paramsMap = dataManager.jsonStringToMap(params);
        RowSet rowSet = new RowSet();
        int total = 0;
        List<Map<String, Object>> list = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .getAddCustomerAnalysisData((String) paramsMap.get("start_date"), (String) paramsMap.get("end_date"));
        List<Row> rows = new ArrayList<Row>();
        for (Map<String, Object> objects : list) {
            Row row = new Row();
            // count(id) as cnt, c.owner.name as name,c.owner.userId as userId
            String name = objects.get("name").toString();
            row.put("y", objects.get("cnt").toString());
            row.put("text", name);
            row.put("value", objects.get("userId").toString());
            row.put("simpleSpell", PinyinUtil.hanziToPinyinSimple(name, false));
            rows.add(row);
            total += Integer.parseInt(objects.get("cnt").toString());
        }
        sort(rows, "asc");
        rowSet.setItems(rows);
        rowSet.setTotal(total);
        return rowSet;
    }

    @Override
    public RowSet getCompositionAnalysisData(String params) {
        Map<String, Object> paramsMap = dataManager.jsonStringToMap(params);
        Long userId = null;
        if (paramsMap.get("userId") != null) {
            userId = ConvertUtil.toLong((paramsMap.get("userId").toString()));
        }

        return ServiceLocator.getInstance().lookup(CustomerServiceItf.class).getCompositionAnalysisData(
                (String) paramsMap.get("enumName"), userId, (String) paramsMap.get("condition"));
    }

    private void initFixedHeaderData(List<Map<String, Object>> headers) {
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("columnName", "group");
        head.put("columnLabel", "部门");
        headers.add(head);
        head = new HashMap<String, Object>();
        head.put("columnName", "name");
        head.put("columnLabel", "姓名");
        headers.add(head);
        head = new HashMap<String, Object>();
        head.put("columnName", "actualTimes");
        head.put("columnLabel", "实际出勤次数");
        headers.add(head);
        head = new HashMap<String, Object>();
        head.put("columnName", "lateTimes");
        head.put("columnLabel", "迟到次数");
        headers.add(head);
        head = new HashMap<String, Object>();
        head.put("columnName", "leaveEarlyTimes");
        head.put("columnLabel", "早退次数");
        headers.add(head);
    }

    /**
     * 
     * @param week
     * @return
     */
    private String getWeekStr(int week) {
        String weekStrPre = "周";
        String weekStrSuf = "";
        if (week == 1) {
            weekStrSuf = "一";
        }
        if (week == 2) {
            weekStrSuf = "二";
        }
        if (week == 3) {
            weekStrSuf = "三";
        }
        if (week == 4) {
            weekStrSuf = "四";
        }
        if (week == 5) {
            weekStrSuf = "五";
        }
        if (week == 6) {
            weekStrSuf = "六";
        }
        if (week == 7) {
            weekStrSuf = "日";
        }
        return weekStrPre + weekStrSuf;
    }

    private List<Map<String, Object>> initHeaderData(String countDate, List<String> dates) {
        String[] countDateArr = countDate.split("-");
        int year = Integer.valueOf(countDateArr[0]);
        int month = Integer.valueOf(countDateArr[1]);
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Date date = new Date();
        Calendar calendarNow = Calendar.getInstance();
        calendarNow.clear();
        calendarNow.setTime(date);
        String now = sdf.format(date);
        int endDay = 0;
        if (now.equals(countDate)) {
            endDay = calendarNow.get(Calendar.DAY_OF_MONTH);
        } else {
            endDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        List<Map<String, Object>> headers = new ArrayList<Map<String, Object>>();
        Map<String, Object> head = new HashMap<String, Object>();
        initFixedHeaderData(headers);

        String pTime = null;
        String week = null;
        for (int i = 1; i < (endDay + 1); i++) {
            head = new HashMap<String, Object>();
            String day = null;
            if (i < 10) {
                day = "0" + i;
            } else {
                day = "" + i;
            }
            if (dates != null) {
                dates.add(day);
                calendar.set(Calendar.DAY_OF_MONTH, i);
                head.put("columnLabel", calendar.getTimeInMillis());
            } else {
                // mod by JIAYUEP 日期 + （周几）
                pTime = countDate + "-" + day;
                week = getWeekStr(DateUtil.dayForWeek(pTime));
                head.put("columnLabel", pTime + "(" + week + ")");
            }
            head.put("columnName", day);
            headers.add(head);
        }
        return headers;
    }

    private String[] getFieldsCsv(List<String> dates) {
        List<String> filedsList = new ArrayList<String>();
        filedsList.add("userId");
        filedsList.add("parentId");
        filedsList.add("name");
        filedsList.add("status");
        filedsList.add("userRole");
        filedsList.add("userLevel");
        filedsList.add("actualTimes");
        filedsList.add("lateTimes");
        filedsList.add("leaveEarlyTimes");
        // filedsList.addAll(dates);
        for (String day : dates) {
            filedsList.add(day + ".onTime");
            filedsList.add(day + ".offTime");
            filedsList.add(day + ".onStatus");
            filedsList.add(day + ".offStatus");
            filedsList.add(day + ".onAbnormalTime");
            filedsList.add(day + ".offAbnormalTime");
        }
        String[] filedsArr = new String[filedsList.size()];
        filedsList.toArray(filedsArr);
        return filedsArr;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Row attendanceCountCsv(Long groupId, Long userId, String countDate) {
        if (StringUtils.isBlank(countDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            countDate = sdf.format(new Date());
        }
        Assert.checkCountDate(countDate, "app.report.attendanceCount.countDate.illege");
        List<Long> userIds = new ArrayList<Long>();
        Map<Long, Map<String, Object>> userInfo = new LinkedHashMap<Long, Map<String, Object>>();
        List<Map<String, Object>> groups = ReportUtil.getUserGroupInfo(groupId, userId, userInfo, userIds, countDate,
                "KQ", false);
        Map<Long, Object> data = null;

        if (userIds != null && userIds.size() > 0) {
            data = ServiceLocator.getInstance().lookup(CheckinServiceItf.class).getCheckinUsers(userIds, countDate);
        }

        Iterator<Entry<Long, Map<String, Object>>> it = userInfo.entrySet().iterator();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        while (it.hasNext()) {
            Entry<Long, Map<String, Object>> entry = it.next();
            Long key = entry.getKey();
            Map<String, Object> map = entry.getValue();
            if (data != null) {
                Object o = data.get(key);
                if (o != null) {
                    map.putAll((Map<String, Object>) o);// 用戶基本信息和考勤信息合併
                }
            }
            list.add(map);
        }
        Row row = new Row();
        row.put("currentTime", new Date().getTime());
        List<String> dates = new ArrayList<String>();
        List<Map<String, Object>> headers = initHeaderData(countDate, dates);
        row.put("header", headers);
        String[] filedsArr = getFieldsCsv(dates);

        String csvString = CsvUtil.parseList2CsvData(filedsArr, list);
        row.put("userGroup", groups);
        row.put("data", csvString);
        return row;
    }

    private List<Map<String, Object>> initVisitHeader(String countDate, String countType) {
        String[] countDateArr = countDate.split("-");
        int year = Integer.valueOf(countDateArr[0]);
        int month = Integer.valueOf(countDateArr[1]);
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, 1);
        int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int week = 0;
        List<Map<String, Object>> headers = new ArrayList<Map<String, Object>>();
        for (int i = 1; i <= days; i++) {
            if (!"day".equals(countType)) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date dateTmp = df.parse(countDate + "-" + i);
                    calendar.clear();
                    calendar.setTime(dateTmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int k = new Integer(calendar.get(Calendar.DAY_OF_WEEK));
                if (k == 1) {// 若当天是周日
                    week++;
                    String startDate = "";
                    if (i - 6 <= 1) {
                        startDate = countDate + "-" + 1;
                    } else {
                        startDate = countDate + "-" + (i - 6);
                    }
                    startDate = startDate + " 00:00:00";
                    String endDate = countDate + "-" + i + " 23:59:59";
                    Date start = DateUtil.getDateTimeByString(startDate);
                    Date end = DateUtil.getDateTimeByString(endDate);
                    Map<String, Object> head = new HashMap<String, Object>();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(start);

                    int weekOfYear = ServiceLocator.getInstance().lookup(CheckinServiceItf.class)
                            .getWeekOfYear(startDate);
                    head.put("countTypeValue", weekOfYear);
                    head.put("weekOfMonth", week);
                    head.put("startDate", start.getTime());
                    head.put("endDate", end.getTime());
                    headers.add(head);
                }
                if (k != 1 && i == days) {// 若是本月最好一天，且不是周日
                    week++;
                    String startDate = countDate + "-" + (i - k + 2) + " 00:00:00";
                    String endDate = countDate + "-" + i + " 23:59:59";
                    Map<String, Object> head = new HashMap<String, Object>();
                    Date start = DateUtil.getDateTimeByString(startDate);
                    Date end = DateUtil.getDateTimeByString(endDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(start);
                    int weekOfYear = ServiceLocator.getInstance().lookup(CheckinServiceItf.class)
                            .getWeekOfYear(startDate);
                    head.put("countTypeValue", weekOfYear);
                    head.put("weekOfMonth", week);
                    head.put("startDate", start.getTime());
                    head.put("endDate", end.getTime());
                    headers.add(head);
                }
            } else {
                Map<String, Object> head = new HashMap<String, Object>();
                String day = countDate;
                if (i < 10) {
                    day = day + "-0" + i;
                } else {
                    day = day + "-" + i;
                }
                head.put("countTypeValue", day);
                String startDate = day + " 00:00:00";
                String endDate = day + " 23:59:59";
                Date start = DateUtil.getDateTimeByString(startDate);
                Date end = DateUtil.getDateTimeByString(endDate);
                head.put("startDate", start.getTime());
                head.put("endDate", end.getTime());
                headers.add(head);
            }
        }
        return headers;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Row checkinVisitCount(Long groupId, Long userId, String countDate, String countType) {
        if (StringUtils.isBlank(countDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            countDate = sdf.format(new Date());
        }
        Assert.checkCountDate(countDate);
        List<Long> userIds = new ArrayList<Long>();
        Map<Long, Map<String, Object>> userInfo = new HashMap<Long, Map<String, Object>>();
        List<Map<String, Object>> groups = ReportUtil.getUserGroupInfo(groupId, userId, userInfo, userIds, countDate,
                "BF", false);
        Map<Long, Object> data = null;
        if (userIds != null && userIds.size() > 0) {
            data = ServiceLocator.getInstance().lookup(CheckinServiceItf.class).getCheckinVisit(userIds, countDate,
                    countType);
        }
        List<Map<String, Object>> headers = this.initVisitHeader(countDate, countType);
        Iterator<Map<String, Object>> groupIt = groups.iterator();
        while (groupIt.hasNext()) {
            Map<String, Object> entry = groupIt.next();
            List<Long> tmpUserIds = (List<Long>) entry.get("childIds");
            List<Object> checkinInfo = new ArrayList<Object>();
            for (Long tmpUserId : tmpUserIds) {
                Map<String, Object> tmp = new HashMap<String, Object>();
                Map<String, Object> m = userInfo.get(tmpUserId);
                tmp.put("userId", m.get("userId"));
                tmp.put("name", m.get("name"));
                String status = (String) m.get("status");         
            	tmp.put("status", status);
                if (data != null) {
                    Object o = data.get(tmpUserId);
                    if (o != null) {
                        Map map = (Map) o;
                        List<Long> count = new ArrayList<Long>();
                        Long monthNum = (Long) map.get("monthNum");
                        if (monthNum == null) {
                            monthNum = 0l;
                        }
                        count.add(monthNum);
                        for (Map<String, Object> header : headers) {
                            Long weekNum = (Long) map.get("" + header.get("countTypeValue"));
                            if (weekNum == null) {
                                weekNum = 0l;
                            }
                            count.add(weekNum);
                        }
                        tmp.put("nums", count);// 用戶基本信息和考勤信息合併
                    }
                }

                checkinInfo.add(tmp);
            }
            entry.put("items", checkinInfo);
        }

        Row row = new Row();

        row.put("header", headers);
        row.put("currentTime", new Date().getTime());
        row.put("data", groups);
        return row;
    }

    @Override
    public List<Map<String, Object>> checkinVisitDetail(Long userId, Long startDate, Long endDate) {
        return ServiceLocator.getInstance().lookup(CheckinServiceItf.class).getCheckinVisitDetail(userId, startDate,
                endDate);
    }

    @Override
    public RowSet customerProgressCount(String countType, Long userId, Long startDate, Long endDate) {
        Date start = null, end = null;
        if (null != countType) {
            switch (countType) {
                case CT.COUNTTYPE_LASTWEEK:
                    start = DateUtil.getLastWeekStart();
                    end = DateUtil.getLastWeekEnd();
                    break;
                case CT.COUNTTYPE_WEEK:
                    start = DateUtil.getCurrentWeekStart();
                    end = DateUtil.getCurrentWeekEnd();
                    break;
                case CT.COUNTTYPE_MONTH:
                    start = DateUtil.getCurrentMonthStart();
                    end = DateUtil.getCurrentMonthEnd();
                    break;
                case CT.COUNTTYPE_QUARTER:
                    start = DateUtil.getCurrentQuarterStart();
                    end = DateUtil.getCurrentQuarterEnd();
                    break;
                case CT.COUNTTYPE_YEAR:
                    start = DateUtil.getCurrentYearStart();
                    end = DateUtil.getCurrentYearEnd();
                    break;
                case CT.COUNTTYPE_CUSTOM:
                    start = DateUtil.getDateTimeByString(startDate.toString());
                    end = DateUtil.parseDate(endDate.toString(), "yyyy-MM-dd HH:mm:ss");
                    if (end == null) {
                        end = DateUtil.parseDate(endDate + " 23:59:59", "yyyy-MM-dd HH:mm:ss");
                    }
                    if (start != null && end != null) {
                        if (start.compareTo(end) > 0) {
                            throw new AppException("app.common.date.endEarlier");
                        }
                    }
                    break;
            }
        }
        List<Map<String, Object>> list = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .customerProgressCount(userId, start, end);
        return this.parseProgressCountData(list);
    }

    private Long _parseDataToMapAndGetTotal(List<Map<String, Object>> list, Map<String, Object> map,
            String defaultStatus) {
        Long total = 0l;
        if (null != list) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> object = list.get(i);
                // count(t.id) as num ,t.status as status,t.owner.userId as
                // userId
                DynamicEnum statusEnum = (DynamicEnum) object.get("status");
                int countNum = 0;
                if (object.get("num") != null && !"".equals(object.get("num"))) {
                    countNum = Integer.valueOf(object.get("num").toString());
                }
                String status = null;
                if (null != statusEnum) {
                    status = statusEnum.getValue();
                } else {
                    status = defaultStatus;
                }
                total += countNum;
                if (map.containsKey(status)) {
                    countNum += ((Integer) map.get(status));
                }
                map.put(status, countNum);
            }
        }
        return total;
    }

    private Map<String, Object> parseDataToMap(List<Map<String, Object>> list) {
        Map<String, Object> map = new HashMap<String, Object>();
        Long total = _parseDataToMapAndGetTotal(list, map, "none");
        map.put("total", total);
        return map;
    }

    private RowSet parseProgressCountData(List<Map<String, Object>> list) {
        Map<String, Object> map = new HashMap<String, Object>();
        Long total = _parseDataToMapAndGetTotal(list, map, "other");
        List<Row> rows = new ArrayList<Row>();

        CustomerMetaData customerMetaData = MetadataCacheBuilder.newBuilder().buildCache().get("metadata");
        CSPEnum cspEnum = customerMetaData.getEnums().get("statusEnum");
        if (null != cspEnum) {
            List<CSPEnumValue> enumValues = cspEnum.getEnumValues();
            if (null != enumValues) {
                for (CSPEnumValue enumValue : enumValues) {
                    Row row = new Row();
                    String statusName = enumValue.getEnumValue();
                    row.put("statusLabel", enumValue.getEnumLabel());
                    row.put("statusName", statusName);
                    if (null != map.get(statusName)) {
                        row.put("countNum", map.get(statusName));
                        rows.add(row);
                    } else {
                        if (enumValue.getIsActive()) {
                            row.put("countNum", 0);
                            rows.add(row);
                        }
                    }
                }
            }
        }

        if (map.containsKey("other")) {
            Row row = new Row();
            row.put("statusName", "");
            row.put("statusLabel", "无");
            row.put("countNum", map.get("other"));
            rows.add(row);
        }
        RowSet set = new RowSet();
        set.setItems(rows);
        set.setTotal(total);
        return set;
    }

    private List<FieldRestObject> sortEnum(List<FieldRestObject> fields,
            ArrayList<Map<String, Object>> customerManager) {
        if (fields == null || customerManager == null) {
            return fields;
        }
        List<FieldRestObject> fieldsSort = new ArrayList<FieldRestObject>();
        for (int i = 0; i < customerManager.size(); i++) {
            String name = customerManager.get(i).get("name").toString();
            for (int j = 0; j < fields.size(); j++) {
                FieldRestObject o = fields.get(j);
                if (name.equals(o.getName())) {
                    fieldsSort.add(o);
                }
            }
        }
        return fieldsSort;
    }

    @Override
    public List<FieldRestObject> getAllCustomerEnums() {
        List<FieldRestObject> fields = new ArrayList<FieldRestObject>();
        IEntity customerEntity = AppWorkManager.getAppMetadataManager().getEntityByName("Customer");
        List<String> disableFields = LayoutManager.getDisableFields(EO.Customer);
        for (IField field : customerEntity.getFields().values()) {
            if (field.getType().equals(FieldTypeEnum.CSP_ENUM)) {
                if (disableFields != null && disableFields.contains(field.getName())) {
                    continue;
                }
                FieldRestObject fieldRestObject = new FieldRestObject();
                fieldRestObject.setFieldType(field.getType());
                fieldRestObject.setId(field.getId());
                fieldRestObject.setName(field.getName());
                fieldRestObject.setLabel(field.getLabel());
                fields.add(fieldRestObject);
            }
        }
        CustomerLayout layout = LayoutManager.getLayout();
        ArrayList<Map<String, Object>> customerManager = layout.getCustomerManager();
        List<FieldRestObject> fieldsSort = sortEnum(fields, customerManager);
        return fieldsSort;
    }

    private void sortUsers(List<UserValue> users, final String sort) {
        Collections.sort(users, new Comparator<UserValue>() {
            public int compare(UserValue a, UserValue b) {
                int ret = 0;
                String aName = a.getName();
                String bName = b.getName();
                if (null == aName) {
                    aName = "";
                }
                if (null == bName) {
                    bName = "";
                }

                String aSimpleSpell = PinyinUtil.hanziToPinyinFull(aName, true);
                aSimpleSpell = aSimpleSpell.toLowerCase();
                String bSimpleSpell = PinyinUtil.hanziToPinyinFull(bName, true);
                bSimpleSpell = bSimpleSpell.toLowerCase();
                if ("desc".equals(sort)) {// 倒序
                    ret = aSimpleSpell.compareTo(bSimpleSpell);
                } else {
                    ret = bSimpleSpell.compareTo(aSimpleSpell);
                }
                return ret;
            }
        });
    }

    private Map<String, Object> getProgressCountData(Long userId, List<Map<String, Object>> list) {
        List<Map<String, Object>> userData = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> object : list) {
            if (object != null) {
                // count(t.id) as num ,t.status as status,t.owner.userId as
                // userId
                if (userId.equals(object.get("userId"))) {
                    userData.add(object);
                }
            }
        }
        return parseDataToMap(userData);
    }

    private List<Long> getChildrenId(Long userId, List<UserValue> users) {
        List<Long> list = new ArrayList<Long>();
        UserValue row = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                .getUserValueByUserId(userId);
        for (UserValue user : users) {
            if (userId.equals(user.getParentId()) || SRU.LEVEL_BOSS.equals(row.getUserLevel())) {
                list.add(user.getId());
            }
        }
        return list;
    }

    private void buildGroup(List<Map<String, Object>> group, Long userId, String userName, List<UserValue> users,
            List<Map<String, Object>> list) {
        Map<String, Object> groupMap = new HashMap<String, Object>();
        List<Long> children = getChildrenId(userId, users);
        if (null != children) {// 是否有下属
            children.add(userId);
            groupMap.put("groupName", userName);
            groupMap.put("groupId", userId);
            groupMap.put("userIds", children);
            // List<Row> groupData = new ArrayList<Row>();
            for (Long childId : children) {
                Map<String, Object> childData = getProgressCountData(childId, list);
                Iterator<Entry<String, Object>> it = childData.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, Object> entry = it.next();
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (groupMap.containsKey(key)) {
                        groupMap.put(key, Long.parseLong(groupMap.get(key) + "") + Long.parseLong(value + ""));
                    } else {
                        groupMap.put(key, value);
                    }
                }
            }
            group.add(groupMap);
        }
    }

    @SuppressWarnings("unchecked")
    private Row progressCount(List<Map<String, Object>> progressCount) {
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> group = new ArrayList<Map<String, Object>>();
        // 查询用户信息
        List<UserValue> users = ServiceLocator.getInstance().lookup(UserServiceItf.class).getHierarchyUsers(userId,
                true);

        sortUsers(users, "desc");

        // 查询上下级信息
        for (UserValue user : users) {
            Long innerUserId = user.getId();
            Map<String, Object> map;
            try {
                map = BeanUtils.describe(user);
                if (map != null) {
                    map.putAll(getProgressCountData(innerUserId, progressCount));
                    if (SRU.STATUS_DISABLE.equals(user.getStatus())) {
                        Object totalObj = map.get("total");
                        if (totalObj != null && !"".equals(totalObj)) {
                            Long total = (Long) totalObj;
                            if (total != 0l) {
                                data.add(map);
                            }
                        }
                    } else {
                        data.add(map);
                    }
                    if (SRU.ROLE_SUPERISOR.equals(map.get("userRole"))) {
                        buildGroup(group, innerUserId, user.getName(), users, progressCount);
                    }
                }
            } catch (Exception e) {
                throw new AppException("app.report.user.transerror");
            }
        }
        for (Map<String, Object> groupData : group) {
            List<Long> children = (List<Long>) groupData.get("userIds");
            List<Map<String, Object>> groupUsers = new ArrayList<Map<String, Object>>();
            groupData.put("users", groupUsers);
            for (Long childId : children) {
                for (int i = data.size() - 1; i > -1; i--) {
                    Map<String, Object> user = data.get(i);
                    Object tmpUserId = user.get("userId");
                    if (childId != null && tmpUserId != null) {
                        Long userIdLong = Long.valueOf(tmpUserId.toString());
                        if (childId.longValue() == userIdLong.longValue()) {
                            groupUsers.add(data.remove(i));
                        }
                    }
                }
            }
        }
        Row row = new Row();
        row.put("userData", data);
        row.put("userGroup", group);
        List<CSPEnumValue> enumsData = ImportUtil.getAllEnum("statusEnum");
        List<CSPEnumValue> metadata = new ArrayList<CSPEnumValue>();

        // 将未停用的放入元数据 停用的 并且有数据的 放入元数据
        for (int i = 0; i < enumsData.size(); i++) {
            CSPEnumValue v = enumsData.get(i);
            boolean active = v.getIsActive();
            String key = v.getEnumValue();
            if (active) {
                metadata.add(v);
            } else {
                for (int j = 0; j < group.size(); j++) {
                    if (group.get(j).containsKey(key)) {
                        if (!metadata.contains(v)) {
                            metadata.add(v);
                        }
                        break;
                    }
                }
                for (int j = 0; j < data.size(); j++) {
                    if (data.get(j).containsKey(key)) {
                        if (!metadata.contains(v)) {
                            metadata.add(v);
                        }
                        break;
                    }
                }
            }
        }

        CSPEnumValue enumValue = new CSPEnumValue();
        enumValue.setEnumLabel("无");
        enumValue.setEnumValue("none");
        enumValue.setIsActive(true);
        // metadata.addAll(enumsData);
        metadata.add(enumValue);
        row.put("metadata", metadata);
        return row;
    }

    @Override
    public Row customerProgress(String countType, Long userId, Long startDate, Long endDate) {
        List<Map<String, Object>> progressCount = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .progressCountCustomer();
        return progressCount(progressCount);
    }

    private Map<String, Date> parseToDate(String countType) {
        Map<String, Date> retMap = new HashMap<String, Date>();
        Date start = null;
        Date end = null;
        if (null != countType) {
            switch (countType) {
                case CT.COUNTTYPE_LASTWEEK:
                    start = DateUtil.getLastWeekStart();
                    end = DateUtil.getLastWeekEnd();

                    break;
                case CT.COUNTTYPE_WEEK:
                    start = DateUtil.getCurrentWeekStart();
                    end = DateUtil.getCurrentWeekEnd();
                    break;
                case CT.COUNTTYPE_MONTH:
                    start = DateUtil.getCurrentMonthStart();
                    end = DateUtil.getCurrentMonthEnd();
                    break;
                case CT.COUNTTYPE_QUARTER:
                    start = DateUtil.getCurrentQuarterStart();
                    end = DateUtil.getCurrentQuarterEnd();
                    break;
                case CT.COUNTTYPE_YEAR:
                    start = DateUtil.getCurrentYearStart();
                    end = DateUtil.getCurrentYearEnd();
                    break;
            }
        }
        retMap.put("start", start);
        retMap.put("end", end);
        return retMap;
    }

    @Override
    public Row workrecordProgress(String countType) {
        Map<String, Date> dateMap = parseToDate(countType);
        Date start = dateMap.get("start");
        Date end = dateMap.get("end");

        List<Map<String, Object>> progressCount = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                .progressCountWorkrecord(start, end);
        return progressCount(progressCount);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Row attendanceCount(Long groupId, Long userId, String countDate) {
        if (StringUtils.isBlank(countDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            countDate = sdf.format(new Date());
        }
        Assert.checkCountDate(countDate, "app.report.attendanceCount.countDate.illege");
        List<Long> userIds = new ArrayList<Long>();
        Map<Long, Map<String, Object>> userInfo = new HashMap<Long, Map<String, Object>>();
        List<Map<String, Object>> groups = ReportUtil.getUserGroupInfo(groupId, userId, userInfo, userIds, countDate,
                "KQ", false);

        Row row = new Row();
        row.put("currentTime", new Date().getTime());
        List<Map<String, Object>> headers = initHeaderData(countDate, null);
        row.put("header", headers);
        Map<Long, Object> data = null;
        if (userIds != null && userIds.size() > 0) {
            data = ServiceLocator.getInstance().lookup(CheckinServiceItf.class).getCheckinUsers(userIds, countDate);
        }

        Iterator<Map<String, Object>> groupIt = groups.iterator();

        while (groupIt.hasNext()) {
            Map<String, Object> entry = groupIt.next();
            List<Long> tmpUserIds = (List<Long>) entry.get("childIds");
            List<Object> checkinInfo = new ArrayList<Object>();
            for (Long tmpUserId : tmpUserIds) {
                Map<String, Object> m = userInfo.get(tmpUserId);
                if (data != null) {
                    Object o = data.get(tmpUserId);
                    if (o != null) {
                        m.putAll((Map<String, Object>) o);// 用戶基本信息和考勤信息合併
                    }
                }
                checkinInfo.add(m);
            }
            entry.put("children", checkinInfo);
        }
        row.put("data", groups);
        return row;
    }

}
