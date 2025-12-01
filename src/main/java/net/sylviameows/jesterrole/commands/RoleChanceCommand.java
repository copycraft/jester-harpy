package net.sylviameows.jesterrole.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.sylviameows.jesterrole.Jester;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

public class RoleChanceCommand {
    public static void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("jester:setChance").then(CommandManager.argument("chance", DoubleArgumentType.doubleArg(0,1)).executes(
                context -> setChance(context.getArgument("chance", Double.class), context.getSource())
        )).executes(
                context -> unfinishedCommand(context.getSource())
        ));
    }

    private static int unfinishedCommand(ServerCommandSource source) {
        source.sendMessage(Text.translatable("command.jester.rolechance.missing_arg").withColor(0xFFAAAA));
        return 0;
    }

    private static int setChance(Double chance, ServerCommandSource source) {
        Jester.CHANCE = chance;
        Text percent = Text.literal(NumberFormat.getPercentInstance().format(chance)).withColor(Jester.ROLE_COLOR);
        source.sendMessage(Text.translatable("command.jester.rolechance.set", percent));
        return 1;
    }
}
