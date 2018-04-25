package com.melodispel.dpgame.data;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Andrea on 2018-04-16.
 */

public final class CustomsGamePlay {

    public static final String TIMED_SESSION = "timed session";

    // When the player is re-playing an already completed level
    public static final String REPEATED_LEVEL = "repeated level";

    public static final String LEVEL_PROGRESS = "level progress";

    public static String CreateCustomsString(ArrayList<String> customs) {

        if (customs != null && customs.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();

            Collections.sort(customs, String.CASE_INSENSITIVE_ORDER);

            for (int i = 0; i < customs.size(); i++) {
                stringBuilder.append(customs.get(i));
                if (i < customs.size() - 1) {
                    stringBuilder.append(";");
                }
            }
            return stringBuilder.toString();
        }
        return null;
    }
}
