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
import com.jaoafa.javajaotan2.lib.DiscordMinecraftLink;
import com.jaoafa.javajaotan2.lib.JavajaotanData;
import com.jaoafa.javajaotan2.lib.MinecraftPermGroup;
import com.jaoafa.javajaotan2.lib.MySQLDBManager;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Event_UserDedDB extends ListenerAdapter {
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        User user = event.getUser();

        if (event.getGuild().getIdLong() != Main.getConfig().getGuildId()) {
            return;
        }

        MySQLDBManager manager = JavajaotanData.getMainMySQLDBManager();
        try {
            Connection conn = manager.getConnection();
            DiscordMinecraftLink dml = DiscordMinecraftLink.get(user.getIdLong());
            if (dml == null) {
                Main.getLogger().warn("Event_UserDedDB#onGuildMemberRemove: DiscordMinecraftLink is null. user: " + user.getId());
                return;
            }
            MinecraftPermGroup group = new MinecraftPermGroup(dml.getMinecraftUUID());

            PreparedStatement stmt = conn.prepareStatement("UPDATE discordlink SET disabled = ?, dead_perm = ?, dead_at = CURRENT_TIMESTAMP WHERE disid = ?");
            stmt.setBoolean(1, true);
            stmt.setString(2, group.getGroup().name());
            stmt.setString(3, user.getId());
            boolean bool = stmt.execute();
            if (!bool) {
                Main.getLogger().info("Event_UserDedDB: dead_at update failed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
