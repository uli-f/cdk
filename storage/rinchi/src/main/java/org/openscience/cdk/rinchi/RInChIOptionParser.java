/* Copyright (C) 2022  Nikolay Kochev <nick@uni-plovdiv.net>
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.rinchi;

import java.util.HashMap;
import java.util.Map;

import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

import io.github.dan2097.jnarinchi.RinchiFlag;
import io.github.dan2097.jnarinchi.RinchiOptions;

/**
 * Provides parsing of RInChI options from a string. Using the JNA RinchiOptions builder directly.
 * @author Nikolay Kochev (adopted original code from John Mayfield)
 */
public class RInChIOptionParser {

	private final ILoggingTool logger = LoggingToolFactory.createLoggingTool(RInChIOptionParser.class);
	private final Map<String,RinchiFlag> optMap = new HashMap<>();
	private final RinchiOptions.RinchiOptionsBuilder options;

	private RInChIOptionParser() {
		for (RinchiFlag flag : RinchiFlag.values())
			optMap.put(flag.name(), flag);

		options = new RinchiOptions.RinchiOptionsBuilder();
	}

	private void processString(String optstr) {
		int pos = 0;
		while (pos < optstr.length()) {
			switch (optstr.charAt(pos)) {
			case ' ':
			case '-':
			case '/':
			case ',':
				pos++; // skip
				break;
			case 'W': // timeout
				pos++;
				int next = getIndexOfEither(optstr,',',' ', pos);
				if (next < 0)
					next = optstr.length();
				String substring = optstr.substring(pos, next);
				try {
					// Note: locale sensitive e.g. 0,01 but we can not pass in milliseconds so doesn't matter so much
					options.withTimeoutMilliSeconds((int)(1000*Double.parseDouble(substring)));
				} catch (NumberFormatException ex) {
					logger.warn("Invalid timeout:" + substring);
				}
				break;
			default:
				next = getIndexOfEither(optstr,',',' ', pos);
				if (next < 0)
					next = optstr.length();
				RinchiFlag flag = optMap.get(optstr.substring(pos, next));
				if (flag != null)
					options.withFlag(flag);
				else
					logger.warn("Ignore unrecognized InChI flag:" + optstr.substring(pos, next));
				pos = next;
			}
		}
	}

	/**
	 * 
	 * @param str the string to search into.
	 * @param chA a character to search for (Unicode code point).
	 * @param chB a character to search for (Unicode code point).
	 * @param fromIndex the index to start the search from.
	 * @return the index of the first occurrence of either of the characters in the character 
	 * sequence represented by the string object that is greater than or equal to fromIndex, 
	 * or -1 if the character does not occur.
	 */
	private static int getIndexOfEither(String str, char chA, char chB, int fromIndex) {
		int iA = str.indexOf(chA, fromIndex);
		int iB = str.indexOf(chB, fromIndex);
		if (iA<0)
			return iB;
		if (iB<0)
			return iA;
		return Math.min(iA, iB);
	}

	static RinchiOptions parseString(String str) {
		if (str == null)
			return null;
		RInChIOptionParser parser = new RInChIOptionParser();
		parser.processString(str);
		return parser.options.build();
	}

}
