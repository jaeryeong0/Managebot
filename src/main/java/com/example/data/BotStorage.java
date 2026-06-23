package com.example.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BotStorage {
    private static final BotStorage INSTANCE = new BotStorage();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("ManageBot/BotStorage");

    private final Path filePath = FabricLoader.getInstance().getConfigDir().resolve("managebot_bots.json");
    private List<BotData> bots = new ArrayList<>();

    private BotStorage() {}

    public static BotStorage getInstance() {
        return INSTANCE;
    }

    public void load() {
        if (!Files.exists(filePath)) {
            save();
            return;
        }
        try (Reader reader = new FileReader(filePath.toFile())) {
            Type listType = new TypeToken<List<BotData>>() {}.getType();
            List<BotData> loaded = GSON.fromJson(reader, listType);
            bots = loaded != null ? loaded : new ArrayList<>();
        } catch (IOException e) {
            LOGGER.error("Failed to load bot list", e);
            bots = new ArrayList<>();
        }
    }

    public void save() {
        try (Writer writer = new FileWriter(filePath.toFile())) {
            GSON.toJson(bots, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save bot list", e);
        }
    }

    public List<BotData> getBots() {
        return bots;
    }

    public void addBot(BotData bot) {
        bots.add(bot);
        save();
    }

    public boolean removeByIndex(int idx) {
        if (idx < 0 || idx >= bots.size()) return false;
        bots.remove(idx);
        save();
        return true;
    }

    public boolean removeByName(String name) {
        boolean removed = bots.removeIf(b -> b.name.equalsIgnoreCase(name));
        if (removed) save();
        return removed;
    }

    public BotData getByIndex(int idx) {
        if (idx < 0 || idx >= bots.size()) return null;
        return bots.get(idx);
    }

    public BotData getByName(String name) {
        return bots.stream().filter(b -> b.name.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public boolean isBotName(String name) {
        return bots.stream().anyMatch(b -> b.name.equalsIgnoreCase(name));
    }
}
