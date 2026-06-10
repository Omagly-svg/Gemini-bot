package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

public class GeminiBotClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            
            // 1. NON-CASE SENSITIVE: Convert the whole message to lowercase right away
            // "Hey Gemini MINE GRASS block" becomes "hey gemini mine grass block"
            String rawMessage = message.getString().toLowerCase();

            if (rawMessage.contains("hey gemini ")) {
                
                int index = rawMessage.indexOf("hey gemini ");
                String commandStr = rawMessage.substring(index + 11).trim();
                
                processGeminiCommand(commandStr);
            }
        });
    }

    private void processGeminiCommand(String command) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String baritoneCommand = "";

        // Mine Block
        if (command.startsWith("mine ")) {
            // 2. SPACE REPLACEMENT: Extract the block name and turn all spaces into underscores
            // "grass block" -> "grass_block"
            // "deepslate   diamond  ore" -> "deepslate_diamond_ore"
            String targetBlock = command.substring(5).trim().replaceAll("\\s+", "_");
            baritoneCommand = "mine " + targetBlock;
        } 
        // Kill/Follow Player
        else if (command.startsWith("kill ")) {
            String targetPlayer = command.substring(5).trim();
            baritoneCommand = "follow " + targetPlayer;
        } 
        // Go to Coordinates
        else if (command.startsWith("go to ")) {
            String coords = command.substring(6).trim();
            baritoneCommand = "goto " + coords;
        } 
        // Stop
        else if (command.equals("stop")) {
            baritoneCommand = "stop";
        }
        else {
            return;
        }

        // Send to Baritone
        client.player.networkHandler.sendChatMessage("#" + baritoneCommand);
    }
}
