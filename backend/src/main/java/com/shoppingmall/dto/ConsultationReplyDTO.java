package com.shoppingmall.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 咨询回复DTO
 */
@Data
public class ConsultationReplyDTO {
    
    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容不能超过1000个字符")
    private String replyContent;
}