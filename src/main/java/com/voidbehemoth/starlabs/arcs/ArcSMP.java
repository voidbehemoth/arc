package com.voidbehemoth.starlabs.arcs;

import com.voidbehemoth.starlabs.arcs.command.ArcCommand;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArcSMP implements ModInitializer {


	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("arc-smp");
	public static final String MOD_ID = "arcs";

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(ArcCommand::register);

	}
}