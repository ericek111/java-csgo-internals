package eu.lixko.csgoshared.offsets;

import com.sun.jna.Function;

public abstract class Hookable {
	public GameInterface iface;
	public int index;
	public Function origFunc;
	
	public Hookable(GameInterface iface, int index) {
		this.iface = iface;
		this.index = index;
	}
	
	public void hook(GameInterface iface, int index) {
		iface.HookFunction(this, index);
		this.origFunc = iface.getOriginalFunction(index);
	}
	
	public void hook() {
		iface.HookFunction(this, index);
		this.origFunc = iface.getOriginalFunction(index);
	}
	
	public void unhook() {
		iface.UnhookFunction(index);
	}
	
}
