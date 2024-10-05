package com.chat.kit.exhandler;

import com.chat.kit.api.response.common.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResult {
    private ErrorCode errorCode;
    private String message;
}
