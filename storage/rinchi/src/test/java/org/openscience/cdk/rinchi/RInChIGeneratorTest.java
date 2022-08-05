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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openscience.cdk.Reaction;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.io.MDLRXNV2000Reader;
import org.openscience.cdk.io.IChemObjectReader.Mode;
import org.openscience.cdk.test.CDKTestCase;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;


public class RInChIGeneratorTest extends CDKTestCase {

	private static final ILoggingTool logger = LoggingToolFactory.createLoggingTool(RInChIGeneratorTest.class);
	
	public static Map<String, String> readRinchiFullInfoFromResourceFile(String fileName) {
		try (InputStream is = RInChIGeneratorTest.class.getResourceAsStream(fileName)) {
			Properties props = new Properties();
			props.load(is);
			Map<String, String> rfi = new HashMap<>();				
			
			String s;
			s = props.getProperty("RInChI");
			if (s != null)
				rfi.put("RInChI", s);			
			s = props.getProperty("RAuxInfo");
			if (s != null)
				rfi.put("RAuxInfo", s);
			s = props.getProperty("Long-RInChIKey");
			if (s != null)
				rfi.put("Long-RInChIKey",s);			
			s = props.getProperty("Short-RInChIKey");
			if (s != null)
				rfi.put("Short-RInChIKey", s);
			s = props.getProperty("Web-RInChIKey");
			if (s != null)
				rfi.put("Web-RInChIKey", s);
			return rfi;
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static IReaction readReactionFromResourceRXNFile(String fileName) throws Exception {

		InputStream ins =  RInChIGeneratorTest.class.getResourceAsStream(fileName);
		MDLRXNV2000Reader reader = new MDLRXNV2000Reader(ins, Mode.STRICT);
		IReaction reaction = new Reaction();
		reaction = reader.read(reaction);
		reader.close();
		return reaction;
	}
}
