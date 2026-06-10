package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            System.out.println("GeminiBot: client or player is null, cannot send command: " + cmd);
            return;
        }

        System.out.println("GeminiBot: sending command -> " + cmd);
        // send chat message so Baritone (if present) executes it
        client.player.sendChatMessage(cmd);
    }
}
