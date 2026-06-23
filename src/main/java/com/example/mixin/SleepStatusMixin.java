package com.example.mixin;

import com.example.data.BotStorage;
import com.example.data.ManageBotConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.stream.Collectors;

/**
 * When sleep_without_bot is true, filters ManageBot-registered players out of the
 * sleeping-percentage calculation so only real players count toward the sleep threshold.
 *
 * Target method: ServerLevel#updateSleepingPlayerList()
 * Target call:   SleepStatus#update(List<ServerPlayer>, int)
 *
 * If Minecraft 26.x renames either of these, update the method/target strings below.
 * require=0 prevents a hard crash if the injection point is missing in this MC version.
 */
@Mixin(ServerLevel.class)
public class SleepStatusMixin {

    @Redirect(
        method = "updateSleepingPlayerList",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/SleepStatus;update(Ljava/util/List;I)Z"
        ),
        require = 0
    )
    private boolean managebot_filterBotsFromSleep(SleepStatus sleepStatus,
                                                   List<ServerPlayer> players,
                                                   int percentage) {
        if (!ManageBotConfig.getInstance().isSleepWithoutBot()) {
            return sleepStatus.update(players, percentage);
        }
        BotStorage storage = BotStorage.getInstance();
        List<ServerPlayer> realPlayers = players.stream()
            .filter(p -> !storage.isBotName(p.getGameProfile().getName()))
            .collect(Collectors.toList());
        return sleepStatus.update(realPlayers, percentage);
    }
}
