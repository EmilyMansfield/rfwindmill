package com.piepenguin.rfwindmill.blocks;

import com.google.common.base.Preconditions;
import com.piepenguin.rfwindmill.items.ModItems;
import com.piepenguin.rfwindmill.items.RFWItem;
import com.piepenguin.rfwindmill.lib.*;
import com.piepenguin.rfwindmill.tileentities.TileEntityRotorBlock;
import com.piepenguin.rfwindmill.tileentities.TileEntityWindmillBlock;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Arrays;
import java.util.List;

/**
 * The main structural block of a windmill, handles creation of the
 * {@link TileEntityWindmillBlock} tile entity and {@link RotorBlock} creation.
 * <p/>
 * Multiple block types are implemented using metadata and an array of relevant
 * values for each type is passed to the constructor.
 * <p/>
 * When sneak-right clicked empty handed info is printed to the chat detailing
 * current RF production rate and RF storage. When sneak-right clicked with a
 * wrench the {@link WindmillBlock} is removed from the world and dropped
 * instantly as an item, as is the attached {@link RotorBlock} (if any).
 * Right clicking with a rotor will attach the rotor to the block and create a
 * corresponding {@link RotorBlock}.
 */
public class WindmillBlock extends Block implements ITileEntityProvider {

    protected final float[] efficiency;
    protected final int[] maximumEnergyTransfer;
    protected final int[] capacity;

    private static final int maxMeta = 4;
    private String name;
    private IIcon[] icons;

    public WindmillBlock(String pName, float[] pEfficiency, int[] pCapacity) {
        super(Material.rock);
        setHardness(3.5f);
        setStepSound(Block.soundTypeMetal);
        efficiency = pEfficiency;
        capacity = pCapacity;
        maximumEnergyTransfer = new int[capacity.length];
        for(int i = 0; i < capacity.length; ++i) {
            maximumEnergyTransfer[i] = (int) (ModConfiguration.getWindmillEnergyTransferMultiplier() * capacity[i]);
        }
        name = pName;
        this.setBlockName(Constants.MODID + "_" + name);
        this.setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerBlock(this, ItemBlockWindmillBlock.class, name);
        icons = new IIcon[maxMeta];
    }

