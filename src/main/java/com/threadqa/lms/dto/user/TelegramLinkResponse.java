package com.threadqa.lms.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramLinkResponse {
    private String telegramLink;
    private String confirmationCode;
    private Boolean isTelegramLinked;
}
