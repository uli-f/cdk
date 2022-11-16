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

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.interfaces.ITetrahedralChirality;
import org.openscience.cdk.interfaces.ITetrahedralChirality.Stereo;
import org.openscience.cdk.io.MDLRXNWriter;
import org.openscience.cdk.io.MDLV2000Writer.SPIN_MULTIPLICITY;

import io.github.dan2097.jnainchi.InchiAtom;
import io.github.dan2097.jnainchi.InchiBond;
import io.github.dan2097.jnainchi.InchiBondStereo;
import io.github.dan2097.jnainchi.InchiBondType;
import io.github.dan2097.jnainchi.InchiRadical;
import io.github.dan2097.jnainchi.InchiStereo;
import io.github.dan2097.jnainchi.InchiStereoParity;
import io.github.dan2097.jnarinchi.JnaRinchi;
import io.github.dan2097.jnarinchi.ReactionComponentRole;
import io.github.dan2097.jnarinchi.ReactionFileFormat;
import io.github.dan2097.jnarinchi.RinchiInput;
import io.github.dan2097.jnarinchi.RinchiInputComponent;
import io.github.dan2097.jnarinchi.RinchiKeyOutput;
import io.github.dan2097.jnarinchi.RinchiKeyType;
import io.github.dan2097.jnarinchi.RinchiOptions;
import io.github.dan2097.jnarinchi.RinchiOutput;
import io.github.dan2097.jnarinchi.Status;

