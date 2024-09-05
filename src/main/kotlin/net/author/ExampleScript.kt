package net.author

import net.botwithus.api.game.hud.inventories.Backpack
import net.botwithus.api.game.hud.inventories.Bank
import net.botwithus.internal.scripts.ScriptDefinition
import net.botwithus.rs3.events.impl.InventoryUpdateEvent
import net.botwithus.rs3.game.Client
import net.botwithus.rs3.game.Coordinate
import net.botwithus.rs3.game.actionbar.ActionBar
import net.botwithus.rs3.game.cs2.layouts.Layout.COMPONENT

import net.botwithus.rs3.game.minimenu.MiniMenu
import net.botwithus.rs3.game.minimenu.actions.ComponentAction
import net.botwithus.rs3.game.minimenu.actions.SelectableAction
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery
import net.botwithus.rs3.game.scene.entities.characters.player.Player
import net.botwithus.rs3.game.scene.entities.`object`.SceneObject
import net.botwithus.rs3.game.skills.Skills
import net.botwithus.rs3.imgui.NativeInteger
import net.botwithus.rs3.script.Execution
import net.botwithus.rs3.script.LoopingScript
import net.botwithus.rs3.script.config.ScriptConfig
import java.util.*

class ExampleScript(
    name: String,
    scriptConfig: ScriptConfig,
    scriptDefinition: ScriptDefinition
) : LoopingScript (name, scriptConfig, scriptDefinition) {

    val random: Random = Random()
    var botState: BotState = BotState.IDLE
    var bankPreset: NativeInteger = NativeInteger(1)
    var bucketsCollected = 0

    enum class BotState {
        IDLE,
        SKILLING,
        BANKING,
    }

    override fun initialize(): Boolean {
        super.initialize()
        // Set the script graphics context to our custom one
        this.sgc = ExampleGraphicsContext(this, console)
        println("Emmas Slime Runner loaded!")

        subscribe(InventoryUpdateEvent::class.java) {
            if (it.inventoryId == 93) {
                when (it.newItem.name) {
                    "Bucket of slime" -> {
                        bucketsCollected += it.newItem.stackSize - it.oldItem.stackSize
                    }
                }
            }
        }
        return true;
    }

    override fun onLoop() {
        val player = Client.getLocalPlayer();
        if (Client.getGameState() != Client.GameState.LOGGED_IN || player == null || botState == BotState.IDLE) {
            Execution.delay(random.nextLong(2500, 5500))
            return
        }
        when (botState) {
            BotState.SKILLING -> {
                Execution.delay(handleSkilling(player))
                return
            }

            BotState.BANKING -> {
                Execution.delay(handleBanking(player))
                return
            }

            else -> {
                println("Unexpected bot state, report to author!")
            }
        }
        Execution.delay(random.nextLong(2000, 4000))
        return
    }

    private fun handleBanking(player: Player): Long {
        if (player.isMoving || player.animationId != -1)
            return random.nextLong(1000, 2000)

        if (Bank.isOpen()) {
            Bank.loadPreset(bankPreset.get())
            println("Bank preset loaded")
            botState = BotState.SKILLING
        } else {
            val sceneObject: SceneObject? =
                SceneObjectQuery.newQuery().name("Bank chest").option("Use").results().nearest()
            sceneObject?.interact("Use")
        }
        if (player.coordinate?.regionId != 13214) {
            val use = ActionBar.useTeleport("War's Retreat Teleport");
            if (use) {
                println("Teleported to War's Retreat")
                Execution.delay(random.nextLong(1000, 2000))
            }
        }

        return random.nextLong(1000, 3000)
    }

    private fun handleSkilling(player: Player): Long {
        if (player.isMoving || player.animationId != -1)
            return random.nextLong(1000, 2000)

        if (Backpack.isFull()) {
            val magicNote = InventoryItemQuery.newQuery().name("Magic notepaper").results().first()

            val bucketOfSlime =
                InventoryItemQuery.newQuery().name("Bucket of slime").results().filter { it.stackSize == 1 }.first()
            if (magicNote != null && bucketOfSlime != null) {
                var bucket = ComponentQuery.newQuery(1473).componentIndex(5).itemName("Bucket of slime").results().filter { it.itemAmount == 1}.firstOrNull()
                var magicnote = ComponentQuery.newQuery(1473).componentIndex(5).itemName("Magic notepaper").results().firstOrNull()
                if (MiniMenu.interact(SelectableAction.SELECTABLE_COMPONENT.type,0,bucket!!.subComponentIndex,96534533).also {println("Clicked bucket")}) {
                    Execution.delay(500)
                    MiniMenu.interact(SelectableAction.SELECT_COMPONENT_ITEM.type, 0, magicnote!!.subComponentIndex, 96534533).also {println("Clicked magic note")}
                    return random.nextLong(1000, 2000)
                }
                Execution.delay(500)

            } else {
                botState = BotState.BANKING
                return random.nextLong(1000, 2000)
            }
            return random.nextLong(1000, 2000)
        }


        // Handling actions based on region ID
        when (player.coordinate?.regionId) {
            14746 -> {
                handleRegion14746()
            }

            14646, 14647 -> {
                handleRegion14646And14647()
            }

            else -> {
                useEctophial()
            }
        }

        return random.nextLong(1000, 3000)
    }


    private fun handleRegion14746() {
        println("Handling region 14746...")
        val stairsCoordinates = listOf(
            Coordinate(3689,9887, 3),
            Coordinate(3672,9887, 2),
            Coordinate(3684,9887, 1)
        )
        val agilityShortcut = listOf(
            Coordinate(3670,9888,3),
        )
        val player = Client.getLocalPlayer()
        if (player != null) {
            val playerZ = player.coordinate.z
            if (Skills.AGILITY.level >= 58 && playerZ == 3) {
                val shortcutCoordinate = agilityShortcut.find { it.z == playerZ }
                if (shortcutCoordinate != null) {
                    val shortcut: SceneObject? = SceneObjectQuery.newQuery().results().nearestTo(shortcutCoordinate)
                    if (shortcut != null && shortcut.coordinate == shortcutCoordinate) {
                        shortcut.interact("Jump-down")
                        println("Used shortcut at $shortcutCoordinate")
                    }
                }
            } else {
                val stairsCoordinate = stairsCoordinates.find { it.z == playerZ }
                if (stairsCoordinate != null) {
                    val stairs: SceneObject? = SceneObjectQuery.newQuery().name("Stairs").results().nearestTo(stairsCoordinate)
                    if (stairs != null) {
                        if (stairs.coordinate == stairsCoordinate) {
                            stairs.interact("Climb-down")
                            println("Used stairs at $stairsCoordinate")
                        } else {
                            println("Found stairs at different coordinate: ${stairs.coordinate}")
                        }
                    } else {
                        println("No stairs found at $stairsCoordinate")
                    }
                }
            }
        }

        val pool : SceneObject? = SceneObjectQuery.newQuery().name("Pool of Slime").results().nearest()
        val playerZ = player!!.coordinate!!.z
        if (pool != null && playerZ == pool.coordinate!!.z) {
            pool.interact("Use slime").also {
                println("Used slime")
            }
        }
    }

        private fun handleRegion14646And14647() {
            println("Looking for and interacting with a trapdoor...")
            val trapdoors = SceneObjectQuery.newQuery().name("Trapdoor").results()
            val climbDownTrapdoor = trapdoors.firstOrNull { it.options.contains("Climb-down") }
            val openTrapdoor = trapdoors.firstOrNull { it.options.contains("Open") }

            when {
                climbDownTrapdoor != null -> {
                    val success = climbDownTrapdoor.interact("Climb-down")
                    if (success) {
                        println("Climbed down trapdoor")
                        Execution.delay(random.nextLong(1000, 2000))
                    } else {
                        println("Failed to climb down trapdoor")
                    }
                }

                openTrapdoor != null -> {
                    val success = openTrapdoor.interact("Open")
                    if (success) {
                        println("Opened trapdoor")
                        Execution.delay(random.nextLong(1000, 2000))
                    } else {
                        println("Failed to open trapdoor")
                    }
                }
            }
        }

        private fun useEctophial() {
            val ectoPhial = InventoryItemQuery.newQuery().name("Ectophial").results().firstOrNull()
            if (ectoPhial != null) {
                val use = Backpack.interact(ectoPhial.slot, 1)
                if (use) {
                    println("Used ectophial at slot ${ectoPhial.slot}")
                    Execution.delay(random.nextLong(1000, 2000))
                }
            }
        }

}