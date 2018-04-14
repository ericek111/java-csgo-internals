package eu.lixko.csgointernals;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import com.github.jonatino.misc.MemoryBuffer;
import com.github.jonatino.process.Module;
import com.github.jonatino.process.Process;
import com.github.jonatino.process.Processes;
import com.sun.jna.Callback;
import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import eu.lixko.csgointernals.Client;
import eu.lixko.csgoshared.natives.CLink;
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
	
	hkCreateMove createMoveHook = new hkCreateMove() {
		Function origFunc;
		public boolean orig(Object[] params) {
			Boolean origRet = (Boolean) origFunc.invoke(Boolean.class, params);
			return origRet.booleanValue();
		}
		public boolean invoke(Pointer thisptr, float flInputSampleTime, long cmd) {
			if(origFunc == null) {
				 origFunc = Offsets.IClientMode.getOriginalFunction(25);
			}
			
			while(true) {
				if(cmd == 0) 
					break;
				
				int lpi = Offsets.VEngineClient.getOriginalFunction(12).invokeInt(new Object[] {Offsets.VClientEntityList.base()});
				long localplayer = Offsets.VClientEntityList.getOriginalFunction(3).invokeLong(new Object[] { Offsets.VClientEntityList.base(), lpi });
				
				if(localplayer == 0) 
					break;
				
				int cmdnum = unsafe.getInt(cmd + 8); // command_number
				if(cmdnum == 0)
					break;

				int buttons = unsafe.getInt(cmd + 52);
				if((buttons & (1 << 1)) == 0) // cmd->buttons & IN_JUMP
					break;

				int flags = unsafe.getInt(localplayer + 0x138);
				if((flags & 1) == 0) {
					buttons &= ~(1 << 1);
					unsafe.putInt(cmd + 52, buttons);
				}
				
				break;
			}
			
			Boolean origRet = (Boolean) origFunc.invoke(Boolean.class, new Object[] { thisptr, flInputSampleTime, cmd});
			return origRet.booleanValue();
		}
	};

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

		loadOffsets();
		
		Offsets.IClientMode.HookFunction(createMoveHook, 25);
		
		System.out.println("Engine initialization complete! Starting client...");
		Client.theClient.startClient();

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
	
	interface cppDestructor extends Callback {
		void invoke();
	}
	
	public class CUserCmd extends Structure implements Structure.ByReference {
		public long destructor;
		public int command_number;
		public int tick_count;
		public float[] viewangles = new float[3];
		public float[] aimdirection = new float[3];
		public float forwardmove;
		public float sidemove;
		public float upmove;
		public int buttons;
		public byte impulse;
		public int weaponselect;
		public int weaponsubtype;
		public int random_seed;
		public short mousedx;
		public short mousedy;
		public boolean hasbeenpredicted;
		public float[] headangles = new float[3];
		public float[] headoffset = new float[3];

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("destructor", "command_number", "tick_count", "viewangles", "aimdirection", "forwardmove", "sidemove", "upmove", "buttons", "impulse", "weaponselect", "weaponsubtype", "random_seed", "mousedx", "mousedy", "hasbeenpredicted", "headangles", "headoffset");
		}
	}

	// bool hkCreateMove(void* thisptr, float flInputSampleTime, CUserCmd* cmd)
	public interface hkCreateMove extends Callback {
		boolean invoke(Pointer thisptr, float flInputSampleTime, long cmd);
	}

}
