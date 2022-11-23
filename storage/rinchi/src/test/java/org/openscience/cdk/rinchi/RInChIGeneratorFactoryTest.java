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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.io.MDLV2000Writer.SPIN_MULTIPLICITY;
import org.openscience.cdk.test.CDKTestCase;

import io.github.dan2097.jnarinchi.RinchiOptions;
import io.github.dan2097.jnarinchi.Status;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class RInChIGeneratorFactoryTest extends CDKTestCase {

	@Test
	public void testGetInstance() throws CDKException {
		RInChIGeneratorFactory factory = RInChIGeneratorFactory.getInstance();
		assertNotNull(factory);
	}

	@Test
	public void testGetInstance_multipleCalls_sameInstance() throws CDKException {
		RInChIGeneratorFactory factory1 = RInChIGeneratorFactory.getInstance();
		assertNotNull(factory1);

		RInChIGeneratorFactory factory2 = RInChIGeneratorFactory.getInstance();
		assertNotNull(factory2);

		RInChIGeneratorFactory factory3 = RInChIGeneratorFactory.getInstance();
		assertNotNull(factory3);

		assertSame(factory1, factory2, "Asserting that getInstance returns the same instance with every call");
		assertSame(factory2, factory3, "Asserting that getInstance returns the same instance with every call");
	}

	@Test
	public void testGetInstance_threadSafety() throws InterruptedException, CDKException {
		RInChIGeneratorFactory singletonInstance = RInChIGeneratorFactory.getInstance();

		int numberOfMethodCalls = 10000;
		ConcurrentLinkedQueue<RInChIGeneratorFactory> factoryInstancesQueue = new ConcurrentLinkedQueue<>();
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfMethodCalls / 10);

		for (int i = 0; i < numberOfMethodCalls; i++) {
			executorService.execute(() -> {				
					RInChIGeneratorFactory factory = RInChIGeneratorFactory.getInstance();
					factoryInstancesQueue.add(factory);
			});
		}

		executorService.shutdown();
		assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));

		Assertions.assertEquals(numberOfMethodCalls, factoryInstancesQueue.size());
		for (RInChIGeneratorFactory factory: factoryInstancesQueue) {
			assertSame(singletonInstance, factory);
		}
	}
	
	@Test
	public void test01() throws CDKException {
		//Create Diels–Alder Reaction using CDK
		//Reactant 1
		IAtomContainer mol1 = new AtomContainer();
		IAtom a1 = new Atom("C");
		IAtom a2 = new Atom("C");
		mol1.addAtom(a1);
		mol1.addAtom(a2);
		mol1.addBond(new Bond(a1, a2, IBond.Order.DOUBLE));
		//Reactant 2
		IAtomContainer mol2 = new AtomContainer();
		IAtom a3 = new Atom("C");
		IAtom a4 = new Atom("C");
		IAtom a5 = new Atom("C");
		IAtom a6 = new Atom("C");
		mol2.addAtom(a3);
		mol2.addAtom(a4);
		mol2.addAtom(a5);
		mol2.addAtom(a6);		
		mol2.addBond(new Bond (a3, a4, IBond.Order.DOUBLE));
		mol2.addBond(new Bond (a4, a5, IBond.Order.SINGLE));
		mol2.addBond(new Bond (a5, a6, IBond.Order.DOUBLE));
		//Product
		IAtomContainer mol3 = new AtomContainer();
		IAtom pa1 = new Atom("C");
		IAtom pa2 = new Atom("C");
		IAtom pa3 = new Atom("C");
		IAtom pa4 = new Atom("C");
		IAtom pa5 = new Atom("C");
		IAtom pa6 = new Atom("C");
		mol3.addAtom(pa1);
		mol3.addAtom(pa2);
		mol3.addAtom(pa3);
		mol3.addAtom(pa4);
		mol3.addAtom(pa5);
		mol3.addAtom(pa6);
		mol3.addBond(new Bond (pa1, pa2, IBond.Order.DOUBLE));
		mol3.addBond(new Bond (pa2, pa3, IBond.Order.SINGLE));
		mol3.addBond(new Bond (pa3, pa4, IBond.Order.SINGLE));
		mol3.addBond(new Bond (pa2, pa3, IBond.Order.SINGLE));
		mol3.addBond(new Bond (pa2, pa3, IBond.Order.SINGLE));
		mol3.addBond(new Bond (pa2, pa3, IBond.Order.SINGLE));
		
		//Create reaction and set reagents and products
		IReaction reaction = new Reaction();
		reaction.addReactant(mol1);
		reaction.addReactant(mol2);
		reaction.addProduct(mol3);
		
		//Generate RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction);
		Assertions.assertEquals(Status.SUCCESS, gen.getRInChIStatus(), "RInChI status: ");		
		Assertions.assertTrue(gen.getRInChI().endsWith("/d+"), "Forward reaction RInChI: ");
		
		//Generate RInChI with option ForceEquilibrium		
		RInChIGenerator gen_eq = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction, "ForceEquilibrium");
		Assertions.assertTrue(gen_eq.getRInChI().endsWith("/d="), "Equilibrium reaction RInChI: ");
				
		//Create reverse reaction and generate RInChI
		IReaction reaction2 = new Reaction();
		reaction2.addReactant(mol3);		
		reaction2.addProduct(mol1);
		reaction2.addProduct(mol2);
		RInChIGenerator gen2 = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction2);
		Assertions.assertTrue(gen2.getRInChI().endsWith("/d-"), "Backward reaction RInChI: ");
		
		//Backward, forward and equilibrium RInChIs differ only in their last char
		int n = gen.getRInChI().length();
		Assertions.assertEquals(gen.getRInChI().substring(0, n-1), gen2.getRInChI().substring(0, n-1), 
				"Forward and backward RInChI comparison: ");
		Assertions.assertEquals(gen.getRInChI().substring(0, n-1), gen_eq.getRInChI().substring(0, n-1), 
				"Forward and equilibrium RInChI comparison: ");
		
		//Backward, forward and equilibrium RInChIs-Keys differ only in their 19-th char
		Assertions.assertEquals(gen.getLongRInChIKey().substring(0, 18), gen2.getLongRInChIKey().substring(0, 18), 
				"Forward and backward Long-RInChI-Key comparison: ");
		Assertions.assertEquals(gen.getLongRInChIKey().substring(19), gen2.getLongRInChIKey().substring(19), 
				"Forward and backward Long-RInChI-Key comparison: ");
		Assertions.assertEquals('F', gen.getLongRInChIKey().charAt(18), "Reaction direction char in Long-RInChI-Key");
		Assertions.assertEquals('E', gen_eq.getLongRInChIKey().charAt(18), "Reaction direction char in Long-RInChI-Key");
		Assertions.assertEquals('B', gen2.getLongRInChIKey().charAt(18), "Reaction direction char in Long-RInChI-Key");
		
		//Generate back a IReaction object from RInChI
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(gen.getRInChI());
		Assertions.assertEquals(Status.SUCCESS, r2r.getStatus(), "RInChI status: ");
		IReaction reaction3 = r2r.getReaction();
		Assertions.assertEquals(2, reaction3.getReactantCount(), "Reactant count: ");
		Assertions.assertEquals(1, reaction3.getProductCount(), "Product count: ");
		Assertions.assertEquals(2, reaction3.getReactants().getAtomContainer(0).getAtomCount(), "Reactant 1 atom count: ");
		Assertions.assertEquals(4, reaction3.getReactants().getAtomContainer(1).getAtomCount(), "Reactant 2 atom count: ");
		Assertions.assertEquals(6, reaction3.getProducts().getAtomContainer(0).getAtomCount(), "Product atom count: ");
	}
	
	@Test
	public void test02_benzene_kekulized() throws CDKException {
		//Create kekulized benzene
		IAtomContainer mol = new AtomContainer();
		IAtom a0 = new Atom("C");
		a0.setImplicitHydrogenCount(1);
		a0.setIsAromatic(true);
		mol.addAtom(a0);
		IAtom a1 = new Atom("C");
		a1.setImplicitHydrogenCount(1);
		a1.setIsAromatic(true);
		mol.addAtom(a1);
		IAtom a2 = new Atom("C");
		a2.setImplicitHydrogenCount(1);
		a2.setIsAromatic(true);
		mol.addAtom(a2);
		IAtom a3 = new Atom("C");
		a3.setImplicitHydrogenCount(1);
		a3.setIsAromatic(true);
		mol.addAtom(a3);
		IAtom a4 = new Atom("C");
		a4.setImplicitHydrogenCount(1);
		a4.setIsAromatic(true);
		mol.addAtom(a4);
		IAtom a5 = new Atom("C");
		a5.setImplicitHydrogenCount(1);
		a5.setIsAromatic(true);
		mol.addAtom(a5);
		IBond b0 = new Bond(a0 ,a1 ,IBond.Order.DOUBLE);
		b0.setIsAromatic(true);
		mol.addBond(b0);
		IBond b1 = new Bond(a1 ,a2 ,IBond.Order.SINGLE);
		b1.setIsAromatic(true);
		mol.addBond(b1);
		IBond b2 = new Bond(a2 ,a3 ,IBond.Order.DOUBLE);
		b2.setIsAromatic(true);
		mol.addBond(b2);
		IBond b3 = new Bond(a3 ,a4 ,IBond.Order.SINGLE);
		b3.setIsAromatic(true);
		mol.addBond(b3);
		IBond b4 = new Bond(a4 ,a5 ,IBond.Order.DOUBLE);
		b4.setIsAromatic(true);
		mol.addBond(b4);
		IBond b5 = new Bond(a0 ,a5 ,IBond.Order.SINGLE);
		b5.setIsAromatic(true);
		mol.addBond(b5);

		//Create reaction and set benzene as a reagent
		IReaction reaction = new Reaction();
		reaction.addReactant(mol);

		//Generate RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction);
		Assertions.assertEquals(Status.SUCCESS, gen.getRInChIStatus(), "RInChI status: ");		
		Assertions.assertEquals("RInChI=1.00.1S/<>C6H6/c1-2-4-6-5-3-1/h1-6H/d-", gen.getRInChI());
		//Generate RInChI using CDK MDL writer
		RInChIGenerator gen2 = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, true);
		Assertions.assertEquals(Status.SUCCESS, gen2.getRInChIStatus(), "RInChI status: ");		
		Assertions.assertEquals("RInChI=1.00.1S/<>C6H6/c1-2-4-6-5-3-1/h1-6H/d-", gen2.getRInChI(), "RInChI for benzene: ");		
	}

	@Test
	public void test03_benzene_aromatic() throws CDKException {
		//Create aromatic benzene for testing conversion of CDK bonds of type UNSET flagged as aromatic
		IAtomContainer mol = new AtomContainer();
		IAtom a0 = new Atom("C");
		a0.setImplicitHydrogenCount(1);
		a0.setIsAromatic(true);
		mol.addAtom(a0);
		IAtom a1 = new Atom("C");
		a1.setImplicitHydrogenCount(1);
		a1.setIsAromatic(true);
		mol.addAtom(a1);
		IAtom a2 = new Atom("C");
		a2.setImplicitHydrogenCount(1);
		a2.setIsAromatic(true);
		mol.addAtom(a2);
		IAtom a3 = new Atom("C");
		a3.setImplicitHydrogenCount(1);
		a3.setIsAromatic(true);
		mol.addAtom(a3);
		IAtom a4 = new Atom("C");
		a4.setImplicitHydrogenCount(1);
		a4.setIsAromatic(true);
		mol.addAtom(a4);
		IAtom a5 = new Atom("C");
		a5.setImplicitHydrogenCount(1);
		a5.setIsAromatic(true);
		mol.addAtom(a5);
		IBond b0 = new Bond(a0 ,a1 ,IBond.Order.UNSET);
		b0.setIsAromatic(true);
		mol.addBond(b0);
		IBond b1 = new Bond(a1 ,a2 ,IBond.Order.UNSET);
		b1.setIsAromatic(true);
		mol.addBond(b1);
		IBond b2 = new Bond(a2 ,a3 ,IBond.Order.UNSET);
		b2.setIsAromatic(true);
		mol.addBond(b2);
		IBond b3 = new Bond(a3 ,a4 ,IBond.Order.UNSET);
		b3.setIsAromatic(true);
		mol.addBond(b3);
		IBond b4 = new Bond(a4 ,a5 ,IBond.Order.UNSET);
		b4.setIsAromatic(true);
		mol.addBond(b4);
		IBond b5 = new Bond(a0 ,a5 ,IBond.Order.UNSET);
		b5.setIsAromatic(true);
		mol.addBond(b5);

		//Create reaction and set benzene as a reagent
		IReaction reaction = new Reaction();
		reaction.addReactant(mol);

		//Generate RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction);
		Assertions.assertEquals(Status.SUCCESS, gen.getRInChIStatus(), "RInChI status: ");		
		Assertions.assertEquals("RInChI=1.00.1S/<>C6H6/c1-2-4-6-5-3-1/h1-6H/d-", gen.getRInChI(), "RInChI for benzene: ");
		
		//Generate RInChI using CDK MDL writer
		//MDLRXNWriter throws Exception for aromatic bonds of type UNSET
		Assertions.assertThrows(CDKException.class, () -> RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, true), "MDLRXNWriter Exception For Aromatic Bonds: ");
	}
	
	@Test
	public void test04_radical_doublet() throws CDKException {
		//Create propane doublet radical (monovalent)
		IAtomContainer mol = new AtomContainer();
		IAtom a0 = new Atom("C");
		a0.setImplicitHydrogenCount(2);		
		mol.addAtom(a0);
		IAtom a1 = new Atom("C");
		a1.setImplicitHydrogenCount(2);		
		mol.addAtom(a1);
		IAtom a2 = new Atom("C");
		a2.setImplicitHydrogenCount(3);
		mol.addAtom(a2);		
		IBond b0 = new Bond(a0 ,a1 ,IBond.Order.SINGLE);
		mol.addBond(b0);
		IBond b1 = new Bond(a1 ,a2 ,IBond.Order.SINGLE);
		mol.addBond(b1);
		//Set radical info
		a0.setProperty(CDKConstants.SPIN_MULTIPLICITY, SPIN_MULTIPLICITY.Monovalent);
		mol.addSingleElectron(0);
						
		//Create reaction and set propane as a reagent
		IReaction reaction = new Reaction();
		reaction.addReactant(mol);
		
		//Generate RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction);
		Assertions.assertEquals(Status.SUCCESS, gen.getRInChIStatus(), "RInChI status: ");		
		Assertions.assertEquals("RInChI=1.00.1S/<>C3H7/c1-3-2/h1,3H2,2H3/d-", gen.getRInChI(), "RInChI for propane radical: ");
		Assertions.assertEquals("RAuxInfo=1.00.1/<>0/N:1,3,2/CRV:1d/rA:3nC.2CC/rB:s1;s2;/rC:;;;", gen.getAuxInfo(), "RAuxInfo for propane radical: ");
				
		//Generate RInChI using CDK MDL writer
		RInChIGenerator gen2 = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, true);
		Assertions.assertEquals("RInChI=1.00.1S/<>C3H7/c1-3-2/h1,3H2,2H3/d-", gen2.getRInChI(), "RInChI for propane radical: ");
		Assertions.assertEquals("RAuxInfo=1.00.1/<>0/N:1,3,2/CRV:1d/rA:3nC.2CC/rB:s1;s2;/rC:;;;", gen2.getAuxInfo(), "RAuxInfo for propane radical: ");				
	}
	
	@Test
	public void test05_radical_triplet() throws CDKException {
		//Create propane triple radical (divalent)
		//!!! propane singlet radical produces the same RAuxInfo (bug or feature in RInChI - unknown ??)
		IAtomContainer mol = new AtomContainer();
		IAtom a0 = new Atom("C");
		a0.setImplicitHydrogenCount(1);		
		mol.addAtom(a0);
		IAtom a1 = new Atom("C");
		a1.setImplicitHydrogenCount(2);		
		mol.addAtom(a1);
		IAtom a2 = new Atom("C");
		a2.setImplicitHydrogenCount(3);
		mol.addAtom(a2);		
		IBond b0 = new Bond(a0 ,a1 ,IBond.Order.SINGLE);
		mol.addBond(b0);
		IBond b1 = new Bond(a1 ,a2 ,IBond.Order.SINGLE);
		mol.addBond(b1);
		//Set radical info		
		a0.setProperty(CDKConstants.SPIN_MULTIPLICITY, SPIN_MULTIPLICITY.DivalentTriplet);
		mol.addSingleElectron(0);
		mol.addSingleElectron(0);
						
		//Create reaction and set propane as a reagent
		IReaction reaction = new Reaction();
		reaction.addReactant(mol);
		
		//Generate RInChI
		RInChIGenerator gen = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction);
		Assertions.assertEquals(Status.SUCCESS, gen.getRInChIStatus(), "RInChI status: ");		
		Assertions.assertEquals("RInChI=1.00.1S/<>C3H6/c1-3-2/h1H,3H2,2H3/d-", gen.getRInChI(), "RInChI for propane radical: ");
		Assertions.assertEquals("RAuxInfo=1.00.1/<>0/N:1,3,2/CRV:1t/rA:3nC.3CC/rB:s1;s2;/rC:;;;", gen.getAuxInfo(), "RAuxInfo for propane radical: ");
				
		//Generate RInChI using CDK MDL writer
		RInChIGenerator gen2 = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction, RinchiOptions.DEFAULT_OPTIONS, true);
		Assertions.assertEquals("RInChI=1.00.1S/<>C3H6/c1-3-2/h1H,3H2,2H3/d-", gen2.getRInChI(), "RInChI for propane radical: ");
		Assertions.assertEquals("RAuxInfo=1.00.1/<>0/N:1,3,2/CRV:1t/rA:3nC.3CC/rB:s1;s2;/rC:;;;", gen2.getAuxInfo(), "RAuxInfo for propane radical: ");						
	}

}
