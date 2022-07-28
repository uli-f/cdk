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
import org.openscience.cdk.interfaces.IReaction;

import io.github.dan2097.jnarinchi.RinchiInput;
import io.github.dan2097.jnarinchi.RinchiKeyOutput;
import io.github.dan2097.jnarinchi.RinchiOptions;
import io.github.dan2097.jnarinchi.RinchiOutput;

public class RInChIGenerator {
	
	private static final RinchiOptions DEFAULT_OPTIONS = new RinchiOptions();
	
	protected RinchiInput input;	
	protected RinchiOutput rinchiOutput;
	protected RinchiKeyOutput rinchiKeyOutput;
	protected IReaction reaction;
	protected RinchiOptions options;
	
	protected boolean useCDK_MDL_IO = false;
	
	protected RInChIGenerator (IReaction reaction) throws CDKException {
		this(reaction, DEFAULT_OPTIONS, false);
	}
	
	protected RInChIGenerator (IReaction reaction, RinchiOptions options) throws CDKException {
		this(reaction, options, false);
	}
	
	protected RInChIGenerator (IReaction reaction, RinchiOptions options, boolean useCDK_MDL_IO) throws CDKException {
		this.reaction = reaction;
		this.options = options;
		this.useCDK_MDL_IO = useCDK_MDL_IO; 
	}
	
	private void generateRinchiFromReaction() throws CDKException {
		//TODO
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
	
	public String getRInChIKey() {
		return rinchiKeyOutput.getRinchiKey();
	}
	
	public String getRInChIKeyType() {
		return rinchiKeyOutput.getRinchiKeyType().toString();
	}
	
	public String getRInChIKeyErrorMessage() {
		return rinchiOutput.getErrorMessage();
	}
	
}
