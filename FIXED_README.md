Updated fabric.mod.json to point to the actual client initializer (com.example.client.GeminiBotClient) and to use a proper mod id/name/metadata.

Notes:
- This file sets the mod as client-only (environment: "client"). If you later add server-side code, change environment to "*" and add a "main" entrypoint.
- After this commit the Actions build will run and produce artifacts. To use the mod in-game, place the generated mod JAR and Baritone's JAR into your Fabric "mods/" folder.
