package com.chanapp.chanjet.customer.service.sync;


import java.util.List;

public interface ISyncQueryData {
	/** 
	 * <p>
	 * 初始化下发的实体
	 * </p>
	 *
	 * @author : lf
	 * @date : 2015年9月7日
	 */
	public void initEntitys();
	/** 
	 * <p>
	 * 获取下发的实体 
	 * </p>
	 * @return
	 *
	 * @author : lf
	 * @date : 2015年9月7日
	 */
	public List<ISyncEntity> getSyncEntityList();
}
