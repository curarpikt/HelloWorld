package com.chanapp.chanjet.customer.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRow;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.customer.vo.system.User;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.cmr.api.metadata.base.field.generated.FieldTypeEnum;
import com.chanjet.csp.cmr.api.metadata.userschema.type.appCSPEnums.ICSPEnum;
import com.chanjet.csp.cmr.api.metadata.userschema.type.businessObject.field.IBOField;
import com.chanjet.csp.cmr.api.metadata.userschema.type.businessObject.field.IForeignKeyBOField;
import com.chanjet.csp.common.base.usertype.AppBaseEntity;
import com.chanjet.csp.common.base.usertype.DynamicEnum;
import com.chanjet.csp.common.base.usertype.EmailAccount;
import com.chanjet.csp.common.base.usertype.GeoPoint;
import com.chanjet.csp.common.base.usertype.MobilePhone;
import com.chanjet.csp.common.base.usertype.Percentage;
import com.chanjet.csp.common.base.usertype.Phone;
import com.chanjet.csp.common.base.util.StringUtils;

@SuppressWarnings("unchecked")
public class BoRowConvertUtil {

    public static final int MAX_LEVEL = 2;

    /**
     * 把boRow转换为前端需要的map，特殊处理了Customer/Contact/User/Enum
     * 
     * @param boRow
     * @return
     */
    public static Row toRow(IBusinessObjectRow boRow) {
        return toRow(boRow, MAX_LEVEL);
    }

    public static Row toRow(IBusinessObjectRow boRow, int maxLevel) {
        return toRow(boRow, 1, maxLevel);
    }

