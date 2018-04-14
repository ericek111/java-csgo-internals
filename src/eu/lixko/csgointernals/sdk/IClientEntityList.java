package eu.lixko.csgointernals.sdk;

import com.github.jonatino.process.Module;

import eu.lixko.csgoshared.offsets.GameInterface;

public class IClientEntityList extends GameInterface {
	public IClientEntityList(Module module, long baseclass, String modulename, String version) {
		super(module, baseclass, modulename, version);
		INSTANCE = this;
	}

	public IClientEntityList(Module module, long addr) {
		super(module, addr);
		INSTANCE = this;
	}

	public IClientEntityList(Module module, String modulename, String version) {
		super(module, modulename, version);
		INSTANCE = this;
	}

	public static IClientEntityList INSTANCE;
	
	public long GetClientEntity(int index) {
		return this.getOriginalFunction(3).invokeLong(new Object[] { this.base(), index });
	}
}
