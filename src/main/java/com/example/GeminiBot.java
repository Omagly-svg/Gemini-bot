package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

import baritone.api.BaritoneAPI;

public class GeminiBot implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {

            String msg = message.getString().toLowerCase();

            if (!msg.startsWith("hey gemini")) return;

            String command = message.substring(10).trim();

            handle(command);
        });
    }

    public void handle(String command) {

        String lower = command.toLowerCase();

        // ⛏ MINE COMMAND
        if (lower.startsWith("mine ")) {

            String[] parts = command.split(" ");
            String block = parts[1];

            BaritoneAPI.getProvider()
                    .getPrimaryBaritone()
                    .getCommandManager()
                    .execute("#mine " + block);
        }

        // 🧍 FOLLOW PLAYER (your "kill" command)
        if (lower.startsWith("kill ")) {

            String player = command.substring(5).trim();

            BaritoneAPI.getProvider()
                    .getPrimaryBaritone()
                    .getCommandManager()
                    .execute("#follow player " + player);
        }

        // 🛑 STOP ALL
        if (lower.equals("stop")) {

            BaritoneAPI.getProvider()
                    .getPrimaryBaritone()
                    .getCommandManager()
                    .execute("#stop");
        }
    }
}