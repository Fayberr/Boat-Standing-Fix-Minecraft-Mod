# Boat Standing Fix

Fabric mod for Minecraft **26.2**.

Fixes the long-standing vanilla bug where standing on top of a boat floating
in water causes stuttering movement, rubber-banding, being pushed or
launched, or appearing to get stuck in the boat (Mojira MC-156980). No new
blocks or items - this only patches vanilla server movement/collision
validation logic.

## Environment: server-side only

All the behavior change happens in server-authoritative movement validation
(`ServerGamePacketListenerImpl`). Install it in the **server's** `mods`
folder (dedicated server) or your own `mods` folder for singleplayer (which
runs its own internal server using the same jar). A vanilla client with no
mod installed can connect to a server running this mod and gets the fix for
free - nothing is required client-side.

## Root cause

Minecraft's player movement is client-authoritative: the client simulates
its own position locally and reports it to the server, which recomputes the
same movement itself and rejects (snaps back) the client's position if the
two disagree too much. Two separate checks inside
`ServerGamePacketListenerImpl.handleMovePlayer` do this:

1. A tolerance check (`movedDist > 0.0625`, a quarter of a block squared
   distance) - a bobbing/drifting boat blows through this most ticks purely
   from its own motion, nothing to do with the player.
2. A "did the new position collide with something that didn't overlap the
   old position" check (`isEntityCollidingWithAnythingNew`) - the boat's own
   shifting hitbox counts as a new collision every time it moves under the
   player.

Both mixins in `ServerGamePacketListenerImplMixin` loosen these checks, but
only when the moving entity is within a small probe box of an `AbstractBoat`
(see `BoatSupport`), so ordinary anti-cheat movement validation is untouched
everywhere else.

Verified via `javap`/decompiling the actual 26.1 and 26.2 merged server jars
(Mojang ships these unobfuscated with full debug info) rather than assumed
from older-version fix mods - both mixin targets (the method signature and
the `0.0625` constant) are byte-identical between 26.1.x and 26.2, and
`AbstractBoat` stays in the same package
(`net.minecraft.world.entity.vehicle.boat`), so no source changes were
needed to port this build from 26.1.x, only the target Minecraft version.

## Building

Needs JDK 25 + Gradle 9.7.0+ (see `gradle.properties` for the pinned local
JDK path). `./gradlew build` or use the Pi's standalone toolchain per the
workspace README.

## License

GPL-3.0-or-later, Copyright (C) 2026 Fayber.
