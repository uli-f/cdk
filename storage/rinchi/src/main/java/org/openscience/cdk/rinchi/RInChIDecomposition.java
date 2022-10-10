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

import java.util.*;

/**
 * This class decomposes a RInChI into the individual InChIs and auxiliary Information (if available) of each reaction component.
 * Moreover, components roles (reactant, product, agent) and the reaction direction are returned.
 * <br>
 * This class places calls to a JNA wrapper for the RInChI C++ library (io.github.dan2097.jnarinchi).
 *
 * @author Nikolay Kochev
 * @author Uli Fechner
 * @cdk.module rinchi
 * @cdk.githash
 */
public class RInChIDecomposition {
	protected final RinchiDecompositionOutput rinchiDecompositionOutput;

	/**
	 * Decomposes a RInChI into a set of InChIs.
	 * @param rinchi RInChI string
	 * @throws CDKException thrown if an error occurs
	 */
	protected RInChIDecomposition(String rinchi) throws CDKException {
		this (rinchi, "");
	}

	/**
	 * Decomposes a RInChI and its auxiliary information into a set of InChIs and AuxInfo.
	 * @param rinchi RInChI string
	 * @param auxInfo RInChI aux info string
	 * @throws CDKException thrown if an error occurs
	 */
	protected RInChIDecomposition(String rinchi, String auxInfo) throws CDKException {
		if (rinchi == null)
			throw new IllegalArgumentException("Null RInChI string provided");
		if (auxInfo == null)
			throw new IllegalArgumentException("Null RInChI aux info string provided");

		this.rinchiDecompositionOutput = decompose (rinchi, auxInfo);
	}
	
	private RinchiDecompositionOutput decompose(String rinchi, String auxInfo) throws CDKException {
		RinchiDecompositionOutput output = JnaRinchi.decomposeRinchi(rinchi, auxInfo);
		
		if (output.getStatus() == RinchiDecompositionStatus.ERROR)
			throw new CDKException("RInChI decomposition error: " + output.getErrorMessage());

		return output;
	}
	
	/**
     * Access the status of the RInChI Decomposition output.
     * @return the status
     */
	public RinchiDecompositionStatus getStatus() { 
		return rinchiDecompositionOutput.getStatus();
	}
	
	/**
	 * Returns reaction component InChIs.
	 * @return unmodifiable list of reaction components InChIs
	 */
	public List<String> getInchis() {
		return Collections.unmodifiableList(Arrays.asList(rinchiDecompositionOutput.getInchis()));
	}
	
	/**
	 * Returns reaction component aux infos.
	 * @return unmodifiable list of RInChI auxiliary information
	 */
	public List<String> getAuxInfo() {
		return Collections.unmodifiableList(Arrays.asList(rinchiDecompositionOutput.getAuxInfos()));
	}
	
	/**
	 * Return a list of reaction component roles.
	 * @return unmodifiable reaction component roles
	 */
	public List<ReactionComponentRole> getReactionComponentRoles() {
		return Collections.unmodifiableList(Arrays.asList(rinchiDecompositionOutput.getRoles()));
	}
	
	/**
	 * Returns RInChI reaction direction.
	 * @return the reaction direction of the RInChI
	 */
	public ReactionDirection getReactionDirection() {
		return rinchiDecompositionOutput.getDirection();
	}

	/**
	 * Gets generated error messages.
	 * @return generated error messages
	 */
	public String getErrorMessage() {
		return rinchiDecompositionOutput.getErrorMessage();
	}

	/**
	 * Returns a map with (InChI, auxiliary information) pairs.
	 * @return unmodifiable map of (InChI, AuxInfo) pairs
	 * @throws IllegalStateException thrown if there is a different number of InChIs and AuxInfo
	 */
	public Map<String, String> getInchiAuxInfoMap() {
		if (rinchiDecompositionOutput.getInchis() == null || rinchiDecompositionOutput.getAuxInfos() == null) {
			return Collections.unmodifiableMap(new HashMap<>());
		}

		if (rinchiDecompositionOutput.getInchis().length != rinchiDecompositionOutput.getAuxInfos().length) {
			throw new IllegalStateException("Different number of InChIs and AuxInfo: There are " +
					rinchiDecompositionOutput.getInchis().length + " InChIs and " + rinchiDecompositionOutput.getAuxInfos().length + " AuxInfo.");
		}

		Map<String,String> inchiAuxInfoMap = new HashMap<>(rinchiDecompositionOutput.getInchis().length);
		for (int i = 0; i < rinchiDecompositionOutput.getInchis().length; i++) {
			inchiAuxInfoMap.put(rinchiDecompositionOutput.getInchis()[i], rinchiDecompositionOutput.getAuxInfos()[i]);
		}

		return Collections.unmodifiableMap(inchiAuxInfoMap);
	}

	/**
	 * Returns a map with (InChI, reaction component role) pairs.
	 * @return unmodifiable map of (InChI, reaction component role) pairs
	 * @throws IllegalStateException thrown if the number of InChIs and reaction component roles is not equal
	 */
	public Map<String, ReactionComponentRole> getInchiReactionComponentRoleMap() {
		if (rinchiDecompositionOutput.getInchis() == null || rinchiDecompositionOutput.getRoles() == null) {
			return Collections.unmodifiableMap(new HashMap<>());
		}

		if (rinchiDecompositionOutput.getInchis().length != rinchiDecompositionOutput.getRoles().length) {
			throw new IllegalStateException("Different number of InChIs and reaction component roles: There are " +
					rinchiDecompositionOutput.getInchis().length + " InChIs and " + rinchiDecompositionOutput.getRoles().length + " reaction component roles.");
		}

		Map<String, ReactionComponentRole> inchiReactionComponentRolesMap = new HashMap<>(rinchiDecompositionOutput.getInchis().length);
		for (int i = 0; i < rinchiDecompositionOutput.getInchis().length; i++) {
			inchiReactionComponentRolesMap.put(rinchiDecompositionOutput.getInchis()[i], rinchiDecompositionOutput.getRoles()[i]);
		}

		return Collections.unmodifiableMap(inchiReactionComponentRolesMap);
	}
}
