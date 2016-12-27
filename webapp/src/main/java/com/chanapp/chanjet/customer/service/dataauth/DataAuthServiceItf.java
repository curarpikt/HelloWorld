package com.chanapp.chanjet.customer.service.dataauth;

import com.chanapp.chanjet.web.service.BaseServiceItf;

public interface DataAuthServiceItf extends BaseServiceItf {
    void removeDataAuth(Long userId);

    void checkAndUpdateDataAuth(Long userId, String privi);

    void buildAffiliateByDisableUser(Long userId);

/*    void updateSysUserByDisableUser(Long userId);*/
}
