package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;

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

            // TODO: Implement mining command
            System.out.println("Mining command: " + block);
        }

        // 🧍 FOLLOW PLAYER
        if (lower.startsWith("kill ")) {

            String player = command.substring(5).trim();

            // TODO: Implement follow command
            System.out.println("Following player: " + player);
        }

        // 🛑 STOP ALL
        if (lower.equals("stop")) {

            // TODO: Implement stop command
            System.out.println("Stopping all actions");
        }
    }
}