    private static Row toRow(IBusinessObjectRow boRow, int level, int maxLevel) {
        Row row = new Row();
        Map<String, IBOField> fields = boRow.getDefinition().getFields();
        for (String field : fields.keySet()) {
            IBOField iField = fields.get(field);
            Object outvalue = boRow.getFieldValue(iField.getName());
            if("permanentKey".equals(field))
            	continue;
            Object value = null;
            if (outvalue != null && outvalue.getClass().getSimpleName().equals("DynAttrValue")) {
                value = ReflectionUtil.getFieldValue(outvalue, "dynAttrValue");
            } else {
                value = outvalue;
            }
            String strValue = null;
            try {
                if (value != null) {
                    strValue = value.toString();
                }
            } catch (Exception e) {
                strValue = "test";
                e.printStackTrace();
            }
            if (value == null || StringUtils.isEmpty(strValue)) {
                continue;
            }
            if (value.equals("null")) {
                row.put(field, value);
                continue;
            }

            FieldTypeEnum fieldTypeEnum = iField.getType();
            if (iField.getName().equals("createdBy") || iField.getName().equals("lastModifiedBy")
                    || iField.getName().equals("owner")||iField.getName().equals("customerOwner")) {
                if (value != null) {
                    User user = null;
                    if (value instanceof User) {
                        user = (User) value;
                    } else if (value instanceof Long) {
                        if (level < maxLevel) {                      
                            user = EnterpriseUtil.getUserNameAndHeadPicById((Long) value);
                        } else {
                            row.put(field, user);
                            continue;
                        }
                    } else if (value instanceof Map) {
                        Object _id = ((HashMap<String, Object>) value).get(SC.id);
                        Long id = null;
                        if (!(_id == null || "".equals(_id) || "null".equalsIgnoreCase(String.valueOf(_id)))) {
                            id = Long.parseLong(String.valueOf(_id));
                        }
                        if (id != null) {
                            if (level < maxLevel) {
                                user = EnterpriseUtil.getUserNameAndHeadPicById(id);
                            } else {
                                Map<String, Object> userRow = new HashMap<String, Object>();
                                userRow.put("id", id);
                                row.put(field, userRow);
                                continue;
                            }
                        }
                    }
                    if (user != null) {
                        Map<String, Object> userRow = new HashMap<String, Object>();
                        userRow.put("id", user.getId());
                        String name = user.getName();
                        userRow.put("name", StringUtils.isEmpty(name) ? "" : name);
                        userRow.put("headPicture",
                                StringUtils.isEmpty(user.getHeadPicture()) ? "" : user.getHeadPicture());
                        userRow.put("headPictrue",
                                StringUtils.isEmpty(user.getHeadPicture()) ? "" : user.getHeadPicture());
                        row.put(field, userRow);
                    }

                }
                continue;
            }

            switch (fieldTypeEnum) {
                case PRIMARY_KEY:
                    row.put(field, value);
                    break;
                case INTEGER:
                    Long number = Long.parseLong(value.toString());
                    row.put(field, number);
                    break;
                case PHONE:
                    Phone phone = (Phone) value;
                    row.put(field, phone.getPhoneNumber());
                    break;
                case EMAIL:
                    EmailAccount email = (EmailAccount) value;
                    row.put(field, email.getAccountId());
                    break;
                case MOBILE_PHONE:
                    MobilePhone mPhone = (MobilePhone) value;
                    row.put(field, mPhone.getPhoneNumber());
                    break;
                case PERCENTAGE:
                    Percentage percentage = (Percentage) value;
                    row.put(field, String.valueOf(percentage.getPercentValue()));
                    break;
                case DATE:
                    Date datestamp = (Date) value;
                    row.put(field, DateUtil.getDateString(datestamp));
                    break;
                case TIMESTAMP:
                    Timestamp timestamp = (Timestamp) value;
                    row.put(field, timestamp.getTime());
                    break;
                case FOREIGN_KEY:
                    if (level < maxLevel) {
                        IForeignKeyBOField iFKField = (IForeignKeyBOField) iField;
                        String refEntityName = iFKField.getRelationship().getSourceEntityName();
                        if (refEntityName.equals("Customer")) {
                            Map<String, Object> _Row = new HashMap<String, Object>();
                            if (value instanceof ICustomerRow) {
                                ICustomerRow customer = (ICustomerRow) value;
                                _Row.put("id", customer.getId());
                                _Row.put("name", StringUtils.isEmpty(customer.getName()) ? "" : customer.getName());
                                row.put(field, _Row);
                            } else {
                                if (value != null) {
                                    Long fkId = 0L;
                                    if ((value instanceof AppBaseEntity)) {
                                        fkId = ((AppBaseEntity) value).getId();
                                    } else if ((value instanceof Long)) {
                                        fkId = (Long) value;
                                    } else if ((value instanceof Integer)) {
                                        fkId = Long.valueOf(((Integer) value).longValue());
                                    } else if ((value instanceof Map)) {
                                        fkId = (Long) ((Map<?, ?>) value).get("id");
                                    }

                                    if (fkId > 0L) {
                                        ICustomerRow customer = ServiceLocator.getInstance()
                                                .lookup(CustomerServiceItf.class).query(fkId);
                                        if(customer!=null){
                                            _Row.put("id", fkId);
                                            _Row.put("name", StringUtils.isEmpty(customer.getName()) ? "" : customer.getName());
                                        }else if ((value instanceof Map)) {                                        
                                            _Row.put("id", fkId); 
                                            if(((Map<?, ?>) value).get("name")!=null){
                                            	  _Row.put("name", ((Map<?, ?>) value).get("name")); 
                                            }
                                        }                             
                                    }
                                }
                            }
                            row.put(field, _Row);
                        } else if (refEntityName.equals("Contact")) {
                            Map<String, Object> _Row = new HashMap<String, Object>();
                            if (value instanceof IContactRow) {
                                IContactRow contact = (IContactRow) value;
                                _Row.put("id", contact.getId());
                                _Row.put("name", StringUtils.isEmpty(contact.getName()) ? "" : contact.getName());
                                row.put(field, _Row);
                            } else {
                                if (value != null) {
                                    Long fkId = 0L;
                                    if ((value instanceof AppBaseEntity)) {
                                        fkId = ((AppBaseEntity) value).getId();
                                    } else if ((value instanceof Long)) {
                                        fkId = (Long) value;
                                    } else if ((value instanceof Integer)) {
                                        fkId = Long.valueOf(((Integer) value).longValue());
                                    } else if ((value instanceof Map)) {
                                        fkId = (Long) ((Map<?, ?>) value).get("id");
                                    }

                                    if (fkId > 0L) {
                                        IContactRow contact = ServiceLocator.getInstance()
                                                .lookup(ContactServiceItf.class).query(fkId);
                                        _Row.put("id", fkId);
                                        _Row.put("name",
                                                StringUtils.isEmpty(contact.getName()) ? "" : contact.getName());
                                    }
                                }
                            }
                            row.put(field, _Row);
                        }
                        else {
                            if (value != null) {
                                Long id = null;
                                IBusinessObjectRow data = null;
                                if (value instanceof Map) {
                                    Object _id = ((HashMap<String, Object>) value).get(SC.id);
                                    if (!(_id == null || "".equals(_id)
                                            || "null".equalsIgnoreCase(String.valueOf(_id)))) {
                                        id = Long.parseLong(String.valueOf(_id));
                                    }
                                } else if (value instanceof AppBaseEntity) {
                                    id = ((AppBaseEntity) value).getId();
                                }
                                if (id != null) {
                                    data = ServiceLocator.getInstance().lookup(refEntityName).query(id);
                                }

                                if (data != null) {
                                    row.put(field, toRow(data, level + 1, maxLevel));
                                }
                            }
                        }
                    } else {
                        if (value instanceof Map) {
                            row.put(field, ((Map<?, ?>) value).get(SC.id));
                        } else {
                            row.put(field, ReflectionUtil.getFieldValueByMethod(value, SC.id));
                        }
                    }
                    break;
                case TEXT:
                    row.put(field, value);
                    break;
                case GEOPOINT:
                    GeoPoint coordinate = (GeoPoint) value;
                    Map<String, Object> coordinateRow = new HashMap<String, Object>();
                    coordinateRow.put("longitude", coordinate.getLongitude());
                    coordinateRow.put("latitude", coordinate.getLatitude());
                    row.put(field, coordinateRow);
                    break;
                case CSP_ENUM:
                    DynamicEnum dynamicEnum = (DynamicEnum) value;
                    Map<String, Object> enumRow = new HashMap<String, Object>();
                    if (dynamicEnum != null) {
                        ICSPEnum cspEnum = AppWorkManager.getAppMetadataManager()
                                .getCSPEnumByName(dynamicEnum.getCspEnumName());
                        if (cspEnum.getEnumValue(dynamicEnum.getValue()) != null) {
                            enumRow.put("value", dynamicEnum.getValue());
                            enumRow.put("label", cspEnum.getEnumValue(dynamicEnum.getValue()).getLabel());
                        } else {
                            enumRow.put("value", dynamicEnum.getValue());
                            enumRow.put("label", dynamicEnum.getLabel());
                        }
                        row.put(field, enumRow);
                    }
                    break;
                case DECIMAL:
                default:
                    row.put(field, value);
                    break;
            }
        }
        return row;
    }

