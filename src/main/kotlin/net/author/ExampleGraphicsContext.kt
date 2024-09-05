package net.author

import net.botwithus.rs3.imgui.ImGui
import net.botwithus.rs3.script.ScriptConsole
import net.botwithus.rs3.script.ScriptGraphicsContext

class ExampleGraphicsContext(
    private val script: ExampleScript,
    console: ScriptConsole
) : ScriptGraphicsContext (console) {

    override fun drawSettings() {
        super.drawSettings()
        ImGui.Begin("Emmas Slime Runner", 0)
        ImGui.SetWindowSize(250f, -1f)
        ImGui.Text("Requirement: Ectophial")
        ImGui.Text("Will take the Shortcut if Agility is at least 58")
        ImGui.Text("Will use Magic Notepaper instead of the bank , if Magic Notepaper is in the inventory")
        ImGui.Text("My scripts state is: " + script.botState)
        ImGui.Text("Buckets collected: " + script.bucketsCollected)
        if (ImGui.Button("Start")) {
            script.botState = ExampleScript.BotState.SKILLING;
        }
        ImGui.SameLine()
        if (ImGui.Button("Stop")) {
            script.botState = ExampleScript.BotState.IDLE
        }
        script.bankPreset.set(ImGui.InputInt("Bank preset", script.bankPreset.get()))
        ImGui.End()
    }

    override fun drawOverlay() {
        super.drawOverlay()
    }

}