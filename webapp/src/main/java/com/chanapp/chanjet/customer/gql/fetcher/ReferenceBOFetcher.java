package com.chanapp.chanjet.customer.gql.fetcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.util.QueryLimitUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.CqlQuery;
import com.chanjet.csp.bo.api.IBindVariable;
import com.chanjet.csp.graphql.datafetcher.CspDataFetcher;
import com.chanjet.csp.graphql.util.GqlQuerySpec;
import com.chanjet.csp.graphql.util.GqlUtil;

import graphql.execution.batched.Batched;
import graphql.language.Field;
import graphql.language.Selection;
import graphql.schema.DataFetchingEnvironment;

public class ReferenceBOFetcher extends CspDataFetcher {
  
  private String sourceBoName;
  private String gqlFieldName;
  private String referTypeFieldName;
  private String referIdFieldName;

  /**
   * 
   * @param sourceBoName source bo name
   * @param boName reference bo name
   * @param gqlFieldName gql field name of source bo object
   * @param referTypeFieldName the field name of source bo that save the reference eo name
   * @param referIdFieldName the field name of source bo that save the reference object id
   */
  public ReferenceBOFetcher(String sourceBoName, String boName, String gqlFieldName, String referTypeFieldName, String referIdFieldName){
    super(boName);
    this.sourceBoName = sourceBoName;
    this.gqlFieldName = gqlFieldName;
    this.referTypeFieldName = referTypeFieldName;
    this.referIdFieldName = referIdFieldName;
  }

  @SuppressWarnings("unchecked")
  @Override
  @Batched
  public Object get(DataFetchingEnvironment environment) {
    
    List<Map<String, Object>> sources = (List<Map<String, Object>>) environment.getSource();
    List<Object> ids = new ArrayList<Object>(sources.size());
    for(Map<String, Object> source : sources){
      ids.add(source.get("id"));
    }
    
    
/*    //select referIdFieldName from source bo
    String cql = String.format("select id, %s as rid from %s where id in (%s)", 
      this.referIdFieldName, this.sourceBoName, GqlUtil.joinIds(ids));        
    if(referTypeFieldName!=null){
      cql += String.format(" and %s='%s'", referTypeFieldName, boName);
    }
   // GqlQuerySpec querySpec = null;
        
    BoSession boSession = AppContext.session(); 
    CqlQuery query = boSession.createCqlQuery(cql, null);
    List<?> list = query.list();*/
    
    BoSession boSession = AppContext.session(); 
	String cqlQueryString = "select id, "+this.referIdFieldName+" as rid from "+this.sourceBoName+" "
			+ " where id in (:ids)";
	HashMap<String, Object> paraMap = new HashMap<String, Object>();
	paraMap.put("ids", ids);
  //select referIdFieldName from source bo

    if(referTypeFieldName!=null){
    	 cqlQueryString += String.format(" and %s='%s'", referTypeFieldName, boName);
    }
	List<?> list = QueryLimitUtil.runCQLQuery(null, boSession, cqlQueryString, paraMap);
    
    List<Object> rids = new ArrayList<Object>();
    List<Long> idWithNulls = new ArrayList<Long>(sources.size());
    if(list != null){
      for(Object o : list){
        Map<String, Object> row = (Map<String, Object>)o;
        Object v = row.get("rid");
        Long rid = null;
        if(v!=null){
          rid = Long.valueOf(v.toString());
          rids.add(rid);
        }
        idWithNulls.add(rid);
      }
    }
    
    boolean idOnly = true;
    GqlQuerySpec querySpec = null;
    if(ids.size() > 0){
      // get fields
      List<Selection> selFields = null;
      for (Field field : environment.getFields()) {
        if (gqlFieldName.equals(field.getName())) {
          selFields = field.getSelectionSet().getSelections();
          break;
        }
      }
      querySpec = new GqlQuerySpec(this.boName);
      querySpec.setFields(this.getFieldsFromSelectionSet(this.boName, selFields, Arrays.asList("id")));
      querySpec.appendCriteriaStr("id in (:ids)");
      ArrayList<IBindVariable> bindVars = new ArrayList<IBindVariable>();
      bindVars.add(boManager.createBindVariable("ids", ids));
      querySpec.setBindVars(bindVars);
    //  querySpec.appendCriteriaStr(String.format("id in (%s)", GqlUtil.joinIds(rids)));
      List<String> fields = querySpec.getFields();
      for (int i = 0; i < fields.size(); i++) {
        if (!fields.get(i).equals("id")) {
          idOnly = false;
        }
      }
    }

    List<Object> result = new ArrayList<Object>();
    if (idOnly) {
      for (Object tmpId : idWithNulls) {
        if (tmpId == null) {
          result.add(null);
        } else {
          HashMap<String, Object> values = new HashMap<String, Object>();
          values.put("id", tmpId);
          result.add(values);
        }
      }
    } else {
    	CqlQuery query = GqlUtil.getCqlQuery(querySpec);
      List<List<Object>> lists = this.splitListByIds(query.listReal(), "id", idWithNulls);
      for(List<Object> obj : lists){
        result.add(obj==null?obj:obj.get(0));
      }
    }
    return result;
  }

}
