package com.penguincrushers;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PenguinCrushersPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PenguinCrushersPlugin.class);
		RuneLite.main(args);
	}
}