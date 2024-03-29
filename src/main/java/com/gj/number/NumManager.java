package com.gj.number;

public class NumManager {
	public static float getSignificantFigures(float num, int n) {
	    if(num == 0) {
	        return 0;
	    }
	    final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
	    final int power = n - (int) d;
	    final double magnitude = Math.pow(10, power);
	    final long shifted = Math.round(num*magnitude);
	    return (float) (shifted/magnitude);		
	}
}
