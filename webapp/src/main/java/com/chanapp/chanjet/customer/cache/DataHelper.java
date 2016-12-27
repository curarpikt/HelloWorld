package com.chanapp.chanjet.customer.cache;

import java.util.Locale;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.cmr.api.metadata.base.field.generated.FieldTypeEnum;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.ICSPEnumField;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.IField;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.IForeignKeyField;

/**
 * 数据处辅助工具
 * 
 * @author wolf
 * 
 */
public class DataHelper {
    private static Locale cruSystemLocale = AppWorkManager.getAppMetadataManager().getEnterprise().getLocale();

    /**
     * 将字段的系统元数据定义转换成UI需要的元数据对象
     * 
     * @param field 系统元数据定义
     * @return 转换后的元数据对象
     */

    public static FieldMetaData convertFieldMetadata(IField field) {
        if (field == null) {
            return null;
        }

        FieldTypeEnum ft = field.getType();
        FieldMetaData metaObj = new FieldMetaData(field.getName(), field.getLabel(cruSystemLocale), ft);
        // metaObj.defaultValue = field.getDefaultValue();
        metaObj.defaultValue = null;
        // 取editable ui hint (没有设置，认为是true)
        String editable = field.getUiHint("editable");
        metaObj.editable = (editable != null && editable.equals("false")) ? false : true;

        metaObj.remark = field.getDescription();

        metaObj.length = 0;

        switch (metaObj.type) {
            case TEXT:
            case QQ_ID:
            case FACEBOOK_ID:
            case TWITTER_ID:
            case WECHAT_ID:
            case YAMMER_ID:
            case GOOGLEPLUS_ID:
            case LINKEDIN_ID:
            case WEIBO_ID:
            case RTF_TEXT:
                metaObj.length = 1000;
                break;
            case CSP_ENUM:
                ICSPEnumField enumMeta = (ICSPEnumField) field;
                metaObj.enumName = enumMeta.getCSPEnumName();
                break;
            case PERCENTAGE:
                metaObj.precision = 2;
                metaObj.length = 14;
                break;
            case DECIMAL:
                metaObj.precision = 2;
                metaObj.length = 14;
                break;
            case FOREIGN_KEY:
                // 取uihint中的picker定义，如果没有，由使用引用EO名称
                String picker = field.getUiHint("picker");
                if (picker == null || picker.isEmpty()) {
                    IForeignKeyField fkMeta = (IForeignKeyField) field;
                    picker = fkMeta.getRelationship().getSourceEntityName();
                }
                metaObj.picker = picker;
                break;
            default:
        }
        return metaObj;
    }
}
