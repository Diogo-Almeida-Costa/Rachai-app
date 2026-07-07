package com.rachai.rule;

import com.framework.core.exception.BusinessException;
import com.framework.extension.rule.ISplitRule;
import com.framework.extension.user.IUser;
import com.rachai.model.Expense;
 
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SplitRule implements ISplitRule<Expense> {
    @Override
    public Map<IUser, BigDecimal> split(Expense resource, List<IUser> participants) {
        if (participants == null || participants.isEmpty()) {
            throw new BusinessException("Não é possível dividir a despesa sem participantes");
        }
 
        BigDecimal total = resource.getTotalValue();
        int n = participants.size();
 
        BigDecimal baseShare = total.divide(BigDecimal.valueOf(n), 2, RoundingMode.DOWN);
        BigDecimal distributed = baseShare.multiply(BigDecimal.valueOf(n));
        BigDecimal remainder = total.subtract(distributed);
        BigDecimal cent = new BigDecimal("0.01");
 
        Map<IUser, BigDecimal> shares = new LinkedHashMap<>();
        int remainderUnits = remainder.divide(cent, 0, RoundingMode.HALF_UP).intValue();
 
        for (int i = 0; i < n; i++) {
            BigDecimal share = baseShare;
            if (i < remainderUnits) {
                share = share.add(cent);
            }
            shares.put(participants.get(i), share);
        }
 
        return shares;
    }
}
