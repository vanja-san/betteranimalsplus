package its_meow.betteranimalsplus.common.entity;

import its_meow.betteranimalsplus.common.entity.util.EntityTypeContainer;
import its_meow.betteranimalsplus.common.entity.util.abstracts.EntityEelBase;
import its_meow.betteranimalsplus.init.ModEntities;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class EntityFreshwaterEel extends EntityEelBase {

    public EntityFreshwaterEel(World worldIn) {
        super(ModEntities.EEL_FRESHWATER.entityType, worldIn);
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.ENTITY_COD_FLOP;
    }

    @Override
    public EntityTypeContainer<EntityFreshwaterEel> getContainer() {
        return ModEntities.EEL_FRESHWATER;
    }
}
