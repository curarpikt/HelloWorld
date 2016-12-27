package com.chanapp.chanjet.customer.restlet.v2.rest.workrecord;

import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRow;
import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;

public class ConvertInfo extends BaseRestlet{

	@Override
	public Object run() {
		Long id = this.getParamAsLong("id");
		Long relateToID = this.getParamAsLong("relateToID");
		String relateToType = this.getParam("relateToType");
		if (relateToID == null || relateToID == 0L  || StringUtils.isEmpty(relateToType) || id == null || id == 0L ) {
			throw new AppException("app.common.params.invalid");
		}
		IAttachmentRow attachment =(IAttachmentRow)ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).findByIdWithAuth(id);
		if(attachment == null){
			throw new AppException("app.common.server.error");
		}
		String url = attachment.getFileDir();
		String orgId = EnterpriseContext.getOrgId();
		attachment.getCategory();//"audio"
		if(!attachment.getRelateToType().equals(relateToType) && !attachment.getRelateToID().equals(relateToID)){
			throw new AppException("app.common.server.invalid.request");
		}
		url = url.substring(url.indexOf(orgId) + orgId.length());
		return url;
	}

}
