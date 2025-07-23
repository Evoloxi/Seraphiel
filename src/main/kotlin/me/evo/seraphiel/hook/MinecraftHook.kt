package me.evo.seraphiel.hook

import net.weavemc.loader.api.Hook
import net.weavemc.loader.impl.shaded.asm.Opcodes
import net.weavemc.loader.impl.shaded.asm.Type
import net.weavemc.loader.impl.shaded.asm.tree.ClassNode
import net.weavemc.loader.impl.shaded.asm.tree.MethodInsnNode
import net.weavemc.loader.impl.shaded.asm.tree.MethodNode
import java.util.function.Supplier

/*
class MinecraftHook : Hook("net/minecraft/client/Minecraft") {
    override fun transform(classNode: ClassNode, assemblerConfig: AssemblerConfig) {
        classNode.methods.stream()
            .filter { m: MethodNode -> m.name == "startGame" }
            .findFirst().orElseThrow(
                Supplier { IllegalStateException("Method startGame not found in Minecraft class") }
            )
            .instructions.insert(
                MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    Type.getInternalName(MinecraftHook::class.java),
                    "onStartGame",
                    "()V"
                )
            )
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun onStartGame() {
            println("Hook Test")
        }
    }
}
*/
