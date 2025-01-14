package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.commands.*;
import com.fibermc.essentialcommands.commands.bench.*;
import com.fibermc.essentialcommands.commands.suggestions.*;
import com.fibermc.essentialcommands.util.EssentialsXParser;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.util.IConsumer;

import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.function.Predicate;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;
import static net.minecraft.server.command.CommandManager.argument;

/**
 * BasicCommands
 */
public class EssentialCommandRegistry {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        RootCommandNode<ServerCommandSource> rootNode = dispatcher.getRoot();

        LiteralCommandNode<ServerCommandSource> essentialCommandsRootNode;
        {
            LiteralCommandNode<ServerCommandSource> ecInfoNode = CommandManager.literal("info")
                .executes(new ModInfoCommand())
                .build();

            essentialCommandsRootNode = CommandManager.literal("essentialcommands")
                .executes(ecInfoNode.getCommand())
                .build();

            essentialCommandsRootNode.addChild(ecInfoNode);
        }

        IConsumer<LiteralCommandNode<ServerCommandSource>> registerNode = (node) -> {
            rootNode.addChild(node);
            essentialCommandsRootNode.addChild(node);
        };

        if (CONFIG.ENABLE_TPA.getValue()) {
            registerNode.accept(CommandManager.literal("tpa")
                .requires(ECPerms.require(ECPerms.Registry.tpa, 0))
                .then(CommandUtil.targetPlayerArgument()
                    .executes(new TeleportAskCommand()))
                .build());

            registerNode.accept(CommandManager.literal("tpcancel")
                .requires(ECPerms.require(ECPerms.Registry.tpa, 0))
                .executes(new TeleportCancelCommand())
                .build());

            registerNode.accept(CommandManager.literal("tpaccept")
                .requires(ECPerms.require(ECPerms.Registry.tpaccept, 0))
                .executes(new TeleportAcceptCommand()::runDefault)
                .then(CommandUtil.targetPlayerArgument()
                    .suggests(TeleportResponseSuggestion.suggestedStrings())
                    .executes(new TeleportAcceptCommand()))
                .build());

            registerNode.accept(CommandManager.literal("tpdeny")
                .requires(ECPerms.require(ECPerms.Registry.tpdeny, 0))
                .executes(new TeleportDenyCommand()::runDefault)
                .then(CommandUtil.targetPlayerArgument()
                    .suggests(TeleportResponseSuggestion.suggestedStrings())
                    .executes(new TeleportDenyCommand()))
                .build());

            registerNode.accept(CommandManager.literal("tpahere")
                .requires(ECPerms.require(ECPerms.Registry.tpahere, 0))
                .then(CommandUtil.targetPlayerArgument()
                    .executes(new TeleportAskHereCommand()))
                .build());
        }

