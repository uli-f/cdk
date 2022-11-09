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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.openscience.cdk.*;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.interfaces.ITetrahedralChirality.Stereo;
import org.openscience.cdk.io.MDLRXNV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer.SPIN_MULTIPLICITY;
import org.openscience.cdk.stereo.StereoElementFactory;
import org.openscience.cdk.stereo.TetrahedralChirality;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import io.github.dan2097.jnainchi.InchiAtom;
import io.github.dan2097.jnainchi.InchiBond;
import io.github.dan2097.jnainchi.InchiBondStereo;
import io.github.dan2097.jnainchi.InchiRadical;
import io.github.dan2097.jnainchi.InchiStereo;
import io.github.dan2097.jnainchi.InchiStereoParity;
import io.github.dan2097.jnainchi.InchiStereoType;
import io.github.dan2097.jnarinchi.FileTextOutput;
import io.github.dan2097.jnarinchi.JnaRinchi;
import io.github.dan2097.jnarinchi.ReactionFileFormat;
import io.github.dan2097.jnarinchi.RinchiInput;
import io.github.dan2097.jnarinchi.RinchiInputComponent;
import io.github.dan2097.jnarinchi.RinchiInputFromRinchiOutput;
import io.github.dan2097.jnarinchi.Status;
import io.github.dan2097.jnarinchi.cheminfo.StereoUtils.MolCoordinatesType;
import io.github.dan2097.jnarinchi.cheminfo.StereoUtils;

/**
 * This class generates the IUPAC Reaction International Chemical Identifier (RInChI) for a CDK IReaction object.
 * <br>
 * This class places calls to a JNA wrapper for the RInChI C++ library (io.github.dan2097.jnarinchi).
 * Native C++ code takes as an input only MDL RXN and MDL RDfile formats. Therefor, the RInChI generation
 * process includes the conversion from an IReaction object to MDL format. This conversion can be carried out by
 * <ol>
 *     <li>JnaRinchi converter (default)</li>
 *     <li>CDK MDL RXN Writer (by providing a value of <code>true</code> for the argument <code>useCDK_MDL_IO</code>
 *     when instantiating this object via its the Factory class method
 *     {@link RInChIGeneratorFactory#getRInChIToReaction(String, String, boolean)}).</li>
 * </ol>
 *
 * @author Nikolay Kochev
 * @cdk.module rinchi
 * @cdk.githash
 */
public class RInChIToReaction {
	protected RinchiInputFromRinchiOutput rInpFromRinchiOutput = null;
	protected FileTextOutput fileTextOutput = null;
	protected IReaction reaction;
	protected final boolean useCDK_MDL_IO;
	protected boolean configureReactionComponents = false;
	private final List<String> reactionGenerationErrors = new ArrayList<>();
	private String curComponentErrorContext = "";
		
	/**
     * Consumes a RInChI and produces a CDK Reaction.
     * @param rinchi RInChI string
     * @throws CDKException if an error is encountered
     */
	protected RInChIToReaction(String rinchi) throws CDKException {
		this (rinchi, "", false, false);
	}
	
	/**
	 * Consumes a RInChI with associated auxiliary information and produces a CDK Reaction.
     * @param rinchi RInChI string
     * @param auxInfo RInChI auxiliary information (AuxInfo) string
     * @throws CDKException if an error is encountered
     */
	protected RInChIToReaction(String rinchi, String auxInfo) throws CDKException {
		this (rinchi, auxInfo, false, false);
	}
	
	/**
	 * Consumes a RInChI with associated auxiliary information and produces a CDK Reaction.
     * @param rinchi RInChI string
	 * @param auxInfo RInChI auxiliary information (AuxInfo) string
     * @param useCDK_MDL_IO determines whether to use CDK MDL RXN Reader for the conversion
     * @throws CDKException if an error is encountered
     */
	protected RInChIToReaction(String rinchi, String auxInfo, boolean useCDK_MDL_IO) throws CDKException {
		this (rinchi, auxInfo, useCDK_MDL_IO, false);
	}
	
