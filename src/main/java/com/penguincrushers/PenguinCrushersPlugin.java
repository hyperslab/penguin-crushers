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
import net.runelite.api.coords.LocalPoint;
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

	// not safe to cross until we've seen the crushers move at least once
	private boolean haveCrushersEverMoved = false;

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

	private WorldPoint lastPlayerLocation;
	private WorldPoint lastPlayerDestination;

	private boolean playerOnDangerTrack = false;
	private boolean playerOnSafeTrack = false;

	// maintain a single status variable for the overlay to use rather than have it call calculations directly
	// this prevents colors changing to safe/danger for one frame before changing to correct/incorrect
	@Getter
	private CrossingStatus currentCrossingStatus = CrossingStatus.UNSAFE_TO_CROSS;

	private void clearData()
	{
		southSideCrushers.clear();
		northWestCrushers.clear();
		southCenterCrusher.clear();
		northEastCrusher.clear();
		haveCrushersEverMoved = false;
		lastPlayerLocation = null;
		lastPlayerDestination = null;
		playerOnDangerTrack = false;
		playerOnSafeTrack = false;
		currentCrossingStatus = CrossingStatus.UNSAFE_TO_CROSS;
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
		haveCrushersEverMoved = false;
		lastPlayerLocation = null;
		lastPlayerDestination = null;
		playerOnDangerTrack = false;
		playerOnSafeTrack = false;
		currentCrossingStatus = CrossingStatus.UNSAFE_TO_CROSS;
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
			haveCrushersEverMoved = false;
			lastPlayerLocation = null;
			lastPlayerDestination = null;
			playerOnSafeTrack = false;
			currentCrossingStatus = CrossingStatus.UNSAFE_TO_CROSS;
			return;
		}

		// set up local variables
		WorldPoint location = client.getLocalPlayer().getWorldLocation();
		LocalPoint localDestination = client.getLocalDestinationLocation();
		WorldPoint destination = localDestination != null ? WorldPoint.fromLocal(client, localDestination) : null;

		// check for crusher movement
		if (didCrushersJustMove())
		{
			haveCrushersEverMoved = true;
		}

		// calculate if player is currently moving dangerously (i.e. did not time movement correctly)
		if (!playerOnDangerTrack)
		{
			if (didPlayerStartCrossingDangerously())
			{
				playerOnDangerTrack = true;
				if (config.playSoundOnIncorrectCrossing())
				{
					client.playSoundEffect(config.incorrectCrossingSoundEffect().getSoundEffectId());
				}
			}
		}
		else
		{
			if (destination == null)  // player is not queued to move
			{
				playerOnDangerTrack = didPlayerStartCrossingDangerously();
			}
			else  // player is queued to move
			{
				// recalculate if destination tile changed
				if (destination.equals(location) || !destination.equals(lastPlayerDestination))
				{
					playerOnDangerTrack = didPlayerStartCrossingDangerously();
				}
			}
		}

		// calculate if player is currently moving safely
		if (!playerOnSafeTrack)
		{
			if (didPlayerStartCrossingSafely())
			{
				playerOnSafeTrack = true;
				playerOnDangerTrack = false;  // becoming safe always overrides danger
				if (config.playSoundOnCorrectCrossing())
				{
					client.playSoundEffect(config.correctCrossingSoundEffect().getSoundEffectId());
				}
			}
		}
		else
		{
			if (destination == null)  // player is not queued to move
			{
				// wait for one tick of no movement before recalculating when next to the exit to allow a tick to climb
				if (!didPlayerJustMove() || !location.equals(END_TILE_LOCATION.dx(-1)))
				{
					playerOnSafeTrack = didPlayerStartCrossingSafely();
				}
			}
			else  // player is queued to move
			{
				// recalculate if destination tile changed
				if (destination.equals(location) || !destination.equals(lastPlayerDestination))
				{
					playerOnSafeTrack = didPlayerStartCrossingSafely();
				}
			}
		}

		// re-set the current crossing status once per tick
		// I don't love tying this to color config but in practice it is currently the only thing status affects
		// maybe splitting enum into like CROSSING_SAFELY_SAFE can move this logic to the overlay where it belongs
		if (playerOnDangerTrack && config.changeColorsOnIncorrectCrossing())
		{
			currentCrossingStatus = CrossingStatus.CROSSING_UNSAFELY;
		}
		else if (playerOnSafeTrack && config.changeColorsOnCorrectCrossing())
		{
			currentCrossingStatus = CrossingStatus.CROSSING_SAFELY;
		}
		else if (isSafeToCross())
		{
			currentCrossingStatus = CrossingStatus.SAFE_TO_CROSS;
		}
		else
		{
			currentCrossingStatus = CrossingStatus.UNSAFE_TO_CROSS;
		}

		// update previous locations/destinations to those of the current tick
		// must be last thing that happens in onGameTick, any new logic must come before this
		for (NPC crusher : getCrushers().keySet())
		{
			updateCrusherLastLocation(crusher, crusher.getWorldLocation());
		}
		lastPlayerLocation = client.getLocalPlayer().getWorldLocation();
		if (client.getLocalDestinationLocation() != null)
		{
			lastPlayerDestination = WorldPoint.fromLocal(client, client.getLocalDestinationLocation());
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

	private boolean isSafeToCross()
	{
		return haveCrushersEverMoved && !didCrushersJustMove();
	}

	private boolean didPlayerJustMove()
	{
		return lastPlayerLocation != null && !lastPlayerLocation.equals(client.getLocalPlayer().getWorldLocation());
	}

	private boolean didPlayerStartCrossingDangerously()
	{
		if (client.getLocalPlayer() == null)
		{
			return false;
		}

		WorldPoint location = client.getLocalPlayer().getWorldLocation();

		// if player isn't moving anymore, it counts as dangerous if they stopped on a danger tile
		if (client.getLocalDestinationLocation() == null)
		{
			return didPlayerJustMove()
					&& location.isInArea(CRUSHER_ZONE)
					&& DANGER_TILE_LOCATIONS.contains(location);
		}

		WorldPoint destination = WorldPoint.fromLocal(client, client.getLocalDestinationLocation());

		return DANGER_TILE_LOCATIONS.contains(destination)  // short circuit if destination is dangerous
				|| didPlayerJustMove()
				&& location.isInArea(CRUSHER_ZONE)
				&& ((DANGER_TILE_LOCATIONS.contains(location) && isSafeToCross())
					|| (SAFE_TILE_LOCATIONS.contains(location) && !isSafeToCross()));
	}

	private boolean didPlayerStartCrossingSafely()
	{
		if (client.getLocalDestinationLocation() == null || client.getLocalPlayer() == null)
		{
			return false;
		}

		WorldPoint destination = WorldPoint.fromLocal(client, client.getLocalDestinationLocation());
		WorldPoint location = client.getLocalPlayer().getWorldLocation();

		// special case: south row escape in progress
		WorldPoint escapeTile = getSouthRowEscapeTile();
		if (escapeTile != null)
		{
			return destination.equals(escapeTile);  // this will only ever be true if blocked by crushers but that's ok
		}

		return location.isInArea(CRUSHER_ZONE)
				&& !DANGER_TILE_LOCATIONS.contains(destination)
				&& ((didPlayerJustMove()  // player moving through the crushers with correct timing
						&& ((DANGER_TILE_LOCATIONS.contains(location) && !isSafeToCross())
							|| (SAFE_TILE_LOCATIONS.contains(location) && isSafeToCross())))
					|| (!didPlayerJustMove()  // player blocked by a crusher, which will result in correct timing
						&& destination.getX() < START_TILE_LOCATION.getX()  // don't trigger when you first come down
						&& lastPlayerLocation != null  // or enter from the north if you're messing around
						&& !DANGER_TILE_LOCATIONS.contains(location))
					|| (didPlayerJustMove()  // player moved onto safe territory (and is implicitly still moving)
						&& !DANGER_TILE_LOCATIONS.contains(location)
						&& !SAFE_TILE_LOCATIONS.contains(location)
						&& (DANGER_TILE_LOCATIONS.contains(lastPlayerLocation)
							|| SAFE_TILE_LOCATIONS.contains(lastPlayerLocation))));
	}

	public WorldPoint getSouthRowEscapeTile()
	{
		if (!config.southRowEscapeAssist() || client.getLocalPlayer() == null)
		{
			return null;  // null return = no need to escape
		}

		WorldPoint location = client.getLocalPlayer().getWorldLocation();

		if (!location.isInArea(CRUSHER_ZONE) || location.getY() != CRUSHER_ZONE.getY())
		{
			return null;  // null return = no need to escape
		}

		// prefer northwest, straight north if on a danger tile, northeast if at the edge
		WorldPoint escapeTile = location.dy(1).dx(-1);
		if (DANGER_TILE_LOCATIONS.contains(location))
		{
			escapeTile = location.dy(1);
		}
		else if (!escapeTile.isInArea(CRUSHER_ZONE))
		{
			escapeTile = location.dy(1).dx(1);
		}
		if (!escapeTile.isInArea(CRUSHER_ZONE) || DANGER_TILE_LOCATIONS.contains(escapeTile))
		{
			escapeTile = null;  // shouldn't ever get here
		}
		return escapeTile;
	}
}
