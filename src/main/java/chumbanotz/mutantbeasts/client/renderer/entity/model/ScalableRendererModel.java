package chumbanotz.mutantbeasts.client.renderer.entity.model;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScalableRendererModel extends RendererModel {
	private float scale = 1.0F;

	public ScalableRendererModel(Model model, int texOffX, int texOffY) {
		super(model, texOffX, texOffY);
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	@Override
	public void render(float scale) {
		if (!this.isHidden && this.showModel) {
			GlStateManager.pushMatrix();
			GlStateManager.scalef(this.scale, this.scale, this.scale);
			super.render(scale);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void renderWithRotation(float scale) {
		if (!this.isHidden && this.showModel) {
			GlStateManager.pushMatrix();
			GlStateManager.scalef(this.scale, this.scale, this.scale);
			super.renderWithRotation(scale);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void postRender(float scale) {
		if (!this.isHidden && this.showModel) {
			GlStateManager.pushMatrix();
			GlStateManager.scalef(this.scale, this.scale, this.scale);
			super.postRender(scale);
			GlStateManager.popMatrix();
		}
	}
}