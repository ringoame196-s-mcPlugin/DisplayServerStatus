package com.github.ringoame196_s_mcPlugin.commands

import com.github.ringoame196_s_mcPlugin.managers.StatusManager
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

class Command(plugin: Plugin) : CommandExecutor {
    private val statusManager = StatusManager(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false
        val subCommand = args[0]

        when (subCommand) {
            CommandConst.UPDATE_COMMAND -> updateCommand(sender)
            else -> {
                val message = "${ChatColor.RED}コマンドが間違っています"
                sender.sendMessage(message)
            }
        }

        return true
    }

    private fun updateCommand(sender: CommandSender) {
        val message = "${ChatColor.GOLD}ステータス更新しました"
        sender.sendMessage(message) // メッセージを送る
        statusManager.update(true) // オンラインステータス更新
    }
}
