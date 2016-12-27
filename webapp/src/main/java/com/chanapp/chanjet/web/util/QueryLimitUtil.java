package com.chanapp.chanjet.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.chanapp.chanjet.customer.constant.SQ;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.jsonquery.JsonQuery;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;

public class QueryLimitUtil {
  private static final Log logger = LogFactory.getLog(QueryLimitUtil.class);

  private static boolean _hasPageQueryStr(String queryJsonStr) {
    JsonQuery jsonQuery = JsonQuery.getInstance(queryJsonStr);
    if (jsonQuery.getMaxResult() > 0) {
      if (jsonQuery.getMaxResult() > SQ.MAX_QUERY_RESULT) {
        jsonQuery.setMaxResult(SQ.MAX_QUERY_RESULT);
        // throw new AppException("csp.data.results.maximum");
      }
      return true;
    }
    return false;
  }

  public static IBusinessObjectRowSet privilegedQuery(String jsonQuerySpec, IBusinessObjectHome home) {
    BoSession session = AppContext.session();
    if (!QueryLimitUtil._hasPageQueryStr(jsonQuerySpec)) {
      IBusinessObjectRowSet rows = home.createRowSet();
      JsonQuery jq = JsonQuery.getInstance(jsonQuerySpec).fixSortBy(); // 翻页查询，强制加入ID，参与排序
      jq.setFirstResult(0).setMaxResult(SQ.MAX_QUERY_RESULT);
      for (;;) {
        jsonQuerySpec = jq.toString();
        IBusinessObjectRowSet _rows = null;
        Long start = System.currentTimeMillis();
        _rows = home.privilegedQuery(session, jsonQuerySpec);
        logger.info(home.getDefinition().getName() + " privilegedQuery use time = "
            + (System.currentTimeMillis() - start) + " ms ,ADDCount:" + jq.getFirstResult() + SQ.MAX_QUERY_RESULT);
        // System.out.println(home.getDefinition().getName()+" privilegedQuery use time = " +
        // (System.currentTimeMillis() - start) + " ms ,ADDCount:"
        // + jq.getFirstResult() + SQ.MAX_QUERY_RESULT);
        if (_rows == null || _rows.size() == 0) {
          break;
        }
        for (IBusinessObjectRow _row : _rows.getRows()) {
          rows.addRow(_row);
        }
        if (_rows.size() < SQ.MAX_QUERY_RESULT) {
          break;
        }
        jq.setFirstResult(jq.getFirstResult() + SQ.MAX_QUERY_RESULT);
      }
      return rows;
    } else {
      return home.privilegedQuery(session, jsonQuerySpec);
    }

  }

  public static List<IBusinessObjectRow> queryList(String jsonQuerySpec, IBusinessObjectHome home) {
    BoSession session = AppContext.session();
    if (!QueryLimitUtil._hasPageQueryStr(jsonQuerySpec)) {
      List<IBusinessObjectRow> rows = new ArrayList<IBusinessObjectRow>();
      ;
      JsonQuery jq = JsonQuery.getInstance(jsonQuerySpec).fixSortBy(); // 翻页查询，强制加入ID，参与排序
      jq.setFirstResult(0).setMaxResult(SQ.MAX_QUERY_RESULT);
      for (;;) {
        jsonQuerySpec = jq.toString();
        IBusinessObjectRowSet _rows = null;
        Long start = System.currentTimeMillis();
        _rows = home.query(session, jsonQuerySpec);
        logger.info(home.getDefinition().getName() + " queryList use time = " + (System.currentTimeMillis() - start)
            + " ms ,ADDCount:" + jq.getFirstResult() + SQ.MAX_QUERY_RESULT);
        /*
         * System.out.println(home.getDefinition().getName()+" query use time = " +
         * (System.currentTimeMillis() - start) + " ms ,ADDCount:" + jq.getFirstResult() +
         * SQ.MAX_QUERY_RESULT);
         */
        if (_rows == null || _rows.size() == 0) {
          break;
        }
        for (IBusinessObjectRow _row : _rows.getRows()) {
          rows.add(_row);
        }
        if (_rows.size() < SQ.MAX_QUERY_RESULT) {
          break;
        }
        jq.setFirstResult(jq.getFirstResult() + SQ.MAX_QUERY_RESULT);
      }
      return rows;
    } else {
      return home.query(session, jsonQuerySpec).getRows();
    }
  }

