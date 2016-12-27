package com.chanapp.chanjet.customer.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.constant.SRU;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

/**
 * @author tds
 *
 */
public class ReportUtil {
    /**
     * 返回用户组信息（上级信息和用户信息）
     * 
     * @param groupIdParam
     * @param userIdParam
     * @param userInfo
     * @param userIds
     * @param countDate
     * @param bizType
     * @return
     */
    public static List<Map<String, Object>> getUserGroupInfo(Long groupIdParam, Long userIdParam,
            Map<Long, Map<String, Object>> userInfo, List<Long> userIds, String countDate, String bizType,
            boolean isExportTask) {
        List<Map<String, Object>> groups = new ArrayList<Map<String, Object>>();
        Map<Long, List<Long>> children = new HashMap<Long, List<Long>>();
        List<UserValue> subordinates = null;
        List<UserValue> subordinates_ = null;
        if (userIdParam != null && userIdParam > 1) {
            UserValue value = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                    .getUserValueByUserId(userIdParam);
            Long parentId = value.getParentId();
            UserValue parentValue = null;
            if (isExportTask) {
                if (parentId != null && parentId > 1) {
                    parentValue = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                            .getUserValueByUserId(parentId);
                } else {
                    parentValue = value;
                }
            } else {
                if (!SRU.LEVEL_SUPERISOR.equals(value.getUserLevel()) && !SRU.ROLE_BOSS.equals(value.getUserRole())) {
                    if (parentId != null && parentId > 1) {
                        parentValue = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                                .getUserValueByUserId(parentId);
                    } else {
                        parentValue = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                                .getOrgBoss();
                    }
                } else {
                    parentValue = value;
                }
            }
            Map<String, Object> group = new HashMap<String, Object>();
            group.put("name", parentValue.getName());
            group.put("userId", parentValue.getId());
            group.put("userRole", parentValue.getUserRole());

            List<Long> userIdList = new ArrayList<Long>();
            userIdList.add(value.getId());

            group.put("childIds", userIdList);
            groups.add(group);
            userIds.add(value.getId());
            BoRowConvertUtil.userValue2Map(userInfo, value);
        } else {
            boolean selectAll = false;// 传组ID了 那么就按组查
            if (groupIdParam == null || groupIdParam < 1) {
                selectAll = true;
            }
            Long currentUserId = EnterpriseContext.getCurrentUser().getUserLongId();
            UserValue sysRelUser = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                    .getUserValueByUserId(currentUserId);
            if (!SRU.LEVEL_BOSS.equals(sysRelUser.getUserLevel())) {
                groupIdParam = currentUserId;
            }
            // 获取用户所属的所有用户信息
            subordinates = ServiceLocator.getInstance().lookup(UserServiceItf.class).getHierarchyUsers(groupIdParam,
                    true, true);
            Long bossId = EnterpriseUtil.getAppUserId();
            // 如果传组了 并且是老板 那么 取出用户里pid是老板的 并且 非管理员的
            if (subordinates != null && !selectAll && SRU.LEVEL_BOSS.equals(sysRelUser.getUserLevel())
                    && groupIdParam.compareTo(bossId) == 0) {
                subordinates_ = new ArrayList<UserValue>();
                for (int i = 0; i < subordinates.size(); i++) {
                    UserValue tmp = subordinates.get(i);
                    if (SRU.LEVEL_BOSS.equals(tmp.getUserLevel())) {
                        subordinates_.add(tmp);// 把老板自己放进来
                    } else if (bossId.compareTo(tmp.getParentId()) == 0
                            && !SRU.LEVEL_SUPERISOR.equals(tmp.getUserLevel())) {
                        subordinates_.add(tmp);
                    }
                }
                subordinates = subordinates_;
            }
        }

        if (subordinates != null) {
            List<UserValue> validUsers = new ArrayList<UserValue>();
            Map<String, Object> para = new HashMap<String, Object>();
            para.put("monthEnd", countDate);
            if (isExportTask) {
                para.put("monthStart", countDate);
                para.put("bizType", bizType);
            }
            bizFilterKQ(validUsers, subordinates, para);
            for (UserValue value : validUsers) {
                userIds.add(value.getId());
                BoRowConvertUtil.userValue2Map(userInfo, value);
                Long parentId = value.getParentId();
                Long dbUserId = value.getId();

                if (parentId != null && parentId > 1) {
                    if (!SRU.LEVEL_SUPERISOR.equals(value.getUserLevel())) {// 主管不加到老版組內
                        if (children.containsKey(parentId)) {
                            children.get(parentId).add(dbUserId);
                        } else {
                            List<Long> userIdList = new ArrayList<Long>();
                            userIdList.add(dbUserId);
                            children.put(parentId, userIdList);
                        }
                    }
                }

                if (SRU.ROLE_BOSS.equals(value.getUserRole()) || SRU.LEVEL_SUPERISOR.equals(value.getUserLevel())) {
                    Map<String, Object> group = new HashMap<String, Object>();
                    if (isExportTask) {
                        group.put("name", value.getName());
                    } else {
                        if (SRU.ROLE_BOSS.equals(value.getUserRole())) {
                            group.put("name", "老板直属");
                        } else {
                            group.put("name", value.getName());
                        }
                    }

                    group.put("userId", dbUserId);
                    group.put("userRole", value.getUserRole());
                    if (children.containsKey(dbUserId)) {
                        children.get(dbUserId).add(dbUserId);
                    } else {
                        List<Long> userIdList = new ArrayList<Long>();
                        userIdList.add(dbUserId);
                        children.put(dbUserId, userIdList);
                    }
                    group.put("childIds", children.get(dbUserId));
                    groups.add(group);
                }
            }

        }
        return groups;
    }

    /**
     * 过滤users，设置validUsers。
     * 
     * @param validUsers
     * @param users
     * @param para
     */
    private static void bizFilterKQ(List<UserValue> validUsers, List<UserValue> users, Map<String, Object> para) {
        UserValue user = null;
        for (int i = 0; i < users.size(); i++) {
            user = users.get(i);
            validUsers.add(user);            
        }
    }

}
