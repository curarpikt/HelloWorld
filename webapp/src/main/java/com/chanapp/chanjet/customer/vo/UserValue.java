package com.chanapp.chanjet.customer.vo;

import com.chanapp.chanjet.customer.constant.ROLE;
import com.chanapp.chanjet.customer.util.PinyinUtil;

public class UserValue {
    private String name;
    private String headPic;
    private String userRole;
    private String status;
    private String phone;
    private String mobile;
    private Long userLevel;
    private String email;
    private int index;
    private boolean selected;
    private Long id;
    private String fullSpell;
    private String shortSpell;
    private String headPicture;
    private Long customercount;
    private Long parentId;
    private Long lastModifiedDateOut;
    private Long userId;


    public Long getUserId() {
        return this.id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLastModifiedDateOut() {
        return lastModifiedDateOut;
    }

    public void setLastModifiedDateOut(Long lastModifiedDateOut) {
        this.lastModifiedDateOut = lastModifiedDateOut;
    }

    public String getHeadPicture() {
    	if(headPicture==null&&headPic!=null){
    		headPicture=this.headPic;
    	}
        return headPicture;
    }

    public void setHeadPicture(String headPicture) {
        this.headPicture = headPicture;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getMobile() {
        if (mobile == null)
            return "";
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Long getUserLevel() {
    	if(ROLE.SYSRELUSER_ROLE_BOSS.equals(this.userRole)||ROLE.SYSRELUSER_ROLE_MANAGER.equals(this.userRole))
    		return 1L;
    	if(ROLE.SYSRELUSER_ROLE_SUPERISOR.equals(this.userRole))
    		return 2L;
    	if(ROLE.SYSRELUSER_ROLE_SALESMAN.equals(this.userRole))
    		return 3L;
        return 0L;
    }
    
    public Long getOrigUserLevel() {
        return userLevel;
    }

    public void setUserLevel(Long userLevel) {
        this.userLevel = userLevel;
    }

    public Long getCustomercount() {
        return customercount;
    }

    public void setCustomercount(Long customercount) {
        this.customercount = customercount;
    }

    public String getHeadPic() {
    	if(headPic==null&&headPicture!=null){
    		headPic=this.headPicture;
    	}
        return headPic;
    }

    public void setHeadPic(String headPic) {
        this.headPic = headPic;
    }

    public String getPhone() {
        if (this.phone == null) {
            return getMobile();
        }
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullSpell() {
        return PinyinUtil.hanziToPinyinFull(name, false);
    }

    public String getFullSpellOrigin() {
        return fullSpell;
    }

    public void setFullSpell(String fullSpell) {
        this.fullSpell = fullSpell;
    }

    public String getShortSpell() {
        return PinyinUtil.hanziToPinyin(name, false);
    }

    public String getShortSpellOrigin() {
        return shortSpell;
    }

    public void setShortSpell(String shortSpell) {
        this.shortSpell = shortSpell;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
