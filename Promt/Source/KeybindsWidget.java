package wtf.vanquish.client.ui.widget.overlay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import wtf.vanquish.api.module.Module;
import wtf.vanquish.api.module.ModuleManager;
import wtf.vanquish.api.system.backend.KeyStorage;
import wtf.vanquish.api.utils.color.UIColors;
import wtf.vanquish.api.utils.render.RenderUtil;
import wtf.vanquish.api.utils.render.fonts.Fonts;
import wtf.vanquish.client.ui.widget.ContainerWidget;
import java.awt.Color;
import java.util.*;

public class KeybindsWidget extends ContainerWidget {
    private final Map<Module, Float> animMap = new HashMap<>();

    public KeybindsWidget() { super(3f, 120f); }
    @Override public String getName() { return "Keybinds"; }
    @Override protected Map<String, ContainerElement.ColoredString> getCurrentData() { return null; }

    @Override
    public void render(MatrixStack ms) {
        ModuleManager.getInstance().getModules().forEach(m -> {
            boolean t = m.isEnabled() && m.hasBind();
            float cur = animMap.getOrDefault(m, 0f);
            animMap.put(m, cur + ((t ? 1f : 0f) - cur) * 0.15f);
        });
        animMap.entrySet().removeIf(e -> e.getValue() < 0.05f && !e.getKey().isEnabled());

        float x = getDraggable().getX(), y = getDraggable().getY(), width = getDraggable().getWidth();
        boolean isRightSide = x + (width / 2f) > MinecraftClient.getInstance().getWindow().getScaledWidth() / 2f;

        float h = scaled(11), p = scaled(3.5f), fS = scaled(6f), iconS = scaled(8f);
        Color bg = new Color(12, 12, 18, 240);

        String title = "Keybinds";
        String iconChar = "K";

        float titleW = getMediumFont().getWidth(title, fS);
        float iconW = Fonts.ICOMOON.getWidth(iconChar, iconS);

        float maxW = titleW + iconW + p * 3;
        for (Map.Entry<Module, Float> e : animMap.entrySet()) {
            if (e.getValue() <= 0.05f) continue;
            String keyName = KeyStorage.getBind(e.getKey().getBind());
            float w = getMediumFont().getWidth(e.getKey().getName(), fS) +
                    getMediumFont().getWidth(keyName, fS) + p * 4.5f;
            if (w > maxW) maxW = w;
        }

        float renderX = isRightSide ? (x + width - maxW) : x;
        float currentY = y;

        RenderUtil.RECT.draw(ms, renderX, currentY, maxW, h, 3f, bg);
        getMediumFont().drawText(ms, title, renderX + p, currentY + h/2f - fS/2f, fS, Color.WHITE, 0f);
        Fonts.ICOMOON.drawGradientText(ms, iconChar, renderX + maxW - p - iconW, currentY + h/2f - iconS/2f, iconS,
                UIColors.primary(), UIColors.secondary(), 1.1f);

        currentY += h + 1.5f;

        for (Module m : ModuleManager.getInstance().getModules()) {
            if (!animMap.containsKey(m)) continue;
            float anim = animMap.get(m);
            if (anim <= 0.05f) continue;

            float rowH = h * anim;
            int alpha = (int)(255 * anim);

            Color dynamicBg = new Color(0, 0, 0, 205);
            Color dynamicText = new Color(255, 255, 255, alpha);
            Color dynamicKeyBg = new Color(0, 0, 0, (int)(180 * anim));
            Color dynamicOutline = new Color(100, 100, 100, 255);

            RenderUtil.BLUR_RECT.draw(ms, renderX, currentY, maxW, rowH, 3f, dynamicBg);

            float textCenterY = currentY + (rowH / 2f) - (fS / 2f);

            getMediumFont().drawText(ms, m.getName(), renderX + p, textCenterY, fS, dynamicText, 0f);

            String keyName = KeyStorage.getBind(m.getBind());
            float kW = getMediumFont().getWidth(keyName, fS);
            float kRectW = (kW + scaled(4));
            float kRectH = (fS + scaled(2)) * anim;
            float kRectX = renderX + maxW - p - kRectW;
            float kRectY = currentY + (rowH / 2f) - (kRectH / 2f);

            if (anim > 0.5f) {
                RenderUtil.RECT.drawOutline(ms, kRectX, kRectY, kRectW, kRectH, 2f, 0.5f, dynamicOutline, dynamicKeyBg);
                getMediumFont().drawText(ms, keyName, kRectX + (kRectW / 2f) - (kW / 2f), textCenterY, fS, dynamicText, 0f);
            }

            currentY += rowH + 1f;
        }

        getDraggable().setWidth(maxW);
        getDraggable().setHeight(currentY - y);
    }
}
