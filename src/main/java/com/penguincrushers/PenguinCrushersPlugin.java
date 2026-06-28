package com.penguincrushers;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Penguin Crushers"
)
public class PenguinCrushersPlugin extends Plugin
{
	// area where the crusher obstacle takes place
	private static final WorldArea CRUSHER_ZONE = new WorldArea(2628, 4054, 8, 4, 0);;

	// 4 different types of crushers; crushers of the same type move at the same time
	private static final int CRUSHER_SOUTH_SIDES_NPC_ID = NpcID.PENG_AGILITY_CRUSHCOURSE_CRUSHBLOCK01_NPC;
	private static final int CRUSHER_NORTH_WEST_NPC_ID = NpcID.PENG_AGILITY_CRUSHCOURSE_CRUSHBLOCK02_NPC;
	private static final int CRUSHER_SOUTH_CENTER_NPC_ID = NpcID.PENG_AGILITY_CRUSHCOURSE_CRUSHBLOCK03_NPC;
	private static final int CRUSHER_NORTH_EAST_NPC_ID = NpcID.PENG_AGILITY_CRUSHCOURSE_CRUSHBLOCK04_NPC;

	// where the player appears after entering the crusher zone
	public static final WorldPoint START_TILE_LOCATION = new WorldPoint(2635, 4055, 0);

	// tile of the stepping stone that leads to the next obstacle
	public static final WorldPoint END_TILE_LOCATION = new WorldPoint(2630, 4057, 0);

	// object ID of the stepping stone in case we ever need it
	// we used to use it, but it was less buggy to just go straight to the tile
	// there are 2 objects matching this ID so getting the usable one requires checking its location as well
	private static final int CRUSHER_EXIT_PLATFORM_OBJECT_ID = ObjectID.PENG_AGILITY_CRUSHCOURSE_STEPSTONE01;

	// tiles where the crushers can deal damage to the player
	public static final Set<WorldPoint> DANGER_TILE_LOCATIONS = ImmutableSet.of(
		new WorldPoint(2634, 4055, 0),
		new WorldPoint(2632, 4055, 0),
		new WorldPoint(2630, 4055, 0),
		new WorldPoint(2633, 4054, 0),
		new WorldPoint(2631, 4054, 0),
		new WorldPoint(2629, 4054, 0)
	);

	// tiles in between the danger tiles where the crushers cannot deal damage to the player
	public static final Set<WorldPoint> SAFE_TILE_LOCATIONS = ImmutableSet.of(
		new WorldPoint(2633, 4055, 0),
		new WorldPoint(2631, 4055, 0),
		new WorldPoint(2632, 4054, 0),
		new WorldPoint(2630, 4054, 0)
	);

	// wait one tick after movement to update the last crusher location values
	private boolean locationsRecentlyUpdated = false;

	// not safe to cross until we've seen the crushers move at least once
	private boolean locationsEverUpdated = false;

	// crusher maps are <crusher, last position>
	// in theory tracking the types of crushers separately lets us predict exact movement patterns...
	// but that might be too powerful even for this silly plugin, so we generally treat them as indistinct for now

	@Getter
	private final Map<NPC, WorldPoint> southSideCrushers = new HashMap<>();

	@Getter
	private final Map<NPC, WorldPoint> northWestCrushers = new HashMap<>();

	@Getter
	private final Map<NPC, WorldPoint> southCenterCrusher = new HashMap<>();

	@Getter
	private final Map<NPC, WorldPoint> northEastCrusher = new HashMap<>();

	public Map<NPC, WorldPoint> getSouthCrushers()
	{
		Map<NPC, WorldPoint> southCrushers = new HashMap<>();
		southCrushers.putAll(southSideCrushers);
		southCrushers.putAll(southCenterCrusher);
		return southCrushers;
	}

	public Map<NPC, WorldPoint> getNorthCrushers()
	{
		Map<NPC, WorldPoint> northCrushers = new HashMap<>();
		northCrushers.putAll(northWestCrushers);
		northCrushers.putAll(northEastCrusher);
		return northCrushers;
	}

	public Map<NPC, WorldPoint> getCrushers()
	{
		Map<NPC, WorldPoint> crushers = new HashMap<>();
		crushers.putAll(southSideCrushers);
		crushers.putAll(northWestCrushers);
		crushers.putAll(southCenterCrusher);
		crushers.putAll(northEastCrusher);
		return crushers;
	}

	private void updateCrusherLastLocation(NPC crusher, WorldPoint lastLocation)
	{
		southSideCrushers.replace(crusher, lastLocation);
		northWestCrushers.replace(crusher, lastLocation);
		southCenterCrusher.replace(crusher, lastLocation);
		northEastCrusher.replace(crusher, lastLocation);
	}

	private void clearData()
	{
		southSideCrushers.clear();
		northWestCrushers.clear();
		southCenterCrusher.clear();
		northEastCrusher.clear();
		locationsRecentlyUpdated = false;
		locationsEverUpdated = false;
	}

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PenguinCrushersOverlay overlay;

	@Inject
	private PenguinCrushersConfig config;

	@Provides
	PenguinCrushersConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PenguinCrushersConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		locationsRecentlyUpdated = false;
		locationsEverUpdated = false;
		log.debug("Penguin crushers started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		clearData();
		log.debug("Penguin crushers stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		switch (gameStateChanged.getGameState())
		{
			case HOPPING:
			case LOGIN_SCREEN:
				clearData();
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		// reset calculations if player is outside the crusher zone
		if (!isInCrusherZone())
		{
			locationsRecentlyUpdated = false;
			locationsEverUpdated = false;
			return;
		}

		if (!locationsRecentlyUpdated)
		{
			for (NPC crusher : getCrushers().keySet())
			{
				updateCrusherLastLocation(crusher, crusher.getWorldLocation());
			}
			locationsRecentlyUpdated = true;
		}

		if (didCrushersJustMove())
		{
			locationsRecentlyUpdated = false;
			locationsEverUpdated = true;
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		switch (npc.getId())
		{
			case CRUSHER_SOUTH_SIDES_NPC_ID:
				southSideCrushers.put(npc, npc.getWorldLocation());
				break;
			case CRUSHER_NORTH_WEST_NPC_ID:
				northWestCrushers.put(npc, npc.getWorldLocation());
				break;
			case CRUSHER_SOUTH_CENTER_NPC_ID:
				southCenterCrusher.put(npc, npc.getWorldLocation());
				break;
			case CRUSHER_NORTH_EAST_NPC_ID:
				northEastCrusher.put(npc, npc.getWorldLocation());
				break;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		NPC npc = npcDespawned.getNpc();
		southSideCrushers.remove(npc);
		northWestCrushers.remove(npc);
		southCenterCrusher.remove(npc);
		northEastCrusher.remove(npc);
	}

	public boolean isInCrusherZone()
	{
		Player local = client.getLocalPlayer();
		if (local == null)
		{
			return false;
		}

		WorldPoint location = local.getWorldLocation();

		return location.isInArea(CRUSHER_ZONE);
	}

	private boolean didCrushersJustMove()
	{
		for (Map.Entry<NPC, WorldPoint> crusherAndLastLocation : getCrushers().entrySet())
		{
			NPC crusher = crusherAndLastLocation.getKey();
			WorldPoint lastLocation = crusherAndLastLocation.getValue();
			if (!crusher.getWorldLocation().equals(lastLocation))
			{
				return true;
			}
		}

		return false;
	}

	public boolean isSafeToCross()
	{
		return locationsEverUpdated && !didCrushersJustMove();
	}
}
