package com.TheRPGAdventurer.ROTD.server.entity.helper.breath.breathweapons;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Random;

import com.TheRPGAdventurer.ROTD.server.entity.EntityTameableDragon;
import com.TheRPGAdventurer.ROTD.server.entity.helper.breath.BreathAffectedBlock;
import com.TheRPGAdventurer.ROTD.server.entity.helper.breath.BreathAffectedEntity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

/**
 * Created by TGG on 5/08/2015.
 *
 * Models the effects of a breathweapon on blocks and entities
 * Usage:
 * 1) Construct with a parent dragon
 * 2) affectBlock() to apply an area of effect to the given block (eg set fire to it)
 * 3) affectEntity() to apply an area of effect to the given entity (eg damage it)
 *
 */
public class BreathWeaponWither extends BreathWeapon {
	
  private int witherduration = 350 * 10; 
	
  public BreathWeaponWither(EntityTameableDragon i_dragon) {
    super(i_dragon);
  }

  /** if the hitDensity is high enough, manipulate the block (eg set fire to it)
   * @param world
   * @param blockPosition  the world [x,y,z] of the block
   * @param currentHitDensity
   * @return the updated block hit density
   */
  public BreathAffectedBlock affectBlock(World world, Vec3i blockPosition, BreathAffectedBlock currentHitDensity) {
    checkNotNull(world);
    checkNotNull(blockPosition);
    checkNotNull(currentHitDensity);

    BlockPos blockPos = new BlockPos(blockPosition);
    IBlockState iBlockState = world.getBlockState(blockPos);
    Block block = iBlockState.getBlock();

    Random rand = new Random();
    
    if (!world.isRemote) { 
        EntityAreaEffectCloud entityareaeffectcloud = new EntityAreaEffectCloud(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
        entityareaeffectcloud.setOwner(this.dragon);
        entityareaeffectcloud.setParticle(EnumParticleTypes.SMOKE_NORMAL);
        entityareaeffectcloud.setRadius(1.4F);
        entityareaeffectcloud.setDuration(600);
        entityareaeffectcloud.setRadiusPerTick((1.0F - entityareaeffectcloud.getRadius()) / (float)entityareaeffectcloud.getDuration());
        entityareaeffectcloud.addEffect(new PotionEffect(MobEffects.WITHER, witherduration));

        entityareaeffectcloud.setPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());                 
        int i = rand.nextInt(10000);
        if(i < 18) {
        world.spawnEntity(entityareaeffectcloud);
      }
    }  return new BreathAffectedBlock();  // reset to zero
  }
  
  /** if the hitDensity is high enough, manipulate the entity (eg set fire to it, damage it)
   * A dragon can't be damaged by its own breathweapon;
   * @param world
   * @param entityID  the ID of the affected entity
   * @param currentHitDensity the hit density
   * @return the updated hit density; null if the entity is dead, doesn't exist, or otherwise not affected
   */
  public BreathAffectedEntity affectEntity(World world, Integer entityID, BreathAffectedEntity currentHitDensity) {
    checkNotNull(world);
    checkNotNull(entityID);
    checkNotNull(currentHitDensity);

    if (entityID == dragon.getEntityId()) return null;
    if(dragon.getControllingPassenger() != null) {if (entityID == dragon.getControllingPlayer().getEntityId()) return null;}

    Entity entity = world.getEntityByID(entityID);
    if (entity == null || !(entity instanceof Entity) || entity.isDead) {
      return null;
    }
    
    final float DAMAGE_PER_HIT_DENSITY = 4.0F;

    float hitDensity = currentHitDensity.getHitDensity();
    if(entity instanceof EntityLivingBase) {
    	EntityLivingBase entity1 = (EntityLivingBase) entity;
    	if(entity1.isPotionActive(MobEffects.FIRE_RESISTANCE)) {
    		entity.attackEntityFrom(DamageSource.IN_FIRE, DAMAGE_PER_HIT_DENSITY + hitDensity);
    	}
    } else if(entity instanceof EntityTameable) {
    	EntityTameable entityTameable = (EntityTameable) entity;
    	if(entityTameable.isTamed()) {
    		entityTameable.attackEntityFrom(DamageSource.WITHER, 0);
    	}
    } else {
       entity.attackEntityFrom(DamageSource.causeMobDamage(dragon), DAMAGE_PER_HIT_DENSITY + hitDensity);
    }

    return currentHitDensity;
  }

}
