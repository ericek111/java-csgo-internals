package eu.lixko.csgointernals.sdk;

import com.github.jonatino.process.Module;

import eu.lixko.csgoshared.offsets.GameInterface;

public class IEngineClient extends GameInterface {
	private Object[] thisbaseobj = new Object[] { this.base() };
	
	public IEngineClient(Module module, long baseclass, String modulename, String version) {
		super(module, baseclass, modulename, version);
		INSTANCE = this;
	}

	public IEngineClient(Module module, long addr) {
		super(module, addr);
		INSTANCE = this;
	}

	public IEngineClient(Module module, String modulename, String version) {
		super(module, modulename, version);
		INSTANCE = this;
	}

	public static IEngineClient INSTANCE;
	
	public int GetLocalPlayer() {
		return this.getOriginalFunction(12).invokeInt(thisbaseobj);
	}
}
