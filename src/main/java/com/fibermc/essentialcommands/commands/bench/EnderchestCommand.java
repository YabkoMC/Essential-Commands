package com.fibermc.essentialcommands.commands.bench;

import com.fibermc.essentialcommands.ECText;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class EnderchestCommand implements Command<ServerCommandSource> {

    public EnderchestCommand() {
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity senderPlayer = source.getPlayer();

        senderPlayer.openHandledScreen(createScreenHandlerFactory(senderPlayer.getEntityWorld(), senderPlayer.getBlockPos()));
        senderPlayer.incrementStat(Stats.OPEN_ENDERCHEST);

        source.sendFeedback(
            ECText.getInstance().getText("cmd.enderchest.feedback").setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
            CONFIG.BROADCAST_TO_OPS.getValue()
        );

        return 0;
    }

    private @NotNull NamedScreenHandlerFactory createScreenHandlerFactory(World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory(
            (syncId, inventory, player) ->
                GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, player.getEnderChestInventory()),
            ECText.getInstance().getText("cmd.enderchest.container_ui_name")
        );
    }

}
