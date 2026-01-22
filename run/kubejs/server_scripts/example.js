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

    event.recipes.gtceu.just_voiding_fluid("lava").inputFluids("minecraft:lava 1000").duration(100);

    event.recipes.gtceu.just_voiding_fluid("blaze").inputFluids("gtceu:blaze 200").duration(100);
});
