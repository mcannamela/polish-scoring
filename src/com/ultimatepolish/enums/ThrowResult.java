package com.ultimatepolish.enums;

public final class ThrowResult {
	public static final int CATCH = 0;
	public static final int DROP = 1;
	public static final int STALWART = 2;
	public static final int BROKEN = 3;
	public static final int NA = 4;
	
	public static final String[] typeString = {"Catch", "Drop", "Stalwart", "Broken", "NA"};

	public static String getString(int throwResult){
			return typeString[throwResult];
		}
	
	
}
