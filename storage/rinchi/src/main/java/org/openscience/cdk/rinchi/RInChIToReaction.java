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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.Reaction;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.interfaces.ITetrahedralChirality;
import org.openscience.cdk.interfaces.ITetrahedralChirality.Stereo;
import org.openscience.cdk.io.MDLRXNV2000Reader;
import org.openscience.cdk.stereo.TetrahedralChirality;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import io.github.dan2097.jnainchi.InchiAtom;
import io.github.dan2097.jnainchi.InchiBond;
import io.github.dan2097.jnainchi.InchiBondStereo;
import io.github.dan2097.jnainchi.InchiStereo;
import io.github.dan2097.jnainchi.InchiStereoParity;
import io.github.dan2097.jnainchi.InchiStereoType;
import io.github.dan2097.jnarinchi.FileTextOutput;
import io.github.dan2097.jnarinchi.FileTextStatus;
import io.github.dan2097.jnarinchi.JnaRinchi;
import io.github.dan2097.jnarinchi.ReactionFileFormat;
import io.github.dan2097.jnarinchi.RinchiInput;
import io.github.dan2097.jnarinchi.RinchiInputComponent;
import io.github.dan2097.jnarinchi.RinchiInputFromRinchiOutput;
import io.github.dan2097.jnarinchi.RinchiStatus;
import io.github.dan2097.jnarinchi.cheminfo.StereoUtils.MolCoordinatesType;
import io.github.dan2097.jnarinchi.cheminfo.StereoUtils;

/**
 * <p>This class generates a CDK IReaction from a RInChI string (and optionally aux info).
 * It places calls to a JNA wrapper for the RInChI C++ library (io.github.dan2097.jnarinchi).
 * Native C++ code exports output only as MDL RXN/RDfile formats. Therefore IReacton generation
 * process includes conversion from MDL format. The latter can be done in two ways:
 * (1) using JnaRinchi converter (default);
 * (2) using CDK MDL RXN Reader (set via input flag variable: useCDK_MDL_IO = true).
 *
 * 
 *
 * @author Nikolay Kochev
 *
 * @cdk.module rinchi
 * @cdk.githash
 */
public class RInChIToReaction {
	
	protected RinchiInputFromRinchiOutput rInpFromRinchiOutput = null;
	
	protected FileTextOutput fileTextOutput = null;
	
	protected IReaction reaction;
	
	protected boolean useCDK_MDL_IO = false;
	
	protected boolean configureMolecules = false;
	
	private List<String> reactionGenerationErrors = new ArrayList<>();
	private String curComponentErrorContext = "";
		
	/**
     * Constructor. Generates CDK Reaction from RInChI.
     * @param rinchi RInChI string     * 
     * @throws CDKException
     */
	protected RInChIToReaction(String rinchi) throws CDKException {
		this (rinchi, "", false);
	}
	
	/**
     * Constructor. Generates CDK Reaction from RInChI.
     * @param rinchi RInChI string
     * @param auxInfo RInChI aux info string
     * @throws CDKException
     */
	protected RInChIToReaction(String rinchi, String auxInfo) throws CDKException {
		this (rinchi, auxInfo, false);
	}
	
	/**
     * Constructor. Generates CDK Reaction from RInChI.
     * @param rinchi RInChI string
     * @param auxInfo RInChI aux info string
     * @param useCDK_MDL_IO determines whether to use CDK MDL RXN Reader
     * @throws CDKException
     */
	protected RInChIToReaction(String rinchi, String auxInfo, boolean useCDK_MDL_IO) throws CDKException {
		if (rinchi == null)
			throw new IllegalArgumentException("Null RInChI string provided");
		if (auxInfo == null)
			throw new IllegalArgumentException("Null RInChI aux info string provided");
		
		this.useCDK_MDL_IO = useCDK_MDL_IO;
		if (useCDK_MDL_IO) {
			//Using CDK RXN Reader to make a Reaction object directly from the file text output
			fileTextOutput = JnaRinchi.rinchiToFileText(rinchi, auxInfo, ReactionFileFormat.RXN);
			generateReactionFromMDLRXNFile();
		}			
		else {	
			this.rInpFromRinchiOutput = JnaRinchi.getRinchiInputFromRinchi(rinchi, auxInfo);
			generateReactionFromRinchi();
		}	
	}
	
