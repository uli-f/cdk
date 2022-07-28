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

import org.openscience.cdk.exception.CDKException;

public class RInChIGeneratorFactory {
	
	private static RInChIGeneratorFactory INSTANCE;
	
	private RInChIGeneratorFactory() throws CDKException {
    }
	
	
	/**
     * Gives the one <code>RInChIGeneratorFactory</code> instance,
     * if needed also creates it.
     *
     * @return the one <code>RInChIGeneratorFactory</code> instance
     * @throws CDKException if unable to load native code when attempting
     *                      to create the factory
     */
    public static RInChIGeneratorFactory getInstance() throws CDKException {
        synchronized (RInChIGeneratorFactory.class) {
            if (INSTANCE == null) {
                INSTANCE = new RInChIGeneratorFactory();
            }
            return INSTANCE;
        }
    }
	
	
}
