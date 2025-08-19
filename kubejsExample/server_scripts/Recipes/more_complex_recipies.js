//If you need more information you can check the wiki
//https://wikis.degrassi.es/docs/modular-machinery-reborn

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

- lootTable -> Allows you to specify a lootTable (like minecraft:chests/ancient_city)
               If passed an argument after the loottable, then you have luck (similar to looting, but for all lootTables)
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


/*
Lets say that you have a bunch of repetive recipes but something
changed, like the boat produces a log and a boat and an anvil
produces a log

You can specify which recipe is considered first
*/
ServerEvents.recipes(event => {
    const time = 20 //in ticks (20 ticks = 1 second)
    const machine_id = "mmr:lcr6"
    event.recipes.modular_machinery_reborn.machine_recipe(machine_id, time)
    .requireItem("minecraft:spruce_boat", 10, 10)
    .produceItem("minecraft:oak_log", 0.1, 40, 10)

    event.recipes.modular_machinery_reborn.machine_recipe(machine_id, time)
    .requireItem("minecraft:spruce_boat", 10, 10)
    .requireItem("minecraft:anvil", 10, 20)
    .produceItem("minecraft:oak_log", 0.1, 40, 10)
    .priority(2) //this recipe is consider first, higher number, higher priority
})

/*
Now lets say that you want to hide the second recipe to be hidden
*/
ServerEvents.recipes(event => {
    const time = 20 //in ticks (20 ticks = 1 second)
    const machine_id = "mmr:lcr6"
    event.recipes.modular_machinery_reborn.machine_recipe(machine_id, time)
    .requireItem("minecraft:spruce_boat", 10, 10)
    .produceItem("minecraft:oak_log", 0.1, 40, 10)

    event.recipes.modular_machinery_reborn.machine_recipe(machine_id, time)
    .requireItem("minecraft:spruce_boat", 10, 10)
    .requireItem("minecraft:anvil", 10, 20)
    .produceItem("minecraft:oak_log", 0.1, 40, 10)
    .hide()
    .priority(2) //this recipe is consider first, higher number, higher priority
})

/*
Now lets say that we have an standard machine that always have 5 slots
but you don't want to put 5 items, well, you can do that!

- emptyItem -> If passed 2 arguments, acts as X and Y on the recipe Viewer
- emptyFluid -> If passed 2 arguments, acts as X and Y on the recipe Viewer
- emptyEnergy -> If passed 2 arguments, acts as X and Y on the recipe Viewer

If not passed anything, like emptyItem(), the default is 0,0
*/
ServerEvents.recipes(event => {
    const time = 20 //in ticks (20 ticks = 1 second)
    const machine_id = "mmr:lcr6"
    event.recipes.modular_machinery_reborn.machine_recipe(machine_id, time)
    .requireItem("minecraft:spruce_boat", 10, 10)
    .emptyItem(10, 20)
    .produceItem("minecraft:oak_log", 0.1, 40, 10)
})

/*
Now, lets say that you want to customize even more your recipes

Well, you can do that
*/
ServerEvents.recipes(event => {
    const time = 20 //in ticks (20 ticks = 1 second)
    const machine_id = "mmr:lcr6"
    event.recipes.modular_machinery_reborn.machine_recipe(machine_id, time)
    .requireItem("minecraft:spruce_boat", 10, 10)
    .produceItem("minecraft:oak_log", 0.1, 40, 10)
    .jei() //if nothing is after jei(), it will use the real recipe
    //if no jei() is present, it will use the real recipe

    event.recipes.modular_machinery_reborn.machine_recipe(machine_id, time)
    .requireItem("minecraft:spruce_boat", 10, 10)
    .requireItem("minecraft:anvil", 10, 20)
    .produceItem("minecraft:oak_log", 0.1, 40, 10)
    .jei()
    //Recipe viewer will show oak boat, but real item is a spruce_boat
    //You can customize here even more
    .requireItem("minecraft:oak_boat", 10, 10)
    .produceItem("minecraft:oak_log", 0.1, 40, 10)
})