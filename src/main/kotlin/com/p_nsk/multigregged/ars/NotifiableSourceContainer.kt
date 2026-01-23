@file:Suppress("PropertyName")

package com.p_nsk.multigregged.ars

import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.trait.ICapabilityTrait
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import gripe._90.arseng.block.entity.IAdvancedSourceTile
import com.p_nsk.multigregged.MultiGreggedMod.Companion.LOGGER
import kotlin.math.min


class NotifiableSourceContainer @JvmOverloads constructor(
    machine: MetaMachine,
    @Persisted
    @DescSynced
    private var maxSource: Int,
    @Persisted
    private val transferLate: Int,
    val io: IO,
    private val capabilityIO: IO = io
) :
    NotifiableRecipeHandlerTrait<SourceIngredient>(machine),
    ICapabilityTrait, IAdvancedSourceTile {
    companion object {
        @JvmField
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            NotifiableSourceContainer::class.java,
            NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER
        )
    }

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER

    @Persisted
    @DescSynced
    private var source: Int = 0

    override fun getHandlerIO(): IO = io
    override fun getCapabilityIO(): IO = capabilityIO

    override fun getContents(): List<Any?> {
        LOGGER.info(
            "[SourceTrait] getContents thread={} io={} capIO={} source={}/{} rate={} machine={}",
            Thread.currentThread().name,
            io,
            capabilityIO,
            source,
            maxSource,
            transferLate,
            machine.javaClass.simpleName
        )
        return listOf(SourceIngredient(source))
    }

    override fun getTotalContentAmount(): Double {
        return source.toDouble()
    }

    override fun handleRecipeInner(
        io: IO,
        recipe: GTRecipe,
        left: MutableList<SourceIngredient>,
        simulate: Boolean
    ): List<SourceIngredient>? {
        // ここが呼ばれていれば、少なくともcapability自体はマッチング/実行フェーズに入っている
        LOGGER.info(
            "[SourceTrait] handleRecipeInner sim={} io={} recipe={} left={} stored={}/{} rate={} handlerIO={} capIO={}",
            simulate,
            io,
            recipe.id,
            left.joinToString(prefix = "[", postfix = "]") { it.source.toString() },
            source,
            maxSource,
            transferLate,
            this.io,
            capabilityIO
        )

        var remaining: Int = left.sumOf { it.source }

        if (io == IO.IN) {
            val extracted = min(remaining, source)
            LOGGER.info("[SourceTrait]  IN need={} extracted={} simulate={}", remaining, extracted, simulate)
            if (!simulate) removeSource(extracted)
            remaining -= extracted
        } else if (io == IO.OUT) {
            val inserted = min(remaining, maxSource - source)
            LOGGER.info("[SourceTrait] OUT need={} inserted={} simulate={}", remaining, inserted, simulate)
            if (!simulate) addSource(inserted)
            remaining -= inserted
        } else {
            LOGGER.warn("[SourceTrait] Unknown IO={} (recipe={})", io, recipe.id)
        }

        val result = if (remaining <= 0) null else listOf(SourceIngredient(remaining))
        LOGGER.info(
            "[SourceTrait] result remaining={} after stored={}/{} -> {}",
            remaining,
            source,
            maxSource,
            result?.joinToString(prefix = "[", postfix = "]") { it.source.toString() } ?: "null"
        )
        return result
    }


    override fun getCapability(): RecipeCapability<SourceIngredient> = SourceRecipeCapability.CAP
    override fun getSize(): Int = 1

    override fun getTransferRate(): Int = transferLate
    override fun getSource(): Int = source
    override fun getMaxSource(): Int = maxSource

    override fun setMaxSource(max: Int) {
        maxSource = max
    }

    override fun setSource(source: Int): Int {
        val before = this.source
        val after = min(source, maxSource)
        this.source = after
        LOGGER.info(
            "[SourceTrait] setSource thread={} io={} capIO={} before={} requested={} after={} max={} rate={}",
            Thread.currentThread().name,
            io,
            capabilityIO,
            before,
            source,
            after,
            maxSource,
            transferLate
        )
        notifyListeners()
        return this.source
    }

    override fun addSource(amount: Int): Int {
        val before = source
        val inserted = min(amount, maxSource - source)
        source += inserted
        LOGGER.info(
            "[SourceTrait] addSource thread={} io={} capIO={} before={} request={} inserted={} after={} max={} canIn={} canOut={}",
            Thread.currentThread().name,
            io,
            capabilityIO,
            before,
            amount,
            inserted,
            source,
            maxSource,
            canCapInput(),
            canCapOutput()
        )
        notifyListeners()
        return source
    }

    override fun removeSource(amount: Int): Int {
        val before = source
        val extracted = min(amount, source)
        source -= extracted
        LOGGER.info(
            "[SourceTrait] removeSource thread={} io={} capIO={} before={} request={} extracted={} after={} max={} canIn={} canOut={}",
            Thread.currentThread().name,
            io,
            capabilityIO,
            before,
            amount,
            extracted,
            source,
            maxSource,
            canCapInput(),
            canCapOutput()
        )
        notifyListeners()
        return source
    }

    override fun canAcceptSource(): Boolean {
        val can = canCapInput() && source < maxSource
        return can
    }

    override fun relayCanTakePower(): Boolean {
        val can = canCapOutput()
        return can
    }

    override fun sourcelinksCanProvidePower(): Boolean {
        val can = canCapInput()
        return can
    }

    override fun notifyListeners() {
        super.notifyListeners()
        LOGGER.info(
            "[SourceTrait] notifyListeners",
        )
    }
}