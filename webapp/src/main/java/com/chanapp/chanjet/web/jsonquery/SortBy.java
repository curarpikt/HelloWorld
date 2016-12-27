package com.chanapp.chanjet.web.jsonquery;

public class SortBy {
    public static final String DESC = "Descending";
    public static final String ASC = "Ascending";

    private String FieldName = "";
    private String Order = "";

    public SortBy(String FieldName, String Order) {
        this.FieldName = FieldName;
        this.Order = Order;
    }

    /**
     * @return the fieldName
     */
    public String getFieldName() {
        return FieldName;
    }

    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName) {
        FieldName = fieldName;
    }

    /**
     * @return the order
     */
    public String getOrder() {
        return Order;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(String order) {
        Order = order;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((FieldName == null) ? 0 : FieldName.hashCode());
        result = prime * result + ((Order == null) ? 0 : Order.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SortBy)) {
            return false;
        }
        SortBy other = (SortBy) obj;
        if (FieldName == null) {
            if (other.FieldName != null) {
                return false;
            }
        } else if (!FieldName.equals(other.FieldName)) {
            return false;
        }
        if (Order == null) {
            if (other.Order != null) {
                return false;
            }
        } else if (!Order.equals(other.Order)) {
            return false;
        }
        return true;
    }
}
