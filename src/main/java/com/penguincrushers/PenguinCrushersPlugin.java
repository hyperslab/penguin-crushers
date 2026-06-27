package com.penguincrushers;

import com.google.inject.Provides;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

	// 4 different types of crushers; crushers of the same time move at the same time
	private static final int CRUSHER_SOUTH_SIDES_NPC_ID = NpcID.PENG_AGILITY_CRUSHCOURSE_CRUSHBLOCK01_NPC;
	private static final int CRUSHER_NORTH_WEST_NPC_ID = NpcID.PENG_AGILITY_CRUSHCOURSE_CRUSHBLOCK02_NPC;
	private static final int CRUSHER_SOUTH_CENTER_NPC_ID = NpcID.PENG_AGILITY_CRUSHCOURSE_CRUSHBLOCK03_NPC;
	private static final int CRUSHER_NORTH_EAST_NPC_ID = NpcID.PENG_AGILITY_CRUSHCOURSE_CRUSHBLOCK04_NPC;

	// stepping stone that proceeds to the next obstacle; there are 2 matching the id so we also filter by coordinates
	private static final int CRUSHER_EXIT_PLATFORM_OBJECT_ID = ObjectID.PENG_AGILITY_CRUSHCOURSE_STEPSTONE01;
	private static final WorldPoint CRUSHER_EXIT_PLATFORM_LOCATION = new WorldPoint(2630, 4057, 0);

	@Getter
	private final Set<NPC> southSideCrushers = new HashSet<>();

	@Getter
	private final Set<NPC> northWestCrushers = new HashSet<>();

	@Getter
	private final Set<NPC> southCenterCrusher = new HashSet<>();

	@Getter
	private final Set<NPC> northEastCrusher = new HashSet<>();

	public Set<NPC> getCrushers()
	{
		return Stream.of(southSideCrushers, northWestCrushers, southCenterCrusher, northEastCrusher).flatMap(Set::stream).collect(Collectors.toSet());
	}

	@Getter
	private final Set<TileObject> crusherExitPlatform = new HashSet<>();

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
		log.debug("Penguin crushers started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		clearSets();
		log.debug("Penguin crushers stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		switch (gameStateChanged.getGameState())
		{
			case HOPPING:
			case LOGIN_SCREEN:
				clearSets();
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (isInCrusherZone())
		{
			log.debug("Welcome to the crusher zone");
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();
		switch (npc.getId())
		{
			case CRUSHER_SOUTH_SIDES_NPC_ID:
				southSideCrushers.add(npc);
				break;
			case CRUSHER_NORTH_WEST_NPC_ID:
				northWestCrushers.add(npc);
				break;
			case CRUSHER_SOUTH_CENTER_NPC_ID:
				southCenterCrusher.add(npc);
				break;
			case CRUSHER_NORTH_EAST_NPC_ID:
				northEastCrusher.add(npc);
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

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		TileObject tileObject = gameObjectSpawned.getGameObject();
		if (tileObject.getId() == CRUSHER_EXIT_PLATFORM_OBJECT_ID && tileObject.getWorldLocation().equals(CRUSHER_EXIT_PLATFORM_LOCATION))
		{
			crusherExitPlatform.add(tileObject);
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		TileObject tileObject = gameObjectDespawned.getGameObject();
		crusherExitPlatform.remove(tileObject);
	}

	private boolean isInCrusherZone()
	{
		Player local = client.getLocalPlayer();
		if (local == null)
		{
			return false;
		}

		WorldPoint location = local.getWorldLocation();

		return location.isInArea(CRUSHER_ZONE);
	}

	private void clearSets()
	{
		southSideCrushers.clear();
		northWestCrushers.clear();
		southCenterCrusher.clear();
		northEastCrusher.clear();
		crusherExitPlatform.clear();
	}
}
