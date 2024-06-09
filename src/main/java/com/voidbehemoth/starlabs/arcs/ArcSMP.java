package com.voidbehemoth.starlabs.arcs;

import com.voidbehemoth.starlabs.arcs.command.ArcCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArcSMP implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("arc-smp");
	public static final String MOD_ID = "arcs";
	public static MinecraftServer SERVER;

//	@Override
//	public void onInitialize() {
//		CommandRegistrationCallback.EVENT.register(ArcCommand::register);
//
//	}

	@Override
	public void onInitializeServer() {
		CommandRegistrationCallback.EVENT.register(ArcCommand::register);
	}
}