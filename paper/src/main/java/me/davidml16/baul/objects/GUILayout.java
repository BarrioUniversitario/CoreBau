package me.davidml16.baul.objects;

import me.clip.placeholderapi.PlaceholderAPI;
import me.davidml16.baul.Main;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GUILayout {

    private HashMap<String, String> messages;
    private HashMap<String, List<String>> messageList;

    public GUILayout(HashMap<String, String> messages, HashMap<String, List<String>> messageList) {
        this.messages = messages;
        this.messageList = messageList;
    }

    public GUILayout() {
        this(new HashMap<String, String>(), new HashMap<String, List<String>>());
    }

    public HashMap<String, String> getMessages() {
        return messages;
    }

    public void setMessages(HashMap<String, String> messages) {
        this.messages = messages;
    }

    public HashMap<String, List<String>> getMessageList() {
        return messageList;
    }

    public void setMessageList(HashMap<String, List<String>> messageList) {
        this.messageList = messageList;
    }

    public int getSlot(String path) {
        return Integer.parseInt(messages.get("Layout.Items." + path + ".Slot"));
    }

    public String getRawMessage(String path) {
        String value = messages.get("Layout." + path);
        return value != null ? value : "";
    }

    public List<String> getRawMessageList(String message) {
        List<String> raw = messageList.get("Layout." + message);
        return raw != null ? new ArrayList<>(raw) : new ArrayList<>();
    }

    public String getMessage(String path) {
        return me.davidml16.baul.utils.Colorize.format(getRawMessage(path));
    }

    public boolean getBoolean(String path) { return Boolean.parseBoolean(messages.get("Layout." + path)); }

    public int getInteger(String path) { return Integer.parseInt(messages.get("Layout." + path)); }

    public List<String> getMessageList(String message) {
        List<String> lines = new ArrayList<>();
        for (String line : getRawMessageList(message)) {
            lines.add(me.davidml16.baul.utils.Colorize.format(line));
        }
        return lines;
    }

    public List<String> getMessageListPlaceholders(Player player, String message) {
        List<String> lines = new ArrayList<>();
        for(String line : getRawMessageList(message)) {
            if(!Main.get().isSetting("placeholderapi"))
                lines.add(me.davidml16.baul.utils.Colorize.format(line));
            else
                lines.add(me.davidml16.baul.utils.Colorize.format(PlaceholderAPI.setPlaceholders(player, line)));
        }
        return lines;
    }

}
