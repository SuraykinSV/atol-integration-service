package com.example.atol_integration_service.dto;

import com.example.atol_integration_service.enums.ReceiptStatus;
import com.example.atol_integration_service.enums.RegistrationErrorType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AtolResponseDto {
    private String uuid;
    private String timestamp;
    private String status;
    private ErrorDto error;
    private PayloadDto payload;

    @Data
    public static class ErrorDto {
        @JsonProperty("error_id")
        private String id;
        private Integer code;
        private String text;
        private RegistrationErrorType type;
    }
    @Data
    public static class PayloadDto {
        private Double total;

        @JsonProperty("fns_site")
        private String fnsSite;

        @JsonProperty("fn_number")
        private String fnNumber;

        @JsonProperty("receipt_datetime")
        private String receiptDatetime;

        @JsonProperty("fiscal_receipt_number")
        private Integer fiscalReceiptNumber;

        @JsonProperty("fiscal_document_number")
        private Integer fiscalDocumentNumber;

        @JsonProperty("fiscal_document_attribute")
        private Long fiscalDocumentAttribute;

    }
}
