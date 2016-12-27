package com.chanapp.chanjet.customer.restlet.v2.rest.invite;

import com.chanapp.chanjet.customer.service.cia.CiaServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 短链邀请用户
 * 
 * @author tds
 *
 */
public class InviteShort extends BaseRestlet {
    @Override
    public Object run() {
        return ServiceLocator.getInstance().lookup(CiaServiceItf.class).inviteShort(getPayload(),boDataAccessManager.getBoSession());
    }

}
