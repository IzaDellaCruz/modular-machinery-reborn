/*
What if we want to add a recipe for our multiblock?
Easy, just as the follow

You can do the following (inputs):

- requireItem
- requireFluid
- requireEnergy -> Per tick
- requireExperience
- requireChemical (needs Modular Machinery Reborn Mekanism addon)
- requireKinetic (needs Modular Machinery Reborn Create addon)
- requireSource (needs Modular Machinery Reborn Ars addon)

You can do the following (outputs):

- produceItem
- produceFluid
- produceEnergy -> In total
- produceExperience
- produceChemical (needs Modular Machinery Reborn Mekanism addon)
- produceKinetic (needs Modular Machinery Reborn Create addon)
- produceSource (needs Modular Machinery Reborn Ars addon)

*/
ServerEvents.recipes(event => {
    const time = 20 //in ticks (20 ticks = 1 second)
    const machine_id = "mmr:lcr6"
    event.recipes.modular_machinery_reborn.machine_recipe(machine_id, time)
    .requireItem("minecraft:acacia_boat")
    .produceItem("minecraft:oak_log")
})

/*
But the items are overlapping each other, how can we solve it?
Well, require and produce has optional arguments that you can pass

If 1 argument is passed -> Item you specified with 100% and position 0,0
If 2 arguments are passed -> Item you specified with Z% and position 0,0
If 3 arguments are passed -> Item you specified with 100% and position X,Y
If 4 arguments are passed -> Item you specified with Z% and position X,Y

Where Z is the chance in a range of 0.0 to 1.0 and X and Y are whole numbers 
Item then chance then X and finally Y, in that order
*/
ServerEvents.recipes(event => {
    const time = 20 //in ticks (20 ticks = 1 second)
    const machine_id = "mmr:lcr6"
    event.recipes.modular_machinery_reborn.machine_recipe(machine_id, time)
    .requireItem("minecraft:spruce_boat", 10, 10)
    .produceItem("minecraft:oak_log", 40, 10)
})

/*
But the arrow isnt centered too...
Well, you have progressX and progressY where you can put a whole number
and customize where the arrow is

Or disable it
*/
ServerEvents.recipes(event => {
    const time = 20 //in ticks (20 ticks = 1 second)
    const machine_id = "mmr:lcr6"
    event.recipes.modular_machinery_reborn.machine_recipe(machine_id, time)
    .progressX(20)
    .progressY(20)
    .requireItem("minecraft:cherry_boat", 10, 10)
    .produceItem("minecraft:oak_log", 40, 10)

    event.recipes.modular_machinery_reborn.machine_recipe(machine_id, time)
    //.progressX(20)
    //.progressY(20)
    .renderProgress(false) //or you can disable it
    .requireItem("minecraft:oak_boat", 10, 10)
    .produceItem("minecraft:oak_log", 40, 10)
})