	/**
	 * Consumes a RInChI with associated auxiliary information and produces a CDK Reaction.
     * @param rinchi RInChI string
	 * @param auxInfo RInChI auxiliary information (AuxInfo) string
     * @param useCDK_MDL_IO determines whether to use CDK MDL RXN Reader for the conversion
     * @param configureReactionComponents determines whether to configure CDK AtomContainer objects storing reaction components
     * @throws CDKException if an error is encountered
     */
	protected RInChIToReaction(String rinchi, String auxInfo, boolean useCDK_MDL_IO, boolean configureReactionComponents) throws CDKException {
		if (rinchi == null)
			throw new IllegalArgumentException("Null RInChI string provided");
		if (auxInfo == null)
			throw new IllegalArgumentException("Null RInChI aux info string provided");
		
		this.useCDK_MDL_IO = useCDK_MDL_IO;
		this.configureReactionComponents = configureReactionComponents; 

		if (useCDK_MDL_IO) {
			// use CDK RXN Reader to make a Reaction object directly from the file text output
			fileTextOutput = JnaRinchi.rinchiToFileText(rinchi, auxInfo, ReactionFileFormat.RXN);
			generateReactionFromMDLRXNFile();
		}			
		else {
			this.rInpFromRinchiOutput = JnaRinchi.getRinchiInputFromRinchi(rinchi, auxInfo);
			generateReactionFromRinchi();
		}	
	}
	
	/**
     * Produces a reaction from RnChI.
	 * The RInChI library data structure (RinchiInput object) is converted to an {@link IReaction}.
     *
     * @throws CDKException if an error is encountered
     */
    protected void generateReactionFromRinchi() throws CDKException {
    	if (rInpFromRinchiOutput.getStatus() == Status.ERROR)
    		throw new CDKException(rInpFromRinchiOutput.getErrorMessage());

    	RinchiInput rinchInput = rInpFromRinchiOutput.getRinchiInput();
    	reaction = DefaultChemObjectBuilder.getInstance().newInstance(IReaction.class);

    	List<RinchiInputComponent> rinchInputComponents = rinchInput.getComponents();
    	for (int i = 0; i < rinchInputComponents.size(); i++) {
    		RinchiInputComponent rinchiInputComponent = rinchInputComponents.get(i);
    		curComponentErrorContext = "Component " + (i+1) + " ";
    		IAtomContainer atomContainer = getComponentMolecule(rinchiInputComponent);

			//Empty structures are allowed
    		if (atomContainer != null) {
    			switch (rinchiInputComponent.getRole()) {
    			case REAGENT:
    				reaction.addReactant(atomContainer);
    				break;
    			case PRODUCT:
    				reaction.addProduct(atomContainer);
    				break;
    			case AGENT:
    				reaction.addAgent(atomContainer);
    				break;
    			}
    		}
    	}
    	
    	if (!reactionGenerationErrors.isEmpty()) {
    		//Replacing rInpFromRinchiOutput with a new one with status ERROR
    		this.rInpFromRinchiOutput = new RinchiInputFromRinchiOutput(rinchInput, Status.ERROR, -1,
    				"Unable to create Reaction object from RinchiInput: " + getAllReactionGenerationErrors());
    		throw new CDKException(rInpFromRinchiOutput.getErrorMessage());
    	}
    }
    
        
    /**
	 * Produces a reaction from RnChI.
	 * The MDL RXN file is obtained from the RInChI library data structure and then
	 * converted to an {@link IReaction} by the {@link MDLRXNV2000Reader} of CDK.
	 *
	 * @throws CDKException if an error is encountered
     */
    protected void generateReactionFromMDLRXNFile() throws CDKException {
    	if (fileTextOutput.getStatus() == Status.ERROR)
    		throw new CDKException(fileTextOutput.getErrorMessage());
    		
    	try {
    		BufferedReader bufferedReader = new BufferedReader(new StringReader(fileTextOutput.getReactionFileText()));
    		MDLRXNV2000Reader reader = new MDLRXNV2000Reader(bufferedReader);
    		this.reaction = reader.read(DefaultChemObjectBuilder.getInstance().newInstance(IReaction.class));
    		this.rInpFromRinchiOutput = new RinchiInputFromRinchiOutput(null, Status.SUCCESS, 0, "");
    		reader.close();
    	} 
    	catch (Exception exception) {
    		this.rInpFromRinchiOutput = new RinchiInputFromRinchiOutput(null, Status.ERROR, -1, 
    				"Error on generating Reaction via MDL RXN Reader: " + exception.getMessage());
    		throw new CDKException(rInpFromRinchiOutput.getErrorMessage());
    	}
    }
    
