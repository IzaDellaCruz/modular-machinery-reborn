
//If you need more information you can check the wiki
//https://wikis.degrassi.es/docs/modular-machinery-reborn

/*
To start creating custom machinery, you would need to make 
a controller, which can be done like this.
This is the absolute minimun
*/
MMREvents.machines(event => {
    event.create("mmr:lcr") //this can be anything, like minecraft:oxygen
})


/*
In-game (after reloading with /reload), you can place the 
controller and how you want to look like,
like a 3x3x3 hollow cube or a mega multiblock with 500 blocks

Once you have the structure, you need to select it using
the Structure Creator (available on creative) and sneak right
click on the controller to get a chat message

Select Kubejs option to get the structure and paste it
*/
MMREvents.machines(event => {
    event.create("mmr:lcr1")
    .structure( //This is how it looks like by default
MMRStructureBuilder.create()
.pattern([["aaa","aaa","aaa"],["ama","a a","aaa"],["aaa","aaa","aaa"]])
.keys({"a":["modular_machinery_reborn:casing_plain"]}))
})

/*
If you need you can put it prettier to read
There are 2 letters, a and m, where m is the controller and
a is Machine Casing

The structure goes in vertical layers, so first column is
the first layer, ...
*/
MMREvents.machines(event => {
    event.create("mmr:lcr2")
    .structure(
        MMRStructureBuilder.create()
        .pattern([
            ["aaa","aaa","aaa"],
            ["ama","a a","aaa"],
            ["aaa","aaa","aaa"]
        ])
        .keys({
            "a":["modular_machinery_reborn:casing_plain"]
        })
    )
})