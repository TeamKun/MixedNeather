package net.kunmc.lab.mixneather

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import kotlin.math.floor

class Mixneather : JavaPlugin() {
    val command = net.kunmc.lab.mixneather.Command()
    override fun onEnable() {
        // Plugin startup logic
        getCommand("mix")!!.setExecutor(command)
        getCommand("mix")!!.tabCompleter = command.genTabCompleter()
        server.scheduler.runTaskTimer(
            this,
            Runnable { command.onTick() },
            10,
            1
        )
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

class Command : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return if (sender is Player) {
            if (sender.isOp) {
                run(sender, command, label, args)
            } else false
        } else run(sender, command, label, args)
    }

    fun run(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 1) {
            return false
        } else {
            when (args[0]) {
                "s", "start" -> {
                    isGoingOn = true
                    count = 0
                }
                "skip" -> {
                    if(isGoingOn){
                        Bukkit.broadcastMessage("Skipped!")
                        count = periodTime
                    }
                }
                "e", "end" -> {
                    isGoingOn = false
                    count = 0
                }
                "test" -> {
//                    println((sender as Player).location)
                    Bukkit.getWorlds().forEach {
                        println(it)
                    }
                }
                else -> return false
            }
            return true
        }
    }

    var isGoingOn = false
    var count = 0
    var periodTime = 20 * 60 * 3

    fun onTick() {
        if (isGoingOn) {
            count++
            if (count >= periodTime) {
                teleportAll()
                count = 0
            }
            Bukkit.getOnlinePlayers().forEach {
                it.sendActionBar("${count / 20}/${periodTime / 20}")
            }
        }
    }

    fun teleportAll() {
        Bukkit.getOnlinePlayers().forEach {
            val environment = when (it.world.environment) {
                /*World.Environment.NORMAL -> {
                    World.Environment.NETHER
                }
                World.Environment.NETHER -> {
                    World.Environment.THE_END
                }
                World.Environment.THE_END -> {
                    World.Environment.NORMAL
                }*/
                World.Environment.NORMAL -> {
                    World.Environment.NETHER
                }

                World.Environment.NETHER -> {
                    World.Environment.NORMAL
                }

                else -> {
                    World.Environment.NORMAL
                }
            }
            it.teleport(
                Location(
                    getWorld(it,environment),
                    it.location.x,
                    it.location.y,
                    it.location.z,
                    it.location.yaw,
                    it.location.pitch
                )
            )
        }
    }

    fun getBaseWorldName(p:Player): String {
        var name = p.location.world.name
        when(p.location.world.environment){
            World.Environment.NORMAL -> {
                // DO NOTHING
            }

            World.Environment.NETHER -> {
                if(name.indexOf("_nether") == -1) throw Exception("Error Occurred in World Get System")
                else name = name.replace("_nether","")
            }

            World.Environment.THE_END -> {
                if(name.indexOf("_the_end") == -1) throw Exception("Error Occurred in World Get System")
                else name = name.replace("_the_end","")
            }
        }
        return name
    }

    fun getWorldName(p:Player,e:World.Environment): String {
        var name = getBaseWorldName(p)
        when(e){
            World.Environment.THE_END -> {
                name += "_the_end"
            }

            World.Environment.NETHER -> {
                name += "_nether"
            }
        }
        return name
    }

    fun getWorld(p:Player,e:World.Environment): World {
        return Bukkit.getWorld(getWorldName(p,e))!!
    }


    fun genTabCompleter(): SmartTabCompleter {
        return SmartTabCompleter(
            mutableListOf(
                TabChain(
                    arrayOf(
                        TabObject(
                            arrayOf(
                                "s", "start"
                            )
                        )
                    )
                ),
                TabChain(
                    arrayOf(
                        TabObject(
                            arrayOf(
                                "e", "end"
                            )
                        )
                    )
                ),
                TabChain(
                    arrayOf(
                        TabObject(
                            arrayOf(
                                "skip"
                            )
                        )
                    )
                )
            )
        )
    }
}