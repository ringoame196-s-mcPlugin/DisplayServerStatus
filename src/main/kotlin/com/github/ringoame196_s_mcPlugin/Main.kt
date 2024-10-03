package com.github.ringoame196_s_mcPlugin

import com.github.ringoame196_s_mcPlugin.commands.Command
import com.github.ringoame196_s_mcPlugin.commands.TabCompleter
import com.github.ringoame196_s_mcPlugin.managers.DiscordManager
import com.github.ringoame196_s_mcPlugin.managers.StatusManager
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        super.onEnable()
        val plugin = this
        val discordManager = DiscordManager()
        val statusManager = StatusManager(plugin)
        saveDefaultConfig() // configファイル生成
        val updateFrequency = config.getInt("updateFrequency") // 更新頻度

        discordManager.bootBOT(plugin) {
            // DiscordBOTが起動した後に実行される
            statusManager.startFixedTermUpdate(updateFrequency) // 定期アップデート開始
        }

        // コマンド登録
        val command = getCommand("displayserverstatus")
        command!!.setExecutor(Command(plugin))
        command.tabCompleter = TabCompleter()
    }

    override fun onDisable() {
        val plugin = this
        if (Data.JDA != null) {
            val statusManager = StatusManager(plugin)
            statusManager.update(false) // ステータスをオフラインにする
            Data.JDA?.shutdown() // botをシャットダウンする
        }
        super.onDisable()
    }
}
