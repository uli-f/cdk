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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.io.MDLRXNWriter;
import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

import io.github.dan2097.jnainchi.InchiAtom;
import io.github.dan2097.jnainchi.InchiBond;
import io.github.dan2097.jnainchi.InchiBondType;
import io.github.dan2097.jnarinchi.JnaRinchi;
import io.github.dan2097.jnarinchi.ReactionComponentRole;
import io.github.dan2097.jnarinchi.ReactionFileFormat;
import io.github.dan2097.jnarinchi.RinchiInput;
import io.github.dan2097.jnarinchi.RinchiInputComponent;
import io.github.dan2097.jnarinchi.RinchiKeyOutput;
import io.github.dan2097.jnarinchi.RinchiKeyStatus;
import io.github.dan2097.jnarinchi.RinchiKeyType;
import io.github.dan2097.jnarinchi.RinchiOptions;
import io.github.dan2097.jnarinchi.RinchiOutput;
import io.github.dan2097.jnarinchi.RinchiStatus;

/**
 * <p>This class generates the IUPAC Reaction International Chemical Identifier (RInChI) for
 * a CDK IReaction object. 
 * It places calls to a JNA wrapper for the RInChI C++ library (io.github.dan2097.jnarinchi).
 * Native C++ code takes as an input only MDL RXN/RDfile formats. Therefore RInChI generation
 * process includes IReaction object conversion to MDL format. The latter can be done in two ways:
 * (1) using JnaRinchi converter (default);
 * (2) using CDK MDL RXN Writer (set via input flag variable: useCDK_MDL_IO = true). 
 *
 * <p>If the atom container has 3D coordinates for all of its atoms then they
 * will be used, otherwise 2D coordinates will be used if available.
 * 
 *
 * @author Nikolay Kochev
 * @cdk.module rinchi
 * @cdk.githash
 */

public class RInChIGenerator {
	
	private static final RinchiOptions DEFAULT_OPTIONS = RinchiOptions.DEFAULT_OPTIONS;
	
	private static final ILoggingTool LOGGER = LoggingToolFactory.createLoggingTool(RInChIGenerator.class);
	
	protected RinchiInput input;	
	protected RinchiOutput rinchiOutput;
	protected RinchiKeyOutput shortRinchiKeyOutput = null;
	protected RinchiKeyOutput longRinchiKeyOutput = null;
	protected RinchiKeyOutput webRinchiKeyOutput = null;
	protected IReaction reaction;
	protected RinchiOptions options;
	protected List<String> rinchiInputGenerationErrors = new ArrayList<>();
	
	protected boolean useCDK_MDL_IO = false;
	
	
	/**
     * <p>Constructor. Generates RInChI from CDK Reaction.
     *
     * <p>Reads atoms, bonds etc from atom containers and converts to format
     * RInChI library requires, then calls the library.
     *
     * @param reaction	Reaction to generate RInChI for.    
     * @throws org.openscience.cdk.exception.CDKException if there is an
     *                                                    error during RInChI generation
     */
	protected RInChIGenerator (IReaction reaction) throws CDKException {
		this(reaction, DEFAULT_OPTIONS, false);
	}
	
	/**
     * <p>Constructor. Generates RInChI from CDK Reaction.
     *
     * <p>Reads atoms, bonds etc from atom containers and converts to format
     * RInChI library requires, then calls the library.
     *
     * @param reaction	Reaction to generate RInChI for. 
     * @param options RInChI generation options (in the format of the JnaRinchi library)   
     * @throws org.openscience.cdk.exception.CDKException if there is an
     *                                                    error during RInChI generation
     */
	protected RInChIGenerator (IReaction reaction, RinchiOptions options) throws CDKException {
		this(reaction, options, false);
	}	
	
	/**
     * <p>Constructor. Generates RInChI from CDK Reaction.
     *
     * <p>Reads atoms, bonds etc from atom containers and converts to format
     * RInChI library requires, then calls the library.
     *
     * @param reaction	Reaction to generate RInChI for. 
     * @param optStr RInChI generation options set as string (options are space or comma separated)   
     * @throws org.openscience.cdk.exception.CDKException if there is an
     *                                                    error during RInChI generation
     */
	protected RInChIGenerator (IReaction reaction, String optStr) throws CDKException {
		this(reaction, RInChIOptionParser.parseString(optStr), false);
	}
	
	/**
     * <p>Constructor. Generates RInChI from CDK Reaction.
     *
     * <p>Reads atoms, bonds etc from atom containers and converts to format
     * RInChI library requires, then calls the library.  
     * IReaction object conversion to MDL format can be done in two ways:
     * (1) using JnaRinchi converter (useCDK_MDL_IO = false);
     * (2) using CDK MDL RXN Writer (useCDK_MDL_IO = true). 
     *
     * @param reaction	Reaction to generate RInChI for. 
     * @param options RInChI generation options (in the format of the JnaRinchi library) 
     * @param useCDK_MDL_IO determines whether to use CDK MDL RXN Writer  
     * @throws org.openscience.cdk.exception.CDKException if there is an
     *                                                    error during RInChI generation
     */
	protected RInChIGenerator (IReaction reaction, RinchiOptions options, boolean useCDK_MDL_IO) throws CDKException {
		this.reaction = reaction;
		this.options = options;
		this.useCDK_MDL_IO = useCDK_MDL_IO;		
		generateRinchiFromReaction();
	}
	
