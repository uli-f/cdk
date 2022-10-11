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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IDoubleBondStereochemistry;
import org.openscience.cdk.interfaces.IDoubleBondStereochemistry.Conformation;
import org.openscience.cdk.io.MDLRXNV2000Reader;
import org.openscience.cdk.io.IChemObjectReader.Mode;
import org.openscience.cdk.io.MDLV2000Writer.SPIN_MULTIPLICITY;
import org.openscience.cdk.test.CDKTestCase;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

import io.github.dan2097.jnarinchi.JnaRinchi;
import io.github.dan2097.jnarinchi.RinchiOptions;
import io.github.dan2097.jnarinchi.RinchiOutput;
import io.github.dan2097.jnarinchi.RinchiStatus;

public class RInChIGeneratorTest extends CDKTestCase {
	private static final ILoggingTool logger = LoggingToolFactory.createLoggingTool(RInChIGeneratorTest.class);
	
	public static Map<String, String> readRinchiFullInfoFromResourceFile(String fileName) throws IOException {
		InputStream is = RInChIGeneratorTest.class.getResourceAsStream(fileName);
		Properties props = new Properties();
		props.load(is);
		is.close();

		Map<String, String> rfi = new HashMap<>();
		if (props.containsKey("RInChI")) {
			rfi.put("RInChI", "RInChI=" + props.getProperty("RInChI"));
		}
		if (props.containsKey("RAuxInfo")) {
			rfi.put("RAuxInfo", "RAuxInfo=" + props.getProperty("RAuxInfo"));
		}
		if (props.containsKey("Long-RInChIKey")) {
			rfi.put("Long-RInChIKey", "Long-RInChIKey=" + props.getProperty("Long-RInChIKey"));
		}
		if (props.containsKey("Short-RInChIKey")) {
			rfi.put("Short-RInChIKey", "Short-RInChIKey=" + props.getProperty("Short-RInChIKey"));
		}
		if (props.containsKey("Web-RInChIKey")) {
			rfi.put("Web-RInChIKey", "Web-RInChIKey=" + props.getProperty("Web-RInChIKey"));
		}

		return rfi;
	}
	
	public static IReaction readReactionFromResourceRXNFile(String fileName) throws Exception {
		InputStream ins =  RInChIGeneratorTest.class.getResourceAsStream(fileName);
		MDLRXNV2000Reader reader = new MDLRXNV2000Reader(ins, Mode.STRICT);
		IReaction reaction = new Reaction();
		reaction = reader.read(reaction);
		reader.close();
		return reaction;
	}
	
	public static String readFileTextFromResourceFile(String fileName) {
		StringBuilder sb = new StringBuilder();
		try (InputStream is = RInChIGeneratorTest.class.getResourceAsStream(fileName);
				BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			return sb.toString();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public void genericExampleTest(String reactionFile, String rinchiFile, boolean useCDK_MDL_IO) throws Exception {
		 logger.info("Testing with: " + reactionFile);
		 IReaction reaction = readReactionFromResourceRXNFile(reactionFile);		 
		 Map<String, String> rfi = readRinchiFullInfoFromResourceFile(rinchiFile);		 
		 RInChIGenerator gen = RInChIGeneratorFactory.getInstance().
				 	getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, useCDK_MDL_IO);
		 Assert.assertNotNull(gen);
		 Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, gen.getRInChIStatus());
		 Assert.assertEquals("RinChI:", rfi.get("RInChI"), gen.getRInChI());

		 if (rfi.containsKey("RAuxInfo"))
		 	Assert.assertEquals("RAuxInfo:", rfi.get("RAuxInfo"), gen.getAuxInfo());

		 if (rfi.containsKey("Long-RInChIKey"))
		 	Assert.assertEquals("Long-RInChIKey:", rfi.get("Long-RInChIKey"), gen.getLongRInChIKey());

		 if (rfi.containsKey("Short-RInChIKey"))
		 	Assert.assertEquals("Short-RInChIKey:", rfi.get("Short-RInChIKey"), gen.getShortRInChIKey());

		 if (rfi.containsKey("Web-RInChIKey"))
		 	Assert.assertEquals("Web-RInChIKey:", rfi.get("Web-RInChIKey"), gen.getWebRInChIKey());
	}
	
	@Test
	public void testExample_Tautomerization_01() throws Exception {
		genericExampleTest("examples/Tautomerization_01.rxn", "examples/Tautomerization_01.txt", false);
		genericExampleTest("examples/Tautomerization_01.rxn", "examples/Tautomerization_01.txt", true);
	}
	
