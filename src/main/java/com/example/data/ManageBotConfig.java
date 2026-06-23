package com.example.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ManageBotConfig {
    private static final ManageBotConfig INSTANCE = new ManageBotConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("ManageBot/Config");

    private final Path filePath = FabricLoader.getInstance().getConfigDir().resolve("managebot_config.json");

    private boolean sleepWithoutBot = false;
    private boolean kickBotsIfNoPlayer = false;
    private boolean spawnBotsIfFirstPlayerJoin = false;

    private ManageBotConfig() {}

    public static ManageBotConfig getInstance() {
        return INSTANCE;
    }

    public void load() {
        if (!Files.exists(filePath)) {
            save();
            return;
        }
        try (Reader reader = new FileReader(filePath.toFile())) {
            JsonObject obj = GSON.fromJson(reader, JsonObject.class);
            if (obj != null) {
                if (obj.has("sleep_without_bot"))
                    sleepWithoutBot = obj.get("sleep_without_bot").getAsBoolean();
                if (obj.has("kick_bots_if_no_player"))
                    kickBotsIfNoPlayer = obj.get("kick_bots_if_no_player").getAsBoolean();
                if (obj.has("spawn_bots_if_first_player_join"))
                    spawnBotsIfFirstPlayerJoin = obj.get("spawn_bots_if_first_player_join").getAsBoolean();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load config", e);
        }
    }

    public void save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("sleep_without_bot", sleepWithoutBot);
        obj.addProperty("kick_bots_if_no_player", kickBotsIfNoPlayer);
        obj.addProperty("spawn_bots_if_first_player_join", spawnBotsIfFirstPlayerJoin);
        try (Writer writer = new FileWriter(filePath.toFile())) {
            GSON.toJson(obj, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    public boolean isSleepWithoutBot() { return sleepWithoutBot; }
    public void setSleepWithoutBot(boolean value) { sleepWithoutBot = value; save(); }

    public boolean isKickBotsIfNoPlayer() { return kickBotsIfNoPlayer; }
    public void setKickBotsIfNoPlayer(boolean value) { kickBotsIfNoPlayer = value; save(); }

    public boolean isSpawnBotsIfFirstPlayerJoin() { return spawnBotsIfFirstPlayerJoin; }
    public void setSpawnBotsIfFirstPlayerJoin(boolean value) { spawnBotsIfFirstPlayerJoin = value; save(); }
}
