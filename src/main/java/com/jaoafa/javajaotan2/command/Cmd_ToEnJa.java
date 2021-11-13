/*
 * jaoLicense
 *
 * Copyright (c) 2021 jao Minecraft Server
 *
 * The following license applies to this project: jaoLicense
 *
 * Japanese: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE.md
 * English: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE-en.md
 */

package com.jaoafa.javajaotan2.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.jda.JDACommandSender;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.javajaotan2.lib.CommandPremise;
import com.jaoafa.javajaotan2.lib.JavajaotanCommand;
import com.jaoafa.javajaotan2.lib.Translate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;

public class Cmd_ToEnJa implements CommandPremise {
    @Override
    public JavajaotanCommand.Detail details() {
        return new JavajaotanCommand.Detail(
            "toenja",
            "Google翻訳を用いて英語へ翻訳をしたあと、日本語へ翻訳を行います。"
        );
    }

    @Override
    public JavajaotanCommand.Cmd register(Command.Builder<JDACommandSender> builder) {
        return new JavajaotanCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "Google翻訳を用いて英語へ翻訳をしたあと、日本語へ翻訳を行います。")
                .argument(StringArgument.greedy("text"))
                .handler(context -> execute(context, this::translateEnJa))
                .build()
        );
    }

    private void translateEnJa(@NotNull Guild guild, @NotNull MessageChannel channel, @NotNull Member member, @NotNull Message message, @NotNull CommandContext<JDACommandSender> context) {
        String text = context.get("text");

        Translate.Language lang1 = Translate.Language.EN;
        Translate.Language lang2 = Translate.Language.JA;

        Translate.TranslateResult result1 = Translate.translate(
            Translate.Language.UNKNOWN,
            lang1,
            text
        );
        if (result1 == null) {
            message.reply("翻訳に失敗しました。").queue();
            return;
        }

        Translate.TranslateResult result2 = Translate.translate(
            lang1,
            lang2,
            text
        );
        if (result2 == null) {
            message.reply("翻訳に失敗しました。").queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("翻訳が成功しました:clap:")
            .addField("`%s` -> `%s`".formatted(result1.from().toString(), lang1.toString()),
                "```%s```".formatted(text),
                false)
            .addField("`%s` -> `%s`".formatted(lang1.toString(), lang2.toString()),
                "```%s```".formatted(text),
                false)
            .setColor(Color.PINK)
            .setTimestamp(Instant.now());
        message.replyEmbeds(embed.build()).queue();
    }
}
