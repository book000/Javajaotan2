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
import com.jaoafa.javajaotan2.lib.WatchStickers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;
import net.dv8tion.jda.api.events.sticker.GuildStickerAddedEvent;
import net.dv8tion.jda.api.events.sticker.GuildStickerRemovedEvent;
import net.dv8tion.jda.api.events.sticker.update.GuildStickerUpdateDescriptionEvent;
import net.dv8tion.jda.api.events.sticker.update.GuildStickerUpdateNameEvent;
import net.dv8tion.jda.api.events.sticker.update.GuildStickerUpdateTagsEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class Event_WatchStickers extends ListenerAdapter {
    @Override
    public void onGuildStickerAdded(@NotNull GuildStickerAddedEvent event) {
        JDA jda = event.getJDA();
        Guild guild = event.getGuild();
        GuildSticker sticker = guild.retrieveSticker(event.getSticker()).complete();
        User user = sticker.getOwner();
        if (user == null) {
            Main.getLogger().warn("Event_WatchStickers#onGuildStickerAdded: User is null");
            return;
        }

        Optional<WatchStickers.StickerGuild> optGuild = Main.getWatchStickers().getStickerGuild(guild);
        if (optGuild.isEmpty()) {
            return;
        }

        WatchStickers.StickerGuild stickerGuild = optGuild.get();
        long log_channel_id = stickerGuild.getLogChannelId();
        TextChannel log_channel = jda.getTextChannelById(log_channel_id);
        if (log_channel == null) {
            return;
        }
        MessageEmbed embed = new EmbedBuilder()
            .setTitle(":new: NEW STICKER : %s".formatted(sticker.getName()))
            .setThumbnail(sticker.getIconUrl())
            .addField("Description", sticker.getDescription(), false)
            .addField("Tags", String.join(", ", sticker.getTags()), false)
            .setAuthor(user.getAsTag(), "https://discord.com/users/" + user.getId(), user.getAvatarUrl())
            .setTimestamp(Instant.now())
            .build();
        log_channel.sendMessageEmbeds(embed).queue();
    }

    @Override
    public void onGuildStickerUpdateName(@NotNull GuildStickerUpdateNameEvent event) {
        JDA jda = event.getJDA();
        Guild guild = event.getGuild();
        GuildSticker sticker = guild.retrieveSticker(event.getSticker()).complete();
        User user = sticker.getOwner();
        if (user == null) {
            Main.getLogger().warn("Event_WatchStickers#onGuildStickerUpdateName: User is null");
            return;
        }


    }

    @Override
    public void onGuildStickerUpdateDescription(@NotNull GuildStickerUpdateDescriptionEvent event) {

    }

    @Override
    public void onGuildStickerUpdateTags(@NotNull GuildStickerUpdateTagsEvent event) {

    }

    void onGuildStickerUpdate(JDA jda, GuildSticker sticker, StickerUpdateRecord record) {
        Optional<WatchStickers.StickerGuild> optGuild = Main.getWatchStickers().getStickerGuild(sticker.getGuild());
        if (optGuild.isEmpty()) {
            return;
        }

        WatchStickers.StickerGuild stickerGuild = optGuild.get();
        long log_channel_id = stickerGuild.getLogChannelId();
        TextChannel log_channel = jda.getTextChannelById(log_channel_id);
        if (log_channel == null) {
            return;
        }

        List<AuditLogEntry> entries = guild.retrieveAuditLogs().type(ActionType.).limit(5).complete();
        User user = null;
        if (!entries.isEmpty()) {
            for (AuditLogEntry entry : entries) {
                if (entry.getTargetIdLong() != emoji.getIdLong()) {
                    continue;
                }
                user = entry.getUser();
            }
        }


    }

    @Override
    public void onGuildStickerRemoved(@NotNull GuildStickerRemovedEvent event) {

    }

    record StickerUpdateRecord(
        EmojiUpdateType type,
        String oldValue,
        String newValue
    ) {
    }

    enum EmojiUpdateType {
        NAME,
        DESCRIPTION,
        TAGS
    }


}
