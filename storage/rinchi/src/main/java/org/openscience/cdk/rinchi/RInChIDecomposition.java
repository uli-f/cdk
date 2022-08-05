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

import io.github.dan2097.jnarinchi.JnaRinchi;
import io.github.dan2097.jnarinchi.ReactionComponentRole;
import io.github.dan2097.jnarinchi.ReactionDirection;
import io.github.dan2097.jnarinchi.RinchiDecompositionOutput;
import io.github.dan2097.jnarinchi.RinchiDecompositionStatus;

public class RInChIDecomposition {

	protected RinchiDecompositionOutput	rinchiDecompositionOutput = null;

	/**
	 * Constructor. Decomposes a RInChI into a set of InChIs.
	 * @param rinchi RInChI string     * 
	 * @throws CDKException
	 */
	protected RInChIDecomposition(String rinchi) throws CDKException {
		this (rinchi, "");
	}

	/**
	 * Constructor. Decomposes a RInChI into a set of InChIs and RAuxInfos.
	 * @param rinchi RInChI string
	 * @param auxInfo RInChI aux info string
	 * @throws CDKException
	 */
	protected RInChIDecomposition(String rinchi, String auxInfo) throws CDKException {
		if (rinchi == null)
			throw new IllegalArgumentException("Null RInChI string provided");
		if (auxInfo == null)
			throw new IllegalArgumentException("Null RInChI aux info string provided");

		decompose (rinchi, auxInfo);
	}
	
	private void decompose(String rinchi, String auxInfo) throws CDKException {
		rinchiDecompositionOutput = JnaRinchi.decomposeRinchi(rinchi, auxInfo);
		
		if (rinchiDecompositionOutput.getStatus() == RinchiDecompositionStatus.ERROR) 
			throw new CDKException("RInChI decomposition error: " + rinchiDecompositionOutput.getErrorMessage());
	}
	
	/**
     * Access the status of the RInChI Decomposition output.
     * @return the status
     */
	public RinchiDecompositionStatus getStatus() { 
		return rinchiDecompositionOutput.getStatus();
	}
	
	/**
	 * Gets reaction component inchis.
	 */
	public String[] getIinchis() {
		return rinchiDecompositionOutput.getInchis();
	}
	
	/**
	 * Gets reaction component aux infos.
	 */
	public String[] getAuxInfos() {
		return rinchiDecompositionOutput.getAuxInfos();
	}
	
	/**
	 * Gets reaction component roles.
	 */
	public ReactionComponentRole[] getRoles() {
		return rinchiDecompositionOutput.getRoles();
	}
	
	/**
	 * Gets RInChI reaction direction.
	 */
	public ReactionDirection getReactionDirection() {
		return rinchiDecompositionOutput.getDirection();
	}

	/**
	 * Gets generated error messages.
	 */
	public String getErrorMessage() {
		return rinchiDecompositionOutput.getErrorMessage();
	}

}
