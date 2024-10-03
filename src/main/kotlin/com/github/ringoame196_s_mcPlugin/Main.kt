package com.github.ringoame196_s_mcPlugin

import org.bukkit.plugin.java.JavaPlugin
import com.github.ringoame196_s_mcPlugin.commands.Command
import com.github.ringoame196_s_mcPlugin.commands.TabCompleter

class Main : JavaPlugin() {
    override fun onEnable() {
        super.onEnable()
        val plugin = this
        val command = getCommand("displayserverstatus")
        command!!.setExecutor(Command())
        command.tabCompleter = TabCompleter()
    }
}
