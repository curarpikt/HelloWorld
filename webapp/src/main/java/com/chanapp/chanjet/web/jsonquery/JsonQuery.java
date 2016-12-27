package com.chanapp.chanjet.web.jsonquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.data.api.DataManager;

/**
 * jsonquery工具类，创建2.0版的jsonquery（兼容1.0）
 * 
 * @author tds
 *
 */
public class JsonQuery {
    private static final DataManager dataManager = AppWorkManager.getDataManager();

    public static final String version = "2.0";

    private String CriteriaStr;
    private Map<String, String> ChildBOCriteriaStr;
    private List<BindVar> BindVars;
    private Map<String, List<BindVar>> ChildBOBindVars;
    private List<String> Fields;
    private List<SortBy> SortBy;
    private int FirstResult;
    private int MaxResult;
    private boolean Count;

    public Criteria Criteria;
    public List<ChildCriteria> ChildBOCriteria;

    private JsonQuery() {
        init();
    }

    private JsonQuery(String json) {
        this();
        parse(json);
    }

    public static JsonQuery getInstance() {
        return new JsonQuery();
    }

    public static JsonQuery getInstance(String json) {
        return new JsonQuery(json);
    }

    private void init() {
        CriteriaStr = "";
        ChildBOCriteriaStr = new HashMap<String, String>();
        BindVars = new ArrayList<BindVar>();
        ChildBOBindVars = new HashMap<String, List<BindVar>>();
        Fields = new ArrayList<String>();
        SortBy = new ArrayList<SortBy>();
        FirstResult = 0;
        MaxResult = 0;
        Count = false;
    }

    /**
     * 会去除重复的字段
     * 
     * @param fs
     * @return
     */
    public JsonQuery addFields(String... fs) {
        if (fs != null) {
            for (String f : fs) {
                if (!this.Fields.contains(f)) {
                    this.Fields.add(f);
                }
            }
        }
        return this;
    }

    public JsonQuery setCriteriaStr(String str) {
        this.CriteriaStr = str;
        return this;
    }

    public JsonQuery setChildBOCriteriaStr(String cbo, String str) {
        this.ChildBOCriteriaStr.put(cbo, str);
        return this;
    }

    public JsonQuery addSort(String field, String order) {
        this.SortBy.add(new SortBy(field, order));
        return this;
    }

    public JsonQuery addBindVars(String name, Object value) {
        this.BindVars.add(new BindVar(name, value));
        return this;
    }

    public JsonQuery setMaxResult(int max) {
        this.MaxResult = max;
        return this;
    }

    public JsonQuery setFirstResult(int first) {
        this.FirstResult = first;
        return this;
    }

    public JsonQuery setCount(boolean c) {
        Count = c;
        return this;
    }

