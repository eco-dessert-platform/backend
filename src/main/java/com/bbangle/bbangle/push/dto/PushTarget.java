package com.bbangle.bbangle.push.dto;

public record PushTarget(
    String memberName,
    String boardTitle,
    String productTitle,
    String pushCategory
) {
}
