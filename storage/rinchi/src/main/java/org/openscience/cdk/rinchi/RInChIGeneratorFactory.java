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

/**
 * <p>Factory providing access to {@link RInChIGenerator} and {@link RInChIToReaction}.
 * See those classes for examples of use. These methods make use of the
 * JNA-RInChI library.
 *
 * <p>The {@link RInChIGeneratorFactory} is a singleton class, which means that there
 * exists only one instance of the class. An instance of this class is obtained
 * with:
 * <pre>
 * RInChIGeneratorFactory factory = RInChIGeneratorFactory.getInstance();
 * </pre>
 *
 * <p>RInChI/Reaction interconversion is implemented in this way so that we can
 * check whether or not the native code required is available. If the native
 * code cannot be loaded during the first call to  <code>getInstance</code>
 * method (when the instance is created) a {@link CDKException} will be thrown. The
 * most common problem is that the native code is not in the * the correct
 * location. 
 * See:
 * <ul>
 * <li>https://github.com/dan2097/jna-inchi
 * <li>http://www.iupac.org/inchi/
 * <li>https://www.inchi-trust.org/
 * </ul>
 *
 * @author Nikolay Kochev
 * @cdk.module rinchi
 * @cdk.githash
 */
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IReaction;

import io.github.dan2097.jnarinchi.RinchiFlag;
import io.github.dan2097.jnarinchi.RinchiOptions;


public class RInChIGeneratorFactory {

	private static RInChIGeneratorFactory INSTANCE;

	private RInChIGeneratorFactory() throws CDKException {
	}


	/**
	 * Gives the one <code>RInChIGeneratorFactory</code> instance,
	 * if needed also creates it.
	 *
	 * @return the one <code>RInChIGeneratorFactory</code> instance
	 * @throws CDKException if unable to load native code when attempting
	 *                      to create the factory
	 */
	public static RInChIGeneratorFactory getInstance() throws CDKException {
		synchronized (RInChIGeneratorFactory.class) {
			if (INSTANCE == null) {
				INSTANCE = new RInChIGeneratorFactory();
			}
			return INSTANCE;
		}
	}

	/**
	 * Gets a Standard RInChI generator for a {@link IReaction}. 
	 *
	 * @param container Reaction to generate RInChI for.
	 * @return the RInChI generator object
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIGenerator getRInChIGenerator(IReaction reaction) throws CDKException {
		return (new RInChIGenerator(reaction));
	}

	/**
	 * Gets a RInChI generator for a {@link IReaction} providing flags to customise the generation. If you
	 * need to provide a timeout the method that accepts an {@link io.github.dan2097.jnainchi.InchiOptions}
	 * should be used.
	 * 
	 * @param reaction Reaction to generate RInChI for.
	 * @param flags the option flags
	 * @return the RInChI generator object
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIGenerator getRInChIGenerator(IReaction reaction, RinchiFlag...flags) throws CDKException {
		if (flags == null) throw new IllegalArgumentException("Null flags");
		RinchiOptions options = new RinchiOptions.RinchiOptionsBuilder()
				.withFlag(flags)
				.build();
		return getRInChIGenerator(reaction, options);
	}

	/**
	 * Get a RInChI generator providing flags to customise the generation.
	 * @param reaction Reaction to generate RInChI for
	 * @param options the rinchi option flags
	 * @return the RInChI generator
	 * @throws CDKException something went wrong
	 */
	public RInChIGenerator getRInChIGenerator(IReaction reaction, RinchiOptions options) throws CDKException {
		if (options == null) throw new IllegalArgumentException("Null flags");
		return (new RInChIGenerator(reaction, options));
	}
	
	/**
	 * Get a RInChI generator providing flags to customise the generation.
	 * @param reaction Reaction to generate RInChI for
	 * @param options Space or comma delimited string of options for RInChI generation
	 * @return the RInChI generator
	 * @throws CDKException something went wrong
	 */
	public RInChIGenerator getRInChIGenerator(IReaction reaction, String options) throws CDKException {
		return (new RInChIGenerator(reaction, options));
	}
	
	/**
	 * Get a RInChI generator providing flags to customise the generation.
	 * @param reaction Reaction to generate RInChI for
	 * @param options the rinchi option flags
	 * @param useCDK_MDL_IO determines whether to use CDK MDL RXN Writer
	 * @return the RInChI generator
	 * @throws CDKException something went wrong
	 */
	public RInChIGenerator getRInChIGenerator(IReaction reaction, RinchiOptions options, boolean useCDK_MDL_IO) throws CDKException {
		return (new RInChIGenerator(reaction, options, useCDK_MDL_IO));
	}
	
	/**
	 * <p>Gets reaction generator for a RInChI string.
	 *
	 * @param rinchi   RInChI to generate reaction from. 
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIToReaction getRInChIToReaction(String rinchi)
			throws CDKException {
		return (new RInChIToReaction(rinchi));
	}
	
	/**
	 * <p>Gets reaction generator for a RInChI string.
	 *
	 * @param rinchi   RInChI to generate reaction from. 
	 * @param auxInfo   RInChI aux info.
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIToReaction getRInChIToReaction(String rinchi, String auxInfo)
			throws CDKException {
		return (new RInChIToReaction(rinchi, auxInfo));
	}
	
	
	/**
	 * <p>Gets reaction generator for a RInChI string.
	 *
	 * @param rinchi   RInChI to generate reaction from. 
	 * @param auxInfo   RInChI aux info.
	 * @param useCDK_MDL_IO determines whether to use CDK MDL RXN Reader
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIToReaction getRInChIToReaction(String rinchi, String auxInfo, boolean useCDK_MDL_IO)
			throws CDKException {
		return (new RInChIToReaction(rinchi, auxInfo, useCDK_MDL_IO));
	}
	
	/**
	 * <p>Gets reaction decomposition for a RInChI string.
	 *
	 * @param rinchi   RInChI to decompose from.
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIDecomposition getRInChIDecomposition(String rinchi)
			throws CDKException {
		return (new RInChIDecomposition(rinchi));
	}
	
	/**
	 * <p>Gets reaction decomposition for a RInChI string.
	 *
	 * @param rinchi   RInChI to decompose from. 
	 * @param auxInfo   RInChI aux info.
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIDecomposition getRInChIDecomposition(String rinchi, String auxInfo)
			throws CDKException {
		return (new RInChIDecomposition(rinchi, auxInfo));
	}


}
