package its_meow.betteranimalsplus.common.entity;

import java.util.Set;

import its_meow.betteranimalsplus.common.entity.util.EntityTypeContainer;
import its_meow.betteranimalsplus.common.entity.util.ISelectiveVariantTypes;
import its_meow.betteranimalsplus.common.entity.util.abstracts.EntityWaterMobPathing;
import its_meow.betteranimalsplus.common.entity.util.abstracts.EntityWaterMobPathingWithTypesAirBreathing;
import its_meow.betteranimalsplus.init.ModEntities;
import its_meow.betteranimalsplus.init.ModLootTables;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.controller.DolphinLookController;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.BreatheAirGoal;
import net.minecraft.entity.ai.goal.FindWaterGoal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary.Type;

public class EntityWhale extends EntityWaterMobPathingWithTypesAirBreathing implements ISelectiveVariantTypes<EntityWaterMobPathing> {

    public int attacksLeft = 0;
    public float lastBodyRotation = 0;

    public EntityWhale(World world) {
        super(ModEntities.WHALE.entityType, world);
        //this.moveController = new WhaleMoveHelper(this);
        this.lookController = new DolphinLookController(this, 10);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new BreatheAirGoal(this));
        this.goalSelector.addGoal(0, new FindWaterGoal(this));
        //this.goalSelector.addGoal(1, new MoveTowardsTargetGoal(this, 1.0D, 30F));
        this.goalSelector.addGoal(2, new WhaleMeleeAttackGoal(this));
        this.goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.0D, 10));
        this.goalSelector.addGoal(4, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(5, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this, new Class[0]));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50D);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(2D);
        this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8D);
    }

    protected SoundEvent getSplashSound() {
        return SoundEvents.ENTITY_DOLPHIN_SPLASH;
    }

    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_DOLPHIN_SWIM;
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float) (this.getVariantName().equals("narwhal") ? this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue() : 1F));
        if(flag) {
            if(!this.getVariantName().equals("narwhal")) {
                if(attacksLeft > 0) {
                    attacksLeft--;
                }
                if(entityIn instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity) entityIn;
                    int ticks = 0;
                    if (this.world.getDifficulty() == Difficulty.EASY) {
                        ticks = 50;
                    } else if (this.world.getDifficulty() == Difficulty.NORMAL) {
                        ticks = 100;
                    } else if (this.world.getDifficulty() == Difficulty.HARD) {
                        ticks = 140;
                    }
                    player.addPotionEffect(new EffectInstance(Effects.SLOWNESS, ticks, 1, false, false));
                    player.addPotionEffect(new EffectInstance(Effects.NAUSEA, ticks + 40, 1, false, false));
                }
            }
            Vec3d pos = this.getPositionVector();
            Vec3d targetPos = entityIn.getPositionVector();
            ((LivingEntity) entityIn).knockBack(this, this.getVariantName().equals("narwhal") ? 0.8F : 2F, pos.x - targetPos.x, pos.z - targetPos.z);
        }
        return flag;
    }

    @Override
    public String[] getTypesFor(Biome biome, Set<Type> types) {
        if(biome == Biomes.COLD_OCEAN || biome == Biomes.DEEP_COLD_OCEAN) {
            return new String[] { "bottlenose", "pilot" };
        } else if(biome == Biomes.DEEP_FROZEN_OCEAN || biome == Biomes.FROZEN_OCEAN) {
            return new String[] { "narwhal", "beluga" };
        } else if(biome == Biomes.DEEP_LUKEWARM_OCEAN || biome == Biomes.DEEP_OCEAN || biome == Biomes.DEEP_WARM_OCEAN) {
            return new String[] { "cuviers", "pilot" };
        } else {
            return new String[] { "cuviers", "pilot", "false_killer" };
        }
    }

    @Override
    protected ResourceLocation getLootTable() {
        return ModLootTables.WHALE;
    }

    @Override
    public EntityTypeContainer<?> getContainer() {
        return ModEntities.WHALE;
    }

    public static class WhaleMoveHelper extends MovementController {
        private final EntityWhale whale;

        public WhaleMoveHelper(EntityWhale whale) {
            super(whale);
            this.whale = whale;
        }

        public void tick() {
            if(this.whale.isInWater()) {
                this.whale.setMotion(this.whale.getMotion().add(0.0D, 0.005D, 0.0D));
            }

            if(this.action == MovementController.Action.MOVE_TO && !this.whale.getNavigator().noPath()) {
                double d0 = this.posX - this.whale.posX;
                double d1 = this.posY - this.whale.posY;
                double d2 = this.posZ - this.whale.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                if(d3 < (double) 2.5000003E-7F) {
                    this.mob.setMoveForward(0.0F);
                } else {
                    float f = (float) (MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
                    this.whale.rotationYaw = this.limitAngle(this.whale.rotationYaw, f, 10.0F);
                    this.whale.renderYawOffset = this.whale.rotationYaw;
                    this.whale.rotationYawHead = this.whale.rotationYaw;
                    float f1 = (float) (this.speed * this.whale.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue());
                    if(this.whale.isInWater()) {
                        this.whale.setAIMoveSpeed(f1 * 0.02F);
                        float f2 = -((float) (MathHelper.atan2(d1, (double) MathHelper.sqrt(d0 * d0 + d2 * d2)) * (double) (180F / (float) Math.PI)));
                        f2 = MathHelper.clamp(MathHelper.wrapDegrees(f2), -85.0F, 85.0F);
                        this.whale.rotationPitch = this.limitAngle(this.whale.rotationPitch, f2, 5.0F);
                        float f3 = MathHelper.cos(this.whale.rotationPitch * ((float) Math.PI / 180F));
                        float f4 = MathHelper.sin(this.whale.rotationPitch * ((float) Math.PI / 180F));
                        this.whale.moveForward = f3 * f1;
                        this.whale.moveVertical = -f4 * f1;
                    } else {
                        this.whale.setAIMoveSpeed(f1 * 0.1F);
                    }

                }
            } else {
                this.whale.setAIMoveSpeed(0.0F);
                this.whale.setMoveStrafing(0.0F);
                this.whale.setMoveVertical(0.0F);
                this.whale.setMoveForward(0.0F);
            }
        }
    }
    
    public static class WhaleMeleeAttackGoal extends MeleeAttackGoal {
        
        private EntityWhale whale;

        public WhaleMeleeAttackGoal(EntityWhale whale) {
            super(whale, 1.2F, true);
            this.whale = whale;
        }
        
        @Override
        public void startExecuting() {
            if(!whale.getVariantName().equals("narwhal")) {
                whale.attacksLeft = 1;
            }
            super.startExecuting();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return (whale.getVariantName().equals("narwhal") || whale.attacksLeft > 0) && super.shouldContinueExecuting();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity p_190102_1_, double p_190102_2_) {
            if(whale.attacksLeft > 0 || whale.getVariantName().equals("narwhal")) {
                super.checkAndPerformAttack(p_190102_1_, p_190102_2_);
            } else {
                this.resetTask();
            }
        }

        @Override
        public void resetTask() {
            super.resetTask();
            if(whale.attacksLeft <= 0 && !whale.getVariantName().equals("narwhal")) {
                this.attacker.setAttackTarget(null);
            }
        }
        
        @Override
        protected double getAttackReachSqr(LivingEntity attackTarget) {
            return (double)(this.attacker.getWidth() * this.attacker.getWidth() + attackTarget.getWidth());
         }
        
    }

}
