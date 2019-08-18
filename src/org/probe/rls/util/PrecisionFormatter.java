package org.probe.rls.util;

import java.text.DecimalFormat;

public class PrecisionFormatter {
	public static double roundToNumDecimals(double value, int numDecimals) {
		String formatString = createFormatString(numDecimals);
		DecimalFormat form = new DecimalFormat(formatString);
		
		return Double.valueOf(form.format(value));
	}
	
	private static String createFormatString(int numDecimals){
		StringBuilder sb = new StringBuilder("#.");
		
		while(numDecimals > 0){
			sb.append("#");
			numDecimals--;
		}
		
		return sb.toString();
	}
}
