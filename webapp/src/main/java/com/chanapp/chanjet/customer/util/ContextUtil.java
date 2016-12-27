package com.chanapp.chanjet.customer.util;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.service.regist.RegistServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.cia.Organization;
import com.chanjet.csp.ccs.api.cia.UserInfo;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

public class ContextUtil {
    private static ContextUtil _instance = null;
    private static String _serverName = null;
    private static String _orgId = null;
    private static String _orgName = null;
    private static final Logger logger = LoggerFactory.getLogger(ContextUtil.class);

    private static synchronized void syncInit() {
        if (_instance == null) {
            _instance = new ContextUtil();
        }
    }

    public static ContextUtil getInstance() {
        if (_instance == null) {
            syncInit();
        }
        return _instance;
    }

    public void setServerName(String serverName) {
        if (_serverName == null) {
            _serverName = serverName;
        }
    }

    public String getServerName() {
        return _serverName;
    }

    public void initOrgInfo() {
        if (_orgId == null) {
            String name = getServerName();
            if (name != null) {
                try {
                    int index = name.indexOf(".");
                    if (index != -1) {
                        name = name.substring(0, name.indexOf("."));
                        Map<String, Object> orgInfo = PortalUtil.getOrgInfoByAccount(name);
                        if (orgInfo != null) {
                            if (orgInfo.containsKey("orgFullName")) {
                                _orgName = orgInfo.get("orgFullName").toString();
                            }
                            if (orgInfo.containsKey("orgId")) {
                                _orgId = orgInfo.get("orgId").toString();
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("gerOrgId error", e);
                }
            }
        }
        if (_orgName == null) {
            try {
                if (_orgId == null) {
                    UserInfo user = EnterpriseContext.getCurrentUser();
                    if (user != null) {
                        _orgId = user.getOrgId();
                    }
                }
                if (_orgId != null) {
                    RegistServiceItf registService = ServiceLocator.getInstance().lookup(RegistServiceItf.class);
                    Map<String, Object> ciaOrgMap = registService.getOrganizationInfoByOrgId(_orgId);
                    if (ciaOrgMap != null && !ciaOrgMap.containsKey("errorCode")) {
                        _orgName = (String) ciaOrgMap.get("orgFullName");
                    }
                }
            } catch (Exception e) {
                logger.error("gerOrgNameFromCia error", e);
            }
        }
    }

    public String getOrgName() {
        UserInfo user = EnterpriseContext.getCurrentUser();
        if (user != null) {
            _orgId = user.getOrgId();
            List<Organization> orgs = user.getOrgList();
            if (orgs != null) {
                for (Organization org : orgs) {
                    if (_orgId.equals(org.getOrgId())) {
                        _orgName = org.getOrgFullName();
                        break;
                    }
                }
            }
        }
        if (_orgName == null) {
            initOrgInfo();
        }
        return _orgName;
    }

    public String getOrgId() {
        UserInfo user = EnterpriseContext.getCurrentUser();
        if (user != null) {
            _orgId = user.getOrgId();
        }
        if (_orgId == null) {
            initOrgInfo();
        }
        return _orgId;
    }
}
