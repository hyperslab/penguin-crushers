package com.penguincrushers;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("penguincrushers")
public interface PenguinCrushersConfig extends Config
{
	@ConfigItem(
		keyName = "showText",
		name = "Show text",
		description = "Show explanatory text above the start and end tiles (if highlighted).",
		position = 0
	)
	default boolean showText()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightCrusherTiles",
		name = "Highlight crusher tiles",
		description = "Highlight the tiles of the crushers.",
		position = 1
	)
	default boolean highlightCrusherTiles()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightStartTile",
		name = "Highlight start tile",
		description = "Highlight the tile the player appears at when entering the crusher obstacle.",
		position = 2
	)
	default boolean highlightStartTile()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightEndTile",
		name = "Highlight end tile",
		description = "Highlight the tile of the stepping stone used to proceed from the crusher obstacle.",
		position = 3
	)
	default boolean highlightEndTile()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightDangerTiles",
		name = "Highlight danger tiles",
		description = "Highlight the tiles where the crushers can deal damage.",
		position = 4
	)
	default boolean highlightDangerTiles()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightSafeTiles",
		name = "Highlight safe tiles",
		description = "Highlight the tiles in the middle of the obstacle where the crushers cannot deal damage.",
		position = 5
	)
	default boolean highlightSafeTiles()
	{
		return true;
	}

	@ConfigItem(
		keyName = "startTileTextColor",
		name = "Start tile text color",
		description = "Configures the color of the text above the start tile.",
		position = 6
	)
	default Color startTileTextColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
		keyName = "endTileTextSafeColor",
		name = "End tile text safe color",
		description = "Configures the color of the text above the end tile when the crushers are not moving.",
		position = 7
	)
	default Color endTileTextSafeColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
		keyName = "endTileTextDangerColor",
		name = "End tile text danger color",
		description = "Configures the color of the text above the end tile when the crushers are moving.",
		position = 8
	)
	default Color endTileTextDangerColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		keyName = "crusherTilesSafeColor",
		name = "Crusher tiles safe color",
		description = "Configures the color of the crusher tile highlights when the crushers are not moving.",
		position = 9
	)
	default Color crusherTilesSafeColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
		keyName = "crusherTilesDangerColor",
		name = "Crusher tiles danger color",
		description = "Configures the color of the crusher tile highlights when the crushers are moving.",
		position = 10
	)
	default Color crusherTilesDangerColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		keyName = "startTileColor",
		name = "Start tile color",
		description = "Configures the color of the start tile highlight.",
		position = 11
	)
	default Color startTileColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
		keyName = "endTileSafeColor",
		name = "End tile safe color",
		description = "Configures the color of the end tile highlight when the crushers are not moving.",
		position = 12
	)
	default Color endTileSafeColor()
	{
		return Color.BLUE;
	}

	@ConfigItem(
		keyName = "endTileDangerColor",
		name = "End tile danger color",
		description = "Configures the color of the end tile highlight when the crushers are moving.",
		position = 13
	)
	default Color endTileDangerColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		keyName = "dangerTilesSafeColor",
		name = "Danger tiles safe color",
		description = "Configures the color of the danger tile highlights when the crushers are not moving.",
		position = 14
	)
	default Color dangerTilesSafeColor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = "dangerTilesDangerColor",
		name = "Danger tiles danger color",
		description = "Configures the color of the danger tile highlights when the crushers are moving.",
		position = 15
	)
	default Color dangerTilesDangerColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		keyName = "safeTilesColor",
		name = "Safe tiles color",
		description = "Configures the color of the safe tile highlights.",
		position = 16
	)
	default Color safeTilesColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
			keyName = "playSoundOnCorrectCrossing",
			name = "Play sound on correct crossing",
			description = "Play a sound effect when a successful crossing begins.",
			position = 17
	)
	default boolean playSoundOnCorrectCrossing()
	{
		return true;
	}

	@ConfigItem(
			keyName = "changeColorsOnCorrectCrossing",
			name = "Change colors on correct crossing",
			description = "Change text and tile colors when a successful crossing begins.",
			position = 18
	)
	default boolean changeColorsOnCorrectCrossing()
	{
		return true;
	}

	@ConfigItem(
			keyName = "endTileTextCorrectColor",
			name = "End tile text correct color",
			description = "Configures the color of the text above the end tile when crossing correctly.",
			position = 19
	)
	default Color endTileTextCorrectColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			keyName = "crusherTilesCorrectColor",
			name = "Crusher tiles correct color",
			description = "Configures the color of the crusher tile highlights when crossing correctly.",
			position = 20
	)
	default Color crusherTilesCorrectColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			keyName = "endTileCorrectColor",
			name = "End tile correct color",
			description = "Configures the color of the end tile highlight when crossing correctly.",
			position = 21
	)
	default Color endTileCorrectColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			keyName = "dangerTilesCorrectColor",
			name = "Danger tiles correct color",
			description = "Configures the color of the danger tile highlights when crossing correctly.",
			position = 22
	)
	default Color dangerTilesCorrectColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			keyName = "safeTilesCorrectColor",
			name = "Safe tiles correct color",
			description = "Configures the color of the safe tile highlights when crossing correctly.",
			position = 22
	)
	default Color safeTilesCorrectColor()
	{
		return Color.CYAN;
	}
}
