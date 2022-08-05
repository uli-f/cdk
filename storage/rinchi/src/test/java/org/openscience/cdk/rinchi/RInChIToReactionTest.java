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

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.test.CDKTestCase;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

import io.github.dan2097.jnarinchi.RinchiOptions;
import io.github.dan2097.jnarinchi.RinchiStatus;


public class RInChIToReactionTest extends CDKTestCase {
	
	private static final ILoggingTool logger = LoggingToolFactory.createLoggingTool(RInChIToReactionTest.class);
	
	public void doubleConversionTest(String rinchi, String auxInfo, boolean useCDK_MDL_IO, boolean useCDK_MDL_IO2) throws Exception {
		//RInChI --> Reaction
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(rinchi, auxInfo, useCDK_MDL_IO);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, r2r.getStatus());
		IReaction reaction = r2r.getReaction();
		Assert.assertNotNull(reaction);
		//Reaction --> RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().
			 	getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, useCDK_MDL_IO2);
		Assert.assertNotNull(gen);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, gen.getRInChIStatus());
		Assert.assertEquals("RinChI:", rinchi, gen.getRInChI());
		if (!auxInfo.isEmpty()) 
			Assert.assertEquals("RAuxInfo:", auxInfo, gen.getAuxInfo());
	}
	
	public void doubleConversionTestForExampleFile(String rinchiFile, boolean useCDK_MDL_IO, boolean useCDK_MDL_IO2) throws Exception {
		//RInChI --> Reaction
		Map<String, String> rfi = RInChIGeneratorTest.readRinchiFullInfoFromResourceFile(rinchiFile);
		String rinchi = rfi.get("RInChI");
		String auxInfo = rfi.get("RAuxInfo");
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(rinchi, auxInfo, useCDK_MDL_IO);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, r2r.getStatus());
		IReaction reaction = r2r.getReaction();
		Assert.assertNotNull(reaction);
		//Reaction --> RInChI 
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().
			 	getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, useCDK_MDL_IO2);
		Assert.assertNotNull(gen);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, gen.getRInChIStatus());
		Assert.assertEquals("RinChI:", rinchi, gen.getRInChI());
		Assert.assertEquals("RAuxInfo:", auxInfo, gen.getAuxInfo());
		Assert.assertEquals("Long-RInChIKey:", rfi.get("Long-RInChIKey"), gen.getLongRInChIKey());
		Assert.assertEquals("Short-RInChIKey:", rfi.get("Short-RInChIKey"), gen.getShortRInChIKey());
		Assert.assertEquals("Web-RInChIKey:", rfi.get("Web-RInChIKey"), gen.getWebRInChIKey());
	}
	
	@Test
	public void testExample_Tautomerization_01() throws Exception {
		doubleConversionTestForExampleFile("examples/Tautomerization_01.txt", false, false);
		doubleConversionTestForExampleFile("examples/Tautomerization_01.txt", true, true);
		//doubleConversionTestForExampleFile("examples/Tautomerization_01.txt", false, true);
		doubleConversionTestForExampleFile("examples/Tautomerization_01.txt", true, false);
	}
	
	@Test
	public void testExample_1_reactant__A() throws Exception {		
		doubleConversionTestForExampleFile("examples/1_reactant_-_A.txt", true, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_A.txt", false, false);
		doubleConversionTestForExampleFile("examples/1_reactant_-_A.txt", true, false);
	}
	
	@Test
	public void testExample_1_reactant__no_product() throws Exception {		
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_product.txt", true, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_product.txt", false, false);
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_product.txt", true, false);
	}
	
	@Test
	public void testExample_1_reactant__no_structure() throws Exception {		
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_structure.txt", true, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_structure.txt", false, false);
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_structure.txt", true, false);
	}
	
	@Test
	public void testExample_1_reactant__R() throws Exception {		
		doubleConversionTestForExampleFile("examples/1_reactant_-_R.txt", true, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_R.txt", false, false);
		doubleConversionTestForExampleFile("examples/1_reactant_-_R.txt", true, false);
	}
	
}
