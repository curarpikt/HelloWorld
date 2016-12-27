package com.chanapp.chanjet.customer.eventhandler.manager;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.metadata.constants.BONames;
import com.chanapp.chanjet.customer.eventhandler.deletehandler.BoDeleteHandler;
import com.chanapp.chanjet.customer.eventhandler.deletehandler.ContactDeleteHandler;
import com.chanapp.chanjet.customer.eventhandler.deletehandler.CustomerDeleteHandler;
import com.chanapp.chanjet.customer.eventhandler.deletehandler.DefaultBoDeleteHandler;

public class BoDeleteHandlerManager {
  private final Map<String, BoDeleteHandler> handlers = new HashMap<>();

  private static class BoDeleteHandlerManagerHolder {
    private static final BoDeleteHandlerManager INSTANCE = new BoDeleteHandlerManager();
  }

  private BoDeleteHandlerManager() {
    init();
  }

  public static BoDeleteHandlerManager getInstance() {
    return BoDeleteHandlerManagerHolder.INSTANCE;
  }

  private void init() {
    handlers.put(BONames.Customer, new CustomerDeleteHandler());
    handlers.put(BONames.Contact, new ContactDeleteHandler());
  }

  public BoDeleteHandler getHandler(String boName) {
    return handlers.containsKey(boName) ? handlers.get(boName) : new DefaultBoDeleteHandler();
  }
}
