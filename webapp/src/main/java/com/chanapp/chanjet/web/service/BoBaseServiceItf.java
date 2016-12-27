package com.chanapp.chanjet.web.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.bo.api.IDataTransformer;
import com.chanjet.csp.bo.api.ReportingResult;

/**
 * BoService的基础接口，每个Bo对应一个Service，当bo不生成javahome类时，会使用默认的实现类BoBaseServiceImpl
 * <ul>
 * <li>BoService需要一个Service接口和一个Service实现类；</li>
 * <li>放在 appid + ".service." + boName.toLowerCase() 包下；</li>
 * <li>接口：{BoName}ServiceItf，实现类：{BoName}ServiceImpl</li>
 * </ul>
 * 
 * @author tds
 *
 * @param <H> BoHome
 * @param <R> BoRow
 * @param <S> BoRowSet
 */
public interface BoBaseServiceItf<H extends IBusinessObjectHome, R extends IBusinessObjectRow, S extends IBusinessObjectRowSet>
        extends BaseServiceItf {

    /**
     * 获取boHome
     * 
     * @return IBusinessObjectHome
     */
    H getBusinessObjectHome();

    /**
     * 获取bo id
     * 
     * @return
     */
    String getBusinessObjectId();

    /**
     * 创建boRow
     * 
     * @return 空的boRow
     */
    R createRow();

    /**
     * 创建boRowSet
     * 
     * @return 空的boRowSet
     */
    S createRowSet();

    /**
     * 修改或新增boRow，会触发preUpsert和postUpsert
     * 
     * @param row
     */
    void upsert(R row);

    /**
     * 删除boRow，会触发preDelete和postDelete
     * 
     * @param row
     */
    void delete(R row);

    /**
     * 删除boRow，会触发preDelete和postDelete
     * 
     * @param row Id
     */
    void delete(Long id);

    /**
     * 根据ID查询boRow
     * 
     * @param id
     * @return 一条boRow
     */
    R query(Long id);

    /**
     * 根据查询条件查询boRow集合
     * 
     * @param jsonQuerySpec
     * @return boRow集合
     */
    S query(String jsonQuerySpec);

    /**
     * 根据rowId 批量删除
     * 
     * <p>
     * <b>WARN：与平台3.x不一致，</b>
     * <li>不会触发pre/postDelete</li>
     * </p>
     * 
     * @param rowIds RowID列表
     * @param allOrNothing 传true表示：删除过程中有1条失败时，立即返回，抛出异常。
     *            传false表示：删除过程中有失败时，跳过，接着删剩下的
     * @return 成功删除的数量
     */
    int batchDelete(List<Long> rowIds, boolean allOrNothing);

    /**
     * 根据查询条件批量删除
     * 
     * <p>
     * <b>WARN：与平台3.x不一致，</b>
     * <li>不会触发pre/postDelete</li>
     * </p>
     * 
     * @param jsonQuery
     * @return 成功删除的数量
     */
    int batchDelete(String jsonQuery);

    /**
     * 批量修改：update bo set fieldName = fieldName + value where jsonQuery
     * 
     * <p>
     * <b>WARN：与平台3.x保持一致，</b>
     * <li>不会触发pre/postUpsert</li>
     * </p>
     * 
     * @param jsonQuery 查询条件
     * @param fieldNames 字段名
     * @param values 要增加或减去的值
     * @param inc true加，false减
     * @return 成功修改的数量
     */
    int batchIncrementalUpdate(String jsonQuery, String[] fieldNames, Object[] values, boolean inc);

    /**
     * 批量新增boRow，完成后会重新加载rowSet
     * 
     * <p>
     * <b>WARN：与平台3.x保持一致，</b>
     * <li>不支持带子BO的新增</li>
     * <li>不会触发pre/postUpsert</li>
     * </p>
     * 
     * @param rowSet
     * @return 成功新增的数量
     */
    int batchInsert(S rowSet);

    /**
     * 批量新增boRow
     * 
     * <p>
     * <b>WARN：与平台3.x保持一致，</b>
     * <li>不支持带子BO的新增</li>
     * <li>不会触发pre/postUpsert</li>
     * </p>
     * 
     * @param rowSet
     * @param isReload 完成后是否重新加载rowSet
     * @return 成功新增的数量
     */
    int batchInsert(S rowSet, boolean isReload);

    /**
     * 批量修改：update bo set updateExpressions where jsonQuery
     * 
     * <p>
     * <b>WARN：与平台3.x保持一致，</b>
     * <li>不会触发pre/postUpsert</li>
     * </p>
     * 
     * @param jsonQuery 查询条件
     * @param updateExpressions 表达式，比如 a=a+1,b=c*d,e='aaa'
     * @return 成功修改的数量
     */
    int batchUpdate(String jsonQuery, String[] updateExpressions);

    /**
     * 批量修改：update bo set fieldName=value where jsonQuery
     * 
     * <p>
     * <b>WARN：与平台3.x保持一致，</b>
     * <li>不会触发pre/postUpsert</li>
     * </p>
     * 
     * @param jsonQuery 查询条件
     * @param fieldNames 字段名
     * @param values 字段值，与fieldNames一一对应
     * @return 成功修改的数量
     */
    int batchUpdate(String jsonQuery, String[] fieldNames, Object[] values);

    /**
     * 根据查询条件查询boRow集合，<b>WARN：慎重使用，会查询出全部数据，可能导致OOM</b>
     * 
     * @param jsonQuerySpec 查询条件
     * @return 符合条件的全部boRow集合
     */
    S queryAll(String jsonQuerySpec);

    /**
     * 根据查询条件查询boRow集合，<b>WARN：慎重使用，会查询出全部数据，可能导致OOM</b>
     * 
     * @param jsonQuerySpec 查询条件
     * @param withDeleted 传false时会附加上'isDeleted=false or isDeleted is null'条件
     *            （注：只是附加上，不会处理原条件中的isDeleted）
     * @return 符合条件的全部boRow集合
     */
    S queryAll(String jsonQuerySpec, boolean withDeleted);

    /**
     * 批量设置isDeleted标识
     * 
     * @param jsonQuery 查询条件
     * @param deleted isDelete标识
     * @return 成功修改的数量
     */
    int batchSetIsDeleted(String jsonQuery, boolean deleted);

    /**
     * 根据ID检查数据权限，并查询数据
     * 
     * @param id 要查询的BO ID
     * @return 一条boRow
     */
    R findByIdWithAuth(Long id);

    /**
     * 根据查询条件统计数量
     * 
     * @param jsonQuerySpec 查询条件
     * @return 总数
     */
    Integer getRowCount(String jsonQuerySpec);

    /**
     * 跨权限查询：根据查询条件统计数量
     * 
     * @param jsonQuerySpec 查询条件
     * @return 总数
     */
    Integer privilegedGetRowCount(String jsonQuerySpec);

    /**
     * 跨权限查询：根据ID查询boRow
     * 
     * @param boId 要查询的BO ID
     * @return 一条boRow
     */
    R privilegedQuery(long boId);

    /**
     * 跨权限查询：根据查询条件查询boRowSet
     * 
     * @param jsonQuerySpec 查询条件
     * @return boRow集合
     */
    S privilegedQuery(String jsonQuerySpec);

    /**
     * 跨权限查询：根据查询条件查询IDataTransformer
     * 
     * @param jsonQuerySpec 查询条件
     * @return 未转为BO的数据集合
     */
    List<Object[]> privilegedQueryNoTransform(String jsonQuerySpec);

    /**
     * 根据localId查询或创建空白的boRow，不会查询isDelete=true的数据
     * 
     * @param localId
     * @return 符合条件的boRow
     */
    R getRowByLocalId(String localId);

    /**
     * 根据localId查询或创建空白的boRow，不会查询isDelete=true的数据
     * 
     * @param localId
     * @param withDeleted 传false时会附加上'isDeleted=false or isDeleted is null'条件
     * @return 符合条件的boRow
     */


    /**
     * 报表查询
     * 
     * @param jsonQuerySpec
     * @param jsonReportSpec
     * @return
     */
    ReportingResult getReportData(String jsonQuerySpec, String jsonReportSpec);

    /**
     * 提供CQL查询
     * 
     * @param cqlQueryString
     * @return
     */
    public List<Map<String, Object>> runCQLQuery(String cqlQueryString);

    public List<Map<String, Object>> runCQLQuery(String cqlQueryString, List<Object> posParameters);

    public List<Map<String, Object>> runCQLQuery(String cqlQueryString, HashMap<String, Object> namedParameters);

    public List<Map<String, Object>> runCQLQuery(String cqlQueryString, int start, int pageCount);

    public List<Map<String, Object>> runCQLQuery(String cqlQueryString, List<Object> posParameters, int start,
            int pageCount);

    public List<Map<String, Object>> runCQLQuery(String cqlQueryString, HashMap<String, Object> namedParameters,
            int start, int pageCount);

    /**
     * 提供CQL Update/Insert/Delete
     * 
     * @param cqlQueryString
     */
    public void runCQLUpdate(String cqlQueryString);
}
