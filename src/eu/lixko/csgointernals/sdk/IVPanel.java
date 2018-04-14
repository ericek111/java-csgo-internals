package eu.lixko.csgointernals.sdk;

import com.github.jonatino.process.Module;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;

import eu.lixko.csgoshared.offsets.GameInterface;
import eu.lixko.csgoshared.offsets.Hookable;

public class IVPanel extends GameInterface {
	private Object[] thisbaseobj = new Object[] { this.base() };
	
	public IVPanel(Module module, long baseclass, String modulename, String version) {
		super(module, baseclass, modulename, version);
		INSTANCE = this;
	}

	public IVPanel(Module module, long addr) {
		super(module, addr);
		INSTANCE = this;
	}

	public IVPanel(Module module, String modulename, String version) {
		super(module, modulename, version);
		INSTANCE = this;
	}

	public static IVPanel INSTANCE;
	
	public String GetName(long vguiPanel) {
		return this.getOriginalFunction(37).invokeString(new Object[] { this.base(), vguiPanel}, false);
	}
	
	// void PaintTraverse(void* thisptr, VPANEL vgui_panel, bool force_repaint, bool allow_force)
	public static abstract class PaintTraverse extends Hookable implements Callback {
		public PaintTraverse() {
			super(INSTANCE, 42);
		}

		public abstract void callback(Pointer thisptr, long vgui_panel, boolean force_repaint, boolean allow_force);
	}

}
