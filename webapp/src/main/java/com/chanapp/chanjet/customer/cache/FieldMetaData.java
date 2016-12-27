package com.chanapp.chanjet.customer.cache;

import com.chanjet.csp.cmr.api.metadata.base.field.generated.FieldTypeEnum;

/**
 * UI需要的字段元数据
 * 
 * @author wolf
 *
 */
public class FieldMetaData {

    public String name;// 属性的名称
    public String label;// 属性显示标签
    public String remark;// 字段说明
    public FieldTypeEnum type; // 属性类型
    public String defaultValue = "";// 属性的默认值
    public Boolean editable = true;// 属性是否可编辑
    public int length = 0;// 属性允许输入的字符数，可选
    public int precision = 2;// 数值属性显示的精度，只有数值类有用
    public String enumName = "";// 枚举属性的枚举值名称
    public String picker = "";// 引用属性的Picker名称

    /**
     * 构造方法
     * 
     * @param name 字段名称
     * @param label 字段label
     * @param type 字段类型
     */
    public FieldMetaData(String name, String label, FieldTypeEnum type) {
        this.name = name;
        this.label = label;
        this.type = type;
    }
}
