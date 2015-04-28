package com.piepenguin.rfwindmill.tileentities;

import com.piepenguin.rfwindmill.lib.Constants;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

public class RenderTileEntityRotorBlock extends TileEntitySpecialRenderer {

    private ResourceLocation texture;
    private ResourceLocation objModelLocation;
    private IModelCustom model;

    public RenderTileEntityRotorBlock() {
        texture = new ResourceLocation(Constants.MODID, "textures/RotorBlockTexture.obj");
        objModelLocation = new ResourceLocation(Constants.MODID, "models/RotorBlockModel.obj");
        model = AdvancedModelLoader.loadModel(objModelLocation);
    }

    @Override
    public void renderTileEntityAt(TileEntity pEntity, double pX, double pY, double pZ, float pDt) {
        TileEntityRotorBlock entity = (TileEntityRotorBlock)pEntity;
        float rotation = entity.getRotation();
        float scale = entity.getScale();

        bindTexture(texture);
        GL11.glPushMatrix();
        GL11.glTranslated(pX, pY, pZ);
        GL11.glScalef(scale, scale, scale);
        GL11.glPushMatrix();
        GL11.glRotatef(rotation, 0, 0, 1.0f);
        model.renderAll();
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }
}
