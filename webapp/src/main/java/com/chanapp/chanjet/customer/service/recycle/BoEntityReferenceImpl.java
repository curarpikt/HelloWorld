package com.chanapp.chanjet.customer.service.recycle;

import java.util.ArrayList;
import java.util.List;

import com.chanapp.chanjet.web.reader.JsonReader;
import com.chanjet.csp.common.base.json.JSONArray;
import com.chanjet.csp.common.base.json.JSONObject;

public class BoEntityReferenceImpl implements BoEntityReference {
  final static JsonReader reader = JsonReader.getInstance("recycle.json");
  final static String FIELD_ID = "id";

  @Override
  public List<BoEntityRelationship> getSourcesOf(String boName) {
    List<BoEntityRelationship> relations = new ArrayList<>();

    JSONArray _relations = reader.getJSONArray(boName);
    if (_relations == null) {
      return relations;
    }

    for (Object _relation : _relations) {
      JSONObject _relationo = (JSONObject) _relation;

      BoEntityRelationship relation = new BoEntityRelationship();
      relation.setSourceBo(_relationo.optString("SourceBo"));
      relation.setSourceField(_relationo.optString("SourceField"));
      relation.setTargetBo(boName);
      relation.setTargetField(FIELD_ID);

      relations.add(relation);
    }

    return relations;
  }

  @Override
  public List<BoEntityRelationship> getTargetsOf(String boName) {
    List<BoEntityRelationship> relations = new ArrayList<>();

    JSONObject json = reader.get();
    if (json == null) {
      return relations;
    }

    for (String targetBo : json.keySet()) {
      JSONArray _relations = json.getJSONArray(targetBo);
      for (Object _relation : _relations) {
        JSONObject _relationo = (JSONObject) _relation;
        String sourceBo = _relationo.optString("SourceBo");
        if (sourceBo.equals(boName)) {
          BoEntityRelationship relation = new BoEntityRelationship();

          relation.setSourceBo(boName);
          relation.setSourceField(_relationo.optString("SourceField"));
          relation.setTargetBo(targetBo);
          relation.setTargetField(FIELD_ID);

          relations.add(relation);
        }
      }
    }

    return relations;
  }

}