import org.openscience.cdk.tools.ILoggingTool;
import org.openscience.cdk.tools.LoggingToolFactory;

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
 *     {@link RInChIGeneratorFactory#getRInChIGenerator(IReaction, RinchiOptions, boolean)}).</li>
 * </ol>
 * <p>
 *     If the atom container has 3D coordinates for all of its atoms then these 3D coordinates
 *     will be used, otherwise 2D coordinates will be used if available.
 * </p>
 * Given an IReaction, let's generate RInChI, RAuxInfo, Long-RInChIKey, Short-RInChIKey and Web-RInChIKey:
 * <pre>
 *     // all we need is an IReaction object, e.g., by loading an RXN file
 *     IReaction reaction = ....;
 *     RInChIGenerator generator = RInChIGeneratorFactory.getInstance().getRInChIGenerator(reaction);
 *     String rinchi = generator.getRInChI();
 *     String rAuxInfo = generator.getAuxInfo();
 *     String longKey = generator.getLongRInChIKey();
 *     String shortKey = generator.getShortRInChIKey();
 *     String webKey = generator.getWebRInChIKey();
 * </pre>
 * Given a RInChI and optionally its RAuxInfo here is how to generate an IReaction:
 * <pre>
 *     RInChIToReaction rinchiToReaction = RInChIGeneratorFactory.getInstance().getRInChIToReaction(rinchi, rAuxInfo);
 *     IReaction reaction = rinchiToReaction.getReaction();
 *
 *     // if a RAuxInfo isn't available an overloaded method can be called
 *     RInChIToReaction rinchiToReactionNoRauxinfo = RInChIGeneratorFactory.getInstance().getRInChIToReaction(rinchi);
 *     IReaction reaction2 = rinchiToReactionNoRauxinfo.getReaction();
 * </pre>
 * And here is how to decompose the RInChI and its associated RAuxInfo into the constituent InChIs and AuxInfo:
 * <pre>
 *     RInChIDecomposition rinchiDecomposition = RInChIGeneratorFactory.getInstance().getRInChIDecomposition(rinchi);
 *     List&lt;String&gt; inchis = rinchiDecomposition.getInchis();
 *     List&lt;String&gt; auxInfos = rinchiDecomposition.getAuxInfo();
 *     // getting the roles of the individual reaction components and the direction of the reaction
 *     List&lt;ReactionComponentRole&gt; roles =  rinchiDecomposition.getReactionComponentRoles();
 *     ReactionDirection direction = rinchiDecomposition.getReactionDirection();
 *
 *     // there are also utility methods to get a map of (Inchi, AuxInfo) pairs ...
 *     Map&lt;String,String&gr; inchiAuxInfoMap = rinchiDecomposition.getInchiAuxInfoMap();
 *     // ... and a map of (inchi, reaction component roles) pairs
 *     Map&lt;String,ReactionComponentRole&gr; inchiReactionComponentRoleMap = rinchiDecomposition.getInchiReactionComponentRoleMap();
 * </pre>
 * @author Nikolay Kochev
 * @cdk.module rinchi
 * @cdk.githash
 */

public class RInChIGenerator {
	private static final ILoggingTool LOGGER = LoggingToolFactory.createLoggingTool(RInChIGenerator.class);
	private static final RinchiOptions DEFAULT_OPTIONS = RinchiOptions.DEFAULT_OPTIONS;
    protected RinchiOptions options;
    protected IReaction reaction;
	protected RinchiOutput rinchiOutput;
	protected RinchiKeyOutput shortRinchiKeyOutput = null;
	protected RinchiKeyOutput longRinchiKeyOutput = null;
	protected RinchiKeyOutput webRinchiKeyOutput = null;
	protected List<String> rinchiInputGenerationErrors = new ArrayList<>();
	protected final boolean useCDK_MDL_IO;

	/**
     * Generates RInChI from a CDK Reaction.
     * <p>
     * Reads atoms, bonds etc from atom containers and converts to the format
     * the RInChI library requires, then calls the library.
	 * </p>
     *
     * @param reaction	reaction to generate RInChI for
     * @throws CDKException if there is an error during RInChI generation
     */
	protected RInChIGenerator (IReaction reaction) throws CDKException {
		this(reaction, DEFAULT_OPTIONS, false);
	}

	/**
	 * Generates RInChI from a CDK Reaction.
	 * <p>
	 * Reads atoms, bonds etc from atom containers and converts to the format
	 * the RInChI library requires, then calls the library.
	 * </p>
	 *
	 * @param reaction	reaction to generate RInChI for
	 * @param options RInChI generation options (in the format as expected by the JnaRinchi library)
	 * @throws CDKException if there is an error during RInChI generation
     */
	protected RInChIGenerator (IReaction reaction, RinchiOptions options) throws CDKException {
		this(reaction, options, false);
	}

	/**
	 * Generates RInChI from a CDK Reaction.
	 * <p>
	 * Reads atoms, bonds etc from atom containers and converts to the format
	 * the RInChI library requires, then calls the library.
	 * </p>
	 *
	 * @param reaction	reaction to generate RInChI for
	 * @param optStr RInChI generation options set as string (options are space or comma separated)
	 * @throws CDKException if there is an error during RInChI generation
     */
	protected RInChIGenerator (IReaction reaction, String optStr) throws CDKException {
		this(reaction, RInChIOptionParser.parseString(optStr), false);
	}

	/**
	 * Generates RInChI from a CDK Reaction.
	 * <p>
	 * Reads atoms, bonds etc from atom containers and converts to the format
	 * the RInChI library requires, then calls the library.
	 * </p>
	 * The conversion from an <code>IReaction</code> object to the MDL CTAB format can be carried out by
	 * <ol>
	 *     <li>JnaRinchi converter (default)</li>
	 *     <li>CDK MDL RXN Writer (by providing a value of <code>true</code> for the argument <code>useCDK_MDL_IO</code></li>
	 * </ol>
	 *
	 * @param reaction	reaction to generate RInChI for
	 * @param options RInChI generation options (in the format as expected by the JnaRinchi library)
	 * @param useCDK_MDL_IO determines whether to use CDK MDL RXN Writer
	 * @throws CDKException if there is an error during RInChI generation
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

				//If agents are present, they are not written in the RXN file, but
				//their number is added into the count line.
				//The latter causes error/exception in the native RInChI RXN reader:
				//RInChI generation problem: class rinchi::MdlRxnfileReaderError:
				//Reading from 'std::istream', line 5:
				//Invalid component count line - must be 6 characters long
				//Therefore the agents are removed before conversion
				if (!reaction.getAgents().isEmpty()) {
					IReaction reaction0 = (IReaction) reaction.clone();
					reaction0.getAgents().removeAllAtomContainers();
					mdlWriter.write(reaction0);
				}
				else
					mdlWriter.write(reaction);

				mdlWriter.close();
				String fileText = writer.toString();
				rinchiOutput = JnaRinchi.fileTextToRinchi(fileText, options, ReactionFileFormat.RXN);
			}
			catch (Exception x) {
				String errMsg = "Unable to write MDL RXN file for reaction: " + x.getMessage();
				rinchiOutput = new RinchiOutput("", "", Status.ERROR, -1, errMsg);
			}
		}
		else {
			RinchiInput rInp = getRinchiInputFromReaction();
			if (rInp == null) {
				String errMsg = "Unable to convert CDK Reaction to RinchiInput: " + getAllRinchiInputGenerationErrors();
				rinchiOutput = new RinchiOutput("", "", Status.ERROR, -1, errMsg);
			}
			else
				rinchiOutput = JnaRinchi.toRinchi(rInp, options);
		}

		if (rinchiOutput.getStatus() == Status.ERROR)
			throw new CDKException("RInChI generation problem: " + rinchiOutput.getErrorMessage());
	}

	private void generateRInChIKey(RinchiKeyType type) throws CDKException {
		RinchiKeyOutput rkOut;
		if (rinchiOutput.getStatus() == Status.ERROR) {
			String err = "Unable to generate RInChIKey since, no RInChI is generated!";
			rkOut = new RinchiKeyOutput("", type, Status.ERROR, -1, err);
		} else {
			rkOut = JnaRinchi.rinchiToRinchiKey(type, rinchiOutput.getRinchi());
		}

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

		if (rkOut.getStatus() == Status.ERROR)
			throw new CDKException("RInChIKey generation problem: " + rkOut.getErrorMessage());
	}

	private RinchiInput getRinchiInputFromReaction() {
		RinchiInput rinchiInput = new RinchiInput();

		addReactionComponentToRinchiInput(rinchiInput, reaction.getReactants(), ReactionComponentRole.REAGENT);
		addReactionComponentToRinchiInput(rinchiInput, reaction.getProducts(), ReactionComponentRole.PRODUCT);
		addReactionComponentToRinchiInput(rinchiInput, reaction.getAgents(), ReactionComponentRole.AGENT);

		if (rinchiInputGenerationErrors.isEmpty())
			return rinchiInput;
		else
			return null;
	}

	private void addReactionComponentToRinchiInput(RinchiInput rinchiInput, IAtomContainerSet atomContainerSet, ReactionComponentRole reactionComponentRole) {
		for (IAtomContainer atomContainer: atomContainerSet.atomContainers()) {
			RinchiInputComponent rinchiInputComponent = getRinchiInputComponent(atomContainer);
			rinchiInputComponent.setRole(reactionComponentRole);
			rinchiInput.addComponent(rinchiInputComponent);
		}
	}

	private RinchiInputComponent getRinchiInputComponent(IAtomContainer atomContainer) {
		RinchiInputComponent rinchiInputComponent = new RinchiInputComponent();
		Map<IAtom,InchiAtom> atomInchiAtomMap = new HashMap<>();

		//Convert atoms
		for (int i = 0; i < atomContainer.getAtomCount(); i++) {
			IAtom atom = atomContainer.getAtom(i);
			InchiAtom inchiAtom = getInchiAtom(atom);
			if (inchiAtom != null) {
				atomInchiAtomMap.put(atom, inchiAtom);
				rinchiInputComponent.addAtom(inchiAtom);
			}
		}

		//Convert bonds
		for (int i = 0; i < atomContainer.getBondCount(); i++) {
			IBond bond = atomContainer.getBond(i);
			InchiBond inchiBond = getInchiBond (bond, atomInchiAtomMap);
			if (inchiBond != null)
				rinchiInputComponent.addBond(inchiBond);
		}

		//Convert stereo elements
		for (IStereoElement stereoElement : atomContainer.stereoElements()) {
			InchiStereo stereo = cdkStereoElementToInchiStereo(stereoElement, atomInchiAtomMap);
			if (stereo != null)
				rinchiInputComponent.addStereo(stereo);
		}

		//Convert radicals
		if (atomContainer.getSingleElectronCount() > 0) {
			 for (int i = 0; i < atomContainer.getAtomCount(); i++) {
				 IAtom atom = atomContainer.getAtom(i);
				 int eCount = atomContainer.getConnectedSingleElectronsCount(atom);
				 switch (eCount) {
				 case 0:
					 continue;
				 case 1:
					 //SPIN_MULTIPLICITY.Monovalent
					 atomInchiAtomMap.get(atom).setRadical(InchiRadical.DOUBLET);
					 break;
				 case 2:
					 SPIN_MULTIPLICITY multiplicity = atom.getProperty(CDKConstants.SPIN_MULTIPLICITY);
					 if (multiplicity != null) {
						InchiRadical radical = cdkSpinMultiplicityToInchiRadical(multiplicity);
						atomInchiAtomMap.get(atom).setRadical(radical);
					 }
					 else {
						 // information loss: divalent, but not clear whether singlet or triplet?
						 // no conversion;						 
						 LOGGER.info("Information loss/radical not converted: "
						 		+ "unable to determone divalent radical type (singlet or triple)");
					 }
					 break;
				 default:
					 //Invalid number of eCount					 
					 LOGGER.info("Radical not converted: invalid number of connected single electrons to atom #" + (i+1));
					 break;
				 }
			 }
		}

		return rinchiInputComponent;
	}

	private InchiAtom getInchiAtom (IAtom atom) {
		//Handle non standard atoms (IPseudoAtom)
		if (atom instanceof IPseudoAtom) {
			IPseudoAtom pAtom = (IPseudoAtom) atom;
			if (pAtom.getLabel()!=null)
				return new InchiAtom(pAtom.getLabel());

			rinchiInputGenerationErrors.add("Unable to convert CDK IPseudoAtom to InchiAtom:"
					+ (pAtom.getLabel()!=null?(" label " + pAtom.getLabel()):"") + " "
					+ (pAtom.getSymbol()!=null?(" symbol " + pAtom.getSymbol()):""));
			return null;
		}

		String atomSymbol = atom.getSymbol();
		InchiAtom inchiAtom = new InchiAtom(atomSymbol);

		//Set charge
		Integer formalCharge = atom.getFormalCharge();
        if (formalCharge == null)
            formalCharge = 0;
        inchiAtom.setCharge(formalCharge);

        //Set isotope
        Integer massNumber = atom.getMassNumber();
        if (massNumber != null)
        	inchiAtom.setIsotopicMass(massNumber);

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
		InchiAtom inchiAtom0 = atomInchiAtomMap.get(bond.getAtom(0));
		InchiAtom inchiAtom1 = atomInchiAtomMap.get(bond.getAtom(1));
		InchiBondType inchiBondType = null;

		switch (bond.getOrder()) {
		case SINGLE:
			inchiBondType = InchiBondType.SINGLE;
			break;
		case DOUBLE:
			inchiBondType = InchiBondType.DOUBLE;
			break;
		case TRIPLE:
			inchiBondType = InchiBondType.TRIPLE;
			break;
		case UNSET:
			if (bond.isAromatic())
				inchiBondType = InchiBondType.ALTERN;
			break;
		}

		if (inchiBondType == null || inchiAtom0 == null || inchiAtom1 == null) {
			rinchiInputGenerationErrors.add("Unable to convert CDK bond to InchiBond: "
					+ bond.getOrder().toString());
			return null;
		}
		else {
			InchiBondStereo bondStereo = cdkBondStereoToInchiBondStereo(bond.getStereo());
			return new InchiBond(inchiAtom0, inchiAtom1, inchiBondType, bondStereo);
		}
	}

	private InchiBondStereo cdkBondStereoToInchiBondStereo(IBond.Stereo stereo) {
		if (stereo == null)
			return InchiBondStereo.NONE;

		switch (stereo) {
		case DOWN:
			return InchiBondStereo.SINGLE_1DOWN;
		case DOWN_INVERTED:
			return InchiBondStereo.SINGLE_2DOWN;
		case UP:
			return InchiBondStereo.SINGLE_1UP;
		case UP_INVERTED:
			return InchiBondStereo.SINGLE_2UP;
		case UP_OR_DOWN:
			return InchiBondStereo.SINGLE_1EITHER;
		case UP_OR_DOWN_INVERTED:
			return InchiBondStereo.SINGLE_2EITHER;
		case E_OR_Z:
			return InchiBondStereo.DOUBLE_EITHER;
		}

		return InchiBondStereo.NONE;
	}

	private InchiStereo cdkStereoElementToInchiStereo (IStereoElement stereoElement, Map<IAtom,InchiAtom> atomInchiAtomMap) {
		if (stereoElement instanceof ITetrahedralChirality) {
			ITetrahedralChirality thc = (ITetrahedralChirality) stereoElement;
			InchiAtom inchiAtomCentral = atomInchiAtomMap.get(thc.getChiralAtom());

			//Within CDK implicit hydrogen and lone pairs are encoded
    		//by adding the central atom in the ligand list
    		//In InchiStereo there is a special atom, InchiStereo.STEREO_IMPLICIT_H,
    		//used for implicit H ligand.
    		//The lone pairs are treated the same way in CDK and InchiStereo.
			InchiAtom inchiAtom1 = atomInchiAtomMap.get(thc.getLigands()[0]);
			if (inchiAtom1 == inchiAtomCentral) {
				//inchiAtom1 is either implicit hydrogen or a lone pair.
				if (inchiAtom1.getImplicitHydrogen() > 0)
					inchiAtom1 = InchiStereo.STEREO_IMPLICIT_H;
			}

			InchiAtom inchiAtom2 = atomInchiAtomMap.get(thc.getLigands()[1]);
			if (inchiAtom2 == inchiAtomCentral) {
				//inchiAtom2 is either implicit hydrogen or a lone pair
				if (inchiAtom2.getImplicitHydrogen() > 0)
					inchiAtom2 = InchiStereo.STEREO_IMPLICIT_H;
			}

			InchiAtom inchiAtom3 = atomInchiAtomMap.get(thc.getLigands()[2]);
			if (inchiAtom3 == inchiAtomCentral) {
				//inchiAtom3 is either implicit hydrogen or a lone pair
				if (inchiAtom3.getImplicitHydrogen() > 0)
					inchiAtom3 = InchiStereo.STEREO_IMPLICIT_H;
			}

			InchiAtom inchiAtom4 = atomInchiAtomMap.get(thc.getLigands()[3]);
			if (inchiAtom4 == inchiAtomCentral) {
				//inchiAtom4 is either implicit hydrogen or a lone pair
				if (inchiAtom4.getImplicitHydrogen() > 0)
					inchiAtom4 = InchiStereo.STEREO_IMPLICIT_H;
			}

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
			//                              CDK observer

			InchiStereoParity parity;
			if (thc.getStereo() == Stereo.CLOCKWISE)
				parity = InchiStereoParity.ODD;  //parity = 1
			else
				parity = InchiStereoParity.EVEN; //parity = 2

			return InchiStereo.createTetrahedralStereo(inchiAtomCentral, inchiAtom1, inchiAtom2, inchiAtom3, inchiAtom4, parity);
		}

		
		//Other types of stereo elements are not handled (converted).
    	//Generally the conversion of double bond and allene atom stereo elements is not needed 
    	//for the proper work of RInChI generation since 
    	//the stereo information is handled via 2D/3D coordinates.
		//Generallt, stereo elements of the other types could not be handled from the 
    	//jna-rinchi libary when converting InchiStereo to MDL RXN/RDFile content.   
		
		return null;
	}

	private InchiRadical cdkSpinMultiplicityToInchiRadical(SPIN_MULTIPLICITY spinMultiplicity) {
		switch (spinMultiplicity) {
		case DivalentSinglet:
			return InchiRadical.SINGLET;
		case Monovalent:
			return InchiRadical.DOUBLET;
		case DivalentTriplet:
			return InchiRadical.TRIPLET;
		}

		return InchiRadical.NONE;
	}

	private String getAllRinchiInputGenerationErrors() {
		StringBuilder sb = new StringBuilder();
		for (String err: rinchiInputGenerationErrors)
			sb.append(err).append("\n");

		return sb.toString();
	}

	/**
     * Gets generated RInChI string.
	 * @return generated RInChI
     */
	public String getRInChI() {
		return rinchiOutput.getRinchi();
	}

	/**
     * Gets auxiliary information.
	 * @return RInChI AuxInfo
     */
	public String getAuxInfo() {
		return rinchiOutput.getAuxInfo();
	}

	/**
	 * Gets generated error messages.
	 * @return generated error messages
	 */
	public String getRInChIErrorMessage() {
		return rinchiOutput.getErrorMessage();
	}

	/**
     * Returns the status of the RInChI output.
     * @return the status
     */
	public Status getRInChIStatus() {
		return rinchiOutput.getStatus();
	}

	/**
     * Gets (and generates if necessary) Short-RInChIKey.
	 * @return Short-RInChIKey
     */
	public String getShortRInChIKey() throws CDKException {
		if (shortRinchiKeyOutput == null)
			generateRInChIKey(RinchiKeyType.SHORT);
		return shortRinchiKeyOutput.getRinchiKey();
	}

	/**
     * Gets (and generates if necessary) Long-RInChIKey.
	 * @return Long-RInChIKey
     */
	public String getLongRInChIKey() throws CDKException {
		if (longRinchiKeyOutput == null)
			generateRInChIKey(RinchiKeyType.LONG);
		return longRinchiKeyOutput.getRinchiKey();
	}

	/**
     * Gets (and generates if necessary) Web-RInChIKey.
	 * @return Web-RInChIKey
     */
	public String getWebRInChIKey() throws CDKException {
		if (webRinchiKeyOutput == null)
			generateRInChIKey(RinchiKeyType.WEB);
		return webRinchiKeyOutput.getRinchiKey();
	}

	/**
	 * Gets generated error messages for RInChIKey.
	 * @return the generated error messages related to the RInChIKey of type <code>type</code>
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

        // we should never end up here, as all different types of RInChIKeys should be handled above
		throw new IllegalArgumentException("RInChIKey of type " + type + " (short designation: " + type.getShortDesignation() + ") not supported.");
	}

	/**
     * Returns the flag that indicates whether CDK MDL input/output capabilities are used for conversion.
	 * @return <code>false</code> if the conversion is carried out by JnaRinchi, <code>true</code> if the conversion is carried out by CDK MDL IO
     */
	public boolean isUseCDK_MDL_IO() {
		return useCDK_MDL_IO;
	}

}
