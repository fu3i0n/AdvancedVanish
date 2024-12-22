package me.quantiom.advancedvanish.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.HelpCommand
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.quantiom.advancedvanish.AdvancedVanish
import me.quantiom.advancedvanish.config.Config
import me.quantiom.advancedvanish.hook.HooksManager
import me.quantiom.advancedvanish.permission.PermissionsManager
import me.quantiom.advancedvanish.state.VanishStateManager
import me.quantiom.advancedvanish.util.AdvancedVanishAPI
import me.quantiom.advancedvanish.util.color
import me.quantiom.advancedvanish.util.isVanished
import me.quantiom.advancedvanish.util.sendComponentMessage
import me.quantiom.advancedvanish.util.sendConfigMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("vanish|advancedvanish|v")
object VanishCommand : BaseCommand() {
    private val HELP_MESSAGE =
        listOf(
            "",
            "<red><strikethrough>----------</strikethrough><bold> AdvancedVanish </bold><red><strikethrough>----------",
            "<red>/vanish <dark_gray>- <white>Toggle vanish.",
            "<red>/vanish version <dark_gray>- <white>Shows the version of the plugin.",
            "<red>/vanish reload <dark_gray>- <white>Reloads the config and hooks.",
            "<red>/vanish interact <dark_gray>- <white>Toggles interacting with blocks while in vanish.",
            "<red>/vanish priority <dark_gray>- <white>Displays your vanish priority.",
            "<red>/vanish list <dark_gray>- <white>Displays a list of vanished players.",
            "<red>/vanish status <player> <dark_gray>- <white>Check if a player is in vanish.",
            "<red>/vanish set <player> <on/off> <dark_gray>- <white>Set another player's vanish.",
            "<red>/vanish toggle <player> <dark_gray>- <white>Toggle another player's vanish.",
            "<red><strikethrough>-----------------------------------",
            "",
        )

    @Default
    private fun onVanishCommand(player: Player) {
        if (!permissionCheck(player, "permissions.vanish", "advancedvanish.vanish")) return

        if (player.isVanished()) {
            AdvancedVanishAPI.unVanishPlayer(player)
            player.sendConfigMessage("vanish-off")
        } else {
            AdvancedVanishAPI.vanishPlayer(player)
            player.sendConfigMessage("vanish-on")
        }
    }

    @Subcommand("version")
    private fun onVersionCommand(sender: CommandSender) {
        if (!permissionCheck(sender, "permissions.version-command", "advancedvanish.version-command")) return

        sender.sendConfigMessage(
            "version-command",
            "%version%" to "v${AdvancedVanish.instance!!.description.version}",
        )
    }

    @Subcommand("reload|reloadconfig")
    private fun onReloadCommand(sender: CommandSender) {
        if (!permissionCheck(
                sender,
                "permissions.reload-config-command",
                "advancedvanish.reload-config-command",
            )
        ) {
            return
        }

        Config.reload().also { sender.sendConfigMessage("config-reloaded") }

        HooksManager.reloadHooks()
        PermissionsManager.setupPermissionsHandler()
    }

    @Subcommand("interact")
    private fun onInteractCommand(player: Player) {
        if (!permissionCheck(player, "permissions.interact-command", "advancedvanish.interact-command")) return

        if (!player.isVanished()) {
            player.sendConfigMessage("must-be-vanished-to-use-command")
        } else {
            if (VanishStateManager.interactEnabled.contains(player.uniqueId)) {
                VanishStateManager.interactEnabled.remove(player.uniqueId)
                player.sendConfigMessage("vanish-interact-toggled", "%interact-status%" to "off")
            } else {
                VanishStateManager.interactEnabled.add(player.uniqueId)
                player.sendConfigMessage("vanish-interact-toggled", "%interact-status%" to "on")
            }
        }
    }

    @Subcommand("priority")
    private fun onPriorityCommand(player: Player) {
        if (!permissionCheck(player, "permissions.priority-command", "advancedvanish.priority-command")) return

        if (PermissionsManager.handler == null) {
            player.sendConfigMessage("not-using-vanish-priority")
        } else {
            player.sendConfigMessage(
                "vanish-priority",
                "%priority%" to PermissionsManager.handler!!.getVanishPriority(player).toString(),
            )
        }
    }

