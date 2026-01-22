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
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import com.lowdragmc.lowdraglib.syncdata.managed.IRef
import gripe._90.arseng.block.entity.IAdvancedSourceTile
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
    NotifiableRecipeHandlerTrait<Int>(machine),
    ICapabilityTrait, IAdvancedSourceTile {
    companion object {
        @JvmField
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            NotifiableSourceContainer::class.java,
            NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER
        )
    }

    override fun getFieldHolder() = MANAGED_FIELD_HOLDER
    private val syncStorage = FieldManagedStorage(this)
    override fun getSyncStorage(): FieldManagedStorage = syncStorage


    @Persisted
    @DescSynced
    private var source: Int = 0;

    override fun onSyncChanged(ref: IRef?, isDirty: Boolean) {
        ref
        super.onSyncChanged(ref, isDirty)
    }


    override fun getHandlerIO(): IO = io
    override fun getCapabilityIO(): IO = capabilityIO

    override fun getContents(): List<Any?> {
        return listOf(source)
    }

    override fun getTotalContentAmount(): Double {
        return source.toDouble()
    }

    override fun handleRecipeInner(
        io: IO,
        recipe: GTRecipe,
        left: MutableList<Int>,
        simulate: Boolean
    ): List<Int>? {
        var remaining: Int = left.sum()

        if (io == IO.IN) {
            val extracted = min(remaining, source)
            if (!simulate) removeSource(extracted)
            remaining -= extracted
        } else if (io == IO.OUT) {
            val inserted = min(remaining, maxSource - source)
            if (!simulate) addSource(inserted)
            remaining -= inserted
        }

        return if (remaining <= 0) null else listOf(remaining)
    }


    override fun getCapability(): RecipeCapability<Int> = SourceRecipeCapability.CAP

    override fun getTransferRate(): Int = transferLate


    override fun getSource(): Int = source
    override fun getMaxSource(): Int = maxSource

    override fun setMaxSource(max: Int) {
        maxSource = max
    }

    override fun setSource(source: Int): Int {
        this.source = min(source, maxSource)
        notifyListeners()
        return this.source
    }

    override fun addSource(amount: Int): Int {
        val inserted = min(amount, maxSource - source)
        source += inserted
        notifyListeners()
//        return inserted
        // なぜかこういうAPI
        return source;
    }

    override fun removeSource(amount: Int): Int {
        val extracted = min(amount, source)
        source -= extracted
        notifyListeners()
//        return extracted
        return source;
    }

    override fun canAcceptSource(): Boolean {
        if (!io.support(IO.IN)) return false
        if (source >= maxSource) return false

        return true;
    }

    override fun relayCanTakePower(): Boolean {
        return io.support(IO.OUT)
    }

    override fun sourcelinksCanProvidePower(): Boolean {
        return io.support(IO.IN)
    }
}