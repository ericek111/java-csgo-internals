package eu.lixko.csgoshared.structs;

import eu.lixko.csgoshared.util.BufferStruct;

public class ExampleBufClass {
	
	public static class ExampleChild extends BufferStruct {
		public int a, b;
	}

	public static class ExampleParent extends BufferStruct {
		public int x, y;
		public ExampleChild[] child = new ExampleChild[2];
	}
}
