/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.commands.SharedSuggestionProvider
 *  net.minecraft.commands.arguments.EntityArgument
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.item.ItemStack
 */
package cn.solo.sanguosha.command;

import cn.solo.sanguosha.config.GeneralManager;
import cn.solo.sanguosha.item.GeneralCardItem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class SgsCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"sgs").then(((LiteralArgumentBuilder)Commands.m_82127_((String)"reload").requires(s -> s.m_6761_(2))).executes(ctx -> {
            int count = GeneralManager.reload();
            ((CommandSourceStack)ctx.getSource()).m_288197_(() -> Component.m_237113_((String)("\u4e09\u56fd\u6740\u914d\u7f6e\u91cd\u8f7d\u5b8c\u6210\uff1a" + count + " \u540d\u6b66\u5c06")), true);
            return count;
        }))).then(((LiteralArgumentBuilder)Commands.m_82127_((String)"give").requires(s -> s.m_6761_(2))).then(Commands.m_82129_((String)"player", (ArgumentType)EntityArgument.m_91466_()).then(((RequiredArgumentBuilder)Commands.m_82129_((String)"general", (ArgumentType)StringArgumentType.word()).suggests((ctx, b) -> SharedSuggestionProvider.m_82970_(GeneralManager.ids(), (SuggestionsBuilder)b)).executes(ctx -> SgsCommands.give((CommandSourceStack)ctx.getSource(), EntityArgument.m_91474_((CommandContext)ctx, (String)"player"), StringArgumentType.getString((CommandContext)ctx, (String)"general"), 1))).then(Commands.m_82129_((String)"count", (ArgumentType)IntegerArgumentType.integer((int)1, (int)64)).executes(ctx -> SgsCommands.give((CommandSourceStack)ctx.getSource(), EntityArgument.m_91474_((CommandContext)ctx, (String)"player"), StringArgumentType.getString((CommandContext)ctx, (String)"general"), IntegerArgumentType.getInteger((CommandContext)ctx, (String)"count"))))))));
    }

    private static int give(CommandSourceStack source, ServerPlayer player, String id, int count) {
        if (GeneralManager.get(id).isEmpty()) {
            source.m_81352_((Component)Component.m_237113_((String)("\u672a\u77e5\u6b66\u5c06\uff1a" + id)));
            return 0;
        }
        ItemStack stack = GeneralCardItem.create(id, count);
        if (!player.m_150109_().m_36054_(stack)) {
            player.m_36176_(stack, false);
        }
        source.m_288197_(() -> Component.m_237113_((String)("\u5df2\u7ed9\u4e88 " + player.m_7755_().getString() + " \u6b66\u5c06\u724c " + id + " x" + count)), true);
        return count;
    }

    private SgsCommands() {
    }
}

