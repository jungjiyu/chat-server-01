package com.chat.kit.api.response;

import com.chat.kit.api.response.common.error.ErrorCode;
import com.chat.kit.api.response.common.success.ResponseCode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> response(ResponseCode code, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .code(code.getCode())
                .message(code.getMessage())
                .data(data)
                .build();

        return response;
    }
    public static <T> ApiResponse<T> response(ErrorCode code, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .code(code.getCode())
                .message(code.getMessage())
                .data(data)
                .build();

        return response;
    }

    public static <T> ApiResponse<T> response(ResponseCode code, String message, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .code(code.getCode())
                .message(message)
                .data(data)
                .build();

        return response;
    }
}

