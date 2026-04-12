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
import com.p_nsk.multigregged.MultiGreggedMod.Companion.LOGGER
import gripe._90.arseng.block.entity.IAdvancedSourceTile
import kotlin.math.min


open class NotifiableSourceContainer @JvmOverloads constructor(
    machine: MetaMachine,
    @Persisted
    @DescSynced
    private var maxSource: Int,
    @Persisted
    private val transferRate: Int,
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
        LOGGER.debug(
            "[SourceTrait] getContents thread={} io={} capIO={} source={}/{} rate={} machine={}",
            Thread.currentThread().name,
            io,
            capabilityIO,
            source,
            maxSource,
            transferRate,
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
        LOGGER.debug(
            "[SourceTrait] handleRecipeInner sim={} io={} recipe={} left={} stored={}/{} rate={} handlerIO={} capIO={}",
            simulate,
            io,
            recipe.id,
            left.joinToString(prefix = "[", postfix = "]") { it.source.toString() },
            source,
            maxSource,
            transferRate,
            this.io,
            capabilityIO
        )

        var remaining: Int = left.sumOf { it.source }

        if (io == IO.IN) {
            val extracted = min(remaining, source)
            LOGGER.debug("[SourceTrait]  IN need={} extracted={} simulate={}", remaining, extracted, simulate)
            if (!simulate) removeSource(extracted)
            remaining -= extracted
        } else if (io == IO.OUT) {
            val inserted = min(remaining, maxSource - source)
            LOGGER.debug("[SourceTrait] OUT need={} inserted={} simulate={}", remaining, inserted, simulate)
            if (!simulate) addSource(inserted)
            remaining -= inserted
        } else {
            LOGGER.warn("[SourceTrait] Unknown IO={} (recipe={})", io, recipe.id)
        }

        val result = if (remaining <= 0) null else listOf(SourceIngredient(remaining))
        LOGGER.debug(
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

    override fun getTransferRate(): Int = transferRate
    override fun getSource(): Int = source
    fun isEmpty(): Boolean = source <= 0
    override fun getMaxSource(): Int = maxSource

    override fun setMaxSource(max: Int) {
        maxSource = max
    }

    override fun setSource(source: Int): Int {
        val before = this.source
        val after = min(source, maxSource)
        this.source = after
        LOGGER.debug(
            "[SourceTrait] setSource thread={} io={} capIO={} before={} requested={} after={} max={} rate={}",
            Thread.currentThread().name,
            io,
            capabilityIO,
            before,
            source,
            after,
            maxSource,
            transferRate
        )
        notifyListeners()
        return this.source
    }

    override fun addSource(amount: Int): Int {
        val before = source
        val inserted = min(amount, maxSource - source)
        source += inserted
        LOGGER.debug(
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
        LOGGER.debug(
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
        LOGGER.debug(
            "[SourceTrait] notifyListeners",
        )
    }
}