#Fluid Potions
A Fabric mod/api that creates fluid and bucket variations of all potions detected in the potion registry
###Adding new potions
Right now, the potion needs to be added to the water tag to behave like water. If there is a way to add them programatically, please make a pull request.

New potions need to be registered in the language json using the base
```
item.fluidpotions.potion_bucket.effect.[EFFECT NAME]
```
Preferably, the translated text would read
```
Bucket of [POTION]
```

###Details
Potion buckets are registered similarly to how potions are registered: the default bucket is an "Uncraftable bucket". The potion information is registered using
```
PotionUtil.setPotion([Itemstack], [CompoundTag])
```
The fluids themselves are not registered like this however, but they each have their filled bucket registered as a PotionBucket.
As such, normal bucket extraction needs to be treated carefully. In the event of this case, please check if the fluid is of instance "PotionFluid" and apply the potion information using this format:
```
PotionUtil.setPotion(filledBucketStack, potionFluid.getPotion());
    filledBucketStack - an itemstack with PotionBucketItem
    potionFluid - the fluid being extracted
```
Bucket insertion should also be treated with care.
If using an access widener (dear god why) to check the fluid of the bucket, it will detect an uncraftable potion fluid.

To detect the proper fluid, use the given getPotionFluid(ItemStack) method in the PotionBucketItem.

###Extra
I might fork some of this mod into a unified bucket API (Grand Unified Buckets)

If anyone is interested, please let me know.