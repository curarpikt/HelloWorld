package com.chanapp.chanjet.customer.vo.system;

public class AppUser {
	  private Long id;
	  private String appId;
	  private Boolean isActive;
	  private Boolean isAppSuperUser;
	  private User user;

	  public Long getId() {
	    return id;
	  }

	  public void setId(Long id) {
	    this.id = id;
	  }

	  public String getAppId() {
	    return appId;
	  }

	  public void setAppId(String appId) {
	    this.appId = appId;
	  }

	  public Boolean getIsActive() {
	    return isActive;
	  }

	  public void setIsActive(Boolean isActive) {
	    this.isActive = isActive;
	  }

	  public Boolean getIsAppSuperUser() {
	    return isAppSuperUser;
	  }

	  public void setIsAppSuperUser(Boolean isAppSuperUser) {
	    this.isAppSuperUser = isAppSuperUser;
	  }

	  public User getUser() {
	    return user;
	  }

	  public void setUser(User user) {
	    this.user = user;
	  }
	}
