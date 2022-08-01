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

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.io.MDLRXNWriter;

import io.github.dan2097.jnarinchi.JnaRinchi;
import io.github.dan2097.jnarinchi.ReactionFileFormat;
import io.github.dan2097.jnarinchi.RinchiInput;
import io.github.dan2097.jnarinchi.RinchiKeyOutput;
import io.github.dan2097.jnarinchi.RinchiKeyType;
import io.github.dan2097.jnarinchi.RinchiOptions;
import io.github.dan2097.jnarinchi.RinchiOutput;
import io.github.dan2097.jnarinchi.RinchiStatus;

public class RInChIGenerator {
	
	private static final RinchiOptions DEFAULT_OPTIONS = RinchiOptions.DEFAULT_OPTIONS;
	
	protected RinchiInput input;	
	protected RinchiOutput rinchiOutput;
	protected RinchiKeyOutput shortRinchiKeyOutput = null;
	protected RinchiKeyOutput longRinchiKeyOutput = null;
	protected RinchiKeyOutput webRinchiKeyOutput = null;
	protected IReaction reaction;
	protected RinchiOptions options;
	protected String rinchiInputGenErrorMsg = null;
	
	protected boolean useCDK_MDL_IO = false;
	
	protected RInChIGenerator (IReaction reaction) throws CDKException {
		this(reaction, DEFAULT_OPTIONS, false);
	}
	
	protected RInChIGenerator (IReaction reaction, RinchiOptions options) throws CDKException {
		this(reaction, options, false);
	}
	
	protected RInChIGenerator (IReaction reaction, String optStr) throws CDKException {
		this(reaction, RInChIOptionParser.parseString(optStr), false);
	}
	
	protected RInChIGenerator (IReaction reaction, RinchiOptions options, boolean useCDK_MDL_IO) throws CDKException {
		this.reaction = reaction;
		this.options = options;
		this.useCDK_MDL_IO = useCDK_MDL_IO;		
		generateRinchiFromReaction();
	}
	
	private void generateRinchiFromReaction() throws CDKException {
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
			}
			catch (Exception x) {
				String errMsg = "Unable to write MDL RXN file for reaction: " + x.getMessage();
				rinchiOutput = new RinchiOutput("", "", RinchiStatus.ERROR, -1, errMsg);;
			}			
		}
		else {
			RinchiInput rInp = getRinchiInputFromReaction();
			if (rInp == null) {
				String errMsg = "Unable to convert CDK Reaction to RinchiInput: " + rinchiInputGenErrorMsg;
				rinchiOutput = new RinchiOutput("", "", RinchiStatus.ERROR, -1, errMsg);
			} 
			else 
				rinchiOutput = JnaRinchi.toRinchi(rInp, options);			
		}
		
		if (rinchiOutput.getStatus() == RinchiStatus.ERROR)
			throw new CDKException("RInChI generation problem: " + rinchiOutput.getErrorMessage());	
	}
	
	private void generateRInChIKey(RinchiKeyType type) {
		//TODO
	}
	
	private RinchiInput getRinchiInputFromReaction() {
		//TODO
		return null;
	}
	
	private String getRXNFileTextFromReaction() {
		return null;
	}
	
	
	public String getRInChI() {
		return rinchiOutput.getRinchi();
	}
	
	public String getAuxInfo() {
		return rinchiOutput.getAuxInfo();
	}
	
	public String getRInChIErrorMessage() {
		return rinchiOutput.getErrorMessage();
	}
	
	public String getRInChIKey(RinchiKeyType type) throws CDKException {
		switch (type) {
		case SHORT:
			if (shortRinchiKeyOutput == null)
				generateRInChIKey(type);
			return shortRinchiKeyOutput.getRinchiKey();
		case LONG:
			if (longRinchiKeyOutput == null)
				generateRInChIKey(type);
			return longRinchiKeyOutput.getRinchiKey();
		case WEB:
			if (webRinchiKeyOutput == null)
				generateRInChIKey(type);
			return webRinchiKeyOutput.getRinchiKey();	
		}
		return null;
	}
		
	public String getRInChIKeyErrorMessage(RinchiKeyType type) {
		switch (type) {
		case SHORT:
			return shortRinchiKeyOutput.getErrorMessage();
		case LONG:
			return longRinchiKeyOutput.getErrorMessage();
		case WEB:
			return webRinchiKeyOutput.getErrorMessage();	
		}
		return "";
	}
	
}