    @SuppressWarnings("unchecked")
    private void parse(String json) {
        if (json == null || json.isEmpty()) {
            return;
        }
        Map<String, Object> jq = dataManager.jsonStringToMap(json);
        if (jq.containsKey("Count")) {
            this.setCount(ConvertUtil.toBoolean(jq.get("Count").toString()));
        }

        if (jq.containsKey("FirstResult")) {
            this.setFirstResult(ConvertUtil.toInt(jq.get("FirstResult").toString()));
        }

        if (jq.containsKey("MaxResult")) {
            this.MaxResult = ConvertUtil.toInt(jq.get("MaxResult").toString());
        }

        this.Fields.clear();
        if (jq.containsKey("Fields")) {
            List<String> fields = (List<String>) jq.get("Fields");
            for (String field : fields) {
                this.Fields.add(field);
            }
        }

        this.SortBy.clear();
        if (jq.containsKey("SortBy")) {
            List<Map<String, String>> sbs = (List<Map<String, String>>) jq.get("SortBy");
            for (Map<String, String> sb : sbs) {
                SortBy st = new SortBy(sb.get("FieldName"), sb.get("Order"));
                this.SortBy.add(st);
            }
        }

        if (jq.containsKey("CriteriaStr")) {
            this.setCriteriaStr(jq.get("CriteriaStr").toString());
        }

        if (jq.containsKey("ChildBOCriteriaStr")) {
            this.ChildBOCriteriaStr = (Map<String, String>) jq.get("ChildBOCriteriaStr");
        }

        this.BindVars.clear();
        if (jq.containsKey("BindVars")) {
            List<Map<String, Object>> bvs = (List<Map<String, Object>>) jq.get("BindVars");
            for (Map<String, Object> bv : bvs) {
                this.BindVars.add(new BindVar(bv.get("name").toString(), bv.get("value")));
            }
        }

        this.ChildBOBindVars.clear();
        if (jq.containsKey("ChildBOBindVars")) {
            Map<String, List<Map<String, Object>>> cbvs = (Map<String, List<Map<String, Object>>>) jq
                    .get("ChildBOBindVars");
            for (Entry<String, List<Map<String, Object>>> entry : cbvs.entrySet()) {
                String key = entry.getKey();
                List<Map<String, Object>> value = entry.getValue();
                List<BindVar> bvs = new ArrayList<BindVar>();
                for (Map<String, Object> bv : value) {
                    bvs.add(new BindVar(bv.get("name").toString(), bv.get("value")));
                }
                if (bvs.size() > 0) {
                    this.ChildBOBindVars.put(key, bvs);
                }
            }
        }

        if (jq.containsKey("Criteria")) {
            this.Criteria = new Criteria();
            Map<String, Object> ct = (Map<String, Object>) jq.get("Criteria");
            this.Criteria.Operator = ct.get("Operator").toString();
            if (ct.containsKey("FieldName")) {
                this.Criteria.FieldName = ct.get("FieldName").toString();
            }
            parseQuery((List<Object>) ct.get("Values"), this.Criteria);
        }

        if (jq.containsKey("ChildBOCriteria")) {
            this.ChildBOCriteria = new ArrayList<ChildCriteria>();
            List<Map<String, Object>> cct = (List<Map<String, Object>>) jq.get("ChildBOCriteria");
            for (Map<String, Object> occ : cct) {
                ChildCriteria _cc = new ChildCriteria();
                _cc.Name = occ.get("Name").toString();
                _cc.Criteria = new Criteria();
                _cc.Criteria.Operator = "and";
                _cc.Criteria.Values = new ArrayList<Object>();
                if (occ.containsKey("Criteria")) {
                    Map<String, Object> ct = (Map<String, Object>) occ.get("Criteria");
                    _cc.Criteria.Operator = ct.get("Operator").toString();
                    if (ct.containsKey("FieldName")) {
                        _cc.Criteria.FieldName = ct.get("FieldName").toString();
                    }
                    parseQuery((List<Object>) ct.get("Values"), _cc.Criteria);
                }
                this.ChildBOCriteria.add(_cc);
            }
        }

    }

    @SuppressWarnings("unchecked")
    private void parseQuery(List<Object> values, Criteria ctx) {
        ctx.Values = new ArrayList<Object>();
        for (Object value : values) {
            if (value instanceof Map) {
                Map<String, Object> val = (Map<String, Object>) value;
                String oper = val.get("Operator").toString();
                if (oper.equalsIgnoreCase("and") || oper.equalsIgnoreCase("or") || oper.equalsIgnoreCase("not")) {
                    Criteria ct1 = new Criteria();
                    ct1.Operator = oper;
                    parseQuery((List<Object>) val.get("Values"), ct1);
                    ctx.Values.add(ct1);
                } else {
                    Criteria ct1 = new Criteria();
                    ct1.Operator = oper;
                    ct1.FieldName = val.get("FieldName").toString();
                    ct1.Values = new ArrayList<Object>();
                    List<Object> vals = (List<Object>) val.get("Values");
                    for (Object val1 : vals) {
                        ct1.Values.add(val1);
                    }
                    ctx.Values.add(ct1);
                }
            } else {
                ctx.Values.add(value);
            }
        }
    }

    private Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();

        if (CriteriaStr != null && !CriteriaStr.isEmpty()) {
            map.put("CriteriaStr", CriteriaStr);
        }

