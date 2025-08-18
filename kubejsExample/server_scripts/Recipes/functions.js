/*
Functions are a way to change recipes on demand instead of hardcoding them
There are 4 Functions:

.requireFunctionToStart("id") -> Decides if you can start a recipe
.requireFunctionOnStart("id") -> Useful to check items to give a boost
.requireFunctionEachTick("id") -> When you want to check item to modify recipe time or stop it
.requireFunctionOnEnd("id") -> Decides if you get a result or not
*/
let IOType = Java.loadClass("es.degrassi.mmreborn.common.machine.IOType");

ServerEvents.recipes(event => {
    const time = 20 //in ticks (20 ticks = 1 second)
    const machine_id = "mmr:lcr6"
    event.recipes.modular_machinery_reborn.machine_recipe(machine_id, time)
    .progressX(54)
    .progressY(20)
    .width(110)
    .height(60)
    .requireEnergy(10000, 0, 4)
    .requireItem("minecraft:birch_boat")
    .produceItem("minecraft:oak_log")
    .requireFunctionOnEachTick("boat_chooser")
})

MMREvents.recipeFunction("boat_chooser", event => {
    let controller = event.machine; //This has more useful functions, more here https://wikis.degrassi.es/docs/modular-machinery-reborn/section/creating-a-new-recipe/article/machine
    let level = event.getTile().getLevel(); //Allows to get to the Level class, useful to do commands or more
    let pos = event.getTile().getBlockPos(); //Controller position
    let speed = event.baseSpeed; //speed of the recipe
    let time_remaining = event.remainingTime; //Time remaining for the recipe
    event.setBaseSpeed(2) //min is 0.1

    let inputItems = controller.getItemsStored(IOType.INPUT); //Get a list with all items
    let outputItems = controller.getItemsStored(IOType.OUTPUT);
    controller.setPaused(true) //To pause the recipe
    
    //if you want to specify an error, use event.error("Text here")
    //If not, dont specify something
})