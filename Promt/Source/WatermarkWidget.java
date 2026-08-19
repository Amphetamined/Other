package wtf.vanquish.client.ui.widget.overlay;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import wtf.vanquish.api.utils.color.UIColors;
import wtf.vanquish.api.utils.render.RenderUtil;
import wtf.vanquish.api.utils.render.fonts.Fonts;
import wtf.vanquish.client.features.modules.render.InterfaceModule;
import wtf.vanquish.client.ui.widget.Widget;
import java.awt.Color;

public class WatermarkWidget extends Widget {
    private float animatedFps = 0;

    public WatermarkWidget() { super(4f, 4f); }
    @Override public String getName() { return "Watermark"; }

    @Override public void render(MatrixStack ms) {
        if (mc.player == null) return;

        final float h = scaled(14);
        final float p = scaled(4);
        final float fontSize = scaled(6);
        final float iconSize = scaled(8f);
        final float icoSize = scaled(5f);
        final float gap = scaled(2.5f);
        final float icon = scaled(3.5f);
        final float iconGap = scaled(1f);
        final float rectGap = 2.5f;

        float x = getDraggable().getX();
        float y = getDraggable().getY();

        InterfaceModule module = InterfaceModule.getInstance();
        boolean showName = module.watermarkElements.isEnabled("Name");
        boolean showFps = module.watermarkElements.isEnabled("FPS");
        boolean showIp = module.watermarkElements.isEnabled("IP");

        animatedFps = MathHelper.lerp(0.1f, animatedFps, mc.getCurrentFps());
        String fpsText = "FPS: " + Math.round(animatedFps);
        String ipText = (mc.getCurrentServerEntry() != null) ? mc.getCurrentServerEntry().address : "singleplayer";
        String pcName = System.getProperty("user.name");

        Color textC = Color.WHITE;
        Color bg = new Color(12, 12, 18, 240);

        float wName = 0;
        if (showName) {
            wName = p + Fonts.ICOMOON.getWidth("L", icoSize) + icon + getMediumFont().getWidth("Minced", fontSize) + p;
        }

        float wOther = p;
        wOther += Fonts.ICOMOON.getWidth("U", iconSize) + iconGap + getMediumFont().getWidth(pcName, fontSize) + gap;
        if (showIp) {
            wOther += Fonts.ICOMOON.getWidth("W", iconSize) + iconGap + getMediumFont().getWidth(ipText, fontSize) + gap;
        }
        if (showFps) {
            wOther += Fonts.ICOMOON.getWidth("y", iconSize) + iconGap + getMediumFont().getWidth(fpsText, fontSize) + gap;
        }
        wOther += p - gap;

        float currentX = x;
        if (showName) {
            RenderUtil.RECT.draw(ms, currentX, y, wName, h, 3, bg);
        }

        float secondRectX = showName ? (currentX + wName + rectGap) : currentX;
        RenderUtil.RECT.draw(ms, secondRectX, y, wOther, h, 3, bg);

        float iconY = y + (h / 2f) - (iconSize / 2f);
        float icoY = y + (h / 2f) - (icoSize / 2f);
        float textY = y + (h / 2f) - (getMediumFont().getHeight(fontSize) / 2f) + 0.5f;

        if (showName) {
            float nameX = currentX + p;
            Fonts.ICOMOON.drawGradientText(ms, "L", nameX, icoY, icoSize, UIColors.primary(), UIColors.secondary(), 1.1f);
            nameX += Fonts.ICOMOON.getWidth("L", icoSize) + icon;
            getMediumFont().drawText(ms, "Minced", nameX, textY, fontSize, textC, 0f);
        }

        float otherX = secondRectX + p;
        Fonts.ICOMOON.drawGradientText(ms, "U", otherX, iconY, iconSize, UIColors.primary(), UIColors.secondary(), 1.1f);
        otherX += Fonts.ICOMOON.getWidth("U", iconSize) + iconGap;
        getMediumFont().drawText(ms, pcName, otherX, textY, fontSize, textC, 0f);
        otherX += getMediumFont().getWidth(pcName, fontSize) + gap;

        if (showIp) {
            Fonts.ICOMOON.drawGradientText(ms, "W", otherX, iconY, iconSize, UIColors.primary(), UIColors.secondary(), 1.1f);
            otherX += Fonts.ICOMOON.getWidth("W", iconSize) + iconGap;
            getMediumFont().drawText(ms, ipText, otherX, textY, fontSize, textC, 0f);
            otherX += getMediumFont().getWidth(ipText, fontSize) + gap;
        }

        if (showFps) {
            Fonts.ICOMOON.drawGradientText(ms, "i", otherX, iconY, iconSize, UIColors.primary(), UIColors.secondary(), 1.1f);
            otherX += Fonts.ICOMOON.getWidth("i", iconSize) + iconGap;
            getMediumFont().drawText(ms, fpsText, otherX, textY, fontSize, textC, 0f);
        }

        getDraggable().setWidth(showName ? (wName + rectGap + wOther) : wOther);
        getDraggable().setHeight(h);
    }
}