        if (ChildBOCriteriaStr != null && !ChildBOCriteriaStr.isEmpty()) {
            Map<String, String> _ChildBOCriteriaStr = new HashMap<String, String>();
            for (Entry<String, String> entry : ChildBOCriteriaStr.entrySet()) {
                _ChildBOCriteriaStr.put(entry.getKey(), entry.getValue());
            }
            map.put("ChildBOCriteriaStr", _ChildBOCriteriaStr);
        }

        if (BindVars != null && !BindVars.isEmpty()) {
            List<Map<String, Object>> _BindVars = new ArrayList<Map<String, Object>>();
            for (BindVar bv : BindVars) {
                Map<String, Object> mbv = new HashMap<String, Object>();
                mbv.put("name", bv.getName());
                mbv.put("value", bv.getValue());
                _BindVars.add(mbv);
            }
            map.put("BindVars", _BindVars);
        }

        if (ChildBOBindVars != null && !ChildBOBindVars.isEmpty()) {
            Map<String, List<Map<String, Object>>> _ChildBOBindVars = new HashMap<String, List<Map<String, Object>>>();
            for (Entry<String, List<BindVar>> entry : ChildBOBindVars.entrySet()) {
                List<Map<String, Object>> _BindVars = new ArrayList<Map<String, Object>>();
                for (BindVar bv : entry.getValue()) {
                    Map<String, Object> mbv = new HashMap<String, Object>();
                    mbv.put("name", bv.getName());
                    mbv.put("value", bv.getValue());
                    _BindVars.add(mbv);
                }
                _ChildBOBindVars.put(entry.getKey(), _BindVars);
            }
            map.put("ChildBOBindVars", _ChildBOBindVars);
        }

        if (Criteria != null && Criteria.Values != null && !Criteria.Values.isEmpty()) {
        	String jsonStr = JSON.toJSONString(Criteria);
        	map.put("Criteria",AppWorkManager.getDataManager().createJSONObject(jsonStr));
        }

        if (ChildBOCriteria != null && !ChildBOCriteria.isEmpty()) {
            map.put("ChildBOCriteria", ChildBOCriteria);
        }

        if (Fields != null && !Fields.isEmpty()) {
            map.put("Fields", Fields);
        }

        if (SortBy != null && !SortBy.isEmpty()) {
            List<Map<String, String>> _SortBy = new ArrayList<Map<String, String>>();
            for (SortBy sb : SortBy) {
                Map<String, String> msb = new HashMap<String, String>();
                msb.put("FieldName", sb.getFieldName());
                msb.put("Order", sb.getOrder());
                _SortBy.add(msb);
            }
            map.put("SortBy", _SortBy);
        }

        if (this.FirstResult > 0) {
            map.put("FirstResult", FirstResult);
        }

        if (this.MaxResult > 0) {
            map.put("MaxResult", MaxResult);
        }

        if (Count) {
            map.put("Count", Count);
        }

        return map;
    }

    @Override
    public String toString() {
        // 不方便加Filter，转成Map过滤掉不需要的属性
        Map<String, Object> map = toMap();
        String jsonString = dataManager.toJSONString(map);

        return jsonString;
    }

    /**
     * @return the firstResult
     */
    public int getFirstResult() {
        return FirstResult;
    }

    /**
     * @return the maxResult
     */
    public int getMaxResult() {
        return MaxResult;
    }

    /**
     * @return the criteriaStr
     */
    public String getCriteriaStr() {
        return CriteriaStr;
    }

    /**
     * @return the sortBy
     */
    public List<SortBy> getSortBy() {
        return SortBy;
    }

    /**
     * @param sortBy the sortBy to set
     */
    public void setSortBy(List<SortBy> sortBy) {
        SortBy = sortBy;
    }

    /**
     * 翻页查询时，SortBy必须加入id，参与排序
     * 
     * @return
     */
    public JsonQuery fixSortBy() {
        if (this.SortBy == null) {
            this.SortBy = new ArrayList<SortBy>();
        }
        boolean hasId = false;
        for (SortBy sb : this.SortBy) {
            if (sb.getFieldName().equals(SC.id)) {
                hasId = true;
                break;
            }
        }
        if (!hasId) {
            this.SortBy.add(new SortBy(SC.id, "Ascending"));
        }
        return this;
    }

}
