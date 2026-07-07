package com.framework.core.ocr;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.framework.core.ocr.TabscannerResponseDTO.LineItem;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)

public class TabscannerResponseDTO {
    private String token;
    private Result result;
    private String message;

    public Result getResult() { return result; }
    public void setResult(Result result) { this.result = result; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private List<LineItem> lineItems; 
        private Double total;
        
        public List<LineItem> getLineItems() { return lineItems; }
        public void setLineItems(List<LineItem> lineItems) { this.lineItems = lineItems; }
        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineItem {
        private String desc;
        private Double lineTotal;
        private Object qty; //aceita qq retorno, dps trata no service

        
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }

        public Double getLineTotal() { return lineTotal; }
        public void setLineTotal(Double lineTotal) { this.lineTotal = lineTotal; }

        public Object getQty() { return qty; }
        public void setQty(Object qty) { this.qty = qty; }

    } 
}