        if (CONFIG.ENABLE_HOME.getValue()) {
            LiteralArgumentBuilder<ServerCommandSource> homeBuilder = CommandManager.literal("home");
            LiteralArgumentBuilder<ServerCommandSource> homeSetBuilder = CommandManager.literal("set");
            LiteralArgumentBuilder<ServerCommandSource> homeTpBuilder = CommandManager.literal("tp");
            LiteralArgumentBuilder<ServerCommandSource> homeDeleteBuilder = CommandManager.literal("delete");
            LiteralArgumentBuilder<ServerCommandSource> homeListBuilder = CommandManager.literal("list");

            homeSetBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_set, 0))
                .then(argument("home_name", StringArgumentType.word())
                    .executes(new HomeSetCommand()));

            homeTpBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_tp, 0))
                .executes(new HomeCommand()::runDefault)
                .then(argument("home_name", StringArgumentType.word())
                    .suggests(HomeSuggestion.suggestedStrings())
                    .executes(new HomeCommand()));

            homeDeleteBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_delete, 0))
                .then(argument("home_name", StringArgumentType.word())
                    .suggests(HomeSuggestion.suggestedStrings())
                    .executes(new HomeDeleteCommand()));

            homeListBuilder
                .requires(ECPerms.require(ECPerms.Registry.home_tp, 0))
                .executes(ListCommandFactory.create(
                    ECText.getInstance().get("cmd.home.list.start"),
                    "home tp",
                    HomeSuggestion::getSuggestionEntries
                ));

            LiteralCommandNode<ServerCommandSource> homeNode = homeBuilder
                .requires(ECPerms.requireAny(ECPerms.Registry.Group.home_group, 0))
                .build();
            homeNode.addChild(homeTpBuilder.build());
            homeNode.addChild(homeSetBuilder.build());
            homeNode.addChild(homeDeleteBuilder.build());
            homeNode.addChild(homeListBuilder.build());

            registerNode.accept(homeNode);
        }


        //Back
        if (CONFIG.ENABLE_BACK.getValue()) {
            LiteralArgumentBuilder<ServerCommandSource> backBuilder = CommandManager.literal("back");
            backBuilder
                .requires(ECPerms.require(ECPerms.Registry.back, 0))
                .executes(new BackCommand());

            LiteralCommandNode<ServerCommandSource> backNode = backBuilder.build();

            rootNode.addChild(backNode);
            essentialCommandsRootNode.addChild(backNode);
        }

        //Warp
        if (CONFIG.ENABLE_WARP.getValue()) {
            LiteralArgumentBuilder<ServerCommandSource> warpBuilder = CommandManager.literal("warp");
            LiteralArgumentBuilder<ServerCommandSource> warpSetBuilder = CommandManager.literal("set");
            LiteralArgumentBuilder<ServerCommandSource> warpTpBuilder = CommandManager.literal("tp");
            LiteralArgumentBuilder<ServerCommandSource> warpDeleteBuilder = CommandManager.literal("delete");
            LiteralArgumentBuilder<ServerCommandSource> warpListBuilder = CommandManager.literal("list");

            warpSetBuilder
                .requires(ECPerms.require(ECPerms.Registry.warp_set, 4))
                .then(argument("warp_name", StringArgumentType.word())
                    .executes(new WarpSetCommand())
                    .then(argument("requires_permission", BoolArgumentType.bool())
                        .executes(new WarpSetCommand())));

            warpTpBuilder
                .requires(ECPerms.require(ECPerms.Registry.warp_tp, 0))
                .then(argument("warp_name", StringArgumentType.word())
                    .suggests(WarpSuggestion.suggestedStrings())
                    .executes(new WarpTpCommand()));

            warpDeleteBuilder
                .requires(ECPerms.require(ECPerms.Registry.warp_delete, 4))
                .then(argument("warp_name", StringArgumentType.word())
                    .suggests(WarpSuggestion.suggestedStrings())
                    .executes(new WarpDeleteCommand()));

            warpListBuilder
                .requires(ECPerms.require(ECPerms.Registry.warp_tp, 0))
                .executes(ListCommandFactory.create(
                    ECText.getInstance().get("cmd.warp.list.start"),
                    "warp tp",
                    (context) -> ManagerLocator.getInstance().getWorldDataManager().getWarpEntries()
                ));

            LiteralCommandNode<ServerCommandSource> warpNode = warpBuilder
                .requires(ECPerms.requireAny(ECPerms.Registry.Group.warp_group, 0))
                .build();
            warpNode.addChild(warpTpBuilder.build());
            warpNode.addChild(warpSetBuilder.build());
            warpNode.addChild(warpDeleteBuilder.build());
            warpNode.addChild(warpListBuilder.build());

            registerNode.accept(warpNode);
        }

        //Spawn
        if (CONFIG.ENABLE_SPAWN.getValue()) {
            LiteralArgumentBuilder<ServerCommandSource> spawnBuilder = CommandManager.literal("spawn");
            LiteralArgumentBuilder<ServerCommandSource> spawnSetBuilder = CommandManager.literal("set");
            LiteralArgumentBuilder<ServerCommandSource> spawnTpBuilder = CommandManager.literal("tp");

            spawnSetBuilder
                .requires(ECPerms.require(ECPerms.Registry.spawn_set, 4))
                .executes(new SpawnSetCommand());

            SpawnCommand cmd = new SpawnCommand();
            spawnBuilder
                .requires(ECPerms.require(ECPerms.Registry.spawn_tp, 0))
                .executes(cmd);
            spawnTpBuilder
                .requires(ECPerms.require(ECPerms.Registry.spawn_tp, 0))
                .executes(cmd);

            LiteralCommandNode<ServerCommandSource> spawnNode = spawnBuilder.build();
            spawnNode.addChild(spawnSetBuilder.build());
            spawnNode.addChild(spawnTpBuilder.build());

            registerNode.accept(spawnNode);
        }

        if (CONFIG.ENABLE_NICK.getValue()) {
            LiteralArgumentBuilder<ServerCommandSource> nickBuilder = CommandManager.literal("nickname");
            LiteralArgumentBuilder<ServerCommandSource> nickSetBuilder = CommandManager.literal("set");
            LiteralArgumentBuilder<ServerCommandSource> nickClearBuilder = CommandManager.literal("clear");
            LiteralArgumentBuilder<ServerCommandSource> nickRevealBuilder = CommandManager.literal("reveal");

            Predicate<ServerCommandSource> permissionSelf = ECPerms.require(ECPerms.Registry.nickname_self, 2);
            Predicate<ServerCommandSource> permissionOther = ECPerms.require(ECPerms.Registry.nickname_others, 4);
            nickSetBuilder.requires(permissionSelf)
                .then(argument("nickname", TextArgumentType.text())
                    .executes(new NicknameSetCommand())
                ).then(CommandUtil.targetPlayerArgument()
                    .requires(permissionOther)
                    .then(argument("nickname", TextArgumentType.text())
                        .executes(new NicknameSetCommand())
                    ).then(argument("nickname_placeholder_api", StringArgumentType.greedyString())
                        .executes(NicknameSetCommand::runStringToText)
                    )
                ).then(argument("nickname_placeholder_api", StringArgumentType.greedyString())
                    .executes(NicknameSetCommand::runStringToText)
                );

            nickClearBuilder
                .requires(ECPerms.require(ECPerms.Registry.nickname_self, 2))
                .executes(new NicknameClearCommand())
                .then(CommandUtil.targetPlayerArgument()
                    .requires(ECPerms.require(ECPerms.Registry.nickname_others, 4))
                    .executes(new NicknameClearCommand()));

            nickRevealBuilder
                .requires(ECPerms.require(ECPerms.Registry.nickname_reveal, 4))
                .then(argument("player_nickname", StringArgumentType.word())
                    .suggests(NicknamePlayersSuggestion.suggestedStrings())
                    .executes(new RealNameCommand())
                );

            LiteralCommandNode<ServerCommandSource> nickNode = nickBuilder
                .requires(ECPerms.requireAny(ECPerms.Registry.Group.nickname_group, 2))
                .build();
            nickNode.addChild(nickSetBuilder.build());
            nickNode.addChild(nickClearBuilder.build());
            nickNode.addChild(nickRevealBuilder.build());

            registerNode.accept(nickNode);
        }

        if (CONFIG.ENABLE_RTP.getValue()) {
            registerNode.accept(CommandManager.literal("randomteleport")
                .requires(ECPerms.require(ECPerms.Registry.randomteleport, 2))
                .executes(new RandomTeleportCommand())
                .build());

            dispatcher.register(CommandManager.literal("rtp")
                .requires(ECPerms.require(ECPerms.Registry.randomteleport, 2))
                .executes(new RandomTeleportCommand())
            );
        }

        if (CONFIG.ENABLE_FLY.getValue()) {
            registerNode.accept(CommandManager.literal("fly")
                .requires(ECPerms.require(ECPerms.Registry.fly_self, 2))
                .executes(new FlyCommand())
                .then(CommandUtil.targetPlayerArgument()
                    .requires(ECPerms.require(ECPerms.Registry.fly_others, 4))
                    .then(argument("flight_enabled", BoolArgumentType.bool())
                        .executes(new FlyCommand())))
                .build());
        }

        if (CONFIG.ENABLE_INVULN.getValue()) {
            registerNode.accept(
                CommandManager.literal("invuln")
                    .requires(ECPerms.require(ECPerms.Registry.invuln_self, 2))
                    .executes(new InvulnCommand())
                    .then(CommandUtil.targetPlayerArgument()
                        .requires(ECPerms.require(ECPerms.Registry.invuln_others, 4))
                        .then(argument("invuln_enabled", BoolArgumentType.bool())
                            .executes(new InvulnCommand())))
                    .build());
        }

        if (CONFIG.ENABLE_WORKBENCH.getValue()) {
            registerNode.accept(CommandManager.literal("workbench")
                .requires(ECPerms.require(ECPerms.Registry.workbench, 0))
                .executes(new WorkbenchCommand())
                .build());

            registerNode.accept(CommandManager.literal("stonecutter")
                .requires(ECPerms.require(ECPerms.Registry.workbench, 0))
                .executes(new StonecutterCommand())
                .build());

            registerNode.accept(CommandManager.literal("grindstone")
                .requires(ECPerms.require(ECPerms.Registry.workbench, 0))
                .executes(new GrindstoneCommand())
                .build());
        }

        if (CONFIG.ENABLE_ANVIL.getValue()) {
            registerNode.accept(CommandManager.literal("anvil")
                .requires(ECPerms.require(ECPerms.Registry.anvil, 0))
                .executes(new AnvilCommand())
                .build());
        }

        if (CONFIG.ENABLE_ENDERCHEST.getValue()) {
            registerNode.accept(CommandManager.literal("enderchest")
                    .requires(ECPerms.require(ECPerms.Registry.enderchest, 0))
                    .executes(new EnderchestCommand())
                .build());
        }

        if (CONFIG.ENABLE_WASTEBIN.getValue()) {
            registerNode.accept(CommandManager.literal("wastebin")
                .requires(ECPerms.require(ECPerms.Registry.wastebin, 0))
                .executes(new WastebinCommand())
                .build());
        }

        if (CONFIG.ENABLE_TOP.getValue()) {
            registerNode.accept(CommandManager.literal("top")
                .requires(ECPerms.require(ECPerms.Registry.top, 2))
                .executes(new TopCommand())
                .build());
        }

        if (CONFIG.ENABLE_GAMETIME.getValue()) {
            registerNode.accept(CommandManager.literal("gametime")
                .requires(ECPerms.require(ECPerms.Registry.gametime, 0))
                .executes(new GametimeCommand())
                .build());
        }

        LiteralCommandNode<ServerCommandSource> configNode = CommandManager.literal("config")
            .requires(ECPerms.requireAny(ECPerms.Registry.Group.config_group, 4))
            .then(CommandManager.literal("reload")
                .executes((context) -> {
                    CONFIG.loadOrCreateProperties();
                    context.getSource().sendFeedback(
                        TextUtil.concat(
                            ECText.getInstance().getText("essentialcommands.fullprefix"),
                            ECText.getInstance().getText("cmd.config.reload")
                        ),
                        true
                    );
                    return 1;
                }).requires(
                    ECPerms.require(ECPerms.Registry.config_reload, 4)
                ).build())
            .then(CommandManager.literal("display")
                .requires(ECPerms.require(ECPerms.Registry.config_reload, 4))
                .executes((context) -> {
                    CONFIG.loadOrCreateProperties();
                    context.getSource().sendFeedback(
                        CONFIG.stateAsText(),
                        false
                    );
                    return 1;
                })
                .then(CommandManager.argument("config_property", StringArgumentType.word())
                    .suggests(ListSuggestion.of(CONFIG::getPublicFieldNames))
                    .executes(context -> {
                        try {
                            context.getSource().sendFeedback(CONFIG.getFieldValueAsText(
                                StringArgumentType.getString(context, "config_property")
                            ), false);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }

                        return 1;
                    })
                )
            ).build();

        essentialCommandsRootNode.addChild(configNode);

        if (CONFIG.ENABLE_ESSENTIALSX_CONVERT.getValue()) {
            essentialCommandsRootNode.addChild(CommandManager.literal("convertEssentialsXPlayerHomes")
                .requires(source -> source.hasPermissionLevel(4))
                .executes((source) -> {
                    Path mcDir = source.getSource().getServer().getRunDirectory().toPath();
                    try {
                        EssentialsXParser.convertPlayerDataDir(
                            mcDir.resolve("plugins/Essentials/userdata").toFile(),
                            mcDir.resolve("world/modplayerdata").toFile(),
                            source.getSource().getServer()
                        );
                        source.getSource().sendFeedback(new LiteralText("Successfully converted data dirs."), CONFIG.BROADCAST_TO_OPS.getValue());
                    } catch (NotDirectoryException | FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }).build()
            );
        }

        rootNode.addChild(essentialCommandsRootNode);
    }

}