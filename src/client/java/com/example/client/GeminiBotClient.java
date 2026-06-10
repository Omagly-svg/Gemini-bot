package com.example.client;

import net.fabricmc.api.ClientModInitializer;

import java.lang.reflect.*;
import java.net.URLClassLoader;
import java.util.Locale;

public class GeminiBotClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("GeminiBot client initialized (client side)!");

        try {
            // Load the ClientReceiveMessageEvents class reflectively to avoid compile-time linkage to Minecraft text classes
            Class<?> eventsClass = Class.forName("net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents");
            Field gameField = eventsClass.getField("GAME");
            Object gameEvent = gameField.get(null);

            // The callback interface is a nested type: ClientReceiveMessageEvents$ReceiveMessageCallback
            Class<?> callbackInterface = Class.forName("net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents$ReceiveMessageCallback");

            Object callbackProxy = Proxy.newProxyInstance(
                    callbackInterface.getClassLoader(),
                    new Class[]{callbackInterface},
                    (proxy, method, args) -> {
                        // The callback receives either (Text message, boolean overlay) or (Text message, MessageType type, boolean overlay)
                        if (args == null || args.length == 0) return null;

                        Object messageObj = args[0];
                        if (messageObj == null) return null;

                        String msgText = extractMessageString(messageObj);
                        if (msgText == null) return null;

                        handleChatMessage(msgText);
                        return null;
                    }
            );

            // Call 'register' on the event object to register our callback
            Method registerMethod = gameEvent.getClass().getMethod("register", Object.class);
            registerMethod.invoke(gameEvent, callbackProxy);

            System.out.println("GeminiBot: chat listener registered via reflection.");

        } catch (ClassNotFoundException e) {
            System.out.println("GeminiBot: Fabric ClientReceiveMessageEvents class not found; chat listener not registered: " + e.getMessage());
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            System.err.println("GeminiBot: failed to register chat listener: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String extractMessageString(Object messageObj) {
        try {
            // Try common method 'getString'
            Method getString = messageObj.getClass().getMethod("getString");
            Object s = getString.invoke(messageObj);
            if (s instanceof String) return (String) s;
        } catch (Exception ignored) {
        }

        try {
            // Fallback to toString()
            return messageObj.toString();
        } catch (Exception ignored) {
        }

        return null;
    }

    private static void handleChatMessage(String msg) {
        if (msg == null) return;
        String lower = msg.toLowerCase(Locale.ROOT).trim();
        final String trigger = "hey gemini";
        if (!lower.startsWith(trigger)) return;

        // Extract the command part preserving original casing after the trigger
        String command = msg.substring(trigger.length()).trim();
        if (command.isEmpty()) return;

        try {
            // Stop
            if (command.equalsIgnoreCase("stop")) {
                sendBaritoneCommand("#stop");
                return;
            }

            // Kill/follow
            if (command.toLowerCase(Locale.ROOT).startsWith("kill ")) {
                String target = command.substring(5).trim();
                if (target.equalsIgnoreCase("everyone") || target.equalsIgnoreCase("players") || target.equalsIgnoreCase("all")) {
                    sendBaritoneCommand("#follow players");
                } else if (!target.isEmpty()) {
                    sendBaritoneCommand("#follow player " + target);
                }
                return;
            }

            // Mine: normalize spaces to underscores
            if (command.toLowerCase(Locale.ROOT).startsWith("mine ")) {
                String blockPart = command.substring(5).trim();
                if (!blockPart.isEmpty()) {
                    String normalized = blockPart.replaceAll("\\s+", "_");
                    sendBaritoneCommand("#mine " + normalized);
                }
                return;
            }

            // Forward other commands
            if (command.startsWith("#")) {
                sendBaritoneCommand(command);
            } else {
                sendBaritoneCommand("#" + command);
            }

        } catch (Exception e) {
            System.err.println("GeminiBot: error handling chat command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void sendBaritoneCommand(String cmd) {
        try {
            // Reflection to obtain MinecraftClient.getInstance() and player.sendChatMessage
            Class<?> mcClass = Class.forName("net.minecraft.client.MinecraftClient");
            Method getInstance = mcClass.getMethod("getInstance");
            Object mc = getInstance.invoke(null);
            if (mc == null) {
                System.out.println("GeminiBot: MinecraftClient instance is null, cannot send command: " + cmd);
                return;
            }

            Field playerField = null;
            try {
                playerField = mcClass.getField("player");
            } catch (NoSuchFieldException ignored) {
                // some mappings have different visibility; try declared fields
                for (Field f : mcClass.getDeclaredFields()) {
                    if (f.getType() != null && f.getName().toLowerCase().contains("player")) {
                        playerField = f;
                        playerField.setAccessible(true);
                        break;
                    }
                }
            }

            if (playerField == null) {
                System.out.println("GeminiBot: player field not found on MinecraftClient, cannot send: " + cmd);
                return;
            }

            Object player = playerField.get(mc);
            if (player == null) {
                System.out.println("GeminiBot: player is null, cannot send command: " + cmd);
                return;
            }

            Class<?> playerClass = player.getClass();
            Method sendChatMessage = null;
            try {
                sendChatMessage = playerClass.getMethod("sendChatMessage", String.class);
            } catch (NoSuchMethodException ex) {
                for (Method m : playerClass.getMethods()) {
                    if (m.getName().toLowerCase().contains("chat") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class) {
                        sendChatMessage = m;
                        break;
                    }
                }
            }

            if (sendChatMessage == null) {
                System.out.println("GeminiBot: could not find sendChatMessage on player, cannot send: " + cmd);
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
