package com.chanapp.chanjet.customer.constant;

/**
 * @author tds
 *
 */
public interface PV {
    int NO_PRVILEGE = Integer.valueOf("000", 2);

    int SELECT_PRVILEGE = Integer.valueOf("001", 2);

    int UPDATE_PRVILEGE = Integer.valueOf("111", 2);

    String SELF_PRIVI = "OWNER";
    String ALL_PRIVI = "ALL";

    String PRIVI_SETUP = "SETUP";
    String PRIVI_NORMAL = "NORMAL";
}
