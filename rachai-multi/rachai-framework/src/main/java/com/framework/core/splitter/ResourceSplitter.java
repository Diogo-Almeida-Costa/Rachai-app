package com.framework.core.splitter;

import com.framework.extension.resource.IResource;
import com.framework.extension.rule.ISplitRule;
import com.framework.extension.user.IUser;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class ResourceSplitter {
    
 public <T extends IResource> Map<IUser, BigDecimal> calculateSplit(T resource, List<IUser> participants, ISplitRule<T> rule) {
        // A lógica de orquestração do particionamento é fixa
        // Mas a regra de negócio matemática é flexível (vem da instância)
        return rule.split(resource, participants);
    }
}
