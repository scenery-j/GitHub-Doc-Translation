package com.gitdoc.translation.quota.dto;

import lombok.Data;

@Data
public class QuotaDTO {
    private Long freeQuota;
    private Long usedQuota;
    private Long remaining;
    private Boolean hasCustomApiKey;
}