    @Override
    public void registerBlockIcons(IIconRegister pIconRegister) {
        for(int i = 0; i < maxMeta; ++i) {
            icons[i] = pIconRegister.registerIcon(Constants.MODID + ":" + name + (i + 1) + "Side");
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int pSide, int pMeta) {
        return icons[pMeta];
    }

    @Override
    public TileEntity createNewTileEntity(World pWorld, int pMeta) {
        return new TileEntityWindmillBlock(efficiency[pMeta], maximumEnergyTransfer[pMeta], capacity[pMeta]);
    }

    @Override
    public boolean hasTileEntity(int pMetadata) {
        return true;
    }

    @Override
    public int damageDropped(int pMeta) {
        return pMeta;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void getSubBlocks(Item pItem, CreativeTabs pCreativeTabs, List list) {
        for(int i = 0; i < maxMeta; ++i) {
            list.add(new ItemStack(pItem, 1, i));
        }
    }

    @Override
    public void onBlockPlacedBy(World pWorld, int pX, int pY, int pZ, EntityLivingBase pEntity, ItemStack pItemStack) {
        // If the ItemBlock version has energy stored in it then give the newly
        // created tile entity that energy
        if(pItemStack.stackTagCompound != null) {
            TileEntityWindmillBlock entity = (TileEntityWindmillBlock) pWorld.getTileEntity(pX, pY, pZ);
            entity.setEnergyStored(pItemStack.stackTagCompound.getInteger(EnergyStorage.NBT_ENERGY));
        }
        super.onBlockPlacedBy(pWorld, pX, pY, pZ, pEntity, pItemStack);
    }

    @Override
    public boolean onBlockActivated(World pWorld, int pX, int pY, int pZ, EntityPlayer pPlayer, int pSide, float pDx, float pDy, float pDz) {
        if(!pWorld.isRemote) {
            if(pPlayer.isSneaking()) {
                // Dismantle block if player has a wrench
                if(Util.hasWrench(pPlayer, pX, pY, pZ)) {
                    dismantle(pWorld, pX, pY, pZ);
                    return true;
                } else {
                    // Print energy information otherwise
                    printChatInfo(pWorld, pX, pY, pZ, pPlayer);
                    return true;
                }
            } else {
                // Attach a rotor if the player is holding one
                ItemStack equippedItem = pPlayer.getCurrentEquippedItem();
                if(equippedItem != null && (equippedItem.getItem() == ModItems.rotor1 ||
                        equippedItem.getItem() == ModItems.rotor2 ||
                        equippedItem.getItem() == ModItems.rotor3 ||
                        equippedItem.getItem() == ModItems.rotor4 ||
                        equippedItem.getItem() == ModItems.rotor5 ||
                        equippedItem.getItem() == ModItems.wheel1)) {
                    // Get the direction offset of the face the player clicked
                    ForgeDirection fDirection = ForgeDirection.getOrientation(pSide);
                    if(fDirection == ForgeDirection.DOWN || fDirection == ForgeDirection.UP) {
                        return false;
                    }
                    int dx = pX + fDirection.offsetX;
                    int dy = pY + fDirection.offsetY;
                    int dz = pZ + fDirection.offsetZ;
                    // Check that the tile entity for this block doesn't already have a rotor
                    // and that a rotor can be placed at the offset
                    TileEntityWindmillBlock entity = (TileEntityWindmillBlock) pWorld.getTileEntity(pX, pY, pZ);
                    if(RotorBlock.canPlace(pWorld, dx, dy, dz, pPlayer, fDirection, 1) && !entity.hasAttachment()) {
                        // Attach the rotor to the windmill
                        RFWItem equippedRotor = (RFWItem) equippedItem.getItem();
                        // No arbitrary switch statements :(
                        int rotorType = -1;
                        if(equippedRotor == ModItems.rotor1) {
                            rotorType = 0;
                        } else if(equippedRotor == ModItems.rotor2) {
                            rotorType = 1;
                        } else if(equippedRotor == ModItems.rotor3) {
                            rotorType = 2;
                        } else if(equippedRotor == ModItems.rotor4) {
                            rotorType = 3;
                        } else if(equippedRotor == ModItems.rotor5) {
                            rotorType = 4;
                        } else if(equippedRotor == ModItems.wheel1) {
                            rotorType = 5;
                        }
                        pWorld.setBlock(dx, dy, dz, ModBlocks.rotorBlock1);
                        // Calculate metadata depending on rotor type and facing
                        int meta = Util.directionToInt(fDirection);
                        if(rotorType == 4) meta += 1 << 2;
                        else if(rotorType > 4) meta += 1 << 3;
                        pWorld.setBlockMetadataWithNotify(dx, dy, dz, meta, 2);
                        TileEntityRotorBlock rotorEntity = (TileEntityRotorBlock) pWorld.getTileEntity(dx, dy, dz);
                        // Tell entities the rotor type
                        rotorEntity.setType(rotorType);
                        entity.setRotor(rotorType, fDirection);
                        // Remove rotor from player's inventory
                        if(equippedItem.stackSize > 1) {
                            equippedItem.stackSize -= 1;
                        } else {
                            pPlayer.destroyCurrentEquippedItem();
                        }
                    } // Brace cascade of shame
                }
            }
        }
        return false;
    }

    @Override
    public void onBlockHarvested(World pWorld, int pX, int pY, int pZ, int pSide, EntityPlayer pPlayer) {
        if(!pWorld.isRemote) {
            dismantle(pWorld, pX, pY, pZ);
        }
    }

    /**
     * Removes the attached rotor (if there is one) and drops it the ground
     * and then removes this block and drops that too, making sure to save the
     * stored energy in the newly created {@link ItemStack}
     *
     * @param pWorld Minecraft {@link World}
     * @param pX     X coordinate of this block
     * @param pY     Y coordinate of this block
     * @param pZ     Z coordinate of this block
     */
    private void dismantle(World pWorld, int pX, int pY, int pZ) {
        // Something has gone very wrong if this block doesn't have a tile entity
        TileEntityWindmillBlock entity = (TileEntityWindmillBlock) pWorld.getTileEntity(pX, pY, pZ);
        Preconditions.checkNotNull(entity);
        // Remove the attached rotor, if there is one
        if(entity.hasAttachment()) {
            ForgeDirection dir = entity.getRotorDir();
            RotorBlock rotor = (RotorBlock) pWorld.getBlock(
                    pX + dir.offsetX,
                    pY + dir.offsetY,
                    pZ + dir.offsetZ);
            rotor.dismantle(pWorld, pX + dir.offsetX, pY + dir.offsetY, pZ + dir.offsetZ);
        }

        // Remove the actual turbine and drop it
        ItemStack itemStack = new ItemStack(this, 1, entity.getBlockMetadata());
        int energy = entity.getEnergyStored();
        if(energy > 0) {
            if(itemStack.getTagCompound() == null) {
                itemStack.setTagCompound(new NBTTagCompound());
            }
            itemStack.getTagCompound().setInteger(EnergyStorage.NBT_ENERGY, energy);
        }
        pWorld.setBlockToAir(pX, pY, pZ);
        EntityItem entityItem = new EntityItem(pWorld, pX + 0.5, pY + 0.5, pZ + 0.5, itemStack);
        entityItem.motionX = 0;
        entityItem.motionZ = 0;
        pWorld.spawnEntityInWorld(entityItem);
    }

    /**
     * Print the amount of energy stored in the windmill in RF and the amount
     * being produced in RF/t to the chat.
     *
     * @param pWorld  Minecraft {@link World}
     * @param pX      X coordinate of this block
     * @param pY      Y coordinate of this block
     * @param pZ      Z coordinate of this block
     * @param pPlayer Player whose chat the message should be printed to
     */
    private void printChatInfo(World pWorld, int pX, int pY, int pZ, EntityPlayer pPlayer) {
        TileEntityWindmillBlock entity = (TileEntityWindmillBlock) pWorld.getTileEntity(pX, pY, pZ);
        String msg = String.format("%s: %d/%d RF %s: %.2f RF/t",
                Lang.localise("energy.stored"),
                entity.getEnergyStored(),
                entity.getMaxEnergyStored(),
                Lang.localise("energy.generating"),
                entity.getCurrentEnergyGeneration());
        pPlayer.addChatMessage(new ChatComponentText(msg));
    }
}
