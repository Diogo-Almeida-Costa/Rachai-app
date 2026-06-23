package com.rachai.framework.extension.rule;

import com.rachai.framework.extension.resource.IResource;
import com.rachai.framework.core.user.User;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

public interface ISplitRule<T extends IResource> {
    Map<User, BigDecimal> split(T resource, List<User> participants);
}
