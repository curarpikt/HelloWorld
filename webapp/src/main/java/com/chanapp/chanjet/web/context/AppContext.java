package com.chanapp.chanjet.web.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.common.base.util.ExceptionUtils;

public class AppContext {
  private AppContext() {
  }

  private static final Logger logger = LoggerFactory.getLogger(AppContext.class);

  public static Object getRequestScopeAttribute(String key) {
    return AppWorkManager.getRequestCacheValue(key);
  }

  public static void setRequestScopeAttribute(String key, Object value) {
    AppWorkManager.setRequestCache(key, value);
  }

  public static void removeRequestScopeAttribute(String key) {
    Object value = getRequestScopeAttribute(key);
    if (value != null) {
      value = null;
    }
    AppWorkManager.setRequestCache(key, null);
  }

  private static final String CONTEXT_SESSION_KEY = "$$CONTEXT_SESSION_KEY$$";

  public static BoSession session() {
    BoSession session = null;
    Object osession = getRequestScopeAttribute(CONTEXT_SESSION_KEY);
    if (osession == null) {
      session = getWebContextSession();
      initSession(session);
    } else {
      session = (BoSession) osession;
    }
    return session;
  }

  public static void initSession() {
    initSession(getWebContextSession());
  }

  public static void initSession(BoSession session) {
    setRequestScopeAttribute(CONTEXT_SESSION_KEY, session);
  }

  public static void clearSession() {
    Object osession = getRequestScopeAttribute(CONTEXT_SESSION_KEY);
    if (osession != null) {
      BoSession session = (BoSession) osession;
      try {
        if (session.isOpen()) {
          AppWorkManager.getBoDataAccessManager().closeBoSession(session);
        }
      } catch (Exception e) {
        logger.error(ExceptionUtils.convertExceptionStackToString(e));
      }
      removeRequestScopeAttribute(CONTEXT_SESSION_KEY);
    }
  }

  public static BoSession getWebContextSession() {
    return AppWorkManager.getBoDataAccessManager().getBoSession();
  }

}
