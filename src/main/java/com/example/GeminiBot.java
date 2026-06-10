package com.example;

import net.fabricmc.api.ClientModInitializer;

public class GeminiBot implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("GeminiBot client initialized!");
    }
}
