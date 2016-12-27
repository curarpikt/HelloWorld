package com.chanapp.chanjet.customer.gql.fetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.web.context.AppContext;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.common.base.dataauth.DataAuthPrivilege;

import graphql.execution.batched.Batched;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class BOPrivilegeFetcher implements DataFetcher {
  
  private String boName;

  public BOPrivilegeFetcher(String boName){
    this.boName  = boName;
  }

  /**
   * Get privilege value for each id, the bit of value as binary is (from left to right): DELETE,UPDATE,QUERY
   */
  @Override
  @Batched //Using batch fetch
  public Object get(DataFetchingEnvironment environment) {
    
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> sources = (List<Map<String, Object>>) environment.getSource();
    List<Long> ids = new ArrayList<Long>(sources.size());
    for(Map<String, Object> source : sources){
      ids.add(Long.valueOf(source.get("id").toString()));
    }
    
    
    BoSession session = AppContext.session();
    List<Long> deleteIds = AppWorkManager.getBoDataAccessManager()
      .getDataAuthorization()
      .isBoAuthorizedList(session, boName, DataAuthPrivilege.DELETE, ids);
    List<Long> updateIds = AppWorkManager.getBoDataAccessManager()
        .getDataAuthorization()
        .isBoAuthorizedList(session, boName, DataAuthPrivilege.UPDATE, ids);
    
    List<Integer> privileges = new ArrayList<Integer>(ids.size());
    for(Long id : ids){
      privileges.add(1+(updateIds.contains(id)?2:0)+(deleteIds.contains(id)?4:0));
    }
    
    return privileges;
  }

}
