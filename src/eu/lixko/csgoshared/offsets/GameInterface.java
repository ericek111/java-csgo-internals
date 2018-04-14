package eu.lixko.csgoshared.offsets;

import com.sun.jna.Callback;
import com.sun.jna.CallbackReference;
import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

import eu.lixko.csgointernals.Engine;
import eu.lixko.csgoshared.natives.CLink;
import eu.lixko.csgoshared.util.StringFormat;

import java.util.ArrayList;

import com.github.jonatino.misc.Cacheable;
import com.github.jonatino.misc.MemoryBuffer;
import com.github.jonatino.misc.Strings;
import com.github.jonatino.natives.unix.dlfcn;
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
			original_funcs.add(Function.getFunction(Cacheable.pointer(fptr)));
		}
		
		int bufsize = Pointer.SIZE * this.totalFunctions;
		new_vtf = new MemoryBuffer(bufsize);
		new_vtf.setBytes(0, Cacheable.pointer(this.origvftptr), bufsize);
		System.out.println("old: " + StringFormat.hex(module.readLong(this.baseclass)) + " > " + Pointer.nativeValue(new_vtf));
		module.writeLong(this.baseclass, Pointer.nativeValue(new_vtf));

		System.out.println("new: " + StringFormat.hex(module.readLong(this.baseclass)));
		Engine.unsafe.putLong(this.baseclass, Pointer.nativeValue(new_vtf));

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
	
	public void HookFunction(Hookable cb, int index) {
		this.HookFunction((Callback) cb, index);
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
	
	public static void DumpInterfaces() {
		final StringBuilder sb = new StringBuilder();
		CLink.dl_iterator callback = new CLink.dl_iterator() {
			public int invoke(Pointer info, long size, long data) {
				CLink.dl_phdr_info phdr = new CLink.dl_phdr_info(info);
				phdr.read();
				System.out.println(phdr.dlpi_name);
				sb.append(phdr.dlpi_name);
				sb.append('\n');
				
				long library = dlfcn.dlopen(phdr.dlpi_name, dlfcn.RTLD_LAZY);
				//System.out.println("\tlibrary: " + StringFormat.hex(library));
				if(library == 0)
					return 0;
				long interfaces_sym = dlfcn.dlsym(library, "s_pInterfaceRegs");
				//System.out.println("\tinterfaces_sym: " + StringFormat.hex(interfaces_sym));
				dlfcn.dlclose(library);
				if(interfaces_sym == 0)
					return 0;
				
				long cur_interface = Engine.unsafe.getLong(interfaces_sym);
				//System.out.println("\tcur_interface: " + StringFormat.hex(cur_interface));
				
				while(cur_interface != 0) {
					sb.append('\t');
					long name = Engine.engineModule().readLong(cur_interface + 8);
					cur_interface = Engine.engineModule().readLong(cur_interface + 16);
					//System.out.println("\tname: " + name + " / next: " + StringFormat.hex(cur_interface));
					if(name == 0)
						continue;
					
					byte[] bytes = Cacheable.array(255);
					Cacheable.pointer(name).read(0, bytes, 0, 255);
					String symbol = Strings.transform(bytes);					
					System.out.println("\t" + symbol);
					//String symbol = Engine.engineModule().readString(name, 255);
					sb.append(symbol);
					sb.append('\n');
				}
				sb.append('\n');
				
				return 0;
			}
		};
		System.out.println("INTERFACE DUMP: ");
		System.out.println(sb.toString());
		
		CLink.INSTANCE.dl_iterate_phdr(callback, 0);
	}
}
