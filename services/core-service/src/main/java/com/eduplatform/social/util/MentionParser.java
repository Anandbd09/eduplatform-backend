// FILE 28: MentionParser.java
package com.eduplatform.social.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionParser {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_]+)");

    /**
     * EXTRACT MENTIONS FROM TEXT
     */
    public static String[] extractMentions(String content) {
        if (content == null || content.isEmpty()) {
            return new String[0];
        }

        List<String> mentions = new ArrayList<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);

        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }

        return mentions.toArray(new String[0]);
    }
}