/* $RCSfile$
 * $Author$    
 * $Date$    
 * $Revision$
 * 
 * Copyright (C) 2002-2004  The Chemistry Development Kit (CDK) project
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 
 * 
 */

package org.openscience.cdk.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openscience.cdk.ElectronContainer;

/**
 * Checks the funcitonality of the ElectronContainer class.
 *
 * @cdk.module test
 *
 * @see org.openscience.cdk.ElectronContainer
 */
public class ElectronContainerTest extends TestCase {

    public ElectronContainerTest(String name) {
        super(name);
    }

    public void setUp() {}

    public static Test suite() {
        return new TestSuite(ElectronContainerTest.class);
    }
    
    public void testElectronContainer() {
        ElectronContainer ec = new ElectronContainer();
        assertNotNull(ec);
        assertEquals(0, ec.getElectronCount());
    }
    
    public void testSetElectronCount_int() {
        ElectronContainer ec = new ElectronContainer();
        ec.setElectronCount(3);
        assertEquals(3, ec.getElectronCount());
    }
    public void testGetElectronCount() {
        testSetElectronCount_int();
    }

    public void testClone() {
        ElectronContainer ec = new ElectronContainer();
        ec.setElectronCount(2);
        Object clone = ec.clone();
        assertNotNull(clone);
        assertTrue(clone instanceof ElectronContainer);
        assertEquals(ec.getElectronCount(), ((ElectronContainer)clone).getElectronCount());
    }
    
}
