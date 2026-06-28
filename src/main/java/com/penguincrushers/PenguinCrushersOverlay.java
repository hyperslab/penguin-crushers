package com.penguincrushers;

import javax.inject.Inject;
import java.awt.*;
import java.util.Map;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
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
        // don't draw anything if player is outside the crusher zone
        if (!plugin.isInCrusherZone())
        {
            return null;
        }

        boolean safe = plugin.isSafeToCross();

        String exitPlatformText = safe ? "Move now!" : "DON'T move!";
        String startTileText = "Start here!";

        Color startTileTextColor = config.startTileTextColor();
        Color exitPlatformTextColor = safe ? config.endTileTextSafeColor() : config.endTileTextDangerColor();
        Color crusherTileColor = safe ? config.crusherTilesSafeColor() : config.crusherTilesDangerColor();
        Color exitPlatformTileColor = safe ? config.endTileSafeColor() : config.endTileDangerColor();
        Color startTileColor = config.startTileColor();
        Color dangerTileColor = safe ? config.dangerTilesSafeColor() : config.dangerTilesDangerColor();
        Color safeTileColor = config.safeTilesColor();

        startTileColor = ColorUtil.colorWithAlpha(startTileColor, startTileColor.getAlpha() / 4);
        dangerTileColor = ColorUtil.colorWithAlpha(dangerTileColor, dangerTileColor.getAlpha() / 5);
        safeTileColor = ColorUtil.colorWithAlpha(safeTileColor, safeTileColor.getAlpha() / 5);

        Stroke smallBorder = new BasicStroke(0.5f);

        if (config.highlightCrusherTiles())
        {
            Map<NPC, WorldPoint> crushers = plugin.getCrushers();
            for (NPC crusher : crushers.keySet())
            {
                Polygon tilePoly = crusher.getCanvasTilePoly();
                if (tilePoly != null)
                {
                    OverlayUtil.renderPolygon(graphics, tilePoly, crusherTileColor);
                }
            }
        }

        if (config.highlightEndTile())
        {
            LocalPoint endTileLocal = LocalPoint.fromWorld(client, PenguinCrushersPlugin.END_TILE_LOCATION);
            if (endTileLocal != null)
            {
                Polygon tilePoly = Perspective.getCanvasTilePoly(client, endTileLocal);
                if (tilePoly != null)
                {
                    OverlayUtil.renderPolygon(graphics, tilePoly, exitPlatformTileColor);
                }

                if (config.showText())
                {
                    Point textLocation = Perspective.getCanvasTextLocation(client, graphics, endTileLocal, exitPlatformText, 160);
                    if (textLocation != null)
                    {
                        OverlayUtil.renderTextLocation(graphics, textLocation, exitPlatformText, exitPlatformTextColor);
                    }
                }
            }
        }

        if (config.highlightStartTile())
        {
            LocalPoint startTileLocal = LocalPoint.fromWorld(client, PenguinCrushersPlugin.START_TILE_LOCATION);
            if (startTileLocal != null)
            {
                Polygon tilePoly = Perspective.getCanvasTilePoly(client, startTileLocal);
                if (tilePoly != null)
                {
                    OverlayUtil.renderPolygon(graphics, tilePoly, startTileColor, startTileColor, smallBorder);
                }

                if (config.showText())
                {
                    Point textLocation = Perspective.getCanvasTextLocation(client, graphics, startTileLocal, startTileText, 160);
                    if (textLocation != null)
                    {
                        OverlayUtil.renderTextLocation(graphics, textLocation, startTileText, startTileTextColor);
                    }
                }
            }
        }

        if (config.highlightDangerTiles())
        {
            for (WorldPoint dangerTile : PenguinCrushersPlugin.DANGER_TILE_LOCATIONS)
            {
                LocalPoint dangerTileLocal = LocalPoint.fromWorld(client, dangerTile);
                if (dangerTileLocal != null)
                {
                    Polygon tilePoly = Perspective.getCanvasTilePoly(client, dangerTileLocal);
                    if (tilePoly != null)
                    {
                        OverlayUtil.renderPolygon(graphics, tilePoly, dangerTileColor, dangerTileColor, smallBorder);
                    }
                }
            }
        }

        if (config.highlightSafeTiles())
        {
            for (WorldPoint safeTile : PenguinCrushersPlugin.SAFE_TILE_LOCATIONS)
            {
                LocalPoint safeTileLocal = LocalPoint.fromWorld(client, safeTile);
                if (safeTileLocal != null)
                {
                    Polygon tilePoly = Perspective.getCanvasTilePoly(client, safeTileLocal);
                    if (tilePoly != null)
                    {
                        OverlayUtil.renderPolygon(graphics, tilePoly, safeTileColor, safeTileColor, smallBorder);
                    }
                }
            }
        }

        return null;
    }
}