	@Test
	public void testExample_1_reactant__A() throws Exception {
		genericExampleTest("examples/1_reactant_-_A.rxn", "examples/1_reactant_-_A.txt", false);
		genericExampleTest("examples/1_reactant_-_A.rxn", "examples/1_reactant_-_A.txt", true);
	}
	
	@Test
	public void testExample_1_reactant__no_product() throws Exception {
		genericExampleTest("examples/1_reactant_-_no_product.rxn", "examples/1_reactant_-_no_product.txt", false);
		genericExampleTest("examples/1_reactant_-_no_product.rxn", "examples/1_reactant_-_no_product.txt", true);
	}
	
	@Test
	public void testExample_1_reactant__no_structure() throws Exception {
		genericExampleTest("examples/1_reactant_-_no_structure.rxn", "examples/1_reactant_-_no_structure.txt", false);
		genericExampleTest("examples/1_reactant_-_no_structure.rxn", "examples/1_reactant_-_no_structure.txt", true);
	}
	
	@Test
	public void testExample_1_reactant__R() throws Exception {
		genericExampleTest("examples/1_reactant_-_R.rxn", "examples/1_reactant_-_R.txt", false);
		genericExampleTest("examples/1_reactant_-_R.rxn", "examples/1_reactant_-_R.txt", true);
	}

	@Test
	public void testExample_no_reactants_one_product() throws Exception {
		genericExampleTest("examples/no_reactants_one_product.rxn", "examples/no_reactants_one_product.rxn.rinchi_strings.txt", false);
		genericExampleTest("examples/no_reactants_one_product.rxn", "examples/no_reactants_one_product.rxn.rinchi_strings.txt", true);
	}

	@Test
	public void testExample_nostruct_one_in_products() throws Exception {
		// TODO commented out because it fails
//		genericExampleTest("examples/nostruct_one_in_products.rxn", "examples/nostruct_one_in_products.rxn.rinchi_strings.txt", false);
		genericExampleTest("examples/nostruct_one_in_products.rxn", "examples/nostruct_one_in_products.rxn.rinchi_strings.txt", true);
	}

	@Test
	public void testExample_nostruct_one_in_reactants() throws Exception {
		// TODO commented out because it fails
//		genericExampleTest("examples/nostruct_one_in_reactants.rxn", "examples/nostruct_one_in_reactants.rxn.rinchi_strings.txt", false);
		genericExampleTest("examples/nostruct_one_in_reactants.rxn", "examples/nostruct_one_in_reactants.rxn.rinchi_strings.txt", true);
	}

	@Test
	public void testExample_nostruct_two_in_reactants() throws Exception {
		// TODO commented out because it fails
//		genericExampleTest("examples/nostruct_two_in_reactants.rxn", "examples/nostruct_two_in_reactants.rxn.rinchi_strings.txt", false);
		genericExampleTest("examples/nostruct_two_in_reactants.rxn", "examples/nostruct_two_in_reactants.rxn.rinchi_strings.txt", true);
	}

	@Test
	public void testExample_R005a() throws Exception {
		// TODO commented out because it fails
//		genericExampleTest("examples/R005a.rxn", "examples/R005a.rxn.rinchi_strings.txt", false);
		genericExampleTest("examples/R005a.rxn", "examples/R005a.rxn.rinchi_strings.txt", true);
	}

	@Test
	public void testExample_R005a_with_agents() throws Exception {
		// TODO commented out because it fails
//		genericExampleTest("examples/R005a_with_agents.rxn", "examples/R005a_with_agents.rxn.rinchi_strings.txt", false);
		// TODO commented out because it fails
		// this probably fails as agent count is considered an error when using the MDL V2000 reader with Mode.STRICT
		// using Mode.RELAXED might solve that issue
//		genericExampleTest("examples/R005a_with_agents.rxn", "examples/R005a_with_agents.rxn.rinchi_strings.txt", true);
	}

	@Test
	public void testExample_two_reactants_no_products() throws Exception {
		genericExampleTest("examples/two_reactants_no_products.rxn", "examples/two_reactants_no_products.rxn.rinchi_strings.txt", false);
		genericExampleTest("examples/two_reactants_no_products.rxn", "examples/two_reactants_no_products.rxn.rinchi_strings.txt", true);
	}

