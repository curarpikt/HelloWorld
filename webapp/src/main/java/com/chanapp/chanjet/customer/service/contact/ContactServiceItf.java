package com.chanapp.chanjet.customer.service.contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.contact.IContactHome;
import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRow;
import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRowSet;
import com.chanapp.chanjet.customer.vo.PageRestObject;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;

public interface ContactServiceItf extends BoBaseServiceItf<IContactHome, IContactRow, IContactRowSet> {
    IContactRow addContactForCustomer(LinkedHashMap<String, Object> contactParam, Long customerId);

    void delByCustomerId(Long customerId);

    List<Long> getCustomerIdByContactSearch(String searchValue);

    Map<Long, Object> getDelNumByCustomerId(List<Long> customerIds);

    HashMap<Long, ArrayList<Row>> findContactByCustomerIds(List<Long> ids);

    IContactRowSet findContactsByCustomer(Long customerId);

    Map<String, Object> addContact(LinkedHashMap<String, Object> contactParam);

    Row updateContact(LinkedHashMap<String, Object> contactParam);

    void deleteContact(Long contactId);

    Row getContact(Long contactId);

    IContactRowSet findByCustomrIds(List<Long> customerids);

    IContactRowSet getContactDeleted();

    Integer countAll();

    IBusinessObjectRowSet findAllWithPage(PageRestObject page);

    Map<String, Object> getContactsByMobileAndPhone(String mobile, String phone);

    Map<String, Object> scanCard(LinkedHashMap<String, Object> contactParam);

    List<Long> getIdListByCustomerIdList(List<Long> ids);

    IContactRow upsertContact(LinkedHashMap<String, Object> contact);

    Map<String, Object> addContactWithMerge(LinkedHashMap<String, Object> contactValue);
}