    /**
     * 把boRowList转换为前端需要的List<map>，特殊处理了Customer/Contact/User/Enum
     * 
     * @param boRows
     * @param maxLevel
     * @return
     */
    public static List<Row> toRowList(List<? extends IBusinessObjectRow> boRows, int maxLevel) {
        List<Row> rowList = new ArrayList<Row>();
        if (null != boRows && boRows.size() > 0) {
            for (IBusinessObjectRow boRow : boRows) {
                rowList.add(toRow(boRow, 1, maxLevel));
            }
        }
        return rowList;
    }
   
    public static List<Row> toRowList(List<? extends IBusinessObjectRow> boRows) {
        return toRowList(boRows, MAX_LEVEL);
    }

    public static RowSet toRowSet(IBusinessObjectRowSet boRows) {
        return toRowSet(boRows, MAX_LEVEL);
    }

    /**
     * 把boRowList转换为前端需要的RowSet，特殊处理了Customer/Contact/User/Enum
     * 
     * @param boRows
     * @param maxLevel
     * @return
     */
    public static RowSet toRowSet(IBusinessObjectRowSet boRows, int maxLevel) {
        RowSet rowSet = new RowSet();
        if (null != boRows && boRows.size() > 0) {
            for (IBusinessObjectRow boRow : boRows.getRows()) {
                rowSet.add(toRow(boRow, 1, maxLevel));
            }
        }
        return rowSet;
    }

    /**
     * 用户信息的格式转换。 visitCount方法中将转换后的用户信息返回给端。
     */
    public static void userValue2Map(Map<Long, Map<String, Object>> userInfo, UserValue value) {
        Map<String, Object> tmp = new HashMap<String, Object>();
        tmp.put("email", value.getEmail());
        tmp.put("mobile", value.getMobile());
        tmp.put("headPicture", value.getHeadPicture());
        tmp.put("name", value.getName());
        tmp.put("parentId", value.getParentId());
        tmp.put("userId", value.getId());
        tmp.put("userRole", value.getUserRole());
        tmp.put("userLevel", value.getUserLevel());
        tmp.put("status", value.getStatus());
        userInfo.put(value.getId(), tmp);
    }
    
    public static Row userValue2Row(UserValue value) {
        Row tmp = new Row();
        tmp.put("email", value.getEmail());
        tmp.put("mobile", value.getMobile());
        tmp.put("headPicture", value.getHeadPicture());
        tmp.put("name", value.getName());
        tmp.put("parentId", value.getParentId());
        tmp.put("userId", value.getId());
        tmp.put("id", value.getId());
        tmp.put("userRole", value.getUserRole());
        tmp.put("userLevel", value.getUserLevel());
        tmp.put("status", value.getStatus());
        return tmp;
    }
}
