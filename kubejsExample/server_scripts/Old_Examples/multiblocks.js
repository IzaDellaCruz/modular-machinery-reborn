MMREvents.machines((event) => {
 event.create("mmr:testing")
     .color('#FF2291FF')
     .name('Testing machineJS')
     .texture("modular_machinery_reborn:biome_reader", false, "mekanism:block/overlay/chemicals_overlay", null)
     .structure(
             MMRStructureBuilder.create()
                 .pattern(
                     [
                         ["rFr", "fpf", "rir"],
                         ["rmr", "i i", "rer"],
                         ["rFr", "fpf", "rir"]
                     ]
                 )
                 .keys(
                     {
                         "r": "modular_machinery_reborn:casing_reinforced",
                         "f": "modular_machinery_reborn:casing_firebox",
                         "F": [
                            "modular_machinery_reborn:casing_firebox",
                            "#modular_machinery_reborn:parallelhatch",
                            "modular_machinery_reborn:height_meter",
                            "modular_machinery_reborn:biome_reader",
                            "modular_machinery_reborn:chunkloader",
                            "modular_machinery_reborn:dimensional_detector"
                         ],
                         "p": "modular_machinery_reborn:casing_plain",
                         "e": ["#modular_machinery_reborn:energyinputhatch", "modular_machinery_reborn:casing_firebox"],
                         "i": [
                             "#modular_machinery_reborn:inputbus",
                             "#modular_machinery_reborn:fluidoutputhatch",
                             "#modular_machinery_reborn:outputbus",
                             "modular_machinery_reborn:casing_firebox"
                         ]
                     }
                 )
         )
 event.create('mmr:test')
     .name('test')
     .addModifier(
         MMRModifierReplacement.create()
             .ingredient('minecraft:gold_block')
             .position(1, 1, 0)
             .addModifier(
                 MMRRecipeModifier.create()
                     .target("modular_machinery_reborn:speed")
                     .input()
                     .multiply()
                     .modifier(0.1)
             )
     )
     .structure(
         MMRStructureBuilder.create()
             .pattern([["ama", "aca", "aba"]])
             .keys({ "a": ["minecraft:diamond_block"], "b": ["modular_machinery_reborn:outputbus_ludicrous"], "c": ["modular_machinery_reborn:inputbus_ludicrous"] })
     )
  event
    .create("mmr:chemical_infuser")
    .name("Chemical Infuser")
    .color("#FF2291FF")
    .structure(
      MMRStructureBuilder.create()
        .pattern([
          ["ddd", "ddd", "ddd"],
          ["dmd", "d d", "ddd"],
          ["ddd", "ddd", "ddd"],
        ])
        .keys({ d: "#modular_machinery_reborn:all_casing" })
    )
    .controllerModel(ControllerModel.of("mekanism:chemical_infuser"));

  event
    .create("mmr:test3")
    .color("#FF874526")
    .name("Testing 3")
    .structure(
      MMRStructureBuilder.create()
        .pattern([["a"], ["m"]])
        .keys({
          a: ["#modular_machinery_reborn:experiencehatch"],
        })
    );
  event
    .create("mmr:test4")
    .color("#FFED4526")
    .name("Testing 4")
    .structure(
      MMRStructureBuilder.create()
        .pattern([["a"], ["m"], ["b"]])
        .keys({
          a: ["#modular_machinery_reborn:experiencehatch"],
          b: ["#modular_machinery_reborn:itembus"],
        })
    );
  event
    .create("mmr:test5")
    .color("#FFED4526")
    .name("Testing 5")
    .structure(
      MMRStructureBuilder.create()
        .pattern([[" m ", "hgf", "eid", "cba"]])
        .keys({
          a: [
            "minecraft:oak_stairs[facing=west,half=bottom,shape=outer_left,waterlogged=false]",
          ],
          b: [
            "minecraft:oak_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]",
          ],
          c: [
            "minecraft:oak_stairs[facing=north,half=bottom,shape=outer_left,waterlogged=false]",
          ],
          d: [
            "minecraft:oak_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]",
          ],
          e: [
            "minecraft:oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]",
          ],
          f: [
            "minecraft:oak_stairs[facing=east,half=bottom,shape=outer_right,waterlogged=false]",
          ],
          g: [
            "minecraft:oak_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]",
          ],
          h: [
            "minecraft:oak_stairs[facing=east,half=bottom,shape=outer_left,waterlogged=false]",
          ],
          i: [
            "#modular_machinery_reborn:outputbus"
          ]
        })
    ).addModifier(
        MMRModifierReplacement.create()
            .ingredient("minecraft:diamond_block")
            .position(0, 1, 0)
            .addModifier(
                MMRRecipeModifier.create()
                    .target("modular_machinery_reborn:speed")
                    .multiply()
                    .modifier(0.5)
            )
    ).addModifier(
         MMRModifierReplacement.create()
             .ingredient("minecraft:gold_block")
             .position(0, 1, 0)
             .addModifier(
                 MMRRecipeModifier.create()
                     .target("modular_machinery_reborn:speed")
                     .multiply()
                     .modifier(0.8)
             )
     );

    event.create("mmr:testing89")
        .name('Testing Structure Creator23132')
        .color('#FF2291FF')
        .structure(
            MMRStructureBuilder.create()
                .pattern(
                    [
                        ["rFr", "ipi", "rir"],
                        ["rmr", "i i", "rer"],
                        ["rfr", "ipi", "rir"]
                    ]
                )
                .keys(
                    {
                        "r": "modular_machinery_reborn:casing_reinforced",
                        "f": "modular_machinery_reborn:casing_firebox",
                        "p": "modular_machinery_reborn:casing_plain",
                        "e": "#modular_machinery_reborn:energyinputhatch",
                        "i": [
                            "#modular_machinery_reborn:itembus",
                            "#modular_machinery_reborn:fluidhatch",
                            "#modular_machinery_reborn_ars:sourcehatch",
                            "#modular_machinery_reborn_mekanism:chemicalhatch",
                            "modular_machinery_reborn:casing_firebox"
                        ],
                        "F": [
                            "modular_machinery_reborn:casing_firebox",
                            "#modular_machinery_reborn:parallelhatch"
                        ]
                    }
                )
        )
        .addModifier(
            MMRModifierReplacement.create()
                .position(0, 1, 0)
                .ingredient("minecraft:diamond_block")
                .addModifier(
                    MMRRecipeModifier.create()
                        .target("modular_machinery_reborn:speed")
                        .input()
                        .multiply()
                        .modifier(2)
                )
        )
        .addModifier(
            MMRModifierReplacement.create()
                .position(0, 1, 0)
                .ingredient("minecraft:gold_block")
                .addModifier(
                    MMRRecipeModifier.create()
                        .target("modular_machinery_reborn:speed")
                        .input()
                        .multiply()
                        .modifier(1.5)
                )
        )
        .addModifier(
            MMRModifierReplacement.create()
                .position(0, 1, 0)
                .ingredient("minecraft:iron_block")
                .addModifier(
                    MMRRecipeModifier.create()
                        .target("modular_machinery_reborn:speed")
                        .input()
                        .multiply()
                        .modifier(1.2)
                )
        )
});

