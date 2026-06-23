package com.example.event;

import com.example.data.BotData;
import com.example.data.BotStorage;
import com.example.data.ManageBotConfig;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.List;

public class PlayerJoinHandler {

    public static void onJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        if (!ManageBotConfig.getInstance().isSpawnBotsIfFirstPlayerJoin()) return;

        BotStorage storage = BotStorage.getInstance();

        // JOIN fires after the player is already in the player list.
        // Count real players currently connected (excluding bots).
        // If exactly 1 real player exists, this is the 0→1 transition.
        long realPlayerCount = server.getPlayerList().getPlayers().stream()
            .filter(p -> !storage.isBotName(p.getGameProfile().getName()))
            .count();

        if (realPlayerCount != 1) return;

        List<BotData> bots = storage.getBots();
        for (BotData bot : bots) {
            // Skip bots that are already connected to avoid duplicate spawning
            if (server.getPlayerList().getPlayerByName(bot.name) != null) continue;

            String cmd = String.format("player %s spawn at %.4f %.4f %.4f", bot.name, bot.x, bot.y, bot.z);
            server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack().withPermission(4),
                cmd
            );
        }
    }
}
