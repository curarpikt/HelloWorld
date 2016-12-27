package com.chanapp.chanjet.customer.handler;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import java.util.ArrayList;
import java.util.List;

import com.chanapp.chanjet.customer.businessobject.api.metadata.constants.AttachmentFieldName;
import com.chanapp.chanjet.customer.businessobject.api.metadata.constants.BONames;
import com.chanapp.chanjet.customer.businessobject.api.metadata.constants.CheckinFieldName;
import com.chanapp.chanjet.customer.businessobject.api.metadata.constants.CommentFieldName;
import com.chanapp.chanjet.customer.gql.fetcher.BOPrivilegeFetcher;
import com.chanapp.chanjet.customer.gql.fetcher.ReferenceBOFetcher;
import com.chanjet.csp.graphql.GqlAppHandler;
import com.chanjet.csp.graphql.GqlSchemaGenerator;
import com.chanjet.csp.graphql.datafetcher.FetchChildCountByJsonQuerySpec;
import com.chanjet.csp.graphql.datafetcher.FetchChildListByJsonQuerySpec;
import com.chanjet.system.systemapp.businessobject.api.metadata.constants.CSPGrantFieldName;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLTypeReference;

public class CustomGqlAppHandler extends GqlAppHandler {

  @Override
  public String getChildDataListName(String boName, String originalName) {
    return super.getChildDataListName(boName, originalName);
  }

  @Override
  public List<GraphQLFieldDefinition> getCustomizedFields(String boName) {
    
    List<GraphQLFieldDefinition> fields = new ArrayList<GraphQLFieldDefinition>();
    
    //add privilege field to all bo
    fields.add(newFieldDefinition().name("privilege")
        .type(Scalars.GraphQLInt)
        .description("操作权限：取值为0-7， 对应三位二进制（从左到右，0为无权限，1为有权限）：删改查权限")
        .dataFetcher(new BOPrivilegeFetcher(boName))
        .build()
      );
    
    //add customer for Checkin BO only
    if(BONames.Checkin.equals(boName)){
      String fieldName = "customer_Customer";
      fields.add(newFieldDefinition().name(fieldName)
        .type(new GraphQLTypeReference(BONames.Customer))
        .description("关联客户")
        .dataFetcher(new ReferenceBOFetcher(BONames.Checkin, BONames.Customer, fieldName, null, CheckinFieldName.CUSTOMER_ID))
        .build()
      );
    }
    
    return fields;
  }

  @Override
  public List<GraphQLFieldDefinition> getCustomizedObjects() {
    return super.getCustomizedObjects();
  }

  @Override
  public List<GraphQLFieldDefinition> getChildCustomizedObjects(String boName) {
    
    List<GraphQLFieldDefinition> fields = new ArrayList<GraphQLFieldDefinition>();
    
    //add CSPGrant for Customer
    if(BONames.Customer.equals(boName)){
      fields.add(newFieldDefinition().name("grants")
        .type(new GraphQLList(new GraphQLTypeReference(BONames.CSPGrant)))
        .description("相关参与人")
        .argument(newArgument().name("jsonQuerySpec").type(GqlSchemaGenerator.getJsonQueryArgumentType()).build())
        .dataFetcher(new FetchChildListByJsonQuerySpec(boName, BONames.CSPGrant, CSPGrantFieldName.ENTITY_NAME, CSPGrantFieldName.OBJ_ID))
        .build()
      );
    }
    
    //add comments, commentCount for WorkRecord BO only
    if(BONames.WorkRecord.equals(boName)){
      fields.add(newFieldDefinition().name("comments")
        .type(new GraphQLList(new GraphQLTypeReference(BONames.Comment)))
        .description("关联评论列表")
        .argument(newArgument().name("jsonQuerySpec").type(GqlSchemaGenerator.getJsonQueryArgumentType()).build())
        .dataFetcher(new FetchChildListByJsonQuerySpec(boName, BONames.Comment, CommentFieldName.RELATE_TO_TYPE, CommentFieldName.RELATE_TO_ID))
        .build()
      );
      
      fields.add(newFieldDefinition().name("commentCount")
        .type(Scalars.GraphQLLong)
        .description("评论数")
        .argument(newArgument().name("jsonQuerySpec").type(GqlSchemaGenerator.getJsonQueryArgumentType()).build())
        .dataFetcher(new FetchChildCountByJsonQuerySpec(boName, BONames.Comment, CommentFieldName.RELATE_TO_TYPE, CommentFieldName.RELATE_TO_ID))
        .build()
      );
    }
    
    //add attachments  for WorkRecord & Checkin BO
    if(BONames.WorkRecord.equals(boName) || BONames.Checkin.equals(boName)){
      fields.add(newFieldDefinition().name("attachments")
        .type(new GraphQLList(new GraphQLTypeReference(BONames.Attachment)))
        .description("关联附件列表")
        .argument(newArgument().name("jsonQuerySpec").type(GqlSchemaGenerator.getJsonQueryArgumentType()).build())
        .dataFetcher(new FetchChildListByJsonQuerySpec(boName, BONames.Attachment, AttachmentFieldName.RELATE_TO_TYPE, AttachmentFieldName.RELATE_TO_ID))
        .build()
      );
    }
    return fields;
  }

  @Override
  public String getForeignKeyFieldName(String boName, String originalName) {
    return super.getForeignKeyFieldName(boName, originalName);
  }

}
