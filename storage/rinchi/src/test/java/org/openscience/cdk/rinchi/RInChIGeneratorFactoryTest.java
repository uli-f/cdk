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
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.test.CDKTestCase;

public class RInChIGeneratorFactoryTest extends CDKTestCase {

	@Test
	public void testGetInstance() throws CDKException {
		RInChIGeneratorFactory factory = RInChIGeneratorFactory.getInstance();
		Assert.assertNotNull(factory);
	}
	
	@Test
	public void tes01() throws CDKException {
		IReaction reaction = new Reaction();
		IAtomContainer mol1 = new AtomContainer();
		IAtomContainer mol2 = new AtomContainer();
		IAtomContainer mol3 = new AtomContainer();
		reaction.addReactant(mol1);
		reaction.addReactant(mol2);
		reaction.addProduct(mol3);
		
		//TODO
	}

}
