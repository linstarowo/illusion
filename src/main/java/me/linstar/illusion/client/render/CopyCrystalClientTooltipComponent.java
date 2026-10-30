package me.linstar.illusion.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.linstar.illusion.client.IllusionClient;
import me.linstar.illusion.item.CopyCrystal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public record CopyCrystalClientTooltipComponent(CopyCrystal.CopyComponent component) implements ClientTooltipComponent {

    @Override
    public int getHeight() {
        return 32;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return 32;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics graphics) {
        if (component.data() == null) return;

        try {
            BakedModel model = component.data().getModelData().getModel();
            BlockState state = component.data().getModelData().getState();
            PoseStack stack = graphics.pose();
            stack.pushPose();
            stack.translate((float) (x + 16), (float) (y + 16), (float) (150));
            stack.scale(16, -16, 16);

            stack.mulPose(Axis.XP.rotationDegrees(30.0F));
            stack.mulPose(Axis.YP.rotationDegrees(225.0F));
            stack.translate(-0.5F, -0.5F, -0.5F);

            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                    stack.last(),
                    graphics.bufferSource().getBuffer(RenderType.CUTOUT),
                    state, model,
                    1.0F, 1.0F, 1.0F,
                    15728880, OverlayTexture.NO_OVERLAY, ModelData.of(IllusionClient.ILLUSION_DATA_PROPERTY, component.data()), RenderType.CUTOUT
            );
            graphics.flush();
            stack.popPose();
        }catch (Exception ignore){}
    }
}
