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
import org.openscience.cdk.test.CDKTestCase;

import io.github.dan2097.jnarinchi.ReactionComponentRole;
import io.github.dan2097.jnarinchi.ReactionDirection;
import io.github.dan2097.jnarinchi.RinchiDecompositionStatus;

public class RInChIDecompositionTest extends CDKTestCase {

	@Test
	public void test01() throws Exception {
		String rinchi = "RInChI=1.00.1S/C6H12O/c1-4-6(3)5(2)7-6/h5H,4H2,1-3H3/t5-,6-/m0/s1!H2O/h1H2/p-1<>C6H14O2/c1-4-6(3,8)5(2)7/h5,7-8H,4H2,1-3H3/t5-,6+/m1/s1/d+";
		RInChIDecomposition rdecomp = RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi);
		Assert.assertEquals("RInChIDecomposition", RinchiDecompositionStatus.SUCCESS, rdecomp.getStatus());
		Assert.assertNotNull("Inchis: " + rdecomp.getIinchis());
		Assert.assertEquals("Number of inchis: ", 3, rdecomp.getIinchis().length);
		Assert.assertEquals("RInChI reaction direction: ", ReactionDirection.FORWARD, rdecomp.getReactionDirection());
		
		Assert.assertEquals("Inchi 0", "InChI=1S/C6H12O/c1-4-6(3)5(2)7-6/h5H,4H2,1-3H3/t5-,6-/m0/s1", rdecomp.getIinchis()[0]);
		Assert.assertEquals("Role 0", ReactionComponentRole.REAGENT, rdecomp.getRoles()[0]);
		Assert.assertEquals("Inchi 1", "InChI=1S/H2O/h1H2/p-1", rdecomp.getIinchis()[1]);
		Assert.assertEquals("Role 1", ReactionComponentRole.REAGENT, rdecomp.getRoles()[1]);
		Assert.assertEquals("Inchi 2", "InChI=1S/C6H14O2/c1-4-6(3,8)5(2)7/h5,7-8H,4H2,1-3H3/t5-,6+/m1/s1", rdecomp.getIinchis()[2]);
		Assert.assertEquals("Role 2", ReactionComponentRole.PRODUCT, rdecomp.getRoles()[2]);
	}
	
	
	@Test
	public void test02() throws Exception {
		String rinchi = "RInChI=1.00.1S/C6H12O/c1-4-6(3)5(2)7-6/h5H,4H2,1-3H3/t5-,6-/m0/s1!H2O/h1H2/p-1<>C6H14O2/c1-4-6(3,8)5(2)7/h5,7-8H,4H2,1-3H3/t5-,6+/m1/s1/d+";
		String auxInfo = "RAuxInfo=1.00.1/0/N:2,5,7,1,4,3,6/it:im/rA:7cCCCCCOC/rB:s1;s1;s3;s4;P3N4;s3;/rC:1.6851,-6.1198,0;.4352,-6.1248,0;2.3146,-7.1997,0;3.4875,-6.7675,0;4.287,-5.8066,0;3.2753,-7.9993,0;1.3603,-8.0071,0;!1/N:1/rA:1nO-/rB:/rC:7.5313,-6.9069,0;<>0/N:2,6,8,1,4,3,5,7/it:im/rA:8cCCCCOCOC/rB:s1;s1;s3;N4;s4;P3;s3;/rC:13.954,-5.7406,0;12.704,-5.7406,0;14.579,-6.8231,0;15.829,-6.8231,0;16.454,-5.7406,0;16.454,-7.9056,0;14.579,-8.0731,0;13.4965,-7.4481,0;";
		RInChIDecomposition rdecomp = RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi, auxInfo);
		Assert.assertEquals("RInChIDecomposition", RinchiDecompositionStatus.SUCCESS, rdecomp.getStatus());
		Assert.assertNotNull("Inchis: " + rdecomp.getIinchis());
		Assert.assertEquals("Number of inchis: ", 3, rdecomp.getIinchis().length);
		Assert.assertEquals("RInChI reaction direction: ", ReactionDirection.FORWARD, rdecomp.getReactionDirection());
		
		Assert.assertEquals("Inchi 0", "InChI=1S/C6H12O/c1-4-6(3)5(2)7-6/h5H,4H2,1-3H3/t5-,6-/m0/s1", rdecomp.getIinchis()[0]);
		Assert.assertEquals("Role 0", ReactionComponentRole.REAGENT, rdecomp.getRoles()[0]);
		Assert.assertEquals("AuxInfo 0", "AuxInfo=1/0/N:2,5,7,1,4,3,6/it:im/rA:7cCCCCCOC/rB:s1;s1;s3;s4;P3N4;s3;/rC:1.6851,-6.1198,0;.4352,-6.1248,0;2.3146,-7.1997,0;3.4875,-6.7675,0;4.287,-5.8066,0;3.2753,-7.9993,0;1.3603,-8.0071,0;", rdecomp.getAuxInfos()[0]);
		Assert.assertEquals("Inchi 1", "InChI=1S/H2O/h1H2/p-1", rdecomp.getIinchis()[1]);
		Assert.assertEquals("Role 1", ReactionComponentRole.REAGENT, rdecomp.getRoles()[1]);
		Assert.assertEquals("AuxInfo 1", "AuxInfo=1/1/N:1/rA:1nO-/rB:/rC:7.5313,-6.9069,0;", rdecomp.getAuxInfos()[1]);
		Assert.assertEquals("Inchi 2", "InChI=1S/C6H14O2/c1-4-6(3,8)5(2)7/h5,7-8H,4H2,1-3H3/t5-,6+/m1/s1", rdecomp.getIinchis()[2]);
		Assert.assertEquals("Role 2", ReactionComponentRole.PRODUCT, rdecomp.getRoles()[2]);
		Assert.assertEquals("AuxInfo 2", "AuxInfo=1/0/N:2,6,8,1,4,3,5,7/it:im/rA:8cCCCCOCOC/rB:s1;s1;s3;N4;s4;P3;s3;/rC:13.954,-5.7406,0;12.704,-5.7406,0;14.579,-6.8231,0;15.829,-6.8231,0;16.454,-5.7406,0;16.454,-7.9056,0;14.579,-8.0731,0;13.4965,-7.4481,0;", rdecomp.getAuxInfos()[2]);
	}
	
}
