package com.example;

import com.example.command.ManageBotCommand;
import com.example.data.BotStorage;
import com.example.data.ManageBotConfig;
import com.example.event.PlayerDisconnectHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManageBotMod implements ModInitializer {
    public static final String MOD_ID = "managebot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        BotStorage.getInstance().load();
        ManageBotConfig.getInstance().load();

        CommandRegistrationCallback.EVENT.register(ManageBotCommand::register);
        ServerPlayConnectionEvents.DISCONNECT.register(PlayerDisconnectHandler::onDisconnect);

        LOGGER.info("ManageBot initialized.");
    }
}
