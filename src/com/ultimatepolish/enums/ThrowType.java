package com.ultimatepolish.enums;

public final class ThrowType {
	public static final int BOTTLE = 0;
	public static final int CUP = 1;
	public static final int POLE = 2;
	public static final int STRIKE = 3;
	public static final int BALL_HIGH = 4;
	public static final int BALL_RIGHT = 5;
	public static final int BALL_LOW = 6;
	public static final int BALL_LEFT = 7;
	public static final int SHORT = 8;
	public static final int TRAP = 9;
	public static final int TRAP_REDEEMED = 10;
	public static final int NOT_THROWN = 11;
	public static final int FIRED_ON = 12;
	
	public static final String[] typeString = {"Bottle", "Cup", "Pole", "Strike", "High", "Right",
		"Low", "Left", "Short", "Trap", "Redeemed Trap", "Not thrown", "Fired on"};
	
	public static boolean isBall(int i){
		boolean b = (i==BALL_HIGH || i==BALL_LOW|| i==BALL_RIGHT|| i==BALL_LEFT); 
		return b;
	}
	
	public static boolean isStackHit(int i){
		boolean b = (i==BOTTLE || i==CUP|| i==POLE); 
		return b;
	}
	
	public static String getString(int throwType){
		return typeString[throwType];
	}
}
