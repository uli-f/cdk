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
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.test.CDKTestCase;

import io.github.dan2097.jnarinchi.ReactionComponentRole;
import io.github.dan2097.jnarinchi.ReactionDirection;
import io.github.dan2097.jnarinchi.Status;

/**
 * @author Nikolay Kochev
 * @author Uli Fechner
 */
public class RInChIDecompositionTest extends CDKTestCase {

    @Test
    public void test_rinchi_is_null() {
        final String rinchi = null;
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi),
                "Rinchi provided as an argument is null");
    }

    @Test
    public void test_auxinfo_is_null() {
        final String rinchi = "";
        final String auxinfo = null;
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi, auxinfo),
                "AuxInfo provided as an argument is null");
    }


    @Test
    public void test_garbage_rinchi_input() {
        final String rinchi = "not-a-rinchi-just-a-string";

        try {
            RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi);
        } catch (CDKException exception) {
            System.out.println(exception.getMessage());
            Assertions.assertTrue(exception.getMessage().startsWith("RInChI decomposition error:"));
            Assertions.assertTrue(exception.getMessage().endsWith("rinchi::RInChIReaderError: Invalid or incompatible RInChI header."));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void test_garbage_auxinfo_input() {
        final String rinchi = "RInChI=1.00.1S/C6H12O/c1-4-6(3)5(2)7-6/h5H,4H2,1-3H3/t5-,6-/m0/s1!H2O/h1H2/p-1<>C6H14O2/c1-4-6(3,8)5(2)7/h5,7-8H,4H2,1-3H3/t5-,6+/m1/s1/d+";
        final String auxinfo = "not-a-valid-auxinfo-string";

        try {
            RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi, auxinfo);
        } catch (CDKException exception) {
            System.out.println(exception.getMessage());
            Assertions.assertTrue(exception.getMessage().startsWith("RInChI decomposition error:"));
            Assertions.assertTrue(exception.getMessage().endsWith("rinchi::RInChIReaderError: Invalid AuxInfo 'AuxInfo=1/nfo-string' for a reaction component."));
            return;
        }

        Assertions.fail();
    }

    @Test
    public void test_01() throws Exception {
        final String rinchi = "RInChI=1.00.1S/C6H12O/c1-4-6(3)5(2)7-6/h5H,4H2,1-3H3/t5-,6-/m0/s1!H2O/h1H2/p-1<>C6H14O2/c1-4-6(3,8)5(2)7/h5,7-8H,4H2,1-3H3/t5-,6+/m1/s1/d+";
        final RInChIDecomposition rdecomp = RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi);
        Assertions.assertEquals(Status.SUCCESS, rdecomp.getStatus(), "RInChIDecomposition");
        Assertions.assertNotNull(rdecomp.getInchis(), "Inchis: ");
        Assertions.assertEquals(3, rdecomp.getInchis().size(), "Number of inchis: ");
        Assertions.assertEquals(ReactionDirection.FORWARD, rdecomp.getReactionDirection(), "RInChI reaction direction: ");

        Assertions.assertEquals("InChI=1S/C6H12O/c1-4-6(3)5(2)7-6/h5H,4H2,1-3H3/t5-,6-/m0/s1", rdecomp.getInchis().get(0), "Inchi 0");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(0), "Role 0");
        Assertions.assertEquals("InChI=1S/H2O/h1H2/p-1", rdecomp.getInchis().get(1), "Inchi 1");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(1), "Role 1");
        Assertions.assertEquals("InChI=1S/C6H14O2/c1-4-6(3,8)5(2)7/h5,7-8H,4H2,1-3H3/t5-,6+/m1/s1", rdecomp.getInchis().get(2), "Inchi 2");
        Assertions.assertEquals(ReactionComponentRole.PRODUCT, rdecomp.getReactionComponentRoles().get(2), "Role 2");
    }


    @Test
    public void test_02() throws Exception {
        final String rinchi = "RInChI=1.00.1S/C6H12O/c1-4-6(3)5(2)7-6/h5H,4H2,1-3H3/t5-,6-/m0/s1!H2O/h1H2/p-1<>C6H14O2/c1-4-6(3,8)5(2)7/h5,7-8H,4H2,1-3H3/t5-,6+/m1/s1/d+";
        final String auxInfo = "RAuxInfo=1.00.1/0/N:2,5,7,1,4,3,6/it:im/rA:7cCCCCCOC/rB:s1;s1;s3;s4;P3N4;s3;/rC:1.6851,-6.1198,0;.4352,-6.1248,0;2.3146,-7.1997,0;3.4875,-6.7675,0;4.287,-5.8066,0;3.2753,-7.9993,0;1.3603,-8.0071,0;!1/N:1/rA:1nO-/rB:/rC:7.5313,-6.9069,0;<>0/N:2,6,8,1,4,3,5,7/it:im/rA:8cCCCCOCOC/rB:s1;s1;s3;N4;s4;P3;s3;/rC:13.954,-5.7406,0;12.704,-5.7406,0;14.579,-6.8231,0;15.829,-6.8231,0;16.454,-5.7406,0;16.454,-7.9056,0;14.579,-8.0731,0;13.4965,-7.4481,0;";
        final RInChIDecomposition rdecomp = RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi, auxInfo);
        Assertions.assertEquals(Status.SUCCESS, rdecomp.getStatus(), "RInChIDecomposition");
        Assertions.assertNotNull(rdecomp.getInchis(), "Inchis: ");
        Assertions.assertEquals(3, rdecomp.getInchis().size(), "Number of inchis: ");
        Assertions.assertEquals(ReactionDirection.FORWARD, rdecomp.getReactionDirection(), "RInChI reaction direction: ");

        Assertions.assertEquals("InChI=1S/C6H12O/c1-4-6(3)5(2)7-6/h5H,4H2,1-3H3/t5-,6-/m0/s1", rdecomp.getInchis().get(0), "Inchi 0");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(0), "Role 0");
        Assertions.assertEquals("AuxInfo=1/0/N:2,5,7,1,4,3,6/it:im/rA:7cCCCCCOC/rB:s1;s1;s3;s4;P3N4;s3;/rC:1.6851,-6.1198,0;.4352,-6.1248,0;2.3146,-7.1997,0;3.4875,-6.7675,0;4.287,-5.8066,0;3.2753,-7.9993,0;1.3603,-8.0071,0;", rdecomp.getAuxInfo().get(0), "AuxInfo 0");
        Assertions.assertEquals("InChI=1S/H2O/h1H2/p-1", rdecomp.getInchis().get(1), "Inchi 1");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(1), "Role 1");
        Assertions.assertEquals("AuxInfo=1/1/N:1/rA:1nO-/rB:/rC:7.5313,-6.9069,0;", rdecomp.getAuxInfo().get(1), "AuxInfo 1");
        Assertions.assertEquals("InChI=1S/C6H14O2/c1-4-6(3,8)5(2)7/h5,7-8H,4H2,1-3H3/t5-,6+/m1/s1", rdecomp.getInchis().get(2), "Inchi 2");
        Assertions.assertEquals(ReactionComponentRole.PRODUCT, rdecomp.getReactionComponentRoles().get(2), "Role 2");
        Assertions.assertEquals("AuxInfo=1/0/N:2,6,8,1,4,3,5,7/it:im/rA:8cCCCCOCOC/rB:s1;s1;s3;N4;s4;P3;s3;/rC:13.954,-5.7406,0;12.704,-5.7406,0;14.579,-6.8231,0;15.829,-6.8231,0;16.454,-5.7406,0;16.454,-7.9056,0;14.579,-8.0731,0;13.4965,-7.4481,0;", rdecomp.getAuxInfo().get(2), "AuxInfo 2");
    }

    @Test
    public void test_03() throws Exception {
        final String rinchi = "RInChI=1.00.1S/<>C8H8O2/c9-8(10)6-7-4-2-1-3-5-7/h1-5H,6H2,(H,9,10)/d+";
        final String auxInfo = "RAuxInfo=1.00.1/<>1/N:3,2,4,1,5,7,6,8,9,10/E:(2,3)(4,5)(9,10)/rA:10nCCCCCCCCOO/rB:d1;s2;d3;s4;s1d5;s6;s7;s8;d8;/rC:14.2607,3.2411,0;13.5462,2.8286,0;13.5462,2.0036,0;14.2607,1.5911,0;14.9752,2.0036,0;14.9752,2.8286,0;15.6897,3.2411,0;16.4041,2.8286,0;17.1186,3.2411,0;16.4041,2.0036,0;";
        final RInChIDecomposition rdecomp = RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi, auxInfo);
        Assertions.assertEquals(Status.SUCCESS, rdecomp.getStatus(), "RInChIDecomposition");
        Assertions.assertNotNull(rdecomp.getInchis(), "Inchis: ");
        Assertions.assertEquals(1, rdecomp.getInchis().size(), "Number of inchis: ");
        Assertions.assertEquals(ReactionDirection.FORWARD, rdecomp.getReactionDirection(), "RInChI reaction direction: ");

        Assertions.assertEquals("InChI=1S/C8H8O2/c9-8(10)6-7-4-2-1-3-5-7/h1-5H,6H2,(H,9,10)", rdecomp.getInchis().get(0), "Inchi 0");
        Assertions.assertEquals(ReactionComponentRole.PRODUCT, rdecomp.getReactionComponentRoles().get(0), "Role 0");
        Assertions.assertEquals("AuxInfo=1/1/N:3,2,4,1,5,7,6,8,9,10/E:(2,3)(4,5)(9,10)/rA:10nCCCCCCCCOO/rB:d1;s2;d3;s4;s1d5;s6;s7;s8;d8;/rC:14.2607,3.2411,0;13.5462,2.8286,0;13.5462,2.0036,0;14.2607,1.5911,0;14.9752,2.0036,0;14.9752,2.8286,0;15.6897,3.2411,0;16.4041,2.8286,0;17.1186,3.2411,0;16.4041,2.0036,0;", rdecomp.getAuxInfo().get(0), "AuxInfo 0");
    }

    @Test
    public void test_04() throws Exception {
        final String rinchi = "RInChI=1.00.1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4?/m0/s1<>C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1!Na.H2O/h;1H2/q+1;/p-1/d-/u1-0-0";
        final String auxInfo = "RAuxInfo=1.00.1/0/N:4,1,3,2,5/E:(1,2)(3,4)/it:im/rA:5nCCCCO/rB:N1;s2;P3;s2s3;/rC:-1.127,-.5635,0;-.4125,-.151,0;.4125,-.151,0;1.127,-.5635,0;0,.5635,0;<>0/N:4,1,3,2,6,5/it:im/rA:6nCCCCOBr/rB:s1;s2;s3;N2;P3;/rC:-.825,-.7557,0;-.4125,-.0412,0;.4125,-.0412,0;.825,.6733,0;-.626,.7557,0;.825,-.7557,0;!1/N:1;2/rA:2nNaO/rB:s1;/rC:-.4125,0,0;.4125,0,0;";
        final RInChIDecomposition rdecomp = RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi, auxInfo);
        Assertions.assertEquals(Status.SUCCESS, rdecomp.getStatus(), "RInChIDecomposition");
        Assertions.assertNotNull(rdecomp.getInchis(), "Inchis: ");
        Assertions.assertEquals(4, rdecomp.getInchis().size(), "Number of inchis: ");
        Assertions.assertEquals(ReactionDirection.BACKWARD, rdecomp.getReactionDirection(), "RInChI reaction direction: ");

        Assertions.assertEquals("InChI=1S/C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1", rdecomp.getInchis().get(0), "Inchi 0");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(0), "Role 0");
        Assertions.assertEquals("AuxInfo=1/0/N:4,1,3,2,6,5/it:im/rA:6nCCCCOBr/rB:s1;s2;s3;N2;P3;/rC:-.825,-.7557,0;-.4125,-.0412,0;.4125,-.0412,0;.825,.6733,0;-.626,.7557,0;.825,-.7557,0;", rdecomp.getAuxInfo().get(0), "AuxInfo 0");
        Assertions.assertEquals("InChI=1S/Na.H2O/h;1H2/q+1;/p-1", rdecomp.getInchis().get(1), "Inchi 1");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(1), "Role 1");
        Assertions.assertEquals("AuxInfo=1/1/N:1;2/rA:2nNaO/rB:s1;/rC:-.4125,0,0;.4125,0,0;", rdecomp.getAuxInfo().get(1), "AuxInfo 1");
        Assertions.assertEquals("InChI=1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4?/m0/s1", rdecomp.getInchis().get(2), "Inchi 2");
        Assertions.assertEquals(ReactionComponentRole.PRODUCT, rdecomp.getReactionComponentRoles().get(2), "Role 2");
        Assertions.assertEquals("AuxInfo=1/0/N:4,1,3,2,5/E:(1,2)(3,4)/it:im/rA:5nCCCCO/rB:N1;s2;P3;s2s3;/rC:-1.127,-.5635,0;-.4125,-.151,0;.4125,-.151,0;1.127,-.5635,0;0,.5635,0;", rdecomp.getAuxInfo().get(2), "AuxInfo 2");
        Assertions.assertEquals("InChI=1S//", rdecomp.getInchis().get(3), "Inchi 3");
        Assertions.assertEquals(ReactionComponentRole.PRODUCT, rdecomp.getReactionComponentRoles().get(3), "Role 3");
        Assertions.assertEquals("AuxInfo=1//", rdecomp.getAuxInfo().get(3), "AuxInfo 3");
    }

    @Test
    public void test_05() throws Exception {
        final String rinchi = "RInChI=1.00.1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4?/m0/s1<>C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1!Na.H2O/h;1H2/q+1;/p-1/d-/u0-1-0";
        final String auxInfo = "RAuxInfo=1.00.1/0/N:4,1,3,2,5/E:(1,2)(3,4)/it:im/rA:5nCCCCO/rB:N1;s2;P3;s2s3;/rC:-1.127,-.5635,0;-.4125,-.151,0;.4125,-.151,0;1.127,-.5635,0;0,.5635,0;<>0/N:4,1,3,2,6,5/it:im/rA:6nCCCCOBr/rB:s1;s2;s3;N2;P3;/rC:-.825,-.7557,0;-.4125,-.0412,0;.4125,-.0412,0;.825,.6733,0;-.626,.7557,0;.825,-.7557,0;!1/N:1;2/rA:2nNaO/rB:s1;/rC:-.4125,0,0;.4125,0,0;";
        final RInChIDecomposition rdecomp = RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi, auxInfo);
        Assertions.assertEquals(Status.SUCCESS, rdecomp.getStatus(), "RInChIDecomposition");
        Assertions.assertNotNull(rdecomp.getInchis(), "Inchis: ");
        Assertions.assertEquals(4, rdecomp.getInchis().size(), "Number of inchis: ");
        Assertions.assertEquals(ReactionDirection.BACKWARD, rdecomp.getReactionDirection(), "RInChI reaction direction: ");

        Assertions.assertEquals("InChI=1S/C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1", rdecomp.getInchis().get(0), "Inchi 0");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(0), "Role 0");
        Assertions.assertEquals("AuxInfo=1/0/N:4,1,3,2,6,5/it:im/rA:6nCCCCOBr/rB:s1;s2;s3;N2;P3;/rC:-.825,-.7557,0;-.4125,-.0412,0;.4125,-.0412,0;.825,.6733,0;-.626,.7557,0;.825,-.7557,0;", rdecomp.getAuxInfo().get(0), "AuxInfo 0");
        Assertions.assertEquals("InChI=1S/Na.H2O/h;1H2/q+1;/p-1", rdecomp.getInchis().get(1), "Inchi 1");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(1), "Role 1");
        Assertions.assertEquals("AuxInfo=1/1/N:1;2/rA:2nNaO/rB:s1;/rC:-.4125,0,0;.4125,0,0;", rdecomp.getAuxInfo().get(1), "AuxInfo 1");
        Assertions.assertEquals("InChI=1S//", rdecomp.getInchis().get(2), "Inchi 2");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(2), "Role 2");
        Assertions.assertEquals("AuxInfo=1//", rdecomp.getAuxInfo().get(2), "AuxInfo 2");
        Assertions.assertEquals("InChI=1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4?/m0/s1", rdecomp.getInchis().get(3), "Inchi 3");
        Assertions.assertEquals(ReactionComponentRole.PRODUCT, rdecomp.getReactionComponentRoles().get(3), "Role 3");
        Assertions.assertEquals("AuxInfo=1/0/N:4,1,3,2,5/E:(1,2)(3,4)/it:im/rA:5nCCCCO/rB:N1;s2;P3;s2s3;/rC:-1.127,-.5635,0;-.4125,-.151,0;.4125,-.151,0;1.127,-.5635,0;0,.5635,0;", rdecomp.getAuxInfo().get(3), "AuxInfo 3");
    }

    @Test
    public void test_06() throws Exception {
        final String rinchi = "RInChI=1.00.1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4?/m0/s1<>C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1!Na.H2O/h;1H2/q+1;/p-1/d-/u0-2-0";
        final RInChIDecomposition rdecomp = RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi);
        Assertions.assertEquals(Status.SUCCESS, rdecomp.getStatus(), "RInChIDecomposition");
        Assertions.assertNotNull(rdecomp.getInchis(), "Inchis: ");
        Assertions.assertEquals(5, rdecomp.getInchis().size(), "Number of inchis: ");
        Assertions.assertEquals(ReactionDirection.BACKWARD, rdecomp.getReactionDirection(), "RInChI reaction direction: ");

        Assertions.assertEquals("InChI=1S/C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1", rdecomp.getInchis().get(0), "Inchi 0");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(0), "Role 0");
        Assertions.assertEquals("", rdecomp.getAuxInfo().get(0), "AuxInfo 0");
        Assertions.assertEquals("InChI=1S/Na.H2O/h;1H2/q+1;/p-1", rdecomp.getInchis().get(1), "Inchi 1");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(1), "Role 1");
        Assertions.assertEquals("", rdecomp.getAuxInfo().get(1), "AuxInfo 1");
        Assertions.assertEquals("InChI=1S//", rdecomp.getInchis().get(2), "Inchi 2");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(2), "Role 2");
        Assertions.assertEquals("", rdecomp.getAuxInfo().get(2), "AuxInfo 2");
        Assertions.assertEquals("InChI=1S//", rdecomp.getInchis().get(3), "Inchi 3");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(3), "Role 3");
        Assertions.assertEquals("", rdecomp.getAuxInfo().get(3), "AuxInfo 3");
        Assertions.assertEquals("InChI=1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4?/m0/s1", rdecomp.getInchis().get(4), "Inchi 4");
        Assertions.assertEquals(ReactionComponentRole.PRODUCT, rdecomp.getReactionComponentRoles().get(4), "Role 4");
        Assertions.assertEquals("", rdecomp.getAuxInfo().get(4), "AuxInfo 4");
    }

    @Test
    public void test_07() throws Exception {
        final String rinchi = "RInChI=1.00.1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4?/m0/s1<>C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1!Na.H2O/h;1H2/q+1;/p-1/d-";
        final String auxinfo = "RAuxInfo=1.00.1/0/N:4,1,3,2,5/E:(1,2)(3,4)/it:im/rA:5nCCCCO/rB:N1;s2;P3;s2s3;/rC:-1.127,-.5635,0;-.4125,-.151,0;.4125,-.151,0;1.127,-.5635,0;0,.5635,0;<>0/N:4,1,3,2,6,5/it:im/rA:6nCCCCOBr/rB:s1;s2;s3;N2;P3;/rC:-.825,-.7557,0;-.4125,-.0412,0;.4125,-.0412,0;.825,.6733,0;-.626,.7557,0;.825,-.7557,0;!1/N:1;2/rA:2nNaO/rB:s1;/rC:-.4125,0,0;.4125,0,0;";
        final RInChIDecomposition rdecomp = RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi, auxinfo);
        Assertions.assertEquals(Status.SUCCESS, rdecomp.getStatus(), "RInChIDecomposition");
        Assertions.assertNotNull(rdecomp.getInchis(), "Inchis: ");
        Assertions.assertEquals(3, rdecomp.getInchis().size(), "Number of inchis: ");
        Assertions.assertEquals(ReactionDirection.BACKWARD, rdecomp.getReactionDirection(), "RInChI reaction direction: ");

        Assertions.assertEquals("InChI=1S/C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1", rdecomp.getInchis().get(0), "Inchi 0");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(0), "Role 0");
        Assertions.assertEquals("AuxInfo=1/0/N:4,1,3,2,6,5/it:im/rA:6nCCCCOBr/rB:s1;s2;s3;N2;P3;/rC:-.825,-.7557,0;-.4125,-.0412,0;.4125,-.0412,0;.825,.6733,0;-.626,.7557,0;.825,-.7557,0;", rdecomp.getAuxInfo().get(0), "AuxInfo 0");
        Assertions.assertEquals("InChI=1S/Na.H2O/h;1H2/q+1;/p-1", rdecomp.getInchis().get(1), "Inchi 1");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(1), "Role 1");
        Assertions.assertEquals("AuxInfo=1/1/N:1;2/rA:2nNaO/rB:s1;/rC:-.4125,0,0;.4125,0,0;", rdecomp.getAuxInfo().get(1), "AuxInfo 1");
        Assertions.assertEquals("InChI=1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4?/m0/s1", rdecomp.getInchis().get(2), "Inchi 2");
        Assertions.assertEquals(ReactionComponentRole.PRODUCT, rdecomp.getReactionComponentRoles().get(2), "Role 2");
        Assertions.assertEquals("AuxInfo=1/0/N:4,1,3,2,5/E:(1,2)(3,4)/it:im/rA:5nCCCCO/rB:N1;s2;P3;s2s3;/rC:-1.127,-.5635,0;-.4125,-.151,0;.4125,-.151,0;1.127,-.5635,0;0,.5635,0;", rdecomp.getAuxInfo().get(2), "AuxInfo 2");
    }

    @Test
    public void test_08() throws Exception {
        final String rinchi = "RInChI=1.00.1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4-/m0/s1<>C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1!Na.H2O/h;1H2/q+1;/p-1<>ClH.Na/h1H;/q;+1/p-1!ClH/h1H!H2O/h1H2/d-";
        final String auxinfo = "RAuxInfo=1.00.1/0/N:3,5,2,1,4/E:(1,2)(3,4)/it:im/rA:5nCCCOC/rB:s1;P2;s1s2;N1;/rC:3.8966,-.151,0;4.7216,-.151,0;5.4361,-.5635,0;4.3091,.5635,0;3.1822,-.5635,0;<>0/N:4,1,3,2,6,5/it:im/rA:6nCCCCOBr/rB:s1;s2;s3;N2;P3;/rC:-.825,-.7557,0;-.4125,-.0412,0;.4125,-.0412,0;.825,.6733,0;-.626,.7557,0;.825,-.7557,0;!1/N:1;2/rA:2nNaO/rB:s1;/rC:-.4125,0,0;.4125,0,0;<>1/N:2;1/rA:2nNa+Cl-/rB:;/rC:.4571,-.5804,0;-.3679,-.5804,0;!0/N:1/rA:2nClH/rB:s1;/rC:.7482,-.5134,0;1.5732,-.5134,0;!0/N:2/rA:3nHOH/rB:s1;s2;/rC:-.2457,.3313,0;.4688,.7438,0;1.1832,.3313,0;";
        final RInChIDecomposition rdecomp = RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi, auxinfo);
        Assertions.assertEquals(Status.SUCCESS, rdecomp.getStatus(), "RInChIDecomposition");
        Assertions.assertNotNull(rdecomp.getInchis(), "Inchis: ");
        Assertions.assertEquals(6, rdecomp.getInchis().size(), "Number of inchis: ");
        Assertions.assertEquals(ReactionDirection.BACKWARD, rdecomp.getReactionDirection(), "RInChI reaction direction: ");

        Assertions.assertEquals("InChI=1S/C4H9BrO/c1-3(5)4(2)6/h3-4,6H,1-2H3/t3-,4+/m1/s1", rdecomp.getInchis().get(0), "Inchi 0");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(0), "Role 0");
        Assertions.assertEquals("AuxInfo=1/0/N:4,1,3,2,6,5/it:im/rA:6nCCCCOBr/rB:s1;s2;s3;N2;P3;/rC:-.825,-.7557,0;-.4125,-.0412,0;.4125,-.0412,0;.825,.6733,0;-.626,.7557,0;.825,-.7557,0;", rdecomp.getAuxInfo().get(0), "AuxInfo 0");
        Assertions.assertEquals("InChI=1S/Na.H2O/h;1H2/q+1;/p-1", rdecomp.getInchis().get(1), "Inchi 1");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(1), "Role 1");
        Assertions.assertEquals("AuxInfo=1/1/N:1;2/rA:2nNaO/rB:s1;/rC:-.4125,0,0;.4125,0,0;", rdecomp.getAuxInfo().get(1), "AuxInfo 1");
        Assertions.assertEquals("InChI=1S/C4H8O/c1-3-4(2)5-3/h3-4H,1-2H3/t3-,4-/m0/s1", rdecomp.getInchis().get(2), "Inchi 2");
        Assertions.assertEquals(ReactionComponentRole.PRODUCT, rdecomp.getReactionComponentRoles().get(2), "Role 2");
        Assertions.assertEquals("AuxInfo=1/0/N:3,5,2,1,4/E:(1,2)(3,4)/it:im/rA:5nCCCOC/rB:s1;P2;s1s2;N1;/rC:3.8966,-.151,0;4.7216,-.151,0;5.4361,-.5635,0;4.3091,.5635,0;3.1822,-.5635,0;", rdecomp.getAuxInfo().get(2), "AuxInfo 2");
        Assertions.assertEquals("InChI=1S/ClH.Na/h1H;/q;+1/p-1", rdecomp.getInchis().get(3), "Inchi 3");
        Assertions.assertEquals(ReactionComponentRole.AGENT, rdecomp.getReactionComponentRoles().get(3), "Role 3");
        Assertions.assertEquals("AuxInfo=1/1/N:2;1/rA:2nNa+Cl-/rB:;/rC:.4571,-.5804,0;-.3679,-.5804,0;", rdecomp.getAuxInfo().get(3), "AuxInfo 3");
        Assertions.assertEquals("InChI=1S/ClH/h1H", rdecomp.getInchis().get(4), "Inchi 4");
        Assertions.assertEquals(ReactionComponentRole.AGENT, rdecomp.getReactionComponentRoles().get(4), "Role 4");
        Assertions.assertEquals("AuxInfo=1/0/N:1/rA:2nClH/rB:s1;/rC:.7482,-.5134,0;1.5732,-.5134,0;", rdecomp.getAuxInfo().get(4), "AuxInfo 4");
        Assertions.assertEquals("InChI=1S/H2O/h1H2", rdecomp.getInchis().get(5), "Inchi 5");
        Assertions.assertEquals(ReactionComponentRole.AGENT, rdecomp.getReactionComponentRoles().get(5), "Role 5");
        Assertions.assertEquals("AuxInfo=1/0/N:2/rA:3nHOH/rB:s1;s2;/rC:-.2457,.3313,0;.4688,.7438,0;1.1832,.3313,0;", rdecomp.getAuxInfo().get(5), "AuxInfo 5");
    }

    @Test
    public void test_09() throws Exception {
        final String rinchi = "RInChI=1.00.1S/<>C5H12O/c1-4(2)5(3)6/h4-6H,1-3H3!C8H8O2/c9-8(10)6-7-4-2-1-3-5-7/h1-5H,6H2,(H,9,10)/d-";
        final String auxinfo = "RAuxInfo=1.00.1/<>0/N:1,3,5,2,4,6/E:(1,2)/rA:6nCCCCCO/rB:s1;s2;s2;s4;s4;/rC:5.2741,1.4438,0;5.9886,1.8563,0;6.703,1.4438,0;5.9886,2.6813,0;6.703,3.0938,0;5.2741,3.0938,0;!1/N:3,2,4,1,5,7,6,8,9,10/E:(2,3)(4,5)(9,10)/rA:10nCCCCCCCCOO/rB:d1;s2;d3;s4;s1d5;s6;s7;s8;d8;/rC:-3.8009,2.917,0;-4.5154,2.5045,0;-4.5154,1.6795,0;-3.8009,1.2669,0;-3.0864,1.6795,0;-3.0864,2.5045,0;-2.372,2.917,0;-1.6575,2.5045,0;-.943,2.917,0;-1.6575,1.6795,0;";
        final RInChIDecomposition rdecomp = RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi, auxinfo);
        Assertions.assertEquals(Status.SUCCESS, rdecomp.getStatus(), "RInChIDecomposition");
        Assertions.assertNotNull(rdecomp.getInchis(), "Inchis: ");
        Assertions.assertEquals(2, rdecomp.getInchis().size(), "Number of inchis: ");
        Assertions.assertEquals(ReactionDirection.BACKWARD, rdecomp.getReactionDirection(), "RInChI reaction direction: ");

        Assertions.assertEquals("InChI=1S/C5H12O/c1-4(2)5(3)6/h4-6H,1-3H3", rdecomp.getInchis().get(0), "Inchi 0");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(0), "Role 0");
        Assertions.assertEquals("AuxInfo=1/0/N:1,3,5,2,4,6/E:(1,2)/rA:6nCCCCCO/rB:s1;s2;s2;s4;s4;/rC:5.2741,1.4438,0;5.9886,1.8563,0;6.703,1.4438,0;5.9886,2.6813,0;6.703,3.0938,0;5.2741,3.0938,0;", rdecomp.getAuxInfo().get(0), "AuxInfo 0");
        Assertions.assertEquals("InChI=1S/C8H8O2/c9-8(10)6-7-4-2-1-3-5-7/h1-5H,6H2,(H,9,10)", rdecomp.getInchis().get(1), "Inchi 1");
        Assertions.assertEquals(ReactionComponentRole.REAGENT, rdecomp.getReactionComponentRoles().get(1), "Role 1");
        Assertions.assertEquals("AuxInfo=1/1/N:3,2,4,1,5,7,6,8,9,10/E:(2,3)(4,5)(9,10)/rA:10nCCCCCCCCOO/rB:d1;s2;d3;s4;s1d5;s6;s7;s8;d8;/rC:-3.8009,2.917,0;-4.5154,2.5045,0;-4.5154,1.6795,0;-3.8009,1.2669,0;-3.0864,1.6795,0;-3.0864,2.5045,0;-2.372,2.917,0;-1.6575,2.5045,0;-.943,2.917,0;-1.6575,1.6795,0;", rdecomp.getAuxInfo().get(1), "AuxInfo 1");
    }

}
