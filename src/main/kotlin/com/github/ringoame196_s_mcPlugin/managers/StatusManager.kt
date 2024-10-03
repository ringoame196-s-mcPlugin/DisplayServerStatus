package com.github.ringoame196_s_mcPlugin.managers

import com.github.ringoame196_s_mcPlugin.Data
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.entities.TextChannel
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import java.awt.Color
import java.io.IOException
import java.net.InetAddress
import java.time.OffsetDateTime

class StatusManager(private val plugin: Plugin) {
    private val config = plugin.config // configファイル
    private val discordManager = DiscordManager()

    fun startFixedTermUpdate(updateFrequency: Int) {
        val time = (updateFrequency * 20 * 60).toLong() // tick -> 秒 -> 分
        object : BukkitRunnable() {
            override fun run() {
                update(true) // オンラインでアップデート
            }
        }.runTaskTimer(plugin, 0L, time) // 1秒間隔 (20 ticks) でタスクを実行
    }

    fun update(online: Boolean) {
        val jda = Data.JDA ?: return
        val channelID = config.getString("channelID")
        val channel = acquisitionChannel(jda, channelID)
        if (channel == null) {
            val message = "[エラー] チャンネルIDが取得できませんでした"
            println(message)
            return
        }
        val messageID = config.getString("messageID") // メッセージID
        val now = OffsetDateTime.now() // 現在時刻

        val embed = if (online) { // オンラインにするかオフラインするかでembed自体を変える
            makeSetOnlineEmbed(now) // オンラインembed
        } else {
            makeSetOfflineEmbed(now) // オフラインembed
        }

        if (messageID == null || messageID == "") {
            sendEmbed(channel, embed) // メッセージを送る
        } else {
            editEmbed(channel, messageID, embed) // 編集する
        }
    }

    private fun acquisitionChannel(jda: JDA, channelID: String?): TextChannel? {
        channelID ?: return null
        return jda.getTextChannelById(channelID) // テキストチャンネルを取得
    }

    private fun makeSetOnlineEmbed(timeString: OffsetDateTime): MessageEmbed {
        val title = "${Data.STATUS_TITLE}${Data.STATUS_ONLINE}"
        val color = Color.GREEN
        val onlinePlayerSize = Bukkit.getOnlinePlayers().size // オンライン人数
        val ping = acquisitionPing() ?: "取得失敗" // ping取得
        val onlinePlayerField = Field(":teddy_bear: オンライン人数", "${onlinePlayerSize}人", false) // オンライン人数
        val pingField = Field(":hourglass: PING値", "${ping}ms", false) // ping値
        val fields = mutableListOf(onlinePlayerField, pingField)

        return discordManager.makeEmbed(title = title, color = color, timestamp = timeString, fields = fields)
    }

    private fun makeSetOfflineEmbed(timeString: OffsetDateTime): MessageEmbed {
        val title = "${Data.STATUS_TITLE}${Data.STATUS_OFFLINE}"
        val color = Color.RED
        return discordManager.makeEmbed(title = title, color = color, timestamp = timeString)
    }

    private fun sendEmbed(channel: TextChannel, embed: MessageEmbed) {
        // ステータスEmbedを送る
        channel.sendMessageEmbeds(embed).queue { message ->
            val messageId = message.id
            saveMessageID(messageId) // messageIDをconfigに設定
        }
    }

    private fun editEmbed(channel: TextChannel, messageID: String, embed: MessageEmbed) {
        // 既にあるステータスEmbedを更新する
        channel.editMessageEmbedsById(messageID, embed).queue(
            { /* 成功時の処理はなし */ },
            { // 失敗時にembedを送る
                sendEmbed(channel, embed)
            }
        )
    }

    private fun saveMessageID(messageID: String) {
        config.set("messageID", messageID)

        // configファイル更新
        val path = "${plugin.dataFolder}/config.yml"
        config.save(path)
        config.load(path)
    }

    private fun acquisitionPing(): Long? {
        val ip = "8.8.8.8" // Google Public DNS
        try {
            val inet = InetAddress.getByName(ip)
            val startTime = System.currentTimeMillis() // Pingの開始時間
            val reachable = inet.isReachable(5000) // タイムアウト5秒
            val endTime = System.currentTimeMillis() // Pingの終了時間

            return if (reachable) {
                endTime - startTime // Ping時間（ミリ秒）
            } else {
                null // 到達できなかった場合
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null // エラーが発生した場合
        }
    }
}
