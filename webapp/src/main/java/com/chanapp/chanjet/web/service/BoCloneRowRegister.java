package com.chanapp.chanjet.web.service;

import java.util.HashSet;
import java.util.Set;

public class BoCloneRowRegister {
  final static Set<String> bos = new HashSet<String>();

  public static void register(String... boNames) {
    for (String boName : boNames) {
      bos.add(boName);
    }
  }

  public static boolean isCloneOrigRow(String boName) {
    return bos.contains(boName);
  }
}