	/**
     * Gets reaction from RnChI, and converts RInChI library data structure (RinchiInput object)
     * into an IReaction.
     *
     * @throws CDKException
     */
    protected void generateReactionFromRinchi() throws CDKException {
    	if (rInpFromRinchiOutput.getStatus() == RinchiStatus.ERROR)
    		throw new CDKException(rInpFromRinchiOutput.getErrorMessage());
    	    	
    	RinchiInput rInput = rInpFromRinchiOutput.getRinchInput();
    	reaction = new Reaction();
    	List<RinchiInputComponent> compList = rInput.getComponents();
    	for (int i = 0; i < compList.size(); i++) {    		
    		RinchiInputComponent ric = compList.get(i);
    		curComponentErrorContext = "Component " + (i+1) + " ";
    		IAtomContainer mol = getComponentMolecule(ric);
    		if (mol != null) {    		
    			switch (ric.getRole()) {
    			case REAGENT:
    				reaction.addReactant(mol);
    				break;
    			case PRODUCT:
    				reaction.addProduct(mol);
    				break;
    			case AGENT:
    				reaction.addAgent(mol);
    				break;
    			}
    		}
    	}
    	
    	if (!reactionGenerationErrors.isEmpty()) {
    		//Replacing rInpFromRinchiOutput with a new one with status ERROR
    		this.rInpFromRinchiOutput = new RinchiInputFromRinchiOutput(rInput, RinchiStatus.ERROR, -1, 
    				"Unable to create Reaction object from RinchiInput: " + getAllReactionGenerationErrors());
    		throw new CDKException(rInpFromRinchiOutput.getErrorMessage());
    	}
    }
    
        
    /**
     * Gets reaction from MDL RXN file obtained from RInChI library data structure.
     *
     * @throws CDKException
     */
    protected void generateReactionFromMDLRXNFile() throws CDKException {
    	if (fileTextOutput.getStatus() == FileTextStatus.ERROR)
    		throw new CDKException(fileTextOutput.getErrorMessage());
    		
    	try {
    		BufferedReader inputBufReader = new BufferedReader(
    				new StringReader(fileTextOutput.getReactionFileText()));
    		MDLRXNV2000Reader reader = new MDLRXNV2000Reader(inputBufReader);
    		IReaction reaction = new Reaction();
    		reaction = reader.read(reaction);     	
    		this.rInpFromRinchiOutput = new RinchiInputFromRinchiOutput(null, RinchiStatus.SUCCESS, 0, "");
    		this.reaction = reaction;
    		reader.close();
    	} 
    	catch (Exception e) {
    		this.rInpFromRinchiOutput = new RinchiInputFromRinchiOutput(null, RinchiStatus.ERROR, -1, 
    				"Error on generating Reaction via MDL RXN Reader: " + e.getMessage());
    		throw new CDKException(rInpFromRinchiOutput.getErrorMessage());
    	}
    }
    
    private IAtomContainer getComponentMolecule(RinchiInputComponent ric) {
    	IAtomContainer mol = new AtomContainer();
    	Map<InchiAtom,IAtom> inchiAtom2AtomMap = new HashMap<>();
    	MolCoordinatesType molCoordType = StereoUtils.getMolCoordinatesType(ric);
    	//Convert atoms
    	for (int i = 0; i < ric.getAtoms().size(); i++) {    		
    		InchiAtom iAt = ric.getAtoms().get(i);
    		IAtom atom = getAtom(iAt, molCoordType);
    		if (atom != null) {
    			inchiAtom2AtomMap.put(iAt, atom);
    			mol.addAtom(atom);
    		}
    	}
    	//Convert bonds
    	for (int i = 0; i < ric.getBonds().size(); i++) {    		
    		InchiBond iBo = ric.getBonds().get(i);
    		IBond bond = getBond(iBo, inchiAtom2AtomMap);
    		if (bond != null) 
    			mol.addBond(bond);
    	}
    	//Convert stereos
    	if (!ric.getStereos().isEmpty()) {
    		List<IStereoElement> stereoElements = new ArrayList<>();
    		for (InchiStereo stereo : ric.getStereos()) {
    			IStereoElement stereoEl = inchiStereoToCDKStereoElement (stereo, inchiAtom2AtomMap);
    			if (stereoEl != null)
    				stereoElements.add(stereoEl);
    		}
    		if (!stereoElements.isEmpty())
    			mol.setStereoElements(stereoElements);
    	}
    	
    	if (configureMolecules) {
    		try {
    			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
    		}
    		catch (Exception x) {
    			reactionGenerationErrors.add(curComponentErrorContext + 
    					"AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms: " + x.getMessage());
    		}
    	}	

    	return mol;
    }
    
    private IAtom getAtom(InchiAtom iAt, MolCoordinatesType coordType) {
    	IAtom atom = new Atom(iAt.getElName());
    	//Set charge
    	int q = iAt.getCharge();
    	if (q != 0)
    		atom.setFormalCharge(q);
    	
    	atom.setImplicitHydrogenCount(iAt.getImplicitHydrogen());
    	
    	//Set isotope
        if(iAt.getIsotopicMass() > 0)        	
        	atom.setMassNumber(iAt.getIsotopicMass());

        //Set coordinates 2D or 3D
        if (coordType == MolCoordinatesType._2D) {
        	Point2d p = new Point2d();
        	p.x = iAt.getX();
        	p.y = iAt.getY();
        	atom.setPoint2d(p);
        } 
        else if (coordType == MolCoordinatesType._3D) {
        	Point3d p = new Point3d();
        	p.x = iAt.getX();
        	p.y = iAt.getY();
        	p.z = iAt.getZ();
        	atom.setPoint3d(p);
        }
        
    	return atom;
    }
    
