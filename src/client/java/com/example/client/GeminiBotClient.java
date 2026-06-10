package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

public class GeminiBotClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("GeminiBot client initialized (client side)!");

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (message == null) return;

            String msg = message.getString();
            if (msg == null) return;

            String lower = msg.toLowerCase();
            final String trigger = "hey gemini";
            if (!lower.startsWith(trigger)) return; // ignore everything not addressed to Gemini

            String command = msg.substring(trigger.length()).trim();
            if (command.isEmpty()) return;

            try {
                // Stop command
                if (command.equalsIgnoreCase("stop")) {
                    sendBaritoneCommand("#stop");
                    return;
                }

                // Kill/follow commands
                if (command.toLowerCase().startsWith("kill ")) {
                    String target = command.substring(5).trim();
                    if (target.equalsIgnoreCase("everyone") || target.equalsIgnoreCase("players") || target.equalsIgnoreCase("all")) {
                        // Follow nearest players
                        sendBaritoneCommand("#follow players");
                    } else if (!target.isEmpty()) {
                        sendBaritoneCommand("#follow player " + target);
                    }
                    return;
                }

                // Mine commands - normalize spaces to underscores so "oak log" -> "oak_log"
                if (command.toLowerCase().startsWith("mine ")) {
                    String blockPart = command.substring(5).trim();
                    if (!blockPart.isEmpty()) {
                        String normalized = blockPart.replaceAll("\\s+", "_");
                        sendBaritoneCommand("#mine " + normalized);
                    }
                    return;
                }

                // Forward other commands: if it already starts with #, keep it, otherwise prefix
                if (command.startsWith("#")) {
                    sendBaritoneCommand(command);
                } else {
                    sendBaritoneCommand("#" + command);
                }

            } catch (Exception e) {
                System.err.println("GeminiBot: failed to handle command: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void sendBaritoneCommand(String cmd) {
        try {
            // Use reflection so we don't need a compile-time dependency on Minecraft client classes
            Class<?> mcClass = Class.forName("net.minecraft.client.MinecraftClient");
            java.lang.reflect.Method getInstance = mcClass.getMethod("getInstance");
            Object mc = getInstance.invoke(null);
            if (mc == null) {
                System.out.println("GeminiBot: MinecraftClient instance is null, cannot send command: " + cmd);
                return;
            }

            // Attempt to get the player field and call sendChatMessage
            java.lang.reflect.Field playerField = mcClass.getField("player");
            Object player = playerField.get(mc);
            if (player == null) {
                System.out.println("GeminiBot: player is null, cannot send command: " + cmd);
                return;
            }

            Class<?> playerClass = player.getClass();
            java.lang.reflect.Method sendChatMessage = null;
            try {
                sendChatMessage = playerClass.getMethod("sendChatMessage", String.class);
            } catch (NoSuchMethodException ex) {
                // Older/newer mappings might have different method names; fallback to tryMethod by name
                for (java.lang.reflect.Method m : playerClass.getMethods()) {
                    if (m.getName().toLowerCase().contains("chat") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class) {
                        sendChatMessage = m;
                        break;
                    }
                }
            }

            if (sendChatMessage == null) {
                System.out.println("GeminiBot: could not find sendChatMessage method on player, cannot send: " + cmd);
                return;
            }

            sendChatMessage.invoke(player, cmd);
            System.out.println("GeminiBot: sent command -> " + cmd);
        } catch (ClassNotFoundException e) {
            System.out.println("GeminiBot: MinecraftClient class not found (not running in client env): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("GeminiBot: failed to send command via reflection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
