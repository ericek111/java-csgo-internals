package eu.lixko.csgointernals;

import java.io.IOException;
import java.lang.reflect.Field;

import com.github.jonatino.misc.MemoryBuffer;
import com.github.jonatino.process.Module;
import com.github.jonatino.process.Process;
import com.github.jonatino.process.Processes;

import eu.lixko.csgointernals.Client;
import eu.lixko.csgoshared.offsets.GameInterface;
import eu.lixko.csgoshared.offsets.Offsets;
import eu.lixko.csgoshared.util.MemoryUtils;
import eu.lixko.csgoshared.util.StringFormat;
import sun.misc.Unsafe;

public final class Engine {

	private static Process process = Processes.byId(MemoryUtils.getPID());
	private static Module clientModule, engineModule;
	public static final Unsafe unsafe;

	private static final int TARGET_TPS = 200;
	private long tps_sleep = (long) ((1f / TARGET_TPS) * 1000);
	private long last_tick = 0;

	public static MemoryBuffer entlistbuffer = new MemoryBuffer(Long.BYTES * 4 * 65);
	public static long tick = 0;
	public static int isInGame = 0;	
	
	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void init() throws InterruptedException, IOException {		
		String clientName = "client_client.so";
		String engineName = "engine_client.so";

		waitUntilFound("client module", () -> (clientModule = process.findModule(clientName)) != null);
		waitUntilFound("engine module", () -> (engineModule = process.findModule(engineName)) != null);
		System.out.println("client: " + StringFormat.hex(clientModule.start()) + " - " + StringFormat.hex(clientModule.end()));
		System.out.println("engine: " + StringFormat.hex(engineModule.start()) + " - " + StringFormat.hex(clientModule.end()));

		//GameInterface.DumpInterfaces();
		
		loadOffsets();
		
		
		
		System.out.println("Engine initialization complete! Starting client...");
		Client.theClient.startClient();
		
		Client.theClient.eventHandler.onEngineLoaded();
		
		/*
		 * Client.theClient.commandManager.executeCommand("exec autoexec.txt");
		 * Client.theClient.eventHandler.onEngineLoaded();
		 * Client.theClient.commandManager.executeCommand("recoilcross toggle");
		 * Client.theClient.commandManager.executeCommand("crosshairdot toggle");
		 * Client.theClient.commandManager.executeCommand("bunnyhop toggle");
		 * Client.theClient.commandManager.executeCommand("namehud toggle");
		 * Client.theClient.commandManager.executeCommand("spectators toggle");
		 * Client.theClient.commandManager.
		 * executeCommand("bind KP_DELETE boneesp toggle");
		 * Client.theClient.commandManager.executeCommand("bind Alt_L glow toggle");
		 * Client.theClient.commandManager.executeCommand("bind kp_end disablepp toggle"
		 * );
		 * Client.theClient.commandManager.executeCommand("bind END autojoinct toggle");
		 * Client.theClient.commandManager.
		 * executeCommand("bind HOME testmodule forceupdate");
		 */
		// Client.theClient.commandManager.executeCommand("bind END testmodule
		// toshowinc");

		while (Client.theClient.isRunning) {
			try {
				Client.theClient.eventHandler.onPreLoop();
			} catch (Exception ex) {
				ex.printStackTrace();
				Thread.sleep(100);
			}

			last_tick = System.nanoTime();
			// System.out.println(last_tick);
			isInGame = engineModule.readInt(Offsets.m_dwClientState + Offsets.m_bIsInGame);
			if (isInGame != 6) {
				Thread.sleep(1000);
				continue;
			}

			Offsets.m_dwLocalPlayer = clientModule.readLong(Offsets.m_dwLocalPlayerPointer);
			if (Offsets.m_dwLocalPlayer < 1) {
				Thread.sleep(1000);
				continue;
			}
			Offsets.m_dwPlayerResources = Engine.clientModule().readLong(Offsets.m_dwPlayerResourcesPointer);

			try {
				Client.theClient.eventHandler.onLoop();
			} catch (Exception ex) {
				ex.printStackTrace();
				Thread.sleep(100);
			}

			if (tick % 1000 == 0)
				Engine.clientModule.read(Offsets.m_dwEntityList, entlistbuffer);

			if (tps_sleep > 0)
				Thread.sleep(tps_sleep);

			double adjust = ((1f / TARGET_TPS) * 1e9) / (System.nanoTime() - last_tick);
			tps_sleep *= adjust;
			if (tps_sleep > ((1f / TARGET_TPS) * 1000))
				tps_sleep = (long) ((1f / TARGET_TPS) * 1000l);
			if (tps_sleep < 1)
				tps_sleep = 1;

			// System.out.println("Looping! " + Math.floor(adjust*1e5)/1e5 + " /
			// " + tps_sleep + " - " + (System.nanoTime() - last_tick) + " > " +
			// ((System.nanoTime() - last_tick) / 1e9));
			tick++;
		}

		Client.theClient.shutdownClient();
	}

	public void end() {
	}

	public static void loadOffsets() {
		Offsets.load();
		/*
		 * System.out.println(); System.out.println("m_dwGlowObject: " +
		 * StringFormat.hex(Offsets.m_dwGlowObject)); System.out.println("m_iAlt1: " +
		 * StringFormat.hex(Offsets.input.alt1)); System.out.println("m_iAlt2: " +
		 * StringFormat.hex(Offsets.input.alt2)); System.out.println("m_dwForceJump: " +
		 * StringFormat.hex(Offsets.input.jump));
		 * System.out.println("m_dw_bOverridePostProcessingDisable: " +
		 * StringFormat.hex(Offsets.m_dw_bOverridePostProcessingDisable));
		 * System.out.println("m_dwPlayerResources: " +
		 * StringFormat.hex(Offsets.m_dwPlayerResourcesPointer));
		 * System.out.println("m_dwForceAttack: " +
		 * StringFormat.hex(Offsets.input.attack));
		 * System.out.println("m_dwEntityList: " +
		 * StringFormat.hex(Offsets.m_dwEntityList));
		 * System.out.println("m_dwLocalPlayerPointer: " +
		 * StringFormat.hex(Offsets.m_dwLocalPlayerPointer) + " / " +
		 * StringFormat.hex(Offsets.m_dwLocalPlayerPointer - clientModule().start()));
		 * System.out.println("m_dwGlobalVars: " +
		 * StringFormat.hex(Offsets.m_dwGlobalVars)); System.out.println();
		 */
	}

	public static Process process() {
		return process;
	}

	public static Module clientModule() {
		return clientModule;
	}

	public static Module engineModule() {
		return engineModule;
	}

	public static boolean IsInGame() {
		// return true;
		return isInGame == 6;
	}

	private static void waitUntilFound(String message, Clause clause) {
		System.out.print("Looking for " + message + ". Please wait.");
		while (!clause.get())
			try {
				Thread.sleep(1000);
				System.out.print(".");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		System.out.println("\nFound " + message + "!");
	}

	@FunctionalInterface
	private interface Clause {
		boolean get();
	}
}
