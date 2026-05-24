package com.rachai.api.dto;

import java.util.List;

public class GroupSuggestionDTO {

    private String suggestedName;
    private String suggestedDescription;
    private List<Long> suggestedMemberIds;
    private String reasoning;

    public GroupSuggestionDTO() {}

    public GroupSuggestionDTO(String suggestedName, String suggestedDescription,
                               List<Long> suggestedMemberIds, String reasoning) {
        this.suggestedName = suggestedName;
        this.suggestedDescription = suggestedDescription;
        this.suggestedMemberIds = suggestedMemberIds;
        this.reasoning = reasoning;
    }

    public String getSuggestedName() { return suggestedName; }
    public void setSuggestedName(String suggestedName) { this.suggestedName = suggestedName; }

    public String getSuggestedDescription() { return suggestedDescription; }
    public void setSuggestedDescription(String suggestedDescription) { this.suggestedDescription = suggestedDescription; }

    public List<Long> getSuggestedMemberIds() { return suggestedMemberIds; }
    public void setSuggestedMemberIds(List<Long> suggestedMemberIds) { this.suggestedMemberIds = suggestedMemberIds; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
}
