package eu.lixko.csgoshared.structs;

import eu.lixko.csgoshared.util.BufferStruct;

public class ExampleBufStruct extends BufferStruct {
	public int a = 2;
	public int b = 4;
	public ExampleBufChild[] bufc = new ExampleBufChild[2];
}
