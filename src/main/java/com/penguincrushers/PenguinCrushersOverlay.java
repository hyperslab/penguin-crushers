package com.penguincrushers;

import javax.inject.Inject;
import java.awt.*;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ColorUtil;

public class PenguinCrushersOverlay extends Overlay
{
    private final Client client;
    private final PenguinCrushersConfig config;
    private final PenguinCrushersPlugin plugin;

    @Inject
    private PenguinCrushersOverlay(Client client, PenguinCrushersPlugin plugin, PenguinCrushersConfig config)
    {
        super(plugin);
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Set<NPC> crushers = plugin.getCrushers();
        Color crusherColor = Color.CYAN;
        for (NPC crusher : crushers)
        {
            Polygon tilePoly = crusher.getCanvasTilePoly();
            if (tilePoly != null)
            {
                OverlayUtil.renderPolygon(graphics, tilePoly, crusherColor);
            }
        }

        Set<TileObject> exitPlatforms = plugin.getCrusherExitPlatform();
        Color exitPlatformColor = Color.BLUE;
        for (TileObject exitPlatform : exitPlatforms)
        {
            Shape clickbox = exitPlatform.getClickbox();
            if (clickbox != null)
            {
                graphics.draw(clickbox);
                graphics.setColor(ColorUtil.colorWithAlpha(exitPlatformColor, exitPlatformColor.getAlpha() / 5));
                graphics.fill(clickbox);
            }
        }

        return null;
    }
}