  public static IBusinessObjectRowSet query(String jsonQuerySpec, IBusinessObjectHome home) {
    BoSession session = AppContext.session();
    if (!QueryLimitUtil._hasPageQueryStr(jsonQuerySpec)) {
      IBusinessObjectRowSet rows = home.createRowSet();
      JsonQuery jq = JsonQuery.getInstance(jsonQuerySpec).fixSortBy(); // 翻页查询，强制加入ID，参与排序
      jq.setFirstResult(0).setMaxResult(SQ.MAX_QUERY_RESULT);
      for (;;) {
        jsonQuerySpec = jq.toString();
        IBusinessObjectRowSet _rows = null;
        Long start = System.currentTimeMillis();
        _rows = home.query(session, jsonQuerySpec);
        logger.info(home.getDefinition().getName() + " query use time = " + (System.currentTimeMillis() - start)
            + " ms ,ADDCount:" + jq.getFirstResult() + SQ.MAX_QUERY_RESULT);
        /*
         * System.out.println(home.getDefinition().getName()+" query use time = " +
         * (System.currentTimeMillis() - start) + " ms ,ADDCount:" + jq.getFirstResult() +
         * SQ.MAX_QUERY_RESULT);
         */
        if (_rows == null || _rows.size() == 0) {
          break;
        }
        for (IBusinessObjectRow _row : _rows.getRows()) {
          rows.addRow(_row);
        }
        if (_rows.size() < SQ.MAX_QUERY_RESULT) {
          break;
        }
        jq.setFirstResult(jq.getFirstResult() + SQ.MAX_QUERY_RESULT);
      }
      return rows;
    } else {
      return home.query(session, jsonQuerySpec);
    }
  }

  public static List<Object[]> privilegedQueryNoTransform(String jsonQuerySpec, IBusinessObjectHome home) {
    BoSession session = AppContext.session();
    if (!QueryLimitUtil._hasPageQueryStr(jsonQuerySpec)) {
      List<Object[]> rows = new ArrayList<Object[]>();
      JsonQuery jq = JsonQuery.getInstance(jsonQuerySpec).fixSortBy(); // 翻页查询，强制加入ID，参与排序
      jq.setFirstResult(0).setMaxResult(SQ.MAX_QUERY_RESULT);
      for (;;) {
        jsonQuerySpec = jq.toString();
        List<Object[]> _rows = null;
        _rows = home.privilegedQueryNoTransform(session, jsonQuerySpec).getRawData();
        if (_rows == null || _rows.size() == 0) {
          break;
        }
        rows.addAll(_rows);
        if (_rows.size() < SQ.MAX_QUERY_RESULT) {
          break;
        }
        jq.setFirstResult(jq.getFirstResult() + SQ.MAX_QUERY_RESULT);
      }
      return rows;
    } else {
      return home.privilegedQueryNoTransform(session, jsonQuerySpec).getRawData();
    }
  }

  @SuppressWarnings("unchecked")
  public static List<Map<String, Object>> runCQLQuery(IBusinessObjectHome home, BoSession session,
      String jsonQuerySpec) {
    List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
    int first = 0;
    for (;;) {

      List<Map<String, Object>> _rows = null;
      _rows =
          AppWorkManager.getBoDataAccessManager().runCQLQuery(home, session, jsonQuerySpec, first, SQ.MAX_QUERY_RESULT);
      if (_rows == null || _rows.size() == 0) {
        break;
      }
      rows.addAll(_rows);
      if (_rows.size() < SQ.MAX_QUERY_RESULT) {
        break;
      }
      first = first + SQ.MAX_QUERY_RESULT;
    }
    return rows;
  }

  @SuppressWarnings("unchecked")
  public static List<Map<String, Object>> runCQLQuery(IBusinessObjectHome home, BoSession session, String jsonQuerySpec,
      List<Object> posParameters) {
    List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
    int first = 0;
    for (;;) {

      List<Map<String, Object>> _rows = null;
      _rows = AppWorkManager.getBoDataAccessManager().runCQLQuery(home, session, jsonQuerySpec, posParameters, first,
        SQ.MAX_QUERY_RESULT);
      if (_rows == null || _rows.size() == 0) {
        break;
      }
      rows.addAll(_rows);
      if (_rows.size() < SQ.MAX_QUERY_RESULT) {
        break;
      }
      first = first + SQ.MAX_QUERY_RESULT;
    }
    return rows;

  }

  @SuppressWarnings("unchecked")
  public static List<Map<String, Object>> runCQLQuery(IBusinessObjectHome home, BoSession session, String jsonQuerySpec,
      HashMap<String, Object> namedParameters) {
    List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
    int first = 0;
    for (;;) {

      List<Map<String, Object>> _rows = null;
      _rows = AppWorkManager.getBoDataAccessManager().runCQLQuery(home, session, jsonQuerySpec, namedParameters, first,
        SQ.MAX_QUERY_RESULT);
      if (_rows == null || _rows.size() == 0) {
        break;
      }
      rows.addAll(_rows);
      if (_rows.size() < SQ.MAX_QUERY_RESULT) {
        break;
      }
      first = first + SQ.MAX_QUERY_RESULT;
    }
    return rows;
  }

}