	private void generateRinchiFromReaction() throws CDKException {
		if (reaction == null)
			throw new CDKException("Null reaction object!");	
		
		if (useCDK_MDL_IO) {
			//Using CDK MDLRXNWriter to Serialize Reaction to MDL RXN.
			//Then RXN file text is used as an input to JnaRinchi
			try {
				// Serialize Reaction to MDL RXN
				StringWriter writer = new StringWriter(10000);		        
				MDLRXNWriter mdlWriter = new MDLRXNWriter(writer);				
				mdlWriter.write(reaction);
				mdlWriter.close();		        
				String fileText = writer.toString(); 
				rinchiOutput = JnaRinchi.fileTextToRinchi(ReactionFileFormat.RXN, fileText, options);
				//if (rinchiOutput.getStatus() == RinchiStatus.ERROR)
				//	LOGGER.debug("MDL RXN file text\n" + fileText);
			}
			catch (Exception x) {
				String errMsg = "Unable to write MDL RXN file for reaction: " + x.getMessage();
				rinchiOutput = new RinchiOutput("", "", RinchiStatus.ERROR, -1, errMsg);;
			}			
		}
		else {
			RinchiInput rInp = getRinchiInputFromReaction();
			if (rInp == null) {
				String errMsg = "Unable to convert CDK Reaction to RinchiInput: " + getAllRinchiInputGenerationErrors();
				rinchiOutput = new RinchiOutput("", "", RinchiStatus.ERROR, -1, errMsg);
			} 
			else 
				rinchiOutput = JnaRinchi.toRinchi(rInp, options);			
		}
		
		if (rinchiOutput.getStatus() == RinchiStatus.ERROR)
			throw new CDKException("RInChI generation problem: " + rinchiOutput.getErrorMessage());	
	}
	
	private void generateRInChIKey(RinchiKeyType type) throws CDKException {
		RinchiKeyOutput rkOut;
		if (rinchiOutput.getStatus() == RinchiStatus.ERROR) {
			String err = "Unable to generate RInChIKey since, no RInChI is generated!";
			rkOut = new RinchiKeyOutput("", type, RinchiKeyStatus.ERROR, -1, err);
		}	
		rkOut = JnaRinchi.rinchiToRinchiKey(type, rinchiOutput.getRinchi());
		
		switch (type) {
		case SHORT:
			shortRinchiKeyOutput = rkOut;
			break;
		case LONG:
			longRinchiKeyOutput = rkOut;
			break;
		case WEB:
			webRinchiKeyOutput = rkOut;
			break;	
		}
		
		if (rkOut.getStatus() == RinchiKeyStatus.ERROR)
			throw new CDKException("RInChIKey generation problem: " + rkOut.getErrorMessage());
	}
	
	private RinchiInput getRinchiInputFromReaction() {
		RinchiInput rinchiInput = new RinchiInput();
		IAtomContainerSet acs;
		
		acs = reaction.getReactants();
		for (int i = 0; i < acs.getAtomContainerCount(); i++) {
			IAtomContainer mol = acs.getAtomContainer(i);
			RinchiInputComponent ric = getRinchiInputCompoment(mol);
			ric.setRole(ReactionComponentRole.REAGENT);
			rinchiInput.addComponent(ric);
		}
		
		acs = reaction.getProducts();
		for (int i = 0; i < acs.getAtomContainerCount(); i++) {
			IAtomContainer mol = acs.getAtomContainer(i);
			RinchiInputComponent ric = getRinchiInputCompoment(mol);
			ric.setRole(ReactionComponentRole.PRODUCT);
			rinchiInput.addComponent(ric);
		}
		
		acs = reaction.getAgents();
		for (int i = 0; i < acs.getAtomContainerCount(); i++) {
			IAtomContainer mol = acs.getAtomContainer(i);
			RinchiInputComponent ric = getRinchiInputCompoment(mol);
			ric.setRole(ReactionComponentRole.AGENT);
			rinchiInput.addComponent(ric);
		}
		
		if (rinchiInputGenerationErrors.isEmpty())
			return rinchiInput;
		else
			return null;
	}
	
