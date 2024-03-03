package de.miraculixx.pip

import com.mojang.blaze3d.platform.InputConstants
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.ChatFormatting
import net.minecraft.client.KeyMapping
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.resources.IoSupplier
import java.io.File
import kotlin.io.path.Path

val file = File("config/pip.json")
lateinit var config: Config
val json = Json {
    encodeDefaults = true
    prettyPrint = true
}

fun loadData() {
    val content = if (file.exists()) file.readText() else "{}"
    config = json.decodeFromString(content)
}

fun init() {
    val toggleKeyBind = KeyBindingHelper.registerKeyBinding(KeyMapping("key.pip.toggle", InputConstants.Type.KEYSYM, InputConstants.KEY_F12, "key.category.pip"))
    val prefix = Component.literal("PiP").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE).append(Component.literal(" >> ").withStyle(ChatFormatting.GRAY))
    ClientTickEvents.END_CLIENT_TICK.register {
        while (toggleKeyBind.consumeClick()) {
            config.pinned = !config.pinned
            it.player?.sendSystemMessage(prefix.copy().append(
                Component.literal("Toggled pinned in picture mode to ").withStyle(ChatFormatting.GRAY).append(
                    Component.literal("${config.pinned}").withStyle(if (config.pinned) ChatFormatting.GREEN else ChatFormatting.RED)
                )
            ))
            it.player?.sendSystemMessage(prefix.copy().append(
                Component.literal("Please restart Minecraft to apply all changes").withStyle(ChatFormatting.GRAY)
            ))
        }

        // Change Icon & Name
        it.window.setTitle("Baba Title")
    }
    
    ClientLifecycleEvents.CLIENT_STOPPING.register {
        file.writeText(Json.encodeToString(config))
    }
}

@Serializable
data class Config(
    var pinned: Boolean = true,
    var title: String? = null,
)