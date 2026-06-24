package com.example.event;

import com.example.data.BotStorage;
import com.example.data.ManageBotConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.permissions.LevelBasedPermissionSet;

import java.util.List;

public class PlayerDisconnectHandler {

    public static void onDisconnect(ServerGamePacketListenerImpl handler, MinecraftServer server) {
        if (!ManageBotConfig.getInstance().isKickBotsIfNoPlayer()) return;

        BotStorage storage = BotStorage.getInstance();
        ServerPlayer disconnecting = handler.player;

        // DISCONNECT fires while the player is still in the list, so subtract them manually
        long realPlayerCount = server.getPlayerList().getPlayers().stream()
            .filter(p -> p != disconnecting)
            .filter(p -> !storage.isBotName(p.getGameProfile().name()))
            .count();

        if (realPlayerCount > 0) return;

        // No real players remain — kick all ManageBot bots
        for (ServerPlayer player : List.copyOf(server.getPlayerList().getPlayers())) {
            if (storage.isBotName(player.getGameProfile().name())) {
                server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack().withPermission(LevelBasedPermissionSet.OWNER),
                    "player " + player.getGameProfile().name() + " kill"
                );
            }
        }
    }
}
