package com.example.command;

import com.example.data.BotData;
import com.example.data.BotStorage;
import com.example.data.ManageBotConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;

import java.util.List;

public class ManageBotCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext buildContext,
                                Commands.CommandSelection environment) {
        dispatcher.register(
            Commands.literal("managebot")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("add")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                            .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                    .executes(ManageBotCommand::executeAdd))))))
                .then(Commands.literal("list")
                    .executes(ManageBotCommand::executeList))
                .then(Commands.literal("delete")
                    .then(Commands.argument("target", StringArgumentType.word())
                        .executes(ManageBotCommand::executeDelete)))
                .then(Commands.literal("spawn")
                    .executes(ManageBotCommand::executeSpawnAll)
                    .then(Commands.argument("target", StringArgumentType.word())
                        .executes(ManageBotCommand::executeSpawn)))
                .then(Commands.literal("kick")
                    .then(Commands.argument("target", StringArgumentType.word())
                        .executes(ManageBotCommand::executeKick)))
                .then(Commands.literal("rule")
                    .then(Commands.literal("sleep_without_bot")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                            .executes(ManageBotCommand::executeRuleSleepWithoutBot)))
                    .then(Commands.literal("kick_bots_if_no_player")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                            .executes(ManageBotCommand::executeRuleKickBotsIfNoPlayer)))
                    .then(Commands.literal("spawn_bots_if_first_player_join")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                            .executes(ManageBotCommand::executeRuleSpawnBotsIfFirstPlayerJoin))))
        );
    }

    private static int executeAdd(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        double x = DoubleArgumentType.getDouble(ctx, "x");
        double y = DoubleArgumentType.getDouble(ctx, "y");
        double z = DoubleArgumentType.getDouble(ctx, "z");

        BotStorage.getInstance().addBot(new BotData(name, x, y, z));
        ctx.getSource().sendSuccess(
            () -> Component.literal("Added bot: " + name + " at (" + x + ", " + y + ", " + z + ")"),
            false
        );
        return 1;
    }

    private static int executeList(CommandContext<CommandSourceStack> ctx) {
        List<BotData> bots = BotStorage.getInstance().getBots();
        if (bots.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("No bots registered."), false);
            return 0;
        }
        for (int i = 0; i < bots.size(); i++) {
            BotData bot = bots.get(i);
            final int idx = i;
            ctx.getSource().sendSuccess(
                () -> Component.literal("[" + idx + "] " + bot.name + " " + bot.x + ", " + bot.y + ", " + bot.z),
                false
            );
        }
        return bots.size();
    }

    private static int executeDelete(CommandContext<CommandSourceStack> ctx) {
        String target = StringArgumentType.getString(ctx, "target");
        BotStorage storage = BotStorage.getInstance();

        boolean removed;
        try {
            int idx = Integer.parseInt(target);
            removed = storage.removeByIndex(idx);
        } catch (NumberFormatException e) {
            removed = storage.removeByName(target);
        }

        if (removed) {
            ctx.getSource().sendSuccess(
                () -> Component.literal("Removed bot: " + target), false
            );
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("Bot not found: " + target));
        return 0;
    }

    private static int executeSpawnAll(CommandContext<CommandSourceStack> ctx) {
        List<BotData> bots = BotStorage.getInstance().getBots();
        if (bots.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("No bots registered."), false);
            return 0;
        }
        MinecraftServer server = ctx.getSource().getServer();
        for (BotData bot : bots) {
            spawnBot(server, bot);
        }
        int count = bots.size();
        ctx.getSource().sendSuccess(() -> Component.literal("Spawned " + count + " bot(s)."), false);
        return count;
    }

    private static int executeSpawn(CommandContext<CommandSourceStack> ctx) {
        String target = StringArgumentType.getString(ctx, "target");
        BotData bot = resolveBot(target);

        if (bot == null) {
            ctx.getSource().sendFailure(Component.literal("Bot not found: " + target));
            return 0;
        }
        spawnBot(ctx.getSource().getServer(), bot);
        ctx.getSource().sendSuccess(() -> Component.literal("Spawning bot: " + bot.name), false);
        return 1;
    }

    private static int executeKick(CommandContext<CommandSourceStack> ctx) {
        String target = StringArgumentType.getString(ctx, "target");
        BotData bot = resolveBot(target);

        if (bot == null) {
            ctx.getSource().sendFailure(Component.literal("Bot not found: " + target));
            return 0;
        }
        MinecraftServer server = ctx.getSource().getServer();
        server.getCommands().performPrefixedCommand(
            server.createCommandSourceStack().withPermission(LevelBasedPermissionSet.OWNER),
            "player " + bot.name + " kill"
        );
        ctx.getSource().sendSuccess(() -> Component.literal("Kicking bot: " + bot.name), false);
        return 1;
    }

    private static int executeRuleSleepWithoutBot(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ManageBotConfig.getInstance().setSleepWithoutBot(value);
        ctx.getSource().sendSuccess(
            () -> Component.literal("Rule set: sleep_without_bot = " + value), false
        );
        return 1;
    }

    private static int executeRuleKickBotsIfNoPlayer(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ManageBotConfig.getInstance().setKickBotsIfNoPlayer(value);
        ctx.getSource().sendSuccess(
            () -> Component.literal("Rule set: kick_bots_if_no_player = " + value), false
        );
        return 1;
    }

    private static int executeRuleSpawnBotsIfFirstPlayerJoin(CommandContext<CommandSourceStack> ctx) {
        boolean value = BoolArgumentType.getBool(ctx, "value");
        ManageBotConfig.getInstance().setSpawnBotsIfFirstPlayerJoin(value);
        ctx.getSource().sendSuccess(
            () -> Component.literal("Rule set: spawn_bots_if_first_player_join = " + value), false
        );
        return 1;
    }

    private static BotData resolveBot(String target) {
        try {
            int idx = Integer.parseInt(target);
            return BotStorage.getInstance().getByIndex(idx);
        } catch (NumberFormatException e) {
            return BotStorage.getInstance().getByName(target);
        }
    }

    private static void spawnBot(MinecraftServer server, BotData bot) {
        String cmd = String.format("player %s spawn at %.4f %.4f %.4f", bot.name, bot.x, bot.y, bot.z);
        server.getCommands().performPrefixedCommand(
            server.createCommandSourceStack().withPermission(LevelBasedPermissionSet.OWNER),
            cmd
        );
    }
}
