package eu.lixko.csgoshared.util;

public class Profiler {
	public static Profiler INSTANCE = null;
	
	static {
		INSTANCE = new Profiler();
	}
}