    private IBond getBond(InchiBond iBo, Map<InchiAtom,IAtom> inchiAtom2AtomMap) {
    	IAtom at0 = inchiAtom2AtomMap.get(iBo.getStart());
    	IAtom at1 = inchiAtom2AtomMap.get(iBo.getEnd());
    	IBond.Order order = null;
    	switch (iBo.getType()) {
    	case SINGLE:
    		order = IBond.Order.SINGLE;
    		break;
    	case DOUBLE:
    		order = IBond.Order.DOUBLE;
    		break;
    	case TRIPLE:
    		order = IBond.Order.TRIPLE;
    		break;	
    	}
    	
    	IBond.Stereo stereo = inchiBondStereoToCDKBondStereoTo(iBo.getStereo());
    	
    	if (order == null || at0 == null || at1 == null) {
    		reactionGenerationErrors.add(curComponentErrorContext + 
    				"Unable to convert InchiBond to CDK bond: " + order.toString());
			return null;
		}	
		else
			return new Bond(at0, at1, order, stereo);
    }
    
    private IBond.Stereo inchiBondStereoToCDKBondStereoTo(InchiBondStereo inchiBondStereo) {
		switch (inchiBondStereo) {
		case SINGLE_1DOWN:
			return IBond.Stereo.DOWN;
		case SINGLE_2DOWN:
			return IBond.Stereo.DOWN_INVERTED;
		case SINGLE_1UP:
			return IBond.Stereo.UP;
		case SINGLE_2UP:
			return IBond.Stereo.UP_INVERTED;
		case SINGLE_1EITHER:
			return IBond.Stereo.UP_OR_DOWN;
		case SINGLE_2EITHER:
			return IBond.Stereo.UP_OR_DOWN_INVERTED;	
		case DOUBLE_EITHER:
			return IBond.Stereo.E_OR_Z;	
		}
    	return IBond.Stereo.NONE;
	}
    
    private IStereoElement inchiStereoToCDKStereoElement (InchiStereo inchiStereo, Map<InchiAtom,IAtom> inchiAtom2AtomMap) {
    	if (inchiStereo.getType() == InchiStereoType.Tetrahedral) {
    		//Within CDK implicit hydrogen and lone pairs are encoded 
    		//by adding the central atom in the ligand list
    		//In InchiStereo there is a special atom, InchiStereo.STEREO_IMPLICIT_H,
    		//used for implicit H ligand. 
    		//The lone pairs are treated the same way in CDK and InchiStereo.
    		IAtom chiralAtom = inchiAtom2AtomMap.get(inchiStereo.getCentralAtom());
    		IAtom[] ligands = new IAtom[4];
    		
    		InchiAtom iAt = inchiStereo.getAtoms()[0];
    		if (iAt == InchiStereo.STEREO_IMPLICIT_H)
    			iAt = inchiStereo.getCentralAtom(); //mapping central atom to get IAtom ligand
    		ligands[0] = inchiAtom2AtomMap.get(iAt);
    		
    		iAt = inchiStereo.getAtoms()[1];
    		if (iAt == InchiStereo.STEREO_IMPLICIT_H)
    			iAt = inchiStereo.getCentralAtom(); //mapping central atom to get IAtom ligand
    		ligands[1] = inchiAtom2AtomMap.get(iAt);
    		
    		iAt = inchiStereo.getAtoms()[2];
    		if (iAt == InchiStereo.STEREO_IMPLICIT_H)
    			iAt = inchiStereo.getCentralAtom(); //mapping central atom to get IAtom ligand
    		ligands[2] = inchiAtom2AtomMap.get(iAt);
    		
    		iAt = inchiStereo.getAtoms()[3];
    		if (iAt == InchiStereo.STEREO_IMPLICIT_H)
    			iAt = inchiStereo.getCentralAtom(); //mapping central atom to get IAtom ligand
    		ligands[3] = inchiAtom2AtomMap.get(iAt);
    		
    		Stereo stereo = Stereo.ANTI_CLOCKWISE;
    		//TODO handle stereo conversion
    		return new TetrahedralChirality(chiralAtom, ligands, stereo);
    	}
    	
    	//TODO hadnle other types of stereo elements
    	
    	return null;
    }
    
    private String getAllReactionGenerationErrors() {
		StringBuilder sb = new StringBuilder(); 
		for (String err: reactionGenerationErrors)
			sb.append(err).append("\n");
		return sb.toString();
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
		if (rInpFromRinchiOutput != null)
			return rInpFromRinchiOutput.getStatus();
		else
			return null;
	}	
	
	/**
     * Gets generated error messages.
     */
    public String getErrorMessage() {
        return rInpFromRinchiOutput.getErrorMessage();
    }
    
    /**
     * Gets flag for using CDK MDL input/output utilities.
     */
	public boolean isUseCDK_MDL_IO() {
		return useCDK_MDL_IO;
	}
	/**
     * Gets the RinchiInput object used for data conversion. 
     * Function returns null when isUseCDK_MDL_IO() == null
     */
	public RinchiInput getResultRinchiInputObject() {
		if (rInpFromRinchiOutput != null)
			return rInpFromRinchiOutput.getRinchInput();
		else 
			return null;
	}
    
}
