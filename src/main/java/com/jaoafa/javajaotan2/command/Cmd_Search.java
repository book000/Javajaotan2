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

package com.jaoafa.javajaotan2.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jaoafa.javajaotan2.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cmd_Search extends Command {
    public Cmd_Search() {
        this.name = "search";
        this.help = "Google検索を用いて検索を行います。";
        this.arguments = "<SearchWord...>";
    }

    @Override
    protected void execute(CommandEvent event) {
        Message message = event.getMessage();
        String text = event.getArgs();
        if (text.trim().isEmpty()) {
            message.reply("検索する文字列を指定してください。").queue();
            return;
        }

        String gcpKey = Main.getConfig().getGCPKey();
        String cx = Main.getConfig().getCustomSearchCX();
        if (gcpKey == null || cx == null) {
            message.reply("Google Cloud Platform Key または Custom Search 検索エンジン API が定義されていないため、このコマンドを使用できません。").queue();
            return;
        }
        SearchResult result = customSearch(gcpKey, cx, text);
        if (result == null) {
            message.reply("検索に失敗しました。").queue();
            return;
        }
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("「%s」の検索結果".formatted(text))
            .setDescription("検索時間: %s, 累計件数: %s".formatted(result.searchTime(), result.totalResult()))
            .setColor(Color.GREEN)
            .setTimestamp(Instant.now());
        for (SearchResultItem item : result.items()) {
            embed.addField(
                decode(item.htmlTitle().replaceAll("</?b>", "**")),
                decode(item.htmlSnippet().replaceAll("</?b>", "**")),
                false
            );
            if (embed.getFields().size() >= 5) {
                break;
            }
        }
        message.replyEmbeds(embed.build()).queue();
    }

    SearchResult customSearch(String gcpKey, String cx, String text) {
        String url = "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s".formatted(gcpKey, cx, text);
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            JSONObject object;
            try (Response response = client.newCall(request).execute()) {
                ResponseBody body = response.body();
                if (body == null) {
                    return null;
                }
                object = new JSONObject(body.string());
            }
            if (!object.has("searchInformation") || !object.has("items")) {
                return null;
            }
            LinkedList<SearchResultItem> items = new LinkedList<>();
            for (int i = 0; i < object.getJSONArray("items").length(); i++) {
                JSONObject item = object.getJSONArray("items").getJSONObject(i);
                items.add(new SearchResultItem(
                    item.getString("title"),
                    item.getString("htmlTitle"),
                    item.getString("link"),
                    item.getString("htmlFormattedUrl"),
                    item.getString("htmlSnippet")
                ));
            }
            return new SearchResult(
                object.getJSONObject("searchInformation").getString("formattedSearchTime"),
                object.getJSONObject("searchInformation").getString("formattedTotalResults"),
                items
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decode(String str) {
        Pattern pattern = Pattern.compile("&#(\\d+);|&#([\\da-fA-F]+);");
        Matcher matcher = pattern.matcher(str);
        StringBuilder sb = new StringBuilder();
        char buf;
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                buf = (char) Integer.parseInt(matcher.group(1));
            } else {
                buf = (char) Integer.parseInt(matcher.group(2), 16);
            }
            matcher.appendReplacement(sb, Character.toString(buf));
        }
        matcher.appendTail(sb);
        Map<String, String> patterns = Map.of(
            "&lt;", "<",
            "&gt;", ">",
            "&amp;", "&",
            "&quot;", "\"",
            "&#x27;", "'",
            "&#x60;", "`",
            "&nbsp;", " "
        );
        String temp = sb.toString();
        for (Map.Entry<String, String> entry : patterns.entrySet()) {
            temp = temp.replaceAll(entry.getKey(), entry.getValue());
        }
        return temp;
    }


    record SearchResult(
        String searchTime,
        String totalResult,
        LinkedList<SearchResultItem> items
    ) {
    }

    record SearchResultItem(
        String title,
        String htmlTitle,
        String link,
        String htmlLink,
        String htmlSnippet
    ) {
    }
}
