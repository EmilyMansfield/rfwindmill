package com.piepenguin.rfwindmill.tileentities;

import com.piepenguin.rfwindmill.lib.Constants;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

/**
 * Handles custom rendering of {@link com.piepenguin.rfwindmill.blocks.RotorBlock}
 * by replacing it with a rotating 3d rotor model. All possible textures are
 * stored in the renderer and determined at runtime via the metadata of the
 * corresponding {@link com.piepenguin.rfwindmill.blocks.RotorBlock}.
 */
public class RenderTileEntityRotorBlock extends TileEntitySpecialRenderer {

    private static ResourceLocation[] textures = {
            new ResourceLocation(Constants.MODID, "models/RotorBlockIronTexture.png"),
            new ResourceLocation(Constants.MODID, "models/RotorBlockElectrumTexture.png"),
            new ResourceLocation(Constants.MODID, "models/RotorBlockSignalumTexture.png"),
            new ResourceLocation(Constants.MODID, "models/RotorBlockEnderiumTexture.png"),
            new ResourceLocation(Constants.MODID, "models/RotorBlockNetherTexture.png"),
            new ResourceLocation(Constants.MODID, "models/RotorBlockDiamondTexture.png")
    };
    private static String objectModelPath = "models/RotorBlockModel.obj";
    private ResourceLocation objModelLocation;
    private IModelCustom model;

    public RenderTileEntityRotorBlock() {
        objModelLocation = new ResourceLocation(Constants.MODID, objectModelPath);
        model = AdvancedModelLoader.loadModel(objModelLocation);
    }

    @Override
    public void renderTileEntityAt(TileEntity pEntity, double pX, double pY, double pZ, float pDt) {
        TileEntityRotorBlock entity = (TileEntityRotorBlock) pEntity;
        float rotation = entity.getRotation();
        float scale = entity.getScale();
        int meta = entity.getBlockMetadata() & 3;
        bindTexture(textures[entity.getTexture()]);
        GL11.glPushMatrix();
        // Position the rotor on the centre of the face and turn it the right way
        switch(meta) {
            case 0:
                GL11.glTranslated(pX + 0.5f, pY + 0.5f, pZ + 1.0f);
                GL11.glRotatef(180.0f, 0, 1.0f, 0);
                break;
            case 1:
                GL11.glTranslated(pX, pY + 0.5f, pZ + 0.5f);
                GL11.glRotatef(90.0f, 0, 1.0f, 0);
                break;
            case 2:
                GL11.glTranslated(pX + 0.5f, pY + 0.5f, pZ);
                break;
            case 3:
                GL11.glTranslated(pX + 1.0f, pY + 0.5f, pZ + 0.5f);
                GL11.glRotatef(270.0f, 0, 1.0f, 0);
                break;
        }
        GL11.glScalef(scale, scale, scale);
        GL11.glRotatef(rotation, 0, 0, 1.0f);
        GL11.glPushMatrix();
        model.renderAll();
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }
}
