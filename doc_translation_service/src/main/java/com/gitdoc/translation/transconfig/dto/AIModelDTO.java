package com.gitdoc.translation.transconfig.dto;

import lombok.Data;

@Data
public class AIModelDTO {
    private String id;
    private String name;
    private Integer contextLength;
    private Double inputPrice;
    private Double outputPrice;
    private String priceUnit = "$/1M tokens";
    private Boolean isFree;
}
