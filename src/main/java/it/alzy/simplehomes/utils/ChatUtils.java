package it.alzy.simplehomes.utils;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.CommandSender;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatUtils {
    

    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
    .character('&')
    .hexColors()
    .build();

    private static final ConcurrentHashMap<String, Component> COMPONENT_CACHE = new ConcurrentHashMap<>(256);

    private static final Component EMPTY_COMPONENT = Component.empty();


    public static Component parse(String message) {
        if(message == null || message.isEmpty()) return EMPTY_COMPONENT;

        Component cached = COMPONENT_CACHE.get(message);
        if(cached != null) return cached;

        Component temp = EMPTY_COMPONENT;
        try {
            LEGACY_COMPONENT_SERIALIZER.deserialize(message);
        } catch(Exception e) {
            temp = Component.text(message);
        }

        if(COMPONENT_CACHE.size() < 256) {
            COMPONENT_CACHE.put(message, temp);
        }

        return temp;
    }


    public void send(CommandSender sender, String message, Object... placeholders) {
        if(sender == null || message == null || message.isEmpty()) return;

        if(placeholders.length == 0) {
            sender.sendMessage(parse(message));
            return;
        }

        if(placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("Placeholders must be K,V (key, value)");
        }

        String processedMessage = applyPlaceholders(message, placeholders);
        sender.sendMessage(parse(processedMessage));
    }

    public void send(CommandSender sender, String message) {
        if(sender == null || message == null || message.isEmpty()) return;
        sender.sendMessage(parse(message));
    }


    private String applyPlaceholders(String message, Object... placeholders) {
        StringBuilder sb = new StringBuilder(message);
        for(int i = 0; i < placeholders.length; i += 2) {
            String k = String.valueOf(placeholders[i]);
            String v = String.valueOf(placeholders[i + 1]);

            int index = 0;
            while((index = sb.indexOf(k, index)) != -1) {
                sb.replace(index, index + k.length(), v);
                index += v.length();
            }
        }
        return sb.toString();
    }

}
