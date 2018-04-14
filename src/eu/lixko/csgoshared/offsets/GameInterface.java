package eu.lixko.csgoshared.offsets;

import com.sun.jna.Callback;
import com.sun.jna.CallbackReference;
import com.sun.jna.Function;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

import eu.lixko.csgointernals.Engine;
import eu.lixko.csgointernals.Engine.CUserCmd;
import eu.lixko.csgoshared.util.StringFormat;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;

import com.github.jonatino.misc.Cacheable;
import com.github.jonatino.misc.MemoryBuffer;
import com.github.jonatino.process.Module;

public class GameInterface {
	private Module module;
	private String version;
	private String modulename;
	private long baseclass = 0;
	private long origvftptr = 0; // pointer to 1st element of VFT
	private int totalFunctions = 0;

	private ArrayList<Function> original_funcs = new ArrayList<>();
	private MemoryBuffer new_vtf; 

	public GameInterface(Module module, String modulename, String version) {
		this(module, 0, modulename, version);
	}

	public GameInterface(Module module, long baseclass, String modulename, String version) {
		this.module = module;
		this.baseclass = baseclass;
		if (baseclass == 0)
			this.baseclass = GetInterface(modulename, version);
		this.modulename = modulename;
		this.version = version;

		if (this.baseclass == 0)
			throw new RuntimeException("Failed to find " + version + " in " + modulename);

		this.origvftptr = module.readLong(this.baseclass);

		for (;; this.totalFunctions++) {
			long fptr = module.readLong(this.origvftptr + Pointer.SIZE * this.totalFunctions);
			if (fptr == 0)
				break;
			// we don't need to worry about using Cacheable.pointer, since it's passed by value anyway:
			// https://github.com/java-native-access/jna/blob/master/src/com/sun/jna/Function.java#L272
			// Cacheable.pointer(fptr)
			original_funcs.add(Function.getFunction(new Pointer(fptr)));
		}
		
		int bufsize = Pointer.SIZE * this.totalFunctions;
		new_vtf = new MemoryBuffer(bufsize);
		new_vtf.setBytes(0, new Pointer(this.origvftptr), bufsize);
		System.out.println("old: " + StringFormat.hex(module.readLong(this.baseclass)) + " > " + Pointer.nativeValue(new_vtf));
		Engine.unsafe.putLong(this.baseclass, Pointer.nativeValue(new_vtf));
		System.out.println("new: " + StringFormat.hex(module.readLong(this.baseclass)));
		module.writeLong(this.baseclass, Pointer.nativeValue(new_vtf));
		
		System.out.println("new: " + StringFormat.hex(module.readLong(this.baseclass)));
		//long newvtf = Native.malloc(this.totalFunctions * Pointer.SIZE);
		
		System.out.println("> Found " + this.totalFunctions + " methods in " + version);
	}

	public GameInterface(Module module, long addr) {
		this(module, addr, "", "");
	}

	public void HookFunction(Callback cb, int index) {
		if(index == this.totalFunctions)
			throw new IndexOutOfBoundsException("Cannot hook " + index + " in " + this.version + " - total functions: " + this.totalFunctions);
		Pointer cbFunc = CallbackReference.getFunctionPointer(cb);
		System.out.println("Hooking " + index + "in" + this.version + " from " + new_vtf.getLong(Pointer.SIZE * index) + " to " + StringFormat.hex(Pointer.nativeValue(cbFunc)));
		new_vtf.setLong(Pointer.SIZE * index, Pointer.nativeValue(cbFunc));
		System.out.println("done: " + StringFormat.hex(new_vtf.getLong(Pointer.SIZE * index)));
	}

	public void UnhookFunction(int index) {
		if(index == this.totalFunctions)
			throw new IndexOutOfBoundsException("Cannot unhook " + index + " in " + this.version + " - total functions: " + this.totalFunctions);
		Function origFunc = this.original_funcs.get(index);
		new_vtf.setLong(Pointer.SIZE * index, Pointer.nativeValue(origFunc));
		//module.writeLong(vftptr + Pointer.SIZE * index, Pointer.nativeValue(origFunc));
	}

	public void UnhookAll() {
		module.writeLong(this.baseclass, origvftptr);
		new_vtf.free();
	}

	public Module module() {
		return this.module;
	}

	public String moduleName() {
		return this.modulename;
	}

	public String version() {
		return this.version;
	}

	public long base() {
		return this.baseclass;
	}

	public long originalVFT() {
		return this.origvftptr;
	}

	public long totalFunctions() {
		return this.totalFunctions;
	}

	public Function getOriginalFunction(int index) {
		return this.original_funcs.get(index);
	}

	public static long GetInterface(String modulename, String version) {
		Function getif = NativeLibrary.getInstance(modulename).getFunction("CreateInterface");
		return getif.invokeLong(new Object[] { version, 0 });
	}
}
