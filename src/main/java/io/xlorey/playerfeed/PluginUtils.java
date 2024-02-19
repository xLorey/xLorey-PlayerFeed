package io.xlorey.playerfeed;

import java.util.List;

/**
 * Author: Deknil
 * Date: 19.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Toolkit for the plugin
 * <p> xLoreyPlayerFeed © 2024. All rights reserved. </p>
 */
public class PluginUtils {
    /**
     * Handles custom text replacements for messages.
     * @param text     The text with placeholders.
     * @param username The username to replace the placeholder with.
     * @return The text with placeholders replaced.
     */
    public static String handleText(String text, String username){
        text = text.replace("<USERNAME>", username);
        text = text.replace("<SPACE_SYMBOL>", "\u200B");
        return text;
    }

    /**
     * Retrieves the message text from the list by key and index.
     * @param key Key for accessing the list of messages in the configuration.
     * @return Message text or empty string if list is empty or key not found.
     */
    public static String getMessageText(String key) {
        List<Object> messageList = Main.getDefaultConfig().getList(key);
        if (messageList == null || messageList.isEmpty()) {
            return "";
        }

        return (String) messageList.get(Main.random.nextInt(messageList.size()));
    }
}
