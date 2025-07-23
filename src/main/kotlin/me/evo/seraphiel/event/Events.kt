package me.evo.seraphiel.event

import me.evo.seraphiel.Location
import net.minecraft.entity.Entity
import net.minecraft.network.play.server.S3BPacketScoreboardObjective
import net.minecraft.util.IChatComponent
import net.weavemc.loader.api.event.CancellableEvent
import net.weavemc.loader.api.event.Event

class LocationChangeEvent(val from: Location, val to: Location) : Event()

sealed class ChatEvent : CancellableEvent() {
    class Send(val message: String) : ChatEvent()
    class Receive(val message: IChatComponent) : ChatEvent()
}

class ScoreboardUpdateEvent(val packet: S3BPacketScoreboardObjective) : Event()

open class EntityListEvent(val entity: Entity) : Event() {

    class Add(entity: Entity) : EntityListEvent(entity)
    class Remove(entity: Entity) : EntityListEvent(entity)
}