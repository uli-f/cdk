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

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.test.CDKTestCase;

import io.github.dan2097.jnarinchi.RinchiStatus;

public class RInChIGeneratorFactoryTest extends CDKTestCase {

	@Test
	public void testGetInstance() throws CDKException {
		RInChIGeneratorFactory factory = RInChIGeneratorFactory.getInstance();
		Assert.assertNotNull(factory);
	}
	
	@Test
	public void tes01() throws CDKException {
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
		Assert.assertEquals("RInChI status: ", RinchiStatus.SUCCESS, gen.getRInChIStatus());		
		Assert.assertEquals("Forward reaction RInChI: ", true, gen.getRInChI().endsWith("/d+"));
		
		//Generate RInChI with option ForceEquilibrium		
		RInChIGenerator gen_eq = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction, "ForceEquilibrium");
		Assert.assertEquals("Equilibrium reaction RInChI: ", true, gen_eq.getRInChI().endsWith("/d="));
				
		//Create reverse reaction and generate RInChI
		IReaction reaction2 = new Reaction();
		reaction2.addReactant(mol3);		
		reaction2.addProduct(mol1);
		reaction2.addProduct(mol2);
		RInChIGenerator gen2 = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction2);
		Assert.assertEquals("Backward reaction RInChI: ", true, gen2.getRInChI().endsWith("/d-"));
		
		//Backward, forward and equilibrium RInChIs differ only in their last char
		int n = gen.getRInChI().length();
		Assert.assertEquals("Forward and backward RInChI comparison: ", 
				gen.getRInChI().substring(0, n-1), gen2.getRInChI().substring(0, n-1));
		Assert.assertEquals("Forward and equilibrium RInChI comparison: ", 
				gen.getRInChI().substring(0, n-1), gen_eq.getRInChI().substring(0, n-1));
		
		//Backward, forward and equilibrium RInChIs-Keys differ only in their 19-th char
		int k = gen.getLongRInChIKey().length();
		Assert.assertEquals("Forward and backward Long-RInChI-Key comparison: ", 
				gen.getLongRInChIKey().substring(0, 18), gen2.getLongRInChIKey().substring(0, 18));
		Assert.assertEquals("Forward and backward Long-RInChI-Key comparison: ", 
				gen.getLongRInChIKey().substring(19), gen2.getLongRInChIKey().substring(19));
		Assert.assertEquals("Reaction direction char in Long-RInChI-Key", 'F', gen.getLongRInChIKey().charAt(18));
		Assert.assertEquals("Reaction direction char in Long-RInChI-Key", 'E', gen_eq.getLongRInChIKey().charAt(18));
		Assert.assertEquals("Reaction direction char in Long-RInChI-Key", 'B', gen2.getLongRInChIKey().charAt(18));
		
		//Generate back a IReaction object from RInChI
		RInChIToReaction r2r = RInChIGeneratorFactory.getInstance().getRInChIToReaction(gen.getRInChI());
		Assert.assertEquals("RInChI status: ", RinchiStatus.SUCCESS, r2r.getStatus());
		IReaction reaction3 = r2r.getReaction();
		Assert.assertEquals("Reactant count: ", 2, reaction3.getReactantCount());
		Assert.assertEquals("Product count: ", 1, reaction3.getProductCount());
		Assert.assertEquals("Reactant 1 atom count: ", 2, reaction3.getReactants().getAtomContainer(0).getAtomCount());
		Assert.assertEquals("Reactant 2 atom count: ", 4, reaction3.getReactants().getAtomContainer(1).getAtomCount());
		Assert.assertEquals("Product atom count: ", 6, reaction3.getProducts().getAtomContainer(0).getAtomCount());
	}

}