    private IAtomContainer getComponentMolecule(RinchiInputComponent rinchiInputComponent) {
    	final IAtomContainer atomContainer = new AtomContainer();
    	final Map<InchiAtom,IAtom> inchiAtom2AtomMap = new HashMap<>();
    	final MolCoordinatesType molCoordinatesType = StereoUtils.getMolCoordinatesType(rinchiInputComponent);
    	
    	//Convert atoms
    	for (int i = 0; i < rinchiInputComponent.getAtoms().size(); i++) {
    		final InchiAtom inchiAtom = rinchiInputComponent.getAtoms().get(i);
    		final IAtom atom = getAtom(inchiAtom, molCoordinatesType);
   			inchiAtom2AtomMap.put(inchiAtom, atom);
  			atomContainer.addAtom(atom);
    	}
    	
    	//Convert bonds
    	for (int i = 0; i < rinchiInputComponent.getBonds().size(); i++) {
    		final InchiBond inchiBond = rinchiInputComponent.getBonds().get(i);
    		IBond bond = getBond(inchiBond, inchiAtom2AtomMap);
    		if (bond != null) 
    			atomContainer.addBond(bond);
    	}
    	
    	//Convert stereos / generate stereo elements
    	if (!rinchiInputComponent.getStereos().isEmpty()) {
    		final List<IStereoElement> stereoElements = new ArrayList<>();
    		for (InchiStereo stereo : rinchiInputComponent.getStereos()) {
    			final IStereoElement stereoElement = inchiStereoToCDKStereoElement (stereo, inchiAtom2AtomMap);
    			if (stereoElement != null)
    				stereoElements.add(stereoElement);
    		}
    		if (!stereoElements.isEmpty())
    			atomContainer.setStereoElements(stereoElements);
    	}
    	else {
    		// No stereo elements available and trying to generate them from 2D/3D coordinates
    		//
    		// Generally, this case should be always expected because
    		// RInChI native library returns chirality via 2D/3D coordinates in the RXN/RDFile text
    		// However, RAuxInfo requires the "chiral flag" which is set only when
    		// stereo elements are set in the molecule
    		if (molCoordinatesType == MolCoordinatesType._2D)
				atomContainer.setStereoElements(StereoElementFactory.using2DCoordinates(atomContainer).createAll());
			else if (molCoordinatesType == MolCoordinatesType._3D)
				atomContainer.setStereoElements(StereoElementFactory.using3DCoordinates(atomContainer).createAll());
    	}
    	
    	//Convert radicals
    	for (int i = 0; i < rinchiInputComponent.getAtoms().size(); i++) {
    		final InchiAtom inchiAtom = rinchiInputComponent.getAtoms().get(i);
    		if (inchiAtom.getRadical() == InchiRadical.NONE)
    			continue;
    		SPIN_MULTIPLICITY multiplicity = cdkSpinMultiplicityToInchiRadical(inchiAtom.getRadical());
    		IAtom atom = atomContainer.getAtom(i);
    		if (atom != null) { 
    			//check is needed because inchiAtom might have not been converted
    			atom.setProperty(CDKConstants.SPIN_MULTIPLICITY, multiplicity);
    			for (int e = 0; e < multiplicity.getSingleElectrons(); e++)
    				atomContainer.addSingleElectron(i);
    		}
    	}	
    	
    	if (configureReactionComponents) {
    		try {
    			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(atomContainer);
    		}
    		catch (Exception exception) {
    			reactionGenerationErrors.add(curComponentErrorContext + 
    					"AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms: " + exception.getMessage());
    		}
    	}	

    	return atomContainer;
    }
    
