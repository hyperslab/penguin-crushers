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
        WorldPoint escapeTile = plugin.getSouthRowEscapeTile();  // will return null if configured off (or not needed)

        String startTileText;
        String exitPlatformText;
        String escapeTileText;
        Color startTileTextColor;
        Color exitPlatformTextColor;
        Color escapeTileTextColor;

        Color crusherTileColor;
        Color exitPlatformTileColor;
        Color startTileColor;
        Color dangerTileColor;
        Color safeTileColor;
        Color escapeTileColor;

        Color timerLeftColor;
        Color timerRightColor;
        Color timerBorderColor;

        switch (crossingStatus)
        {
            case UNSAFE_TO_CROSS:
                startTileText = "Start here!";
                exitPlatformText = "DON'T move!";
                escapeTileText = "DON'T move!";
                startTileTextColor = config.startTileTextColor();
                exitPlatformTextColor = config.endTileTextDangerColor();
                escapeTileTextColor = config.southRowEscapeTileTextDangerColor();

                crusherTileColor = config.crusherTilesDangerColor();
                exitPlatformTileColor = config.endTileDangerColor();
                startTileColor = config.startTileColor();
                dangerTileColor = config.dangerTilesDangerColor();
                safeTileColor = config.safeTilesColor();
                escapeTileColor = config.southRowEscapeTileDangerColor();

                timerLeftColor = config.timerFullColor();
                timerRightColor = config.timerFullColor();
                timerBorderColor = config.timerBorderColor();
                break;
            case SAFE_TO_CROSS:
                startTileText = "Start here!";
                exitPlatformText = "Move now!";
                escapeTileText = "Move here!";
                startTileTextColor = config.startTileTextColor();
                exitPlatformTextColor = config.endTileTextSafeColor();
                escapeTileTextColor = config.southRowEscapeTileTextSafeColor();

                crusherTileColor = config.crusherTilesSafeColor();
                exitPlatformTileColor = config.endTileSafeColor();
                startTileColor = config.startTileColor();
                dangerTileColor = config.dangerTilesSafeColor();
                safeTileColor = config.safeTilesColor();
                escapeTileColor = config.southRowEscapeTileSafeColor();

                timerLeftColor = config.timerLeftColor();
                timerRightColor = config.timerRightColor();
                timerBorderColor = config.timerBorderColor();
                break;
            case CROSSING_SAFELY:
                startTileText = "Start here!";
                exitPlatformText = "Clear!";
                escapeTileText = "Escape clear!";
                startTileTextColor = config.startTileTextColor();
                exitPlatformTextColor = config.endTileTextCorrectColor();
                escapeTileTextColor = config.southRowEscapeTileTextSafeColor();

                crusherTileColor = config.crusherTilesCorrectColor();
                exitPlatformTileColor = config.endTileCorrectColor();
                startTileColor = config.startTileColor();
                dangerTileColor = config.dangerTilesCorrectColor();
                safeTileColor = config.safeTilesCorrectColor();
                escapeTileColor = config.southRowEscapeTileSafeColor();

                timerLeftColor = config.timerLeftColor();
                timerRightColor = config.timerRightColor();
                timerBorderColor = config.timerBorderColor();
                break;
            case CROSSING_UNSAFELY:
                startTileText = "Start here!";
                exitPlatformText = "Danger! Get to safety!";
                escapeTileText = "Move here, quick!";
                startTileTextColor = config.startTileTextColor();
                exitPlatformTextColor = config.endTileTextIncorrectColor();
                escapeTileTextColor = config.southRowEscapeTileTextSafeColor();

                crusherTileColor = config.crusherTilesIncorrectColor();
                exitPlatformTileColor = config.endTileIncorrectColor();
                startTileColor = config.startTileColor();
                dangerTileColor = config.dangerTilesIncorrectColor();
                safeTileColor = config.safeTilesIncorrectColor();
                escapeTileColor = config.southRowEscapeTileSafeColor();

                timerLeftColor = config.timerFullColor();
                timerRightColor = config.timerFullColor();
                timerBorderColor = config.timerBorderColor();
                break;
            default:  // same as UNSAFE_TO_CROSS (but should never be hit)
                startTileText = "Start here!";
                exitPlatformText = "DON'T move!";
                escapeTileText = "DON'T move!";
                startTileTextColor = config.startTileTextColor();
                exitPlatformTextColor = config.endTileTextDangerColor();
                escapeTileTextColor = config.southRowEscapeTileTextDangerColor();

                crusherTileColor = config.crusherTilesDangerColor();
                exitPlatformTileColor = config.endTileDangerColor();
                startTileColor = config.startTileColor();
                dangerTileColor = config.dangerTilesDangerColor();
                safeTileColor = config.safeTilesColor();
                escapeTileColor = config.southRowEscapeTileDangerColor();

                timerLeftColor = config.timerFullColor();
                timerRightColor = config.timerFullColor();
                timerBorderColor = config.timerBorderColor();
                break;
        }

        startTileColor = ColorUtil.colorWithAlpha(startTileColor, startTileColor.getAlpha() / 4);
        dangerTileColor = ColorUtil.colorWithAlpha(dangerTileColor, dangerTileColor.getAlpha() / 5);
        safeTileColor = ColorUtil.colorWithAlpha(safeTileColor, safeTileColor.getAlpha() / 5);

        Stroke smallBorder = new BasicStroke(0.5f);
        Stroke normalBorder = new BasicStroke(1.2f);

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

        if (config.highlightEndTile() && escapeTile == null)
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

        if (config.showTimer() && (crossingStatus == CrossingStatus.SAFE_TO_CROSS || crossingStatus == CrossingStatus.UNSAFE_TO_CROSS))
        {
            LocalPoint endTileLocal = LocalPoint.fromWorld(client, PenguinCrushersPlugin.END_TILE_LOCATION);
            if (endTileLocal != null)
            {
                Point timerLocation = Perspective.getCanvasTextLocation(client, graphics, endTileLocal, "", 160);

                // assume a tick is always 600ms (in reality it varies a little but not really a way to anticipate it)
                double percentFull = Math.min(System.currentTimeMillis() - plugin.getLastSafeTimeStart(), 600) / 600f;

                if (timerLocation != null)
                {
                    timerLocation = new Point(timerLocation.getX() - 40, timerLocation.getY() + 8);

                    Shape timerBounds = new Rectangle(timerLocation.getX(), timerLocation.getY(), 80, 8);
                    Shape timerBar = new Rectangle(timerLocation.getX(), timerLocation.getY(), (int) Math.round(80f * percentFull), 8);

                    OverlayUtil.renderPolygon(graphics, timerBounds, timerBorderColor, timerRightColor, normalBorder);
                    OverlayUtil.renderPolygon(graphics, timerBar, timerBorderColor, timerLeftColor, smallBorder);
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

        if (escapeTile != null)
        {
            LocalPoint escapeTileLocal = LocalPoint.fromWorld(client, escapeTile);
            if (escapeTileLocal != null)
            {
                Polygon tilePoly = Perspective.getCanvasTilePoly(client, escapeTileLocal);
                if (tilePoly != null)
                {
                    OverlayUtil.renderPolygon(graphics, tilePoly, escapeTileColor);
                }

                if (config.showText())
                {
                    Point textLocation = Perspective.getCanvasTextLocation(client, graphics, escapeTileLocal, escapeTileText, 160);
                    if (textLocation != null)
                    {
                        OverlayUtil.renderTextLocation(graphics, textLocation, escapeTileText, escapeTileTextColor);
                    }
                }
            }
        }

        return null;
    }
}
