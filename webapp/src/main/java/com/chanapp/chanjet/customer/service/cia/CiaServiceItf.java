package com.chanapp.chanjet.customer.service.cia;

import java.io.File;
import java.util.Map;

import com.chanapp.chanjet.web.service.BaseServiceItf;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.common.base.exception.AppException;

public interface CiaServiceItf extends BaseServiceItf {

    String getOrgFullName(String orgId);

    Map<String, Object> getOrganizationInfoByOrgId(String orgId);

    Map<String, Object> updateOrganizationName(String orgId, String orgName);

    Map<String, Object> records(Integer pageSize, Integer pageNo);

    Map<String, Object> send(String data,BoSession session);

    Map<String, Object> inviteShort(String data,BoSession session);

    Map<String, Object> findUserByIdentify(String identify);

    Map<String, Object> sendBindingActiveEmailOrMobile(String userId, String identify, String activeWay);

    Map<String, Object> findOrgPartnerAppRelationList(String orgId);

    Map<String, Object> fingPartnerInfoByUsername(String partnerId);

    Map<String, Object> bindingOrgPartnerAppRelation(String orgId, String partnerId);

    Map<String, Object> activeBindingEmailOrMobile(String userId, String activeCode, String activeWay,
            String activeType);

    Map<String, Object> resetThridPlatformUserPwd(String pwd, String passwordLevel);

    Map<String, Object> getUserInfoFromCia();

    Map<String, Object> updateHeadPicture(File headPic);

	Map<String, Object> cancelAppManager(Long userId) throws AppException;

	Map<String, Object> addAppManager(Long userId) throws AppException;

}