    private IAtom getAtom(InchiAtom inchiAtom, MolCoordinatesType coordType) {
    	final IAtom atom = new Atom(inchiAtom.getElName());

		//Set charge
    	int charge = inchiAtom.getCharge();
    	if (charge != 0)
    		atom.setFormalCharge(charge);
    	
    	atom.setImplicitHydrogenCount(inchiAtom.getImplicitHydrogen());
    	
    	//Set isotope
        if(inchiAtom.getIsotopicMass() > 0)
        	atom.setMassNumber(inchiAtom.getIsotopicMass());

        //Set coordinates 2D or 3D
        if (coordType == MolCoordinatesType._2D) {
        	Point2d p = new Point2d();
        	p.x = inchiAtom.getX();
        	p.y = inchiAtom.getY();
        	atom.setPoint2d(p);
        } 
        else if (coordType == MolCoordinatesType._3D) {
        	Point3d p = new Point3d();
        	p.x = inchiAtom.getX();
        	p.y = inchiAtom.getY();
        	p.z = inchiAtom.getZ();
        	atom.setPoint3d(p);
        }
        
    	return atom;
    }
    
    private IBond getBond(InchiBond inchiBond, Map<InchiAtom,IAtom> inchiAtom2AtomMap) {
    	final IAtom atom1 = inchiAtom2AtomMap.get(inchiBond.getStart());
    	final IAtom atom2 = inchiAtom2AtomMap.get(inchiBond.getEnd());
    	IBond.Order order = null;

    	switch (inchiBond.getType()) {
    	case SINGLE:
    		order = IBond.Order.SINGLE;
    		break;
    	case DOUBLE:
    		order = IBond.Order.DOUBLE;
    		break;
    	case TRIPLE:
    		order = IBond.Order.TRIPLE;
    		break;
    	case ALTERN:
    		order = IBond.Order.UNSET;
    		break;
    	}
    	
    	final IBond.Stereo stereo = inchiBondStereoToCDKBondStereoTo(inchiBond.getStereo());
    	
    	if (order == null || atom1 == null || atom2 == null) {
    		reactionGenerationErrors.add(curComponentErrorContext + 
    				"Unable to convert InchiBond to CDK bond: " + (order == null ? "null" : order.toString()));
			return null;
		}	
		else {
			final IBond bond = new Bond(atom1, atom2, order, stereo);
			if (order == IBond.Order.UNSET) //this case is for aromatic bonds
				bond.setIsAromatic(true);
			return bond;
		}	
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
    		final IAtom chiralAtom = inchiAtom2AtomMap.get(inchiStereo.getCentralAtom());
    		final IAtom[] ligands = new IAtom[4];
    		
    		InchiAtom inchiAtom0 = inchiStereo.getAtoms()[0];
    		if (inchiAtom0 == InchiStereo.STEREO_IMPLICIT_H)
    			inchiAtom0 = inchiStereo.getCentralAtom(); //mapping central atom to get IAtom ligand
    		ligands[0] = inchiAtom2AtomMap.get(inchiAtom0);

			InchiAtom inchiAtom1 = inchiStereo.getAtoms()[1];
    		if (inchiAtom1 == InchiStereo.STEREO_IMPLICIT_H)
    			inchiAtom1 = inchiStereo.getCentralAtom(); //mapping central atom to get IAtom ligand
    		ligands[1] = inchiAtom2AtomMap.get(inchiAtom1);

			InchiAtom inchiAtom2 = inchiStereo.getAtoms()[2];
    		if (inchiAtom2 == InchiStereo.STEREO_IMPLICIT_H)
    			inchiAtom2 = inchiStereo.getCentralAtom(); //mapping central atom to get IAtom ligand
    		ligands[2] = inchiAtom2AtomMap.get(inchiAtom2);

			InchiAtom inchiAtom3 = inchiStereo.getAtoms()[3];
    		if (inchiAtom3 == InchiStereo.STEREO_IMPLICIT_H)
    			inchiAtom3 = inchiStereo.getCentralAtom(); //mapping central atom to get IAtom ligand
    		ligands[3] = inchiAtom2AtomMap.get(inchiAtom3);

    		//MDL Parity definition: 
    		//View the center from a position such that the bond connecting the highest-numbered atom (4) 
    		//projects behind the plane formed by atoms 1, 2 and 3.
    		//Sighting towards atom number 4 through the plane (123), you see that the three remaining atoms can be arranged 
    		//in either a clockwise (parity = 1, ODD) or counterclockwise (parity = 2, EVEN)
    		//A hydrogen atom should be considered the highest numbered atom, in this case atom 4
    		//
    		//CDK Tetrahedral Chirality specification:
    		//the first ligand points towards to observer, and the three other ligands point away from the observer; 
    		//the stereo then defines the order of the second, third, and fourth ligand to be clockwise or anti-clockwise.	
    		//			
    		// In the scheme bellow: 
    		// MDL: observer --> 1,2,3 --> 4   clockwise (parity = 1, ODD)
    		// CDK: observer --> 1 --> 2,3,4   clockwise  
    		// 
    		//
    		//          in the plane  (3)
    		//                         |
    		//                         |
    		// MDL observer --->      cen ....(4) behind the plane
    		//                       /   \\
    		//                      /     \\
    		//      in the plane  (2)      (1)  in front of the plain 
    		//                                
    		//                              ^                             
    		//                              |
    		//                                           
    		//                              CDK observer
    		
    		if (inchiStereo.getParity() == InchiStereoParity.ODD) {
				return new TetrahedralChirality(chiralAtom, ligands, Stereo.CLOCKWISE);
			}
    		else {
				return new TetrahedralChirality(chiralAtom, ligands, Stereo.ANTI_CLOCKWISE);
    			//Potential information "loss":
    			//InchiStereoParity.UNDEFINED type is also saved as ANTI_CLOCKWISE
    		}
    	}
    	
