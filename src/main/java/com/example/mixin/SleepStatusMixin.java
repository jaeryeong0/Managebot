package com.example.mixin;

import com.example.data.BotStorage;
import com.example.data.ManageBotConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.stream.Collectors;

/**
 * When sleep_without_bot is true, filters ManageBot-registered players out of the
 * sleeping-percentage calculation so only real players count toward the sleep threshold.
 *
 * In MC 26.x: SleepStatus moved to net.minecraft.server.players,
 * and update() no longer takes the percentage int (read from GameRules internally).
 */
@Mixin(ServerLevel.class)
public class SleepStatusMixin {

    @Redirect(
        method = "updateSleepingPlayerList",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/SleepStatus;update(Ljava/util/List;)Z"
        ),
        require = 0
    )
    private boolean managebot_filterBotsFromSleep(SleepStatus sleepStatus,
                                                   List<ServerPlayer> players) {
        if (!ManageBotConfig.getInstance().isSleepWithoutBot()) {
            return sleepStatus.update(players);
        }
        BotStorage storage = BotStorage.getInstance();
        List<ServerPlayer> realPlayers = players.stream()
            .filter(p -> !storage.isBotName(p.getGameProfile().name()))
            .collect(Collectors.toList());
        return sleepStatus.update(realPlayers);
    }
}
