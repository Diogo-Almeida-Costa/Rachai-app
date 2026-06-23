package com.rachai.api.instance.rule;

import com.rachai.framework.core.user.User;
import com.rachai.framework.extension.rule.ISplitRule;
import com.rachai.api.instance.resource.ExpenseResource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EqualSplitRule implements ISplitRule<ExpenseResource> {
    @Override
    public Map<User, BigDecimal> split(ExpenseResource resource, List<User> participants) {
        Map<User, BigDecimal> splits = new HashMap<>();
        if (participants == null || participants.isEmpty()) {
            return splits;
        }

        BigDecimal amountPerUser = resource.getAmount().divide(new BigDecimal(participants.size()), 2, RoundingMode.HALF_UP);
        
        for (User user : participants) {
            splits.put(user, amountPerUser);
        }
        
        return splits;
    }
}
