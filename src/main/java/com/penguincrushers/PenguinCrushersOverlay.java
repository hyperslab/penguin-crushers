package com.penguincrushers;

import javax.inject.Inject;
import java.awt.*;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

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
        // don't draw anything if player is outside the crusher zone
        if (!plugin.isInCrusherZone())
        {
            return null;
        }

        boolean safe = plugin.isSafeToCross();

        Color crusherTileColor = safe ? Color.CYAN : Color.RED;
        Color exitPlatformTileColor = safe ? Color.BLUE : Color.RED;
        String exitPlatformText = safe ? "Move now!" : "DON'T move!";
        Color exitPlatformTextColor = safe ? Color.GREEN : Color.RED;

        Map<NPC, WorldPoint> crushers = plugin.getCrushers();
        for (NPC crusher : crushers.keySet())
        {
            Polygon tilePoly = crusher.getCanvasTilePoly();
            if (tilePoly != null)
            {
                OverlayUtil.renderPolygon(graphics, tilePoly, crusherTileColor);
            }
        }

        Set<TileObject> exitPlatforms = plugin.getCrusherExitPlatform();
        for (TileObject exitPlatform : exitPlatforms)
        {
            Polygon tilePoly = exitPlatform.getCanvasTilePoly();
            if (tilePoly != null)
            {
                OverlayUtil.renderPolygon(graphics, tilePoly, exitPlatformTileColor);
            }

            Point textLocation = exitPlatform.getCanvasTextLocation(graphics, exitPlatformText, 160);
            if (textLocation != null)
            {
                OverlayUtil.renderTextLocation(graphics, textLocation, exitPlatformText, exitPlatformTextColor);
            }
        }

        return null;
    }
}
