package com.chanapp.chanjet.customer.service.sync;

import java.util.Map;

import com.chanapp.chanjet.web.service.BaseServiceItf;

/**
 * @author tds
 *
 */
public interface SyncServiceItf extends BaseServiceItf {
    Map<String, Object> loadCsvData(String SyncData);

    Map<String, Object> syncSave(String SyncData);
}
