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

import io.github.dan2097.jnarinchi.RinchiOptions;
import io.github.dan2097.jnarinchi.Status;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.test.CDKTestCase;

import java.util.Map;


public class RInChIToReactionTest extends CDKTestCase {

	public void doubleConversionTest(String rinchi, String auxInfo, boolean useCDK_MDL_IO, boolean useCDK_MDL_IO2) throws Exception {
		//RInChI --> Reaction
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(rinchi, auxInfo, useCDK_MDL_IO);
		Assert.assertEquals("RInChI status:", Status.SUCCESS, r2r.getStatus());
		IReaction reaction = r2r.getReaction();
		Assert.assertNotNull(reaction);
		//Reaction --> RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().
			 	getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, useCDK_MDL_IO2);
		Assert.assertNotNull(gen);
		Assert.assertEquals("RInChI status:", Status.SUCCESS, gen.getRInChIStatus());
		Assert.assertEquals("RinChI:", rinchi, gen.getRInChI());
		if (!auxInfo.isEmpty())
			Assert.assertEquals("RAuxInfo:", auxInfo, gen.getAuxInfo());
	}

	public void doubleConversionTestForExampleFile(String rinchiFile, boolean useCDK_MDL_IO, boolean useCDK_MDL_IO2) throws Exception {
		doubleConversionTestForExampleFile(rinchiFile, useCDK_MDL_IO, useCDK_MDL_IO2, true);
	}

	public void doubleConversionTestForExampleFile(String rinchiFile, boolean useCDK_MDL_IO, boolean useCDK_MDL_IO2,
			boolean compareRAuxInfo) throws Exception {
		//RInChI --> Reaction
		Map<String, String> rfi = RInChIGeneratorTest.readRinchiFullInfoFromResourceFile(rinchiFile);
		String rinchi = rfi.get("RInChI");
		String auxInfo = rfi.get("RAuxInfo");
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(rinchi, auxInfo, useCDK_MDL_IO);
		Assert.assertEquals("RInChI status:", Status.SUCCESS, r2r.getStatus());
		IReaction reaction = r2r.getReaction();
		Assert.assertNotNull(reaction);
		//Reaction --> RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().
			 	getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, useCDK_MDL_IO2);
		Assert.assertNotNull(gen);
		Assert.assertEquals("RInChI status:", Status.SUCCESS, gen.getRInChIStatus());
		Assert.assertEquals("RinChI:", rinchi, gen.getRInChI());
		if (compareRAuxInfo)
			Assert.assertEquals("RAuxInfo:", auxInfo, gen.getAuxInfo());
		Assert.assertEquals("Long-RInChIKey:", rfi.get("Long-RInChIKey"), gen.getLongRInChIKey());
		Assert.assertEquals("Short-RInChIKey:", rfi.get("Short-RInChIKey"), gen.getShortRInChIKey());
		Assert.assertEquals("Web-RInChIKey:", rfi.get("Web-RInChIKey"), gen.getWebRInChIKey());
	}

	public void doubleConversionTestForExampleFile_excludeAgents(String rinchiFile,
			boolean useCDK_MDL_IO, boolean useCDK_MDL_IO2) throws Exception {
		//RInChI --> Reaction
		Map<String, String> rfi = RInChIGeneratorTest.readRinchiFullInfoFromResourceFile(rinchiFile);
		String rinchi = rfi.get("RInChI");
		String auxInfo = rfi.get("RAuxInfo");
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(rinchi, auxInfo, useCDK_MDL_IO);
		Assert.assertEquals("RInChI status:", Status.SUCCESS, r2r.getStatus());
		IReaction reaction = r2r.getReaction();
		Assert.assertNotNull(reaction);
		//Reaction --> RInChI
		String expectedRinchi = removeAgents(rinchi);
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().
			 	getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, useCDK_MDL_IO2);
		Assert.assertNotNull(gen);
		Assert.assertEquals("RInChI status:", Status.SUCCESS, gen.getRInChIStatus());
		Assert.assertEquals("RinChI with excluded agents:", expectedRinchi, gen.getRInChI());
	}

	private String removeAgents(String rinchi) {
		//RInChI=1.00.1S/layer2<>layer3<>layer4/d(+,-,=)/u#2-#3-#4
		//Agents are on the layer4
		String[] tokens = rinchi.split("<>");

		if (tokens.length != 3) {
			//layer4 is missing (tokens.length < 3)
			//or something else happen (e.g. incorrect rinchi, tokens.length > 3)
			return rinchi;
		}

		String newToken;
		int pos = tokens[2].indexOf("/d");
		if (pos == -1)
			newToken = "";
		else
			newToken = tokens[2].substring(pos);

		return tokens[0] + "<>" + tokens[1] + newToken;
	}

	@Test
	public void testExample_1_reactant__A() throws Exception {
		doubleConversionTestForExampleFile("examples/1_reactant_-_A.txt", true, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_A.txt", false, false);
		doubleConversionTestForExampleFile("examples/1_reactant_-_A.txt", false, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_A.txt", true, false);
	}

	@Test
	public void testExample_1_reactant__no_product() throws Exception {
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_product.txt", true, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_product.txt", false, false);
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_product.txt", false, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_product.txt", true, false);
	}

	@Test
	public void testExample_1_reactant__no_structure() throws Exception {
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_structure.txt", true, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_structure.txt", false, false);
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_structure.txt", false, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_no_structure.txt", true, false);
	}

	@Test
	public void testExample_1_reactant__R() throws Exception {
		doubleConversionTestForExampleFile("examples/1_reactant_-_R.txt", true, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_R.txt", false, false);
		doubleConversionTestForExampleFile("examples/1_reactant_-_R.txt", false, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_R.txt", true, false);
	}

	@Test
	public void testExample_1_reactant__X() throws Exception {
		doubleConversionTestForExampleFile("examples/1_reactant_-_X.txt", true, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_X.txt", false, false);
		doubleConversionTestForExampleFile("examples/1_reactant_-_X.txt", false, true);
		doubleConversionTestForExampleFile("examples/1_reactant_-_X.txt", true, false);
	}

	@Test
	public void testExample_Esterification_01_flat() throws Exception {
		doubleConversionTestForExampleFile("examples/Esterification_01_flat.txt", false, false);
		//For flags useCDK_MDL_IO/useCDK_MDL_IO2 = true, reaction agents are not converted
		doubleConversionTestForExampleFile_excludeAgents("examples/Esterification_01_flat.txt", true, true);
		doubleConversionTestForExampleFile_excludeAgents("examples/Esterification_01_flat.txt", true, false);
		doubleConversionTestForExampleFile_excludeAgents("examples/Esterification_01_flat.txt", false, true);
	}

	@Test
	public void testExample_Esterification_01() throws Exception {
		doubleConversionTestForExampleFile("examples/Esterification_01.txt", false, false);
		//For flags useCDK_MDL_IO/useCDK_MDL_IO2 = true, reaction agents are not converted
		doubleConversionTestForExampleFile_excludeAgents("examples/Esterification_01.txt", true, true);
		doubleConversionTestForExampleFile_excludeAgents("examples/Esterification_01.txt", true, false);
		doubleConversionTestForExampleFile_excludeAgents("examples/Esterification_01.txt", false, true);
	}

	@Test
	public void testExample_Esterification_02() throws Exception {
		doubleConversionTestForExampleFile("examples/Esterification_02.txt", false, false);
		//For flags useCDK_MDL_IO/useCDK_MDL_IO2 = true, reaction agents are not converted
		doubleConversionTestForExampleFile_excludeAgents("examples/Esterification_02.txt", true, true);
		doubleConversionTestForExampleFile_excludeAgents("examples/Esterification_02.txt", true, false);
		doubleConversionTestForExampleFile_excludeAgents("examples/Esterification_02.txt", false, true);
	}

	@Test
	public void testExample_Esterification_03() throws Exception {
		doubleConversionTestForExampleFile("examples/Esterification_03.txt", false, false);
		//For flags useCDK_MDL_IO/useCDK_MDL_IO2 = true, reaction agents are not converted
		doubleConversionTestForExampleFile_excludeAgents("examples/Esterification_03.txt", true, true);
		doubleConversionTestForExampleFile_excludeAgents("examples/Esterification_03.txt", true, false);
		doubleConversionTestForExampleFile_excludeAgents("examples/Esterification_03.txt", false, true);
	}

	@Test
	public void testExample_Inverted_stereochemistry() throws Exception {
		doubleConversionTestForExampleFile("examples/Inverted_stereochemistry.txt", true, true);
		doubleConversionTestForExampleFile("examples/Inverted_stereochemistry.txt", true, false);
		doubleConversionTestForExampleFile("examples/Inverted_stereochemistry.txt", false, false);
		doubleConversionTestForExampleFile("examples/Inverted_stereochemistry.txt", false, true);
	}

	@Test
	public void testExample_Multiplesteps() throws Exception {
		doubleConversionTestForExampleFile("examples/Multiplesteps.txt", false, false);
		//For flags useCDK_MDL_IO/useCDK_MDL_IO2 = true, reaction agents are not converted
		doubleConversionTestForExampleFile_excludeAgents("examples/Multiplesteps.txt", true, true);
		doubleConversionTestForExampleFile_excludeAgents("examples/Multiplesteps.txt", true, false);
		doubleConversionTestForExampleFile_excludeAgents("examples/Multiplesteps.txt", false, true);
	}

	@Test
	public void testExample_No_reactant_1_product_02() throws Exception {
		doubleConversionTestForExampleFile("examples/No_reactant_-_1_product_02.txt", true, true);
		doubleConversionTestForExampleFile("examples/No_reactant_-_1_product_02.txt", false, false);
		doubleConversionTestForExampleFile("examples/No_reactant_-_1_product_02.txt", false, true);
		doubleConversionTestForExampleFile("examples/No_reactant_-_1_product_02.txt", true, false);
	}

	@Test
	public void testExample_No_reactant_1_product() throws Exception {
		doubleConversionTestForExampleFile("examples/No_reactant_-_1_product.txt", true, true);
		doubleConversionTestForExampleFile("examples/No_reactant_-_1_product.txt", false, false);
		doubleConversionTestForExampleFile("examples/No_reactant_-_1_product.txt", false, true);
		doubleConversionTestForExampleFile("examples/No_reactant_-_1_product.txt", true, false);
	}

	@Test
	public void testExample_No_reactant___no_product() throws Exception {
		doubleConversionTestForExampleFile("examples/No_reactant_-_no_product.txt", false, false);
		//conversion with useCDK_MDL_IO option is not working for this case
	}

	@Test
	public void testExample_no_structure__1_product() throws Exception {
		doubleConversionTestForExampleFile("examples/no_structure_-_1_product.txt", true, true);
		doubleConversionTestForExampleFile("examples/no_structure_-_1_product.txt", false, false);
		doubleConversionTestForExampleFile("examples/no_structure_-_1_product.txt", false, true);
		doubleConversionTestForExampleFile("examples/no_structure_-_1_product.txt", true, false);
	}

	@Test
	public void testExample_No_Structure_0_02() throws Exception {
		doubleConversionTestForExampleFile("examples/No_Structure_0-02.txt", false, false);
		//conversion with useCDK_MDL_IO option is not working for this case
	}

	@Test
	public void testExample_nostruct__X() throws Exception {
		doubleConversionTestForExampleFile("examples/nostruct_-_X.txt", true, true);
		doubleConversionTestForExampleFile("examples/nostruct_-_X.txt", false, false);
		doubleConversionTestForExampleFile("examples/nostruct_-_X.txt", false, true);
		doubleConversionTestForExampleFile("examples/nostruct_-_X.txt", true, false);
	}

	@Test
	public void testExample_R___A() throws Exception {
		doubleConversionTestForExampleFile("examples/R-_-A.txt", true, true);
		doubleConversionTestForExampleFile("examples/R-_-A.txt", false, false);
		doubleConversionTestForExampleFile("examples/R-_-A.txt", false, true);
		doubleConversionTestForExampleFile("examples/R-_-A.txt", true, false);
	}

	@Test
	public void testExample_RingOpening01() throws Exception {
		doubleConversionTestForExampleFile("examples/RingOpening01.txt", true, true);
		doubleConversionTestForExampleFile("examples/RingOpening01.txt", true, false);
		doubleConversionTestForExampleFile("examples/RingOpening01.txt", false, false);
		doubleConversionTestForExampleFile("examples/RingOpening01.txt", false, true);

	}

	@Test
	public void testExample_star_star___nostruct() throws Exception {
		doubleConversionTestForExampleFile("examples/star_star_-_nostruct.txt", true, true);
		doubleConversionTestForExampleFile("examples/star_star_-_nostruct.txt", false, false);
		doubleConversionTestForExampleFile("examples/star_star_-_nostruct.txt", false, true);
		doubleConversionTestForExampleFile("examples/star_star_-_nostruct.txt", true, false);
	}

	@Test
	public void testExample_Styrene___Polystyrene_as_no_struct() throws Exception {
		doubleConversionTestForExampleFile("examples/Styrene_-_Polystyrene_as_no-struct.txt", true, true);
		doubleConversionTestForExampleFile("examples/Styrene_-_Polystyrene_as_no-struct.txt", false, false);
		doubleConversionTestForExampleFile("examples/Styrene_-_Polystyrene_as_no-struct.txt", false, true);
		doubleConversionTestForExampleFile("examples/Styrene_-_Polystyrene_as_no-struct.txt", true, false);
	}

	@Test
	public void testExample_Tautomerization_01() throws Exception {
		doubleConversionTestForExampleFile("examples/Tautomerization_01.txt", true, true);
		doubleConversionTestForExampleFile("examples/Tautomerization_01.txt", false, false);
		doubleConversionTestForExampleFile("examples/Tautomerization_01.txt", false, true);
		doubleConversionTestForExampleFile("examples/Tautomerization_01.txt", true, false);
	}

	@Test
	public void testExample_X___1_product() throws Exception {
		doubleConversionTestForExampleFile("examples/X_-_1_product.txt", true, true);
		doubleConversionTestForExampleFile("examples/X_-_1_product.txt", false, false);
		doubleConversionTestForExampleFile("examples/X_-_1_product.txt", false, true);
		doubleConversionTestForExampleFile("examples/X_-_1_product.txt", true, false);
	}

	@Test
	public void testIsotopeConversion_01() throws Exception {
		doubleConversionTest("RInChI=1.00.1S/C6H10O/c7-6-4-2-1-3-5-6/h4,7H,1-3,5H2/i5+1<>CH4/h1H4/d+", "", true, true);
		doubleConversionTest("RInChI=1.00.1S/C6H10O/c7-6-4-2-1-3-5-6/h4,7H,1-3,5H2/i5+1<>CH4/h1H4/d+", "", false, false);
		doubleConversionTest("RInChI=1.00.1S/C6H10O/c7-6-4-2-1-3-5-6/h4,7H,1-3,5H2/i5+1<>CH4/h1H4/d+", "", false, true);
		doubleConversionTest("RInChI=1.00.1S/C6H10O/c7-6-4-2-1-3-5-6/h4,7H,1-3,5H2/i5+1<>CH4/h1H4/d+", "", true, false);
	}

	@Test
	public void testExample_no_reactants_one_product() throws Exception {
		doubleConversionTest("RInChI=1.00.1S/<>C8H8O2/c9-8(10)6-7-4-2-1-3-5-7/h1-5H,6H2,(H,9,10)/d+", "", true, true);
		doubleConversionTest("RInChI=1.00.1S/<>C8H8O2/c9-8(10)6-7-4-2-1-3-5-7/h1-5H,6H2,(H,9,10)/d+", "", false, false);
		doubleConversionTest("RInChI=1.00.1S/<>C8H8O2/c9-8(10)6-7-4-2-1-3-5-7/h1-5H,6H2,(H,9,10)/d+", "", false, true);
		doubleConversionTest("RInChI=1.00.1S/<>C8H8O2/c9-8(10)6-7-4-2-1-3-5-7/h1-5H,6H2,(H,9,10)/d+", "", true, false);
	}

	@Test
	public void testExample_two_reactants_no_products() throws Exception {
		doubleConversionTest("RInChI=1.00.1S/<>C5H12O/c1-4(2)5(3)6/h4-6H,1-3H3!C8H8O2/c9-8(10)6-7-4-2-1-3-5-7/h1-5H,6H2,(H,9,10)/d-",
				"", true, true);
		doubleConversionTest("RInChI=1.00.1S/<>C5H12O/c1-4(2)5(3)6/h4-6H,1-3H3!C8H8O2/c9-8(10)6-7-4-2-1-3-5-7/h1-5H,6H2,(H,9,10)/d-",
				"", false, false);
		doubleConversionTest("RInChI=1.00.1S/<>C5H12O/c1-4(2)5(3)6/h4-6H,1-3H3!C8H8O2/c9-8(10)6-7-4-2-1-3-5-7/h1-5H,6H2,(H,9,10)/d-",
				"", false, true);
		doubleConversionTest("RInChI=1.00.1S/<>C5H12O/c1-4(2)5(3)6/h4-6H,1-3H3!C8H8O2/c9-8(10)6-7-4-2-1-3-5-7/h1-5H,6H2,(H,9,10)/d-",
				"", true, false);
	}

	// In the native RInChI library the handling of stereo chemistry is inferred from 2D/3D coordinates of the input files.
	// Therefore, any stereo chemistry information that is present in the RInChI string gets lost when converting
	//   (1) from RInChI to a CDK Reaction and then
	//   (2) from the CDK Reaction to RInChI
	// IF there is NO RAuxInfo for the RInchI that includes the 2D/3D coordinates.
	// The following 2 tests show this in code via assertions.
	@Test
	public void testStereochemistryLayer_roundTrippingDoesNotWorkWithoutRauxinfo() throws Exception {
		// this is the RInChI string we use as a starting point
		// it has sterochemistry layers (/b, /t, /s) for the reactant and product
		final String rinchi = "RInChI=1.00.1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4?/m0/s1<>C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1/d+/u0-1-0";
		final String auxInfo = "";
		// this is the RInChI string we end up with after round-tripping (RInChI --> CDK Reaction --> RInChI)
		// both the reactant and the product do not have any the sterochemistry layers (anymore)
		final String rinchiAfterRoundtrip = "RInChI=1.00.1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3<>C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/d+/u0-1-0";

		//RInChI --> Reaction
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(rinchi, auxInfo);
		Assert.assertEquals("RInChI status:", Status.SUCCESS, r2r.getStatus());
		IReaction reaction = r2r.getReaction();
		Assert.assertNotNull(reaction);

		//Reaction --> RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS);
		Assert.assertNotNull(gen);
		Assert.assertEquals("RInChI status:", Status.SUCCESS, gen.getRInChIStatus());

		// PLEASE NOTE: the rinchi after the round-trip conversion is the 'expected' in this assertion
		// as we know that we loose the stereochemistry layers as a result of this conversion
		Assert.assertEquals("RinChI:", rinchiAfterRoundtrip, gen.getRInChI());
	}

	@Test
	public void testStereochemistryLayer_roundTrippingDoesWorkWithRauxinfo() throws Exception {
		doubleConversionTest("RInChI=1.00.1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4?/m0/s1<>C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1/d+/u0-1-0",
				"RAuxInfo=1.00.1/0/N:7,6,5,4,3/E:(1,2)(3,4)/it:im/rA:7cHHOCCCC/rB:;;s1s3;n2s3s4;s4;P5;/rC:-4.2164,7.2658,0;-6.0091,7.6104,0;-5.6247," +
						"6.6824,0;-4.7997,6.6824,0;-5.2122,7.3969,0;-4.0853,6.2699,0;-5.2122,8.2219,0;<>0/N:8,7,6,5,4,3/it:im/rA:8cHHOBrCCCC/rB:;;;p1s3;n2s4s5;s5;s6;" +
						"/rC:5.4979,6.9923,0;4.2604,7.7068,0;3.8479,6.9923,0;5.9104,7.7068,0;4.6729,6.9923,0;5.0854,7.7068,0;5.0854,6.2778,0;4.6729,8.4212,0;",
				true, true);
		doubleConversionTest("RInChI=1.00.1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4?/m0/s1<>C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1/d+/u0-1-0",
				"RAuxInfo=1.00.1/0/N:7,6,5,4,3/E:(1,2)(3,4)/it:im/rA:7cHHOCCCC/rB:;;s1s3;n2s3s4;s4;P5;/rC:-4.2164,7.2658,0;-6.0091,7.6104,0;-5.6247," +
						"6.6824,0;-4.7997,6.6824,0;-5.2122,7.3969,0;-4.0853,6.2699,0;-5.2122,8.2219,0;<>0/N:8,7,6,5,4,3/it:im/rA:8cHHOBrCCCC/rB:;;;p1s3;n2s4s5;s5;s6;" +
						"/rC:5.4979,6.9923,0;4.2604,7.7068,0;3.8479,6.9923,0;5.9104,7.7068,0;4.6729,6.9923,0;5.0854,7.7068,0;5.0854,6.2778,0;4.6729,8.4212,0;",
				false, false);
		doubleConversionTest("RInChI=1.00.1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4?/m0/s1<>C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1/d+/u0-1-0",
				"RAuxInfo=1.00.1/0/N:7,6,5,4,3/E:(1,2)(3,4)/it:im/rA:7cHHOCCCC/rB:;;s1s3;n2s3s4;s4;P5;/rC:-4.2164,7.2658,0;-6.0091,7.6104,0;-5.6247," +
						"6.6824,0;-4.7997,6.6824,0;-5.2122,7.3969,0;-4.0853,6.2699,0;-5.2122,8.2219,0;<>0/N:8,7,6,5,4,3/it:im/rA:8cHHOBrCCCC/rB:;;;p1s3;n2s4s5;s5;s6;" +
						"/rC:5.4979,6.9923,0;4.2604,7.7068,0;3.8479,6.9923,0;5.9104,7.7068,0;4.6729,6.9923,0;5.0854,7.7068,0;5.0854,6.2778,0;4.6729,8.4212,0;",
				false, true);
		doubleConversionTest("RInChI=1.00.1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4?/m0/s1<>C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1/d+/u0-1-0",
				"RAuxInfo=1.00.1/0/N:7,6,5,4,3/E:(1,2)(3,4)/it:im/rA:7cHHOCCCC/rB:;;s1s3;n2s3s4;s4;P5;/rC:-4.2164,7.2658,0;-6.0091,7.6104,0;-5.6247," +
						"6.6824,0;-4.7997,6.6824,0;-5.2122,7.3969,0;-4.0853,6.2699,0;-5.2122,8.2219,0;<>0/N:8,7,6,5,4,3/it:im/rA:8cHHOBrCCCC/rB:;;;p1s3;n2s4s5;s5;s6;" +
						"/rC:5.4979,6.9923,0;4.2604,7.7068,0;3.8479,6.9923,0;5.9104,7.7068,0;4.6729,6.9923,0;5.0854,7.7068,0;5.0854,6.2778,0;4.6729,8.4212,0;",
				true, false);
	}
}
