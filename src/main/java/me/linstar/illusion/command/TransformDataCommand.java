package me.linstar.illusion.command;

import com.mojang.brigadier.CommandDispatcher;
import me.linstar.illusion.data.LegacyIllusionSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class TransformDataCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("illusion").requires(stack -> stack.hasPermission(4))
                .then(Commands.literal("transform_legacy_data").executes(ctx -> TransformDataCommand.execute(ctx.getSource())))
        );
    }

    public static int execute(CommandSourceStack stack) {
        var level = stack.getLevel();
        stack.sendSystemMessage(Component.literal("Migrate the old version of Illusion data in this level. This will cause lag.").withStyle(ChatFormatting.GRAY));
        LegacyIllusionSavedData.get(level).transform(level);
        stack.sendSystemMessage(Component.literal("Finished").withStyle(ChatFormatting.GREEN));
        return 0;
    }
}