	private RinchiInputComponent getRinchiInputCompoment(IAtomContainer mol) {
		RinchiInputComponent ric = new RinchiInputComponent();
		Map<IAtom,InchiAtom> atomInchiAtomMap = new HashMap<>();
		//Convert atoms
		for (int i = 0; i < mol.getAtomCount(); i++) {
			IAtom atom = mol.getAtom(i);
			InchiAtom iAt = getInchiAtom(atom);
			if (iAt != null) {
				atomInchiAtomMap.put(atom, iAt);
				ric.addAtom(iAt);
			}	
		}
		//Convert bonds
		for (int i = 0; i < mol.getBondCount(); i++) {
			IBond bond = mol.getBond(i);
			InchiBond iBo = getInchiBond (bond, atomInchiAtomMap);
			if (iBo != null)
				ric.addBond(iBo);
		}
		return ric;
	}
	
	private InchiAtom getInchiAtom (IAtom atom) {		
		//TODO handle non standard atoms e.g. IPseudoAtom
		String atSymbol = atom.getSymbol();
		InchiAtom inchiAtom = new InchiAtom(atSymbol);
		//Set charge
		Integer q = atom.getFormalCharge();
        if (q == null)
            q = 0;
        inchiAtom.setCharge(q);
        
        //TODO Set isotope
        
        //Set coordinates: 3D takes precedence 
        if (atom.getPoint3d() != null) {
        	inchiAtom.setX(atom.getPoint3d().x);
        	inchiAtom.setY(atom.getPoint3d().y);
        	inchiAtom.setZ(atom.getPoint3d().z);
        }
        else if (atom.getPoint2d() != null) {
        	inchiAtom.setX(atom.getPoint2d().x);
        	inchiAtom.setY(atom.getPoint2d().y);
        }
		return inchiAtom;
	}
	
	private InchiBond getInchiBond (IBond bond, Map<IAtom,InchiAtom> atomInchiAtomMap) {
		InchiAtom at0 = atomInchiAtomMap.get(bond.getAtom(0));
		InchiAtom at1 = atomInchiAtomMap.get(bond.getAtom(1));
		InchiBondType boType = null;
		switch (bond.getOrder()) {
		case SINGLE:
			boType = InchiBondType.SINGLE;
			break;
		case DOUBLE:
			boType = InchiBondType.DOUBLE;
			break;
		case TRIPLE:
			boType = InchiBondType.TRIPLE;
			break;	
		}
		if (boType == null || at0 == null || at1 == null) {
			rinchiInputGenerationErrors.add("Unable to convert CDK bond to InchiBond: " 
					+ bond.getOrder().toString());
			return null;
		}	
		else
			return new InchiBond(at0, at1, boType);
	}
	
	private String getAllRinchiInputGenerationErrors() {
		StringBuilder sb = new StringBuilder(); 
		for (String err: rinchiInputGenerationErrors)
			sb.append(err).append("\n");
		return sb.toString();
	}
	
	/**
     * Gets generated RInChI string.
     */
	public String getRInChI() {
		return rinchiOutput.getRinchi();
	}
	
	/**
     * Gets auxillary information.
     */
	public String getAuxInfo() {
		return rinchiOutput.getAuxInfo();
	}
	
	/**
	 * Gets generated error messages.
	 */
	public String getRInChIErrorMessage() {
		return rinchiOutput.getErrorMessage();
	}
	
	/**
     * Access the status of the RInChI output.
     * @return the status
     */
	public RinchiStatus getRInChIStatus() {
		return rinchiOutput.getStatus();
	}
	
	/**
     * Gets (generates) Short-RInChIKey.
     */
	public String getShortRInChIKey() throws CDKException {		
		if (shortRinchiKeyOutput == null)
			generateRInChIKey(RinchiKeyType.SHORT);
		return shortRinchiKeyOutput.getRinchiKey();
	}
	
	/**
     * Gets (generates) Long-RInChIKey.
     */
	public String getLongRInChIKey() throws CDKException {		
		if (longRinchiKeyOutput == null)
			generateRInChIKey(RinchiKeyType.LONG);
		return longRinchiKeyOutput.getRinchiKey();
	}	
	
	/**
     * Gets (generates) Web-RInChIKey.
     */
	public String getWebRInChIKey() throws CDKException {		
		if (webRinchiKeyOutput == null)
			generateRInChIKey(RinchiKeyType.WEB);
		return webRinchiKeyOutput.getRinchiKey();
	}	
	
	/**
	 * Gets generated error messages for RInChIKey.
	 */
	public String getRInChIKeyErrorMessage(RinchiKeyType type) {
		switch (type) {
		case SHORT:
			if (shortRinchiKeyOutput != null)
				return shortRinchiKeyOutput.getErrorMessage();
		case LONG:
			if (longRinchiKeyOutput != null)
				return longRinchiKeyOutput.getErrorMessage();
		case WEB:
			if (webRinchiKeyOutput != null)
				return webRinchiKeyOutput.getErrorMessage();	
		}
		return "";
	}
	
	/**
     * Gets flag for using CDK MDL input/output utilities.
     */
	public boolean isUseCDK_MDL_IO() {
		return useCDK_MDL_IO;
	}
		
}
