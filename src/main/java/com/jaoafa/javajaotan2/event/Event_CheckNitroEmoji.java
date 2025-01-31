/*
 * jaoLicense
 *
 * Copyright (c) 2022 jao Minecraft Server
 *
 * The following license applies to this project: jaoLicense
 *
 * Japanese: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE.md
 * English: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE-en.md
 */

package com.jaoafa.javajaotan2.event;

import com.jaoafa.javajaotan2.Main;
import com.jaoafa.javajaotan2.lib.JavajaotanLibrary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.emoji.EmojiAddedEvent;
import net.dv8tion.jda.api.events.emoji.EmojiRemovedEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

public class Event_CheckNitroEmoji extends ListenerAdapter {
    List<RichCustomEmoji> guildEmojis = null;

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (Main.getConfig().getGuildId() != event.getGuild().getIdLong()) {
            return;
        }
        Message message = event.getMessage();
        Member member = event.getMember();
        if (member == null) {
            return;
        }
        Guild guild = event.getGuild();
        if (guildEmojis == null) {
            guildEmojis = guild.retrieveEmojis().complete();
        }

        List<CustomEmoji> emojis = message.getMentions().getCustomEmojis();
        // GIF絵文字を使用しているか
        boolean usingAnimated = emojis.stream().anyMatch(CustomEmoji::isAnimated);
        // いずれかの絵文字が、このGuildにはない絵文字を利用しているかどうか
        boolean isOtherGuildEmoji = emojis
            .stream()
            .anyMatch(e -> guildEmojis
                .stream()
                .noneMatch(guildEmoji -> guildEmoji.getIdLong() == e.getIdLong()));

        Role roleNitrotan = guild.getRoleById(795153241385861130L);
        if (roleNitrotan == null) {
            Main.getLogger().error("Nitrotan role is not found");
            return;
        }

        if (!usingAnimated && !isOtherGuildEmoji) {
            return;
        }
        boolean isNitrotan = JavajaotanLibrary.isGrantedRole(member, roleNitrotan);

        if (!isNitrotan) {
            String title = usingAnimated ? "usingAnimated" : "isOtherGuildEmoji";
            String desc = usingAnimated ? "アニメーション絵文字を使用した投稿を行ったため、" : "外部サーバの絵文字を使用した投稿を行ったため、";
            notifyConnection(member, "Nitrotan役職付与 (%s)".formatted(title), "%sNitrotan役職を付与しました。".formatted(desc));
            guild.addRoleToMember(member, roleNitrotan).queue();
        }

        Path path = Path.of("nitrotan.json");
        try {
            JSONObject object = new JSONObject();
            if (Files.exists(path)) {
                object = new JSONObject(Files.readString(path));
            }
            object.put(member.getId(), new Date().getTime());
            Files.writeString(path, object.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEmojiAdded(@NotNull EmojiAddedEvent event) {
        if (Main.getConfig().getGuildId() != event.getGuild().getIdLong()) {
            return;
        }
        Main.getLogger().info("Emoji added (Cache refresh): " + event.getEmoji().getName());
        event.getGuild().retrieveEmojis().queue(
            emojis -> guildEmojis = emojis,
            Throwable::printStackTrace
        );
    }

    @Override
    public void onEmojiRemoved(@NotNull EmojiRemovedEvent event) {
        if (Main.getConfig().getGuildId() != event.getGuild().getIdLong()) {
            return;
        }
        Main.getLogger().info("Emoji removed (Cache refresh): " + event.getEmoji().getName());
        event.getGuild().retrieveEmojis().queue(
            emojis -> guildEmojis = emojis,
            Throwable::printStackTrace
        );
    }

    private void notifyConnection(Member member, String title, String description) {
        TextChannel channel = Main.getJDA().getTextChannelById(891021520099500082L);

        if (channel == null) return;

        EmbedBuilder embed = new EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .setColor(Color.LIGHT_GRAY)
            .setAuthor(member.getUser().getAsTag(), "https://discord.com/users/" + member.getId(), member.getUser().getEffectiveAvatarUrl());

        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