    @Subcommand("list")
    private fun onListCommand(player: Player) {
        if (!permissionCheck(player, "permissions.list-command", "advancedvanish.list-command")) return

        val players =
            AdvancedVanishAPI.vanishedPlayers
                .map(Bukkit::getPlayer)
                .map { it!! }
                .joinToString(", ", transform = Player::getName)

        player.sendConfigMessage("vanished-list", "%vanished-players%" to players.ifEmpty { "None" })
    }

    @Subcommand("status")
    @Syntax("<player>")
    @CommandCompletion("@players")
    private fun onStatusCommand(
        sender: CommandSender,
        target: OnlinePlayer,
    ) {
        if (!permissionCheck(sender, "permissions.status-command", "advancedvanish.status-command")) return

        sender.sendConfigMessage(
            "vanish-status-command",
            "%target-name%" to target.player.name,
            "%vanish-status%" to if (target.player.isVanished()) "on" else "off",
            "%vanish-status-word%" to if (target.player.isVanished()) "vanished" else "not vanished",
        )
    }

    @Subcommand("set")
    @Syntax("<player> <on/off>")
    @CommandCompletion("@players")
    private fun onSetCommand(
        sender: CommandSender,
        target: OnlinePlayer,
        status: String,
    ) {
        if (!permissionCheck(sender, "permissions.set-other-command", "advancedvanish.set-other-command")) return

        val toChange = status.lowercase() == "on" || status.lowercase() == "true"
        var sendAlready = false

        if (target.player.isVanished()) {
            if (toChange) {
                sendAlready = true
            } else {
                AdvancedVanishAPI.unVanishPlayer(target.player)
            }
        } else {
            if (!toChange) {
                sendAlready = true
            } else {
                AdvancedVanishAPI.vanishPlayer(target.player)
            }
        }

        if (sendAlready) {
            sender.sendConfigMessage(
                "vanish-set-other-command-already",
                "%target-name%" to target.player.name,
                "%vanish-status%" to if (target.player.isVanished()) "on" else "off",
                "%vanish-status-word%" to if (target.player.isVanished()) "vanished" else "not vanished",
            )
        } else {
            sender.sendConfigMessage(
                "vanish-set-other-command",
                "%target-name%" to target.player.name,
                "%vanish-status%" to if (target.player.isVanished()) "on" else "off",
                "%vanish-status-word%" to if (target.player.isVanished()) "vanished" else "not vanished",
            )
        }
    }

    @Subcommand("toggle")
    @Syntax("<player>")
    @CommandCompletion("@players")
    private fun onToggleCommand(
        sender: CommandSender,
        target: OnlinePlayer,
    ) {
        if (!permissionCheck(sender, "permissions.toggle-other-command", "advancedvanish.toggle-other-command")) return

        if (target.player.isVanished()) {
            AdvancedVanishAPI.unVanishPlayer(target.player)
        } else {
            AdvancedVanishAPI.vanishPlayer(target.player)
        }

        sender.sendConfigMessage(
            "vanish-set-other-command",
            "%target-name%" to target.player.name,
            "%vanish-status%" to if (target.player.isVanished()) "on" else "off",
            "%vanish-status-word%" to if (target.player.isVanished()) "vanished" else "not vanished",
        )
    }

    @HelpCommand
    private fun onHelp(sender: CommandSender) {
        if (!permissionCheck(sender, "permissions.help-command", "advancedvanish.help-command")) return

        this.HELP_MESSAGE.forEach { sender.sendComponentMessage(it.color()) }
    }

    private fun permissionCheck(
        sender: CommandSender,
        key: String,
        default: String,
    ): Boolean {
        if (!sender.hasPermission(Config.getValueOrDefault(key, default))) {
            sender.sendConfigMessage("no-permission")
            return false
        }

        return true
    }
}
