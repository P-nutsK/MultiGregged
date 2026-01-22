// priority: 0

// Visit the wiki for more info - https://kubejs.com/

console.info("Hello, World! (Loaded startup scripts)");
const MGDRecipeCapabilities = Java.loadClass("com.p_nsk.multigregged.MGDRecipeCapabilities");
const MGDPartAbilities = Java.loadClass("com.p_nsk.multigregged.MGDPartAbilities");
const IO = Java.loadClass("com.gregtechceu.gtceu.api.capability.recipe.IO");

GTCEuStartupEvents.registry("gtceu:recipe_type", event => {
    event
        .create("volcanic_sourcelink")
        .setMaxIOSize(0, 0, 1, 0)
        .setMaxSize(IO.OUT, MGDRecipeCapabilities.SOURCE, 1)
        .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, FillDirection.LEFT_TO_RIGHT)
        .setSound(GTSoundEntries.CHEMICAL);

    event
        .create("just_voiding_fluid")
        .setMaxIOSize(0, 0, 1, 0)
        .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, FillDirection.LEFT_TO_RIGHT)
        .setSound(GTSoundEntries.CHEMICAL);
});

GTCEuStartupEvents.registry("gtceu:machine", event => {
    event
        .create("large_volcanic_sourcelink", "multiblock")
        // WorkableElectricMultiblockMachineの代わりに使うクラス
        .rotationState(RotationState.NON_Y_AXIS)
        // 使用できるレシピ
        .recipeTypes(["volcanic_sourcelink", "just_voiding_fluid"])
        // 最初に並列を試みてから他の修飾子を適用します
        .recipeModifiers(true, [GTRecipeModifiers.OC_NON_PERFECT, GTRecipeModifiers.BATCH_MODE])
        // 基本はworkableCasingModelの一つ目の引数と同じ
        .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
        // マルチブロックの形成パターン
        .pattern(
            /** @param {Internal.MachineDefinition} definition */
            definition =>
                FactoryBlockPattern.start()
                    .aisle("AAA", "CCC", "CCC", "AAA")
                    .aisle("AAA", "C C", "C C", "AMA")
                    .aisle("A@A", "CCC", "CCC", "AAA")
                    // コントローラーとしてマークされたブロック
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("M", Predicates.ability(PartAbility.MUFFLER))
                    .where(" ", Predicates.air())
                    .where(
                        "A",
                        Predicates.blocks("gtceu:solid_machine_casing")
                            .setMinGlobalLimited(5)
                            // レシピタイプからI/Oバス/ハッチを許可
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.abilities(MGDPartAbilities.OUTPUT_SOURCE))
                            // メンテナンスハッチ
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)),
                    )
                    .where("C", Predicates.blocks("minecraft:glass"))
                    .build(),
        )
        .workableCasingModel(
            "gtceu:block/casings/solid/machine_casing_solid_steel",
            "gtceu:block/multiblock/implosion_compressor",
        );
});
