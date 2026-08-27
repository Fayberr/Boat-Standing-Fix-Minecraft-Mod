# Boat Standing Fix

Fabric mod for Minecraft **1.21.11**.

Fixes the long-standing vanilla bug where standing on top of a boat floating
in water causes stuttering movement, rubber-banding, being pushed or
launched, or appearing to get stuck in the boat (Mojira MC-156980). No new
blocks or items - this only patches vanilla server movement/collision
validation logic.

## Environment: server-side only

All the behavior change happens in server-authoritative movement validation
(`ServerPlayNetworkHandler`). Install it in the **server's** `mods` folder
(dedicated server) or your own `mods` folder for singleplayer (which runs
its own internal server using the same jar). A vanilla client with no mod
installed can connect to a server running this mod and gets the fix for
free - nothing is required client-side.

## Root cause

Minecraft's player movement is client-authoritative: the client simulates
its own position locally and reports it to the server, which recomputes the
same movement itself and rejects (snaps back) the client's position if the
two disagree too much. Two separate checks inside
`ServerPlayNetworkHandler.onPlayerMove` do this:

1. A tolerance check (`movedDist > 0.0625`, a quarter of a block squared
   distance) - a bobbing/drifting boat blows through this most ticks purely
   from its own motion, nothing to do with the player.
2. A "did the new position collide with something that didn't overlap the
   old position" check (`isEntityNotCollidingWithBlocks`) - the boat's own
   shifting hitbox counts as a new collision every time it moves under the
   player.

The mixin in `ServerPlayNetworkHandlerMixin` loosens these checks, but only
when the moving entity is within a small probe box of an
`AbstractBoatEntity` (see `BoatSupport`), so ordinary anti-cheat movement
validation is untouched everywhere else.

## Port notes (1.21.11, Yarn mappings)

This is a from-scratch re-implementation against Yarn mappings, not a
recompile of the 26.x (Mojmap) source - the two toolchains use different
mapping sets, a different Loom version, and several of the touched
classes/methods have different names between them. Every symbol below was
individually confirmed with `javap` against the real, Loom-remapped
1.21.11 merged server jar (not assumed from older mapping docs) before
being used:

| Mojmap (26.x) | Yarn (1.21.11) |
|---|---|
| `ServerGamePacketListenerImpl` | `ServerPlayNetworkHandler` |
| `handleMovePlayer` | `onPlayerMove` |
| `handleMoveVehicle` | `onVehicleMove` |
| `isEntityCollidingWithAnythingNew` | `isEntityNotCollidingWithBlocks` |
| `Entity` | `Entity` (different package) |
| `AbstractBoat` | `AbstractBoatEntity` |
| `AABB` / `AABB.inflate(...)` | `Box` / `Box.expand(...)` |
| `LevelReader` | `WorldView` |
| `entity.level()` | `entity.getEntityWorld()` |
| `entity.isPassenger()` | `entity.hasVehicle()` |
| `EntityGetter.getEntitiesOfClass` | `EntityView.getEntitiesByClass` |

Two things worth calling out explicitly, since they are not obvious from
the names alone:

- **`isEntityNotCollidingWithBlocks` is not block-only** despite the name.
  Its internal `WorldView.getCollisions` call concatenates both entity-vs-
  entity and block/fluid collisions, so it is functionally equivalent to
  26.x's `isEntityCollidingWithAnythingNew` for this fix, and the boolean
  sense is also the same: it returns `true` when a *new* collision (one
  that did not exist at the old position) was found, not "not colliding".
  The mixin cancels it back to `false` (no new collision) when the entity
  is boat-supported, same as the 26.x version.
- **`EntityView.getEntitiesByClass` (like `EntityGetter.getEntitiesOfClass`
  on 26.x) has no "except self" parameter**, unlike the sibling
  `getOtherEntities(except, box, predicate)` overload. `BoatSupport` filters
  the boat itself out of the query explicitly (`boat -> boat != entity`) so
  a boat's own vehicle-movement validation doesn't match against itself and
  unconditionally disable that check for every boat.

## Building

Needs JDK 21+ and the checked-in Gradle wrapper (Gradle 9.2.1, see
`gradle.properties` for the pinned local JDK path). `./gradlew build`.

## License

GPL-3.0-or-later, Copyright (C) 2026 Fayber.
