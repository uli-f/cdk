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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IDoubleBondStereochemistry;
import org.openscience.cdk.interfaces.IDoubleBondStereochemistry.Conformation;
import org.openscience.cdk.io.MDLRXNV2000Reader;
import org.openscience.cdk.io.IChemObjectReader.Mode;
import org.openscience.cdk.test.CDKTestCase;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

import io.github.dan2097.jnarinchi.JnaRinchi;
import io.github.dan2097.jnarinchi.RinchiOptions;
import io.github.dan2097.jnarinchi.RinchiOutput;
import io.github.dan2097.jnarinchi.RinchiStatus;



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
				rfi.put("RInChI", "RInChI=" + s);			
			s = props.getProperty("RAuxInfo");
			if (s != null)
				rfi.put("RAuxInfo", "RAuxInfo=" + s);
			s = props.getProperty("Long-RInChIKey");
			if (s != null)
				rfi.put("Long-RInChIKey", "Long-RInChIKey=" + s);			
			s = props.getProperty("Short-RInChIKey");
			if (s != null)
				rfi.put("Short-RInChIKey", "Short-RInChIKey=" + s);
			s = props.getProperty("Web-RInChIKey");
			if (s != null)
				rfi.put("Web-RInChIKey", "Web-RInChIKey=" + s);
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
		 Assert.assertEquals("RAuxInfo:", rfi.get("RAuxInfo"), gen.getAuxInfo());
		 Assert.assertEquals("Long-RInChIKey:", rfi.get("Long-RInChIKey"), gen.getLongRInChIKey());
		 Assert.assertEquals("Short-RInChIKey:", rfi.get("Short-RInChIKey"), gen.getShortRInChIKey());
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
			Assert.assertEquals("Instance of IDoubleBondStereochemistry: ", true, (se instanceof IDoubleBondStereochemistry));
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
			Assert.assertEquals("Instance of IDoubleBondStereochemistry: ", true, (se instanceof IDoubleBondStereochemistry));
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
			Assert.assertEquals("Instance of IDoubleBondStereochemistry: ", true, (se instanceof IDoubleBondStereochemistry));
			IDoubleBondStereochemistry dbse = (IDoubleBondStereochemistry)se;
			Assert.assertEquals("DoubleBondStereochemistry comformation: ", Conformation.TOGETHER, dbse.getStereo());
			break; //only one stereo element is expected
		}
		IAtomContainer prod = reaction2.getProducts().getAtomContainer(0);
		Assert.assertNotNull(prod.stereoElements());
		for (IStereoElement<?,?> se : prod.stereoElements()) {
			Assert.assertEquals("Instance of IDoubleBondStereochemistry: ", true, (se instanceof IDoubleBondStereochemistry));
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
			Assert.assertEquals("Instance of IDoubleBondStereochemistry: ", true, (se instanceof IDoubleBondStereochemistry));
			IDoubleBondStereochemistry dbse = (IDoubleBondStereochemistry)se;
			Assert.assertEquals("DoubleBondStereochemistry comformation: ", Conformation.TOGETHER, dbse.getStereo());
			break; //only one stereo element is expected
		}
		IAtomContainer prod = reaction2.getProducts().getAtomContainer(0);
		Assert.assertNotNull(prod.stereoElements());
		for (IStereoElement<?,?> se : prod.stereoElements()) {
			Assert.assertEquals("Instance of IDoubleBondStereochemistry: ", true, (se instanceof IDoubleBondStereochemistry));
			IDoubleBondStereochemistry dbse = (IDoubleBondStereochemistry)se;
			Assert.assertEquals("DoubleBondStereochemistry comformation: ", Conformation.OPPOSITE, dbse.getStereo());
			break; //only one stereo element is expected
		}
	}
	
	@Test
	public void testStereoDoubleBond03() throws Exception {
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
		Assert.assertEquals("Agent 1 implicit H atoms:", new Integer(3), 
					reaction.getAgents().getAtomContainer(0).getAtom(0).getImplicitHydrogenCount());
		Assert.assertEquals("Agent 2 atom:", "Li", reaction.getAgents().getAtomContainer(1).getAtom(0).getSymbol());
		//Check double bond stereo
		IAtomContainer prod = reaction.getProducts().getAtomContainer(0);
		Assert.assertNotNull(prod.stereoElements());
		for (IStereoElement<?,?> se : prod.stereoElements()) {
			Assert.assertEquals("Instance of IDoubleBondStereochemistry: ", true, (se instanceof IDoubleBondStereochemistry));
			IDoubleBondStereochemistry dbse = (IDoubleBondStereochemistry)se;
			Assert.assertEquals("DoubleBondStereochemistry comformation: ", Conformation.OPPOSITE, dbse.getStereo());
			break; //only one stereo element is expected
		}
	}
	
}
