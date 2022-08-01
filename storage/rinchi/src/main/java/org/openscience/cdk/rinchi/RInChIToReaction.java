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
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IReaction;

import io.github.dan2097.jnarinchi.JnaRinchi;
import io.github.dan2097.jnarinchi.RinchiInputFromRinchiOutput;
import io.github.dan2097.jnarinchi.RinchiStatus;

public class RInChIToReaction {
	
	protected RinchiInputFromRinchiOutput output;
	
	protected IReaction reaction;
	
	/**
     * Constructor. Generates CDK Reaction from RInChI.
     * @param rinchi RInChI string
     * @param auxInfo RInChI aux info string
     * @throws CDKException
     */
	protected RInChIToReaction(String rinchi, IChemObjectBuilder builder) throws CDKException {
		this (rinchi, "", builder);
	}
	
	/**
     * Constructor. Generates CDK Reaction from RInChI.
     * @param rinchi RInChI string
     * @param auxInfo RInChI aux info string
     * @throws CDKException
     */
	protected RInChIToReaction(String rinchi, String auxInfo, IChemObjectBuilder builder) throws CDKException {
		if (rinchi == null)
			throw new IllegalArgumentException("Null RInChI string provided");
		if (auxInfo == null)
			throw new IllegalArgumentException("Null RInChI aux info string provided");
		
		this.output = JnaRinchi.getRnchiInputFromRinchi(rinchi, auxInfo);
		generateReactionFromRinchi(builder);
	}
	
	/**
     * Gets reaction from RnChI, and converts RInChI library data structure (RinchiInput object)
     * into an IReactionr.
     *
     * @throws CDKException
     */
    protected void generateReactionFromRinchi(IChemObjectBuilder builder) throws CDKException {
    	//TODO
    }
	/**
     * Returns generated reaction.
     * @return A Reaction object
     */
	public IReaction getReaction() {
		return reaction;
	}
	
	/**
     * Access the status of the RInChI output.
     * @return the status
     */
	public RinchiStatus getStatus() {
		return output.getStatus();
	}
	
	/**
     * Gets generated error messages.
     */
    public String getErrorMessage() {
        return output.getErrorMessage();
    }
}
