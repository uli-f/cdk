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

import org.openscience.cdk.Reaction;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.io.MDLRXNV2000Reader;

import io.github.dan2097.jnarinchi.FileTextOutput;
import io.github.dan2097.jnarinchi.FileTextStatus;
import io.github.dan2097.jnarinchi.JnaRinchi;
import io.github.dan2097.jnarinchi.ReactionFileFormat;
import io.github.dan2097.jnarinchi.RinchiInput;
import io.github.dan2097.jnarinchi.RinchiInputFromRinchiOutput;
import io.github.dan2097.jnarinchi.RinchiStatus;

public class RInChIToReaction {
	
	protected RinchiInputFromRinchiOutput rInpFromRinchiOutput = null;
	
	protected FileTextOutput fileTextOutput = null;
	
	protected IReaction reaction;
	
	protected boolean useCDK_MDL_IO = false;
	
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
    		
    	//TODO
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
    	}
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

	public boolean isUseCDK_MDL_IO() {
		return useCDK_MDL_IO;
	}
    
}