	@Test
	public void test_ok__nostruct_A_useCDK_MDL_IO_false() throws Exception {
		final String filename = "examples/ok__nostruct-A.rxn";
		final String expected = "RInChI=1.00.1S//d+/u1-1-0";

		IReaction reaction = readReactionFromResourceRXNFile(filename);
		RInChIGenerator rinchiGenerator = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, false);
		Assert.assertNotNull(rinchiGenerator);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, rinchiGenerator.getRInChIStatus());
		Assert.assertEquals("RinChI:", expected, rinchiGenerator.getRInChI());
	}

	@Test
	public void test_ok__nostruct_A_useCDK_MDL_IO_true() throws Exception {
		final String filename = "examples/ok__nostruct-A.rxn";
		final String expected = "RInChI=1.00.1S//d+/u1-1-0";

		IReaction reaction = readReactionFromResourceRXNFile(filename);
		RInChIGenerator rinchiGenerator = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, true);
		Assert.assertNotNull(rinchiGenerator);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, rinchiGenerator.getRInChIStatus());
		Assert.assertEquals("RinChI:", expected, rinchiGenerator.getRInChI());
	}

	@Test
	public void test_ok__R_A_useCDK_MDL_IO_false() throws Exception {
		final String filename = "examples/ok__R-A.rxn";
		final String expected = "RInChI=1.00.1S//d+/u1-1-0";

		IReaction reaction = readReactionFromResourceRXNFile(filename);
		RInChIGenerator rinchiGenerator = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, false);
		Assert.assertNotNull(rinchiGenerator);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, rinchiGenerator.getRInChIStatus());
		Assert.assertEquals("RinChI:", expected, rinchiGenerator.getRInChI());
	}

	@Test
	public void test_ok__R_A_useCDK_MDL_IO_true() throws Exception {
		final String filename = "examples/ok__R-A.rxn";
		final String expected = "RInChI=1.00.1S//d+/u1-1-0";

		IReaction reaction = readReactionFromResourceRXNFile(filename);
		RInChIGenerator rinchiGenerator = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, true);
		Assert.assertNotNull(rinchiGenerator);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, rinchiGenerator.getRInChIStatus());
		Assert.assertEquals("RinChI:", expected, rinchiGenerator.getRInChI());
	}

	@Test
	public void test_err__R_reactant_A_product_useCDK_MDL_IO_false() throws Exception {
		final String filename = "examples/err__R_reactant-A_product.rxn";

		IReaction reaction = readReactionFromResourceRXNFile(filename);
		RInChIGenerator rinchiGenerator = null;
		try {
			rinchiGenerator = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, false);
		} catch (CDKException exception) {
			Assert.assertNull(rinchiGenerator);
			Assert.assertTrue(exception.getMessage().startsWith("RInChI generation problem:"));
			Assert.assertTrue(exception.getMessage().endsWith("rinchi::InChIGeneratorError: Error: no InChI has been created."));
			return;
		}

		Assert.fail("Expected: CDKException; Actual: no CDKException was raised.");
	}

	@Test
	public void test_err__R_reactant_A_product_useCDK_MDL_IO_true() throws Exception {
		final String filename = "examples/err__R_reactant-A_product.rxn";

		IReaction reaction = readReactionFromResourceRXNFile(filename);
		RInChIGenerator rinchiGenerator = null;
		try {
			rinchiGenerator = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, true);
		} catch (CDKException exception) {
			Assert.assertNull(rinchiGenerator);
			Assert.assertTrue(exception.getMessage().startsWith("RInChI generation problem:"));
			Assert.assertTrue(exception.getMessage().endsWith("rinchi::InChIGeneratorError: Error: no InChI has been created."));
			return;
		}

		Assert.fail("Expected: CDKException; Actual: no CDKException was raised.");
	}

	@Test
	public void test_1_variation_4_steps() {
		//Get RInChI from RDFile (using Jna-RInchi on a lower level)
		final String reactionFilename = "examples/1_variation_4_steps.rdf";
		final String expected = "RInChI=1.00.1S/C12H17NOS/c1-3-13(4-2)12(14)15-10-11-8-6-5-7-9-11/h5-9H,3-4,10H2,1-2H3<>C4H11N/c1-3-5-4-2" +
				"/h5H,3-4H2,1-2H3!C7H7Br/c8-6-7-4-2-1-3-5-7/h1-5H,6H2!CO/c1-2!S8/c1-2-4-6-8-7-5-3-1<>2ClH.2H2N.Pt/h2*1H;2*1H2;/q;;2*-1;+4" +
				"/p-2!2ClH.Pd/h2*1H;/q;;+2/p-2!3C2H4O2.Fe/c3*1-2(3)4;/h3*1H3,(H,3,4);/q;;;+3/p-3!C4H10O/c1-3-5-4-2/h3-4H2,1-2H3!C4H8O" +
				"/c1-2-4-5-3-1/h1-4H2!C4H8O/c1-2-4-5-3-1/h1-4H2!C4H8O/c1-2-4-5-3-1/h1-4H2!C4H9.Li/c1-3-4-2;/h1,3-4H2,2H3;!C6H12" +
				"/c1-2-4-6-5-3-1/h1-6H2!C6H14/c1-3-5-6-4-2/h3-6H2,1-2H3!C6H6/c1-2-4-6-5-3-1/h1-6H/d-";
		final String reactionText = readFileTextFromResourceFile(reactionFilename);
		final RinchiOutput rinchiOutput = JnaRinchi.fileTextToRinchi(reactionText);

		Assert.assertEquals("RInChIStatus: ", RinchiStatus.SUCCESS, rinchiOutput.getStatus());
		Assert.assertEquals("RInChI error messages: ", "", rinchiOutput.getErrorMessage());
		Assert.assertEquals("RInChI for " + reactionFilename, expected, rinchiOutput.getRinchi());
	}

	@Test
	public void test_5_variations_1_step_each() {
		//Get RInChI from RDFile (using Jna-RInchi on a lower level)
		final String reactionFilename = "examples/5_variations_1_step_each.rdf";
		final String expected = "RInChI=1.00.1S/C6H10O/c7-6-4-2-1-3-5-6/h1-5H2<>C6H12O/c7-6-4-2-1-3-5-6/h6-7H,1-5H2<>3C18H15P.2ClH.Ru" +
				"/c3*1-4-10-16(11-5-1)19(17-12-6-2-7-13-17)18-14-8-3-9-15-18;;;/h3*1-15H;2*1H;/q;;;;;+2/p-2!C3H8O" +
				"/c1-3(2)4/h3-4H,1-2H3!Na.H2O/h;1H2/q+1;/p-1/d+";
		final String reactionText = readFileTextFromResourceFile(reactionFilename);
		final RinchiOutput rinchiOutput = JnaRinchi.fileTextToRinchi(reactionText);

		Assert.assertEquals("RInChIStatus: ", RinchiStatus.SUCCESS, rinchiOutput.getStatus());
		Assert.assertEquals("RInChI error messages: ", "", rinchiOutput.getErrorMessage());
		Assert.assertEquals("RInChI for " + reactionFilename, expected, rinchiOutput.getRinchi());
	}

	@Test
	public void test_err__star_reactant_product() {
		//Get RInChI from RDFile (using Jna-RInchi on a lower level)
		final String reactionFilename = "examples/err__star_reactant-product.rdf";
		final String expected = "";
		final String reactionText = readFileTextFromResourceFile(reactionFilename);
		final RinchiOutput rinchiOutput = JnaRinchi.fileTextToRinchi(reactionText);

		Assert.assertEquals("RInChIStatus: ", RinchiStatus.ERROR, rinchiOutput.getStatus());
		Assert.assertTrue("RInChI error messages: ", rinchiOutput.getErrorMessage().contains("rinchi::MdlRDfileReaderError: Reading from 'std::istream', line 87,"));
		Assert.assertTrue("RInChI error messages: ", rinchiOutput.getErrorMessage().endsWith("rinchi::InChIGeneratorError: Error: no InChI has been created."));
		Assert.assertEquals("RInChI for " + reactionFilename, expected, rinchiOutput.getRinchi());
	}

	@Test
	public void test_Example_01_CCR() {
		final String reactionFilename = "examples/Example_01_CCR.rdf";
		final String expected = "RInChI=1.00.1S/C18H13NO5S2/c1-12-2-4-14(5-3-12)26(21,22)19-13-6-9-18(10-7-13)23-15-8-11-25-16(15)" +
				"17(20)24-18/h2-11H,1H3/b19-13-<>C18H15NO5S2/c1-12-2-8-15(9-3-12)26(22,23)19-13-4-6-14(7-5-13)24-16-10-11-25-17(16)" +
				"18(20)21/h2-11,19H,1H3,(H,20,21)<>C2H3N/c1-2-3/h1H3!C8H20N.ClHO4/c1-5-9(6-2,7-3)8-4;2-1(3,4)5/h5-8H2,1-4H3;(H,2,3,4,5)/" +
				"q+1;/p-1/d-";
		final String reactionText = readFileTextFromResourceFile(reactionFilename);
		final RinchiOutput rinchiOutput = JnaRinchi.fileTextToRinchi(reactionText);

		Assert.assertEquals("RInChIStatus: ", RinchiStatus.SUCCESS, rinchiOutput.getStatus());
		Assert.assertEquals("RInChI error messages: ", "", rinchiOutput.getErrorMessage());
		Assert.assertEquals("RInChI for " + reactionFilename, expected, rinchiOutput.getRinchi());
	}

	@Test
	public void test_Example_03_metab_UDM() {
		final String reactionFilename = "examples/Example_03_metab_UDM.rdf";
		final String expected = "RInChI=1.00.1S/C7H13BrN2O2/c1-3-7(8,4-2)5(11)10-6(9)12/h3-4H2,1-2H3,(H3,9,10,11,12)<>" +
				"C7H14N2O2/c1-3-5(4-2)6(10)9-7(8)11/h5H,3-4H2,1-2H3,(H3,8,9,10,11)<>2H3O4P/c2*1-5(2,3)4/h2*(H3,1,2,3,4)" +
				"/p-3!C9H15BrN2O3/c1-4-9(10,5-2)7(14)12-8(15)11-6(3)13/h4-5H2,1-3H3,(H2,11,12,13,14,15)!H2O/h1H2!Na.H/d+";
		final String reactionText = readFileTextFromResourceFile(reactionFilename);
		final RinchiOutput rinchiOutput = JnaRinchi.fileTextToRinchi(reactionText);

		Assert.assertEquals("RInChIStatus: ", RinchiStatus.SUCCESS, rinchiOutput.getStatus());
		Assert.assertEquals("RInChI error messages: ", "", rinchiOutput.getErrorMessage());
		Assert.assertEquals("RInChI for " + reactionFilename, expected, rinchiOutput.getRinchi());
	}

	@Test
	public void test_Example_04_simple() {
		final String reactionFilename = "examples/Example_04_simple.rdf";
		final String expected = "RInChI=1.00.1S/C24H34O2Si/c1-6-21(18-25)17-20(2)19-26-27(24(3,4)5,22-13-9-7-10-14-22)23-15-11-8-12-16-23" +
				"/h7-16,18,20-21H,6,17,19H2,1-5H3/t20-,21-/m1/s1<>C24H36O2Si/c1-6-21(18-25)17-20(2)19-26-27(24(3,4)5,22-13-9-7-10-14-22)" +
				"23-15-11-8-12-16-23/h7-16,20-21,25H,6,17-19H2,1-5H3/t20-,21-/m1/s1/d-";
		final String reactionText = readFileTextFromResourceFile(reactionFilename);
		final RinchiOutput rinchiOutput = JnaRinchi.fileTextToRinchi(reactionText);

		Assert.assertEquals("RInChIStatus: ", RinchiStatus.SUCCESS, rinchiOutput.getStatus());
		Assert.assertEquals("RInChI error messages: ", "", rinchiOutput.getErrorMessage());
		Assert.assertEquals("RInChI for " + reactionFilename, expected, rinchiOutput.getRinchi());
	}

	@Test
	public void test_Example_05_groups_UDM() {
		final String reactionFilename = "examples/Example_05_groups_UDM.rdf";
		final String expected = "RInChI=1.00.1S/C28H48O4Si/c1-19(2)33(20(3)4,21(5)6)32-28(24(9)27(30)25(10)29)23(8)16-22(7)17-31-18-26-14-12-11-13-15-26" +
				"/h11-16,19-22,24-25,28-29H,17-18H2,1-10H3/b23-16+/t22-,24+,25+,28+/m1/s1<>C34H62O4Si2" +
				"/c1-24(2)40(25(3)4,26(5)6)38-33(29(9)32(35)30(10)37-39(14,15)34(11,12)13)28(8)21-27(7)22-36-23-31-19-17-16-18-20-31" +
				"/h16-21,24-27,29-30,33H,22-23H2,1-15H3/b28-21+/t27-,29+,30+,33+/m1/s1<>C10H16O4S/c1-9(2)7-3-4-10(9,8(11)5-7)6-15(12,13)14" +
				"/h7H,3-6H2,1-2H3,(H,12,13,14)!CH2Cl2/c2-1-3/h1H2!CH4O/c1-2/h2H,1H3!CH4O/c1-2/h2H,1H3/d-";
		final String reactionText = readFileTextFromResourceFile(reactionFilename);
		final RinchiOutput rinchiOutput = JnaRinchi.fileTextToRinchi(reactionText);

		Assert.assertEquals("RInChIStatus: ", RinchiStatus.SUCCESS, rinchiOutput.getStatus());
		Assert.assertEquals("RInChI error messages: ", "", rinchiOutput.getErrorMessage());
		Assert.assertEquals("RInChI for " + reactionFilename, expected, rinchiOutput.getRinchi());
	}

	@Test
	public void test_ok__nostruct_X() {
		final String reactionFilename = "examples/ok__nostruct-X.rdf";
		final String expected = "RInChI=1.00.1S/<><>C4H8O/c1-2-4-5-3-1/h1-4H2!Mn.2O/d+/u1-1-0";
		final String reactionText = readFileTextFromResourceFile(reactionFilename);
		final RinchiOutput rinchiOutput = JnaRinchi.fileTextToRinchi(reactionText);

		Assert.assertEquals("RInChIStatus: ", RinchiStatus.SUCCESS, rinchiOutput.getStatus());
		Assert.assertEquals("RInChI error messages: ", "", rinchiOutput.getErrorMessage());
		Assert.assertEquals("RInChI for " + reactionFilename, expected, rinchiOutput.getRinchi());
	}

	@Test
	public void test_ok__R_X() {
		final String reactionFilename = "examples/ok__R-X.rdf";
		final String expected = "RInChI=1.00.1S/<><>C2H6O/c1-2-3/h3H,2H2,1H3!Cu/d+/u1-1-0";
		final String reactionText = readFileTextFromResourceFile(reactionFilename);
		final RinchiOutput rinchiOutput = JnaRinchi.fileTextToRinchi(reactionText);

		Assert.assertEquals("RInChIStatus: ", RinchiStatus.SUCCESS, rinchiOutput.getStatus());
		Assert.assertEquals("RInChI error messages: ", "", rinchiOutput.getErrorMessage());
		Assert.assertEquals("RInChI for " + reactionFilename, expected, rinchiOutput.getRinchi());
	}

	@Test
	public void test_ok__star_star_nostruct() {
		final String reactionFilename = "examples/ok__star_star-nostruct.rdf";
		final String expected = "RInChI=1.00.1S/<><>Cu.O/d+/u2-1-0";
		final String reactionText = readFileTextFromResourceFile(reactionFilename);
		final RinchiOutput rinchiOutput = JnaRinchi.fileTextToRinchi(reactionText);

		Assert.assertEquals("RInChIStatus: ", RinchiStatus.SUCCESS, rinchiOutput.getStatus());
		Assert.assertEquals("RInChI error messages: ", "", rinchiOutput.getErrorMessage());
		Assert.assertEquals("RInChI for " + reactionFilename, expected, rinchiOutput.getRinchi());
	}

	@Test
	public void testStereoDoubleBond01() throws Exception {
		//CC#CC>>C/C=C\C
		IReaction reaction = readReactionFromResourceRXNFile("reaction-data/Lindlar_hydrogenation.rxn");
		//Reaction --> RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().
				getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS);
		Assert.assertNotNull(gen);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, gen.getRInChIStatus());
		//RInChI --> Reaction
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(gen.getRInChI(), gen.getAuxInfo());
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, r2r.getStatus());
		IReaction reaction2 = r2r.getReaction();
		Assert.assertNotNull(reaction2);
		//Check double bond stereo
		IAtomContainer prod = reaction2.getProducts().getAtomContainer(0);
		Assert.assertNotNull(prod.stereoElements());
		for (IStereoElement<?,?> se : prod.stereoElements()) {
			Assert.assertTrue("Instance of IDoubleBondStereochemistry: ", (se instanceof IDoubleBondStereochemistry));
			IDoubleBondStereochemistry dbse = (IDoubleBondStereochemistry)se;
			Assert.assertEquals("DoubleBondStereochemistry comformation: ", Conformation.TOGETHER, dbse.getStereo());
			break; //only one stereo element is expected
		}
	}
	
	@Test
	public void testStereoDoubleBond01_B() throws Exception {
		//CC#CC>>C/C=C\C
		IReaction reaction = readReactionFromResourceRXNFile("reaction-data/Lindlar_hydrogenation.rxn");
		//Testing with option: useCDK_MDL_IO = true
		//Reaction --> RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().
				getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, true);
		Assert.assertNotNull(gen);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, gen.getRInChIStatus());
		//RInChI --> Reaction
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(gen.getRInChI(), gen.getAuxInfo(), true);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, r2r.getStatus());
		IReaction reaction2 = r2r.getReaction();
		Assert.assertNotNull(reaction2);
		//Check double bond stereo
		IAtomContainer prod = reaction2.getProducts().getAtomContainer(0);
		Assert.assertNotNull(prod.stereoElements());
		for (IStereoElement<?,?> se : prod.stereoElements()) {
			Assert.assertTrue("Instance of IDoubleBondStereochemistry: ", (se instanceof IDoubleBondStereochemistry));
			IDoubleBondStereochemistry dbse = (IDoubleBondStereochemistry)se;
			Assert.assertEquals("DoubleBondStereochemistry comformation: ", Conformation.TOGETHER, dbse.getStereo());
			break; //only one stereo element is expected
		}
	}
	
	@Test
	public void testStereoDoubleBond02() throws Exception {
		//C/C=C\C>>C/C=C/C
		IReaction reaction = readReactionFromResourceRXNFile("reaction-data/Cis_trans_isomerization.rxn");
		//Reaction --> RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().
				getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS);
		Assert.assertNotNull(gen);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, gen.getRInChIStatus());
		//RInChI --> Reaction
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(gen.getRInChI(), gen.getAuxInfo());
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, r2r.getStatus());
		IReaction reaction2 = r2r.getReaction();
		Assert.assertNotNull(reaction2);
		//Check double stereo bond stereo
		IAtomContainer reagent = reaction2.getReactants().getAtomContainer(0);
		Assert.assertNotNull(reagent.stereoElements());
		for (IStereoElement<?,?> se : reagent.stereoElements()) {
			Assert.assertTrue("Instance of IDoubleBondStereochemistry: ", (se instanceof IDoubleBondStereochemistry));
			IDoubleBondStereochemistry dbse = (IDoubleBondStereochemistry)se;
			Assert.assertEquals("DoubleBondStereochemistry comformation: ", Conformation.TOGETHER, dbse.getStereo());
			break; //only one stereo element is expected
		}
		IAtomContainer prod = reaction2.getProducts().getAtomContainer(0);
		Assert.assertNotNull(prod.stereoElements());
		for (IStereoElement<?,?> se : prod.stereoElements()) {
			Assert.assertTrue("Instance of IDoubleBondStereochemistry: ", (se instanceof IDoubleBondStereochemistry));
			IDoubleBondStereochemistry dbse = (IDoubleBondStereochemistry)se;
			Assert.assertEquals("DoubleBondStereochemistry comformation: ", Conformation.OPPOSITE, dbse.getStereo());
			break; //only one stereo element is expected
		}
	}
	
	@Test
	public void testStereoDoubleBond02_B() throws Exception {
		//C/C=C\C>>C/C=C/C
		IReaction reaction = readReactionFromResourceRXNFile("reaction-data/Cis_trans_isomerization.rxn");
		//Testing with option: useCDK_MDL_IO = true
		//Reaction --> RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().
				getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, true);
		Assert.assertNotNull(gen);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, gen.getRInChIStatus());
		//RInChI --> Reaction
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(gen.getRInChI(), gen.getAuxInfo(), true);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, r2r.getStatus());
		IReaction reaction2 = r2r.getReaction();
		Assert.assertNotNull(reaction2);
		//Check double stereo bond stereo
		IAtomContainer reagent = reaction2.getReactants().getAtomContainer(0);
		Assert.assertNotNull(reagent.stereoElements());
		for (IStereoElement<?,?> se : reagent.stereoElements()) {
			Assert.assertTrue("Instance of IDoubleBondStereochemistry: ", (se instanceof IDoubleBondStereochemistry));
			IDoubleBondStereochemistry dbse = (IDoubleBondStereochemistry)se;
			Assert.assertEquals("DoubleBondStereochemistry comformation: ", Conformation.TOGETHER, dbse.getStereo());
			break; //only one stereo element is expected
		}
		IAtomContainer prod = reaction2.getProducts().getAtomContainer(0);
		Assert.assertNotNull(prod.stereoElements());
		for (IStereoElement<?,?> se : prod.stereoElements()) {
			Assert.assertTrue("Instance of IDoubleBondStereochemistry: ", (se instanceof IDoubleBondStereochemistry));
			IDoubleBondStereochemistry dbse = (IDoubleBondStereochemistry)se;
			Assert.assertEquals("DoubleBondStereochemistry comformation: ", Conformation.OPPOSITE, dbse.getStereo());
			break; //only one stereo element is expected
		}
	}
	
	@Test
	public void testStereoDoubleBond_with_Agents() throws Exception {
		//CC#CC > [Li].[NH3] > C/C=C\C		
		//Get RInChI from RDFile (using Jna-RInchi on a lower level)
		String reactText = readFileTextFromResourceFile("reaction-data/Birch_reduction.rdf");		
		RinchiOutput rinchiOut = JnaRinchi.fileTextToRinchi(reactText);		
		//RInChI --> Reaction
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(rinchiOut.getRinchi(), rinchiOut.getAuxInfo());
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, r2r.getStatus());
		IReaction reaction = r2r.getReaction();
		Assert.assertNotNull(reaction);
		//Check reaction components count
		Assert.assertEquals("Number of reactants:", 1, reaction.getReactantCount());
		Assert.assertEquals("Number of products:", 1, reaction.getProductCount());
		Assert.assertEquals("Number of agents:", 2, reaction.getAgents().getAtomContainerCount());
		//Check agents:
		Assert.assertEquals("Agent 1 atom:", "N", reaction.getAgents().getAtomContainer(0).getAtom(0).getSymbol());
		Assert.assertEquals("Agent 1 implicit H atoms:", Integer.valueOf(3),
					reaction.getAgents().getAtomContainer(0).getAtom(0).getImplicitHydrogenCount());
		Assert.assertEquals("Agent 2 atom:", "Li", reaction.getAgents().getAtomContainer(1).getAtom(0).getSymbol());
		//Check double bond stereo
		IAtomContainer prod = reaction.getProducts().getAtomContainer(0);
		Assert.assertNotNull(prod.stereoElements());
		for (IStereoElement<?,?> se : prod.stereoElements()) {
			Assert.assertTrue("Instance of IDoubleBondStereochemistry: ",(se instanceof IDoubleBondStereochemistry));
			IDoubleBondStereochemistry dbse = (IDoubleBondStereochemistry)se;
			Assert.assertEquals("DoubleBondStereochemistry comformation: ", Conformation.OPPOSITE, dbse.getStereo());
			break; //only one stereo element is expected
		}
	}
	
	@Test
	public void testRadicalReaction01() throws Exception {
		//Radical chain reaction of halogenation 
		//[CH2-radical](C)(C)C + Br-Br --> Br-radical + BrC(C)(C)C
		
		IReaction reaction = readReactionFromResourceRXNFile("reaction-data/Radical_reaction.rxn");
		//Reaction --> RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().
				getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS);
		Assert.assertNotNull(gen);
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, gen.getRInChIStatus());
		
		//RInChI --> Reaction
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(gen.getRInChI(), gen.getAuxInfo());
		Assert.assertEquals("RInChI status:",RinchiStatus.SUCCESS, r2r.getStatus());
		IReaction reaction2 = r2r.getReaction();
		Assert.assertNotNull(reaction2);
		
		//Check reactant radicals
		int reactantIndex = -1;
		for (int i = 0; i < reaction.getProducts().getAtomContainerCount(); i++)
			if (reaction.getReactants().getAtomContainer(i).getAtomCount() == 4) //[CH2](C)(C)C
				reactantIndex = i;		
		IAtomContainer reactant = reaction.getReactants().getAtomContainer(reactantIndex);
		int nReactantRadicals = 0;
		for (IAtom a: reactant.atoms()) {
			SPIN_MULTIPLICITY mult = a.getProperty(CDKConstants.SPIN_MULTIPLICITY);
			if (mult != null) 
				if (mult == SPIN_MULTIPLICITY.Monovalent)
					nReactantRadicals++;
		}
		Assert.assertEquals("nReactantRadicals:", 1, nReactantRadicals);
		//Check product radicals
		int productIndex = -1;
		for (int i = 0; i < reaction.getProducts().getAtomContainerCount(); i++)
			if (reaction.getProducts().getAtomContainer(i).getAtomCount() == 1) // [Br]
				productIndex = i;
		IAtomContainer product = reaction.getProducts().getAtomContainer(productIndex);
		int nProductRadicals = 0;
		for (IAtom a: product.atoms()) {
			SPIN_MULTIPLICITY mult = a.getProperty(CDKConstants.SPIN_MULTIPLICITY);
			if (mult != null) 
				if (mult == SPIN_MULTIPLICITY.Monovalent)
					nProductRadicals++;
		}
		Assert.assertEquals("ProductRadicals:", 1, nProductRadicals);
	}
	
}
