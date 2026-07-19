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

        CrossingStatus crossingStatus = plugin.getCurrentCrossingStatus();

        String startTileText;
        String exitPlatformText;
        Color startTileTextColor;
        Color exitPlatformTextColor;

        Color crusherTileColor;
        Color exitPlatformTileColor;
        Color startTileColor;
        Color dangerTileColor;
        Color safeTileColor;

        switch (crossingStatus)
        {
            case UNSAFE_TO_CROSS:
                startTileText = "Start here!";
                exitPlatformText = "DON'T move!";
                startTileTextColor = config.startTileTextColor();
                exitPlatformTextColor = config.endTileTextDangerColor();

                crusherTileColor = config.crusherTilesDangerColor();
                exitPlatformTileColor = config.endTileDangerColor();
                startTileColor = config.startTileColor();
                dangerTileColor = config.dangerTilesDangerColor();
                safeTileColor = config.safeTilesColor();
                break;
            case SAFE_TO_CROSS:
                startTileText = "Start here!";
                exitPlatformText = "Move now!";
                startTileTextColor = config.startTileTextColor();
                exitPlatformTextColor = config.endTileTextSafeColor();

                crusherTileColor = config.crusherTilesSafeColor();
                exitPlatformTileColor = config.endTileSafeColor();
                startTileColor = config.startTileColor();
                dangerTileColor = config.dangerTilesSafeColor();
                safeTileColor = config.safeTilesColor();
                break;
            case CROSSING_SAFELY:
                startTileText = "Start here!";
                exitPlatformText = "Success!";
                startTileTextColor = config.startTileTextColor();
                exitPlatformTextColor = config.endTileTextCorrectColor();

                crusherTileColor = config.crusherTilesCorrectColor();
                exitPlatformTileColor = config.endTileCorrectColor();
                startTileColor = config.startTileColor();
                dangerTileColor = config.dangerTilesCorrectColor();
                safeTileColor = config.safeTilesCorrectColor();
                break;
            case CROSSING_UNSAFELY:  // TODO separate configurable colors
                startTileText = "Start here!";
                exitPlatformText = "Wrong! Danger!";
                startTileTextColor = config.startTileTextColor();
                exitPlatformTextColor = config.endTileTextDangerColor();

                crusherTileColor = config.crusherTilesDangerColor();
                exitPlatformTileColor = config.endTileDangerColor();
                startTileColor = config.startTileColor();
                dangerTileColor = config.dangerTilesDangerColor();
                safeTileColor = config.safeTilesColor();
                break;
            default:  // same as UNSAFE_TO_CROSS
                startTileText = "Start here!";
                exitPlatformText = "DON'T move!";
                startTileTextColor = config.startTileTextColor();
                exitPlatformTextColor = config.endTileTextDangerColor();

                crusherTileColor = config.crusherTilesDangerColor();
                exitPlatformTileColor = config.endTileDangerColor();
                startTileColor = config.startTileColor();
                dangerTileColor = config.dangerTilesDangerColor();
                safeTileColor = config.safeTilesColor();
                break;
        }

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
