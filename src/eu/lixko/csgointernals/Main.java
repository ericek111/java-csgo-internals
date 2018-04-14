package eu.lixko.csgointernals;

import eu.lixko.csgointernals.Engine;

public class Main {
	public static final Engine engine = new Engine();
	
	public static void main(String[] args) throws Exception {
		engine.init();
	}
	
	public static void finish() {
		engine.end();
	}
}
