package com.rachai.framework.core.splitter;

import com.rachai.framework.extension.resource.IResource;
import com.rachai.framework.extension.rule.ISplitRule;
import com.rachai.framework.core.user.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class ResourceSplitter {
    
    public <T extends IResource> Map<User, BigDecimal> calculateSplit(T resource, List<User> participants, ISplitRule<T> rule) {
        // A lógica de orquestração do particionamento é fixa
        // Mas a regra de negócio matemática é flexível
        return rule.split(resource, participants);
    }
}
