package com.chanapp.chanjet.customer.vo.system;

import java.util.Set;

public class User {
	  private Long id;
	  private Long userId;
	  private String name;
	  private String userName;
	  private String headPicture;
	  private String email;
	  private String mobile;
	  private Boolean isSuperUser;
	  private boolean isActive;
	  private Set<UserRole> userRoles;

	  public Long getId() {
	    return id;
	  }

	  public void setId(Long id) {
	    this.id = id;
	  }

	  public Long getUserId() {
	    return userId;
	  }

	  public void setUserId(Long userId) {
	    this.userId = userId;
	  }

	  public String getName() {
	    return name;
	  }

	  public void setName(String name) {
	    this.name = name;
	  }

	  public String getUserName() {
	    return userName;
	  }

	  public void setUserName(String userName) {
	    this.userName = userName;
	  }

	  public String getHeadPicture() {
	    return headPicture;
	  }

	  public void setHeadPicture(String headPicture) {
	    this.headPicture = headPicture;
	  }

	  public String getEmail() {
	    return email;
	  }

	  public void setEmail(String email) {
	    this.email = email;
	  }

	  public String getMobile() {
	    return mobile;
	  }

	  public void setMobile(String mobile) {
	    this.mobile = mobile;
	  }

	  public Boolean isSuperUser() {
	    return isSuperUser;
	  }

	  public void setSuperUser(Boolean isSuperUser) {
	    this.isSuperUser = isSuperUser;
	  }

	  public boolean isActive() {
	    return isActive;
	  }

	  public void setActive(boolean isActive) {
	    this.isActive = isActive;
	  }

	  public Set<UserRole> getUserRoles() {
	    return userRoles;
	  }

	  public void setUserRoles(Set<UserRole> userRoles) {
	    this.userRoles = userRoles;
	  }

	}
