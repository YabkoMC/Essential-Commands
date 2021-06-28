package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.access.PlayerEntityAccess;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class NicknameClearCommand implements Command<ServerCommandSource>  {
    public NicknameClearCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //Store command sender
        ServerPlayerEntity senderPlayerEntity = context.getSource().getPlayer();
        ServerPlayerEntity targetPlayer = null;
        try {
            targetPlayer = EntityArgumentType.getPlayer(context, "target");
        } catch (IllegalArgumentException e) {
            targetPlayer = senderPlayerEntity;
        }

        PlayerEntityAccess targetPlayerEntityAccess = (PlayerEntityAccess) targetPlayer;
        targetPlayerEntityAccess.getEcPlayerData().setNickname(null);

        //inform command sender that the nickname has been set
        senderPlayerEntity.sendSystemMessage(
            new LiteralText("")
                .append(new LiteralText("Nickname set to '").setStyle(Config.FORMATTING_DEFAULT))
                .append(new LiteralText(senderPlayerEntity.getGameProfile().getName())
                ).append(new LiteralText("'.").setStyle(Config.FORMATTING_DEFAULT))
            , new UUID(0, 0));

        return 1;
    }

}