// priority: 0

// Visit the wiki for more info - https://kubejs.com/

console.info("Hello, World! (Loaded server scripts)");

ServerEvents.recipes(event => {
    event.recipes.gtceu
        .volcanic_sourcelink("lava")
        .inputFluids("minecraft:lava 1000")
        .output(MGDRecipeCapabilities.SOURCE, 1000)
        .duration(100);

    event.recipes.gtceu
        .volcanic_sourcelink("blaze")
        .inputFluids("gtceu:blaze 200")
        .output(MGDRecipeCapabilities.SOURCE, 2000)
        .duration(100);

    event.recipes.gtceu
        .bonk_reactor("blaze_powder")
        .itemInputs("minecraft:blaze_rod")
        .itemOutputs("10x minecraft:blaze_powder")
        .input(MGDRecipeCapabilities.BONK, 8)
        .duration(100);

    event.recipes.gtceu
        .source_reactor("berry")
        .itemInputs("minecraft:sweet_berries")
        .itemOutputs("ars_nouveau:sourceberry_bush")
        .input(MGDRecipeCapabilities.SOURCE, 1000)
        .duration(100);
});
