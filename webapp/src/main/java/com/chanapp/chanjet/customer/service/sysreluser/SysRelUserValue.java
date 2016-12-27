package com.chanapp.chanjet.customer.service.sysreluser;

import java.io.Serializable;


public class SysRelUserValue implements Serializable {

    public String getHeadPictrue() {
        return headPictrue;
    }

    public void setHeadPictrue(String headPictrue) {
        this.headPictrue = headPictrue;
    }

    private static final long serialVersionUID = -4531975709287879107L;
    private Long id;
    private String status;
    private String userRole;
    /**
     * user表主键
     */
    private Long userSerialNo;
    private Long userId;
    private Long userLevel;
    /**
     * 父级用户ID
     */
    private Long parentId;

    // user
    private Long version;
    private String position;
    private Long sex;
    private String remark;
    private Boolean isSeeddata;
    private String education;
    private Boolean superUser;
    private String username;
    private String weixin;
    private String nickName;
    // private String objectId;
    private String name;
    private String zipCode;
    private String qq;
    private String headPicture;
    private String major;
    private String idCard;
    private String msn;
    private String website;
    private String hobby;
    private String graduateSchool;

    private Long birthday;
    private Long createTime;
    private Long createdDate;
    private Long modifyTime;
    private Long graduationYear;
    private Long joinTime;
    private Long lastModifiedDate;

    private Boolean isDeleted;
    private String officePhone;
    private String institute;
    private Long userChanjetId;
    private String createdByApp;
    private String address;
    private String email;
    private String weibo;
    private String mobile;
    private String headPictrue;

    private String fullSpell;
    private String simpleSpell;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Long getUserSerialNo() {
        return userSerialNo;
    }

    public void setUserSerialNo(Long userSerialNo) {
        this.userSerialNo = userSerialNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(Long userLevel) {
        this.userLevel = userLevel;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }


    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Long getBirthday() {
        return birthday;
    }

    public void setBirthday(Long birthday) {
        this.birthday = birthday;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getSex() {
        return sex;
    }

    public void setSex(Long sex) {
        this.sex = sex;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getIsSeeddata() {
        return isSeeddata;
    }

    public void setIsSeeddata(Boolean isSeeddata) {
        this.isSeeddata = isSeeddata;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public Boolean getSuperUser() {
        return superUser;
    }

    public void setSuperUser(Boolean superUser) {
        this.superUser = superUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWeixin() {
        return weixin;
    }

    public void setWeixin(String weixin) {
        this.weixin = weixin;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public Long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getHeadPicture() {
        return headPicture;
    }

    public void setHeadPicture(String headPicture) {
        this.headPicture = headPicture;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getMsn() {
        return msn;
    }

    public void setMsn(String msn) {
        this.msn = msn;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public String getGraduateSchool() {
        return graduateSchool;
    }

    public void setGraduateSchool(String graduateSchool) {
        this.graduateSchool = graduateSchool;
    }

    public Long getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Long graduationYear) {
        this.graduationYear = graduationYear;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public Long getUserChanjetId() {
        return userChanjetId;
    }

    public void setUserChanjetId(Long userChanjetId) {
        this.userChanjetId = userChanjetId;
    }

    public String getCreatedByApp() {
        return createdByApp;
    }

    public void setCreatedByApp(String createdByApp) {
        this.createdByApp = createdByApp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Long joinTime) {
        this.joinTime = joinTime;
    }

    public Long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getWeibo() {
        return weibo;
    }

    public void setWeibo(String weibo) {
        this.weibo = weibo;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFullSpell() {
        return fullSpell;
    }

    public void setFullSpell(String fullSpell) {
        this.fullSpell = fullSpell;
    }

    public String getSimpleSpell() {
        return simpleSpell;
    }

    public void setSimpleSpell(String simpleSpell) {
        this.simpleSpell = simpleSpell;
    }

}
