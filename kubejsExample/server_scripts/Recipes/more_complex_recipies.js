/*
You can specify more requirements to your recipes

For the other blocks, you have the following (needs the block):
- chunkload -> Allows to chunkload X number of chunks, if another 2 more arguments,
               puts X and Y text on recipe viewer

- biomes -> Allows to specify if you can run a recipe in certain biomes or not
            Needs a list (["biome_here", "biome_here"]) of biomes
            If passed after the list a true, then, those biome becomes a blacklist
            If passed 3 arguments or 4, the last 2 are considered the X and Y on JEI

- dimensions -> Similar to biomes, but for dimensions

- weather -> Allows to run a recipe when on a specific weather ("rain", "clear", "snow", "thunder")
             If passed another 2 arguments, the last 2 are considered the X and Y on Jei

- time -> Allows you to specify a range where recipe can be done (it's relative, from 0 to 24000), more info here https://wikis.degrassi.es/docs/modular-machinery-reborn/section/misc/article/range
          If passed another 2 arguments, the last 2 are considered the X and Y on Jei

- requieredHeight -> Similar to time, but for height (range is from -64 to 320)

- lootTable -> Allows you to specify a lootTable
               If passed an argument after the loottable, then you have luck (similar to looting)
               If passed 3 arguments or 4, the last 2 are considered the X and Y on JEI

- damageItem / repairItem -> Allows to change durability of certain item
                             If the item is easy in nbt like a sword, a function can be better to deal with those
*/
ServerEvents.recipes(event => {
    const time = 20 //in ticks (20 ticks = 1 second)
    const machine_id = "mmr:lcr6"
    event.recipes.modular_machinery_reborn.machine_recipe(machine_id, time)
    .requireItem("minecraft:dark_oak_boat")
    .chunkload(3)
})