package com.chanapp.chanjet.customer.service.importrecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.importrecordrelationnew.IImportRecordRelationNewHome;
import com.chanapp.chanjet.customer.businessobject.api.importrecordrelationnew.IImportRecordRelationNewRow;
import com.chanapp.chanjet.customer.businessobject.api.importrecordrelationnew.IImportRecordRelationNewRowSet;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.IPT;
import com.chanapp.chanjet.customer.service.importrecordnew.ImportUtil;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.data.api.DataManager;

public abstract class ImportServiceImpl implements ImportServiceItf {

	protected int taskCount = 200;
	
	protected static final DataManager dataManager = AppWorkManager.getDataManager();

	protected IImportRecordRelationNewHome relationHome = (IImportRecordRelationNewHome) AppWorkManager
			.getBusinessObjectManager().getPrimaryBusinessObjectHome(BO.ImportRecordRelationNew);

	@Override
	public final void importTask(List<List<ImportData>> dataList, Long taskId,BoSession session) {
		List<IImportRecordRelationNewRow> recList = new ArrayList<IImportRecordRelationNewRow>();
		Long sheetTotal = 0L;
		Long errorTotal = 0L;
		if (dataList != null && dataList.size() > 0) {
			sheetTotal = Long.valueOf(dataList.size());
		}
		// 100条提交一次数据
		for (int i = 0; i < dataList.size(); i += taskCount) {
			int lastIndex = dataList.size();
			if (lastIndex > (i + taskCount)) {
				lastIndex = i + taskCount - 1;
			}
			List<List<ImportData>> data = dataList.subList(i, lastIndex);
			/*
			 * BoSession localSession =
			 * AppWorkManager.getBoDataAccessManager().createLocalBoSession();
			 * BoTransactionManager tranxManager =
			 * AppWorkManager.getBoTransactionManager(); TransactionTracker
			 * tracker = null;
			 */
			try {
				// tracker = tranxManager.beginTransaction(localSession);
				//导入业务数据
				taskDo(taskId, data,session, recList);
				// tranxManager.commitTransaction(localSession, tracker);
			} catch (Exception e) {
				e.printStackTrace();
				/*
				 * //TODO 异常处理 if (tracker != null && localSession != null &&
				 * localSession.getTransaction()!=null&&localSession.
				 * getTransaction().isActive()) {
				 * tranxManager.rollbackTransaction(localSession); }
				 */
			}

		}
		//插入导入记录子表
		if(recList.size()>0){
			errorTotal = Long.valueOf(recList.size());
			IImportRecordRelationNewRowSet childRowSet = relationHome.createRowSet();
			for (IImportRecordRelationNewRow childRow : recList) {
				childRowSet.addRow(childRow);
			}
			relationHome.batchInsert(session,childRowSet);
		}
		//回写主表		
		updateImportNumField(taskId,sheetTotal,errorTotal);
	}
	
	public abstract void updateImportNumField(Long taskId,Long total,Long error);


	private void taskDo(Long taskId, List<List<ImportData>> dataRows, BoSession localSession,
			List<IImportRecordRelationNewRow> recList) {
		for (List<ImportData> dataRow : dataRows) {
			boolean error = commonVaild(dataRow,localSession);
			if (error == false) {
				importData(dataRow, localSession);
			}
			if (error || isImportError()) {
				IImportRecordRelationNewRow instanceRel = relationHome.createRow(localSession);
				instanceRel.setCont(getDataJsonForWeb(dataRow));
				instanceRel.setUserId(AppWorkManager.getCurrAppUserId());
				instanceRel.setParentId(taskId);
				instanceRel.setSheetType(getSheetNo());
				recList.add(instanceRel);
				continue;
			}
		}
	}

	protected String getDataJsonForWeb(List<ImportData> dataRow) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		for (ImportData data : dataRow) {
			dataMap.put(data.getField(), data);
		}
		return dataManager.toJSONString(dataMap);
	}

	public abstract Long getSheetNo();
	
	public abstract boolean isImportError();

	public abstract void importData(List<ImportData> data, BoSession localSession);

	public boolean specialFiledVaild(Map<String, ImportData> dataMap, BoSession localSession) {
		return false;
	}

	public boolean commonVaild(List<ImportData> data,BoSession localSession) {
		boolean error = false;
		boolean specialError = false;
		Map<String, ImportData> dataMap = new HashMap<String, ImportData>();
		for (ImportData dataRow : data) {
			String _value = dataRow.getValue();
			dataMap.put(dataRow.getField(), dataRow);
			Integer length = dataRow.getLength();
			String enumName = dataRow.getEnumName();
			String type = dataRow.getType();
			if (StringUtils.isEmpty(_value)) {
				continue;
			}
			// 验证长度
			if (StringUtils.isNotEmpty(_value)) {
				int _length = _value.length();
				if (length > 0 && _length > length) {
					dataRow.setErrorType(IPT.ERRORTYPE_TOLONG);
					dataRow.setMsg("字符不得超过" + length + "个字符");

				}
			}
			if (StringUtils.isNotEmpty(_value) && enumName != null && StringUtils.isNotEmpty(enumName)) {
				// 验证必要字段：枚举值
				String enumVal = ImportUtil.getEnumValue(enumName, _value);
				if (enumVal == null) {
					dataRow.setMsg(IPT.MSG_ENUM);
					dataRow.setErrorType(IPT.ERRORTYPE_ENUM);
					error = true;
				} else {
					dataRow.setInsertValue(enumVal);
				}
			}
			if (dataRow.getField().equals(SC.owner)) {
				List<Long> ownerIdList = ServiceLocator.getInstance().lookup(UserServiceItf.class)
						.getUserIdByName(dataRow.getValue());
				if (ownerIdList == null || ownerIdList.size() == 0) {
					dataRow.setMsg(IPT.MSG_NON);
					dataRow.setErrorType(IPT.ERRORTYPE_NON);
					error = true;
				} else if (ownerIdList.size() != 1) {
					dataRow.setMsg(IPT.MSG_MULTI);
					dataRow.setErrorType(IPT.ERRORTYPE_MULTI);
					error = true;
				} else {
					Long ownerId = ownerIdList.get(0);
					dataRow.setInsertValue(ownerId);
				}
			}

			Object value = ImportUtil.parseValue(type, _value);
			if (value == null) {
				dataRow.setMsg(IPT.MSG_FORMAT);
				dataRow.setErrorType(IPT.ERRORTYPE_FORMAT);
				error = true;
			}
		}
		specialError = specialFiledVaild(dataMap, localSession);
		return error || specialError;
	}

}
