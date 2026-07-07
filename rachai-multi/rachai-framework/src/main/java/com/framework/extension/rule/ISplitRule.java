package com.framework.extension.rule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.framework.extension.resource.IResource;
import com.framework.extension.user.IUser;

public interface ISplitRule<T extends IResource> {
    Map<IUser, BigDecimal> split(T resource, List<IUser> participants);
}
