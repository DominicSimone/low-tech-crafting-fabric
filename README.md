## Low Tech Crafting

A Fabric, 1.20.1 port of the [existing Forge mod Low Tech Crafting](https://www.curseforge.com/minecraft/mc-mods/low-tech-crafting) and inspired by [ilmango's video](https://www.youtube.com/watch?v=2_HL309IZ0M) on the subject.

Requires both [Fabric and the Fabric API](https://fabricmc.net/use/installer/).

### Features

- Adds a Crafting Table variant with a recipe of one redstone block and one crafting table.
- Hoppers/Droppers can insert items into this table, only inserting max one item into empty recipe slots.
- Hoppers can remove items from this table, prioritizing crafting if the ingredients match a recipe and pulling from a hidden crafting result inventory slot. 
- This block emits a comparator signal equal to the number of occupied slots, from 0 (empty) to 10 (all nine grid slots + hidden crafting result slot non-empty)