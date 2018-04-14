package eu.lixko.csgointernals.sdk;

import java.util.Arrays;
import java.util.List;

import com.github.jonatino.process.Module;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import eu.lixko.csgoshared.offsets.GameInterface;
import eu.lixko.csgoshared.offsets.Hookable;

public class IClientMode extends GameInterface {
	public static IClientMode INSTANCE;

	public IClientMode(Module module, long baseclass, String modulename, String version) {
		super(module, baseclass, modulename, version);
		INSTANCE = this;
	}

	public IClientMode(Module module, long addr) {
		super(module, addr);
		INSTANCE = this;
	}

	public IClientMode(Module module, String modulename, String version) {
		super(module, modulename, version);
		INSTANCE = this;
	}

	// void OverrideView(void* thisptr, CViewSetup* pSetup)
	public static abstract class OverrideView extends Hookable implements Callback {
		public OverrideView() {
			super(INSTANCE, 19);
		}

		public abstract void callback(Pointer thisptr, long pSetup);
	}

	// bool hkCreateMove(void* thisptr, float flInputSampleTime, CUserCmd* cmd)
	public static abstract class CreateMove extends Hookable implements Callback {
		public CreateMove() {
			super(INSTANCE, 25);
		}

		public abstract boolean callback(Pointer thisptr, float flInputSampleTime, long cmd);
	}
	// OR this: public interface hkCreateMove extends Callback { boolean invoke(Pointer thisptr, float flInputSampleTime, long cmd); }
	
	public static class CUserCmd extends Structure implements Structure.ByReference {
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
	
}
