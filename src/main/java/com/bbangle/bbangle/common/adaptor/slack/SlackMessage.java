package com.bbangle.bbangle.common.adaptor.slack;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
public class SlackMessage {

    private List<SlackBlock> blocks;

    public static SlackMessage fromException(HttpServletRequest request, Throwable t) {
        return new SlackMessage(List.of(
            SlackBlock.createBlock("header", "plain_text",
                SlackBlock.SlackText.createHeader(t.getMessage())),
            SlackBlock.createBlock("section", "plain_text",
                SlackBlock.SlackText.createSection(request, t))
        ));
    }

    public static SlackMessage fromText(String header, String contents) {
        return new SlackMessage(List.of(
            SlackBlock.createBlock("header", "plain_text", header),
            SlackBlock.createBlock("section", "plain_text", contents)
        ));
    }


    @Data
    @AllArgsConstructor
    public static class SlackBlock {

        private String type; // ex) header, section
        private SlackText text;


        public static SlackBlock createBlock(String part, String type, String title) {
            return new SlackBlock(part, new SlackText(type, title));
        }


        @Data
        @AllArgsConstructor
        public static class SlackText {

            private String type; // ex) plain_text
            private String text;

            public static String createHeader(String text) {
                return truncateText(text, 150);
            }

            public static String createSection(HttpServletRequest request, Throwable t) {
                return String.format(
                    "- url: %s \n - 위치: %s \n - message: %s ",
                    request.getRequestURI(),
                    extractMethodPosition(t),
                    truncateText(t.getMessage(), 3000)
                );
            }

            /**
             * 에러 발생한 메소드 위치 반환
             */
            private static String extractMethodPosition(Throwable t) {
                Optional<StackTraceElement> optional = Arrays.stream(t.getStackTrace())
                    .filter(it -> it.getClassName().contains("bbangle"))
                    .findFirst();

                StackTraceElement targetElement = optional.orElseGet(() -> t.getStackTrace()[0]);
                return String.format("%s, %s", targetElement.getClassName(),
                    targetElement.getMethodName());
            }

            /**
             * 문자열 자르기
             */
            private static String truncateText(String text, int maxLength) {
                if (text.length() <= maxLength) {
                    return text;
                }
                return text.substring(0, maxLength - 3) + "...";
            }
        }
    }
}


