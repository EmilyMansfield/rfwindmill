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
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class WindmillBlock extends Block implements ITileEntityProvider {

    protected final int maximumEnergyGeneration;
    protected final int maximumEnergyTransfer;
    protected final int capacity;

    private static final int maxMeta = 4;
    private String name;
    private IIcon[] icons;

    public WindmillBlock(String pName, int pMaximumEnergyGeneration, int pCapacity) {
        super(Material.rock);
        setHardness(3.5f);
        setStepSound(Block.soundTypeMetal);
        maximumEnergyGeneration = pMaximumEnergyGeneration;
        maximumEnergyTransfer = pMaximumEnergyGeneration * ModConfiguration.getWindmillEnergyTransferMultiplier();
        capacity = pCapacity;
        name = pName;
        this.setBlockName(Constants.MODID + "_" + name);
        this.setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerBlock(this, ItemBlockWindmillBlock.class, name);
        icons = new IIcon[maxMeta];
    }

    @Override
    public void registerBlockIcons(IIconRegister pIconRegister) {
        for(int i = 0; i < maxMeta; ++i) {
            icons[i] = pIconRegister.registerIcon(Constants.MODID + ":" + name + "i" + "Side");
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int pSide, int pMeta) {
        return icons[pMeta];
    }

    @Override
    public TileEntity createNewTileEntity(World pWorld, int pMeta) {
        return new TileEntityWindmillBlock(maximumEnergyGeneration, maximumEnergyTransfer, capacity);
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
        int direction = MathHelper.floor_double((double) (pEntity.rotationYaw * 4.0f / 360.0f) + 0.50) & 3;
        pWorld.setBlockMetadataWithNotify(pX, pY, pZ, direction, 2);
        if(pItemStack.stackTagCompound != null) {
            TileEntityWindmillBlock entity = (TileEntityWindmillBlock)pWorld.getTileEntity(pX, pY, pZ);
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
                }
                else {
                    // Print energy information otherwise
                    printChatInfo(pWorld, pX, pY, pZ, pPlayer);
                    return true;
                }
            }
            else {
                ItemStack equippedItem = pPlayer.getCurrentEquippedItem();
                if(equippedItem != null && (equippedItem.getItem() == ModItems.rotor1 ||
                        equippedItem.getItem() == ModItems.rotor2 ||
                        equippedItem.getItem() == ModItems.rotor3 ||
                        equippedItem.getItem() == ModItems.rotor4)) {
                    int direction = MathHelper.floor_double((double) (pPlayer.rotationYaw * 4.0f / 360.0f) + 0.50) & 3;
                    ForgeDirection fDirection = Util.intToDirection(direction);
                    int dx = pX + fDirection.offsetX;
                    int dy = pY + fDirection.offsetY;
                    int dz = pZ + fDirection.offsetZ;
                    TileEntityWindmillBlock entity = (TileEntityWindmillBlock)pWorld.getTileEntity(pX, pY, pZ);
                    if(RotorBlock.canPlace(pWorld, dx, dy, dz, pPlayer, fDirection) && !entity.hasRotor()) {
                        // Attach the rotor to the windmill
                        RFWItem equippedRotor = (RFWItem)equippedItem.getItem();
                        pWorld.setBlock(dx, dy, dz, ModBlocks.rotorBlock1);
                        pWorld.setBlockMetadataWithNotify(dx, dy, dz, direction, 2);
                        TileEntityRotorBlock rotorEntity = (TileEntityRotorBlock)pWorld.getTileEntity(dx, dy, dz);
                        // No arbitrary switch statements :(
                        int rotorType = -1;
                        if(equippedRotor == ModItems.rotor1) {
                            rotorType = 0;
                        }
                        else if(equippedRotor == ModItems.rotor2) {
                            rotorType = 1;
                        }
                        else if(equippedRotor == ModItems.rotor3) {
                            rotorType = 2;
                        }
                        else if(equippedRotor == ModItems.rotor4) {
                            rotorType = 3;
                        }
                        // Tell entities the rotor type
                        rotorEntity.setType(rotorType);
                        entity.setRotor(rotorType, fDirection);
                        // Remove rotor from player's inventory
                        if(equippedItem.stackSize > 1) {
                            equippedItem.stackSize -= 1;
                        }
                        else {
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

    private void dismantle(World pWorld, int pX, int pY, int pZ) {
        // Remove the attached rotor, if there is one
        TileEntityWindmillBlock entity = (TileEntityWindmillBlock)pWorld.getTileEntity(pX, pY, pZ);
        Preconditions.checkNotNull(entity);
        if(entity.hasRotor()) {
            ForgeDirection dir = entity.getRotorDir();
            RotorBlock rotor = (RotorBlock)pWorld.getBlock(
                    pX + dir.offsetX,
                    pY + dir.offsetY,
                    pZ + dir.offsetZ);
            rotor.dismantle(pWorld, pX + dir.offsetX, pY + dir.offsetY, pZ + dir.offsetZ);
        }

        // Remove the actual turbine and drop it
        ItemStack itemStack = new ItemStack(this);
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

    private void printChatInfo(World pWorld, int pX, int pY, int pZ, EntityPlayer pPlayer) {
        TileEntityWindmillBlock entity = (TileEntityWindmillBlock)pWorld.getTileEntity(pX, pY, pZ);
        String msg = String.format("%s: %d/%d RF %s: %.2f RF/t",
                Lang.localise("energy.stored"),
                entity.getEnergyStored(),
                entity.getMaxEnergyStored(),
                Lang.localise("energy.generating"),
                entity.getEnergyGeneration());
        pPlayer.addChatMessage(new ChatComponentText(msg));
    }
}
