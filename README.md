# AdvancedVanish Fork
![license](https://img.shields.io/github/license/quantiom/EventHandler?color=%23b59e28&style=for-the-badge) ![made-with-kotlin](https://img.shields.io/badge/MADE%20WITH-KOTLIN-%23b59e28?style=for-the-badge&logo=java)  ![last-commit](https://img.shields.io/github/last-commit/quantiom/AdvancedVanish?color=%23b59e28&style=for-the-badge)  

AdvancedVanish is a fully customizable and advanced vanish plugin made with Kotlin.  

Spigot Resource: https://www.spigotmc.org/resources/advancedvanish.86036/

GitBook: https://quantioms.gitbook.io/advancedvanish/

## Features
- Fully customizable through the [config](src/main/resources/config.yml). (70+ options)
  - Messages
  - Permissions
  - Actions
  - Hooks
  - Placeholders
  - Much more...
- Vanished players are **completely** invisible, as if they are not even online.
- Vanish priorities/levels ([more info](https://quantioms.gitbook.io/advancedvanish/features#vanish-priorities))
  - Supports many different permissions plugins. (LuckPerms, PermissionsEx, bPermissions, GroupManager)
- Many configurable hooks which provide support to other plugins.
   - Essentials
   - PlaceholderAPI
   - DiscordSRV
   - Dynmap
   - Much more...
- Togglable Actions when vanished (15+)
- Commands:
  - `/vanish` *- Toggle vanish.*
  - `/vanish reload` *- Reloads the config and hooks*
  - `/vanish priority` *- Displays your vanish priority.*
  - `/vanish list` *- Dispalys a list of vanished players.*
  - `/vanish status <player>` *- Check if a player is in vanish.*
  - `/vanish set <player> <on/off>` *- Set another player's vanish.*
  - `/vanish toggle <player>` *- Toggle another player's vanish.*
  - `/vanish interact` *- Toggles interacting while in vanish*
- Cross Server Vanish Synchronization
- For the rest of the features, check out the [GitBook page](https://quantioms.gitbook.io/advancedvanish/).

## Hooks
In AdvancedVanish, there are many hooks which provide support to other plugins.  
A full list of hooks with their descriptions can be found on the [GitBook page](https://quantioms.gitbook.io/advancedvanish/features#hooks).  

## Vanish Priority
An explanation and guide of how to use vanish priorities can be found on the [GitBook page](https://quantioms.gitbook.io/advancedvanish/features#vanish-priorities).  
*Note: Requires a supported permissions plugin to function*

## API
Before utilizing the API, make sure that the `AdvancedVanish` plugin is
enabled, or add `depend: [AdvancedVanish]` or `softdepend: [AdvancedVanish]` to 
your plugin's `plugin.yml`.

### Maven
Add this repository to your `pom.xml`:
```xml
<repository>
  <id>repsy</id>
  <name>quantiom</name>
  <url>https://repo.repsy.io/mvn/quantiom/minecraft</url>
</repository>
```

Add the dependency and replace `<version>...</version>` with the current version:
```xml
<dependency>
  <groupId>me.quantiom</groupId>
  <artifactId>advancedvanish</artifactId>
  <version>1.2.6</version>
</dependency>
```

### Methods
```kotlin
AdvancedVanishAPI.vanishPlayer(player: Player): Unit
AdvancedVanishAPI.unVanishPlayer(player: Player): Unit
AdvancedVanishAPI.isPlayerVanished(player: Player): Boolean
AdvancedVanishAPI.canSee(player: Player, target: Player): Boolean
```
### Extensions
```kotlin
Player.isVanished(): Boolean
```
### Events
- `PrePlayerVanishEvent` - Gets called before vanishing a player, implements `Cancellable`.
- `PlayerVanishEvent` - Gets called after a player vanishes.
- `PrePlayerUnVanishEvent` - Gets called before a player unvanishes, implements `Cancellable`.
- `PlayerUnVanishEvent` - Gets called after a player unvanishes.
### Example Usage
```kotlin
class ExamplePlugin : JavaPlugin(), Listener {
    override fun onEnable() {
        this.server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    private fun onVanish(event: PlayerVanishEvent) {
        val vanishedPlayers = AdvancedVanishAPI.vanishedPlayers
            .map(Bukkit::getPlayer)
            .joinToString(", ", transform = Player::getName)

        this.logger.log(Level.INFO, "${event.player.name} has entered vanish.")
        this.logger.log(Level.INFO, "Current vanished players: ${vanishedPlayers}.")
    }

    @EventHandler
    private fun onUnVanish(event: PrePlayerUnVanishEvent) {
        event.isCancelled = true // Don't let players unvanish
    }
}
```