    	//TODO handle other types of stereo elements

    	return null;
    }
    
    private SPIN_MULTIPLICITY cdkSpinMultiplicityToInchiRadical(InchiRadical radical) {
		
    	switch (radical) {
    	case SINGLET:
    		return SPIN_MULTIPLICITY.DivalentSinglet;
    	case DOUBLET:
    		return SPIN_MULTIPLICITY.Monovalent;
    	case TRIPLET:
    		return SPIN_MULTIPLICITY.DivalentTriplet;
		}
		
		return SPIN_MULTIPLICITY.None;
	}
    
    private String getAllReactionGenerationErrors() {
		StringBuilder sb = new StringBuilder(); 
		for (String err: reactionGenerationErrors)
			sb.append(err).append("\n");

		return sb.toString();
	}
    
	/**
     * Returns generated reaction.
     * @return the reaction object generated from the RInChI
     */
	public IReaction getReaction() {
		return reaction;
	}
	
	/**
     * Access the status of the RInChI output.
     * @return the status
     */
	public Status getStatus() {
		if (rInpFromRinchiOutput != null)
			return rInpFromRinchiOutput.getStatus();
		else
			return null;
	}

	/**
     * Gets generated error messages.
	 * @return generated error messages
     */
    public String getErrorMessage() {
        return rInpFromRinchiOutput.getErrorMessage();
    }

	/**
	 * Returns the flag that indicates whether CDK MDL input/output capabilities are used for conversion.
	 *
	 * @return <code>false</code> if the conversion is carried out by JnaRinchi, <code>true</code> if the conversion is carried out by CDK MDL IO
	 */
	public boolean isUseCDK_MDL_IO() {
		return useCDK_MDL_IO;
	}
	
	/**
	 * Returns the flag that indicates whether CDK AtomContainers for result reaction components are to be configured.
	 *
	 * @return <code>true</code> if the reaction components are to be configured, otherwise <code>false</code> is returned
	 */	
	public boolean isConfigureReactionComponents() {
		return configureReactionComponents;
	}

	/**
     * Gets the RinchiInput object used for data conversion. 
	 * @return the RinchiInput object used for conversion or <code>null</code> if {@link #isUseCDK_MDL_IO()} is <code>null</code>
     */
	public RinchiInput getResultRinchiInputObject() {
		if (rInpFromRinchiOutput != null)
			return rInpFromRinchiOutput.getRinchiInput();
		else 
			return null;
	}
    
}
