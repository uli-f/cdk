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

import io.github.dan2097.jnarinchi.RinchiFlag;
import io.github.dan2097.jnarinchi.RinchiOptions;

/**
 * Factory providing access to {@link RInChIGenerator}, {@link RInChIToReaction}
 * and {@link RInChIDecomposition}.
 * <br>
 * See these classes for examples of use. Methods in these classes make use of the
 * JNA-RInChI library.
 * <p>
 * The {@link RInChIGeneratorFactory} is a singleton class, which means that there
 * exists only one instance of the class. An instance of this class is obtained
 * with:
 * <pre>
 * RInChIGeneratorFactory factory = RInChIGeneratorFactory.getInstance();
 * </pre>
 * </p>
 * <p>
 *     RInChI/Reaction interconversion is implemented in this way so that we can
 * check whether the required native code is available. If the native
 * code cannot be loaded during the first call to {@link #getInstance()}
 * (when the instance is created) a {@link CDKException} will be thrown. The
 * most common problem is that the native code is not in the correct location.
 * </p>
 * See:
 * <ul>
 * <li><a href="https://github.com/dan2097/jna-inchi">https://github.com/dan2097/jna-inchi</a></li>
 * <li><a href="http://www.iupac.org/inchi/">http://www.iupac.org/inchi/</a></li>
 * <li><a href="https://www.inchi-trust.org/">https://www.inchi-trust.org/</a></li>
 * </ul>
 *
 * @author Nikolay Kochev
 * @cdk.module rinchi
 * @cdk.githash
 */
public class RInChIGeneratorFactory {
	// this singleton pattern with a static inner class lets the JVM take
	// care of (1) lazy instantiation and (2) concurrency
	// https://blog.paumard.org/2011/04/22/bilan-sur-le-pattern-singleton/
	private static class SingletonInstanceHolder {
		public final static RInChIGeneratorFactory INSTANCE;

		static {
			INSTANCE = new RInChIGeneratorFactory();
			// TODO do we want to load the rinchi native library at this point so that we get an exception early on?
		}
	}

	private RInChIGeneratorFactory() {
	}


	/**
	 * Return the singleton instance of this class, if needed also creates it.
	 *
	 * @return the singleton instance of this class
	 */
	public static RInChIGeneratorFactory getInstance() {
		return SingletonInstanceHolder.INSTANCE;
	}

	/**
	 * Gets a Standard RInChI generator for a {@link IReaction}. 
	 *
	 * @param reaction reaction to generate RInChI for
	 * @return the RInChI generator object
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIGenerator getRInChIGenerator(IReaction reaction) throws CDKException {
		return (new RInChIGenerator(reaction));
	}

	/**
	 * Gets a RInChI generator for a {@link IReaction} providing flags to customise the generation.
	 * If a timeout for the RInChI generation is required one of the methods that accept an
	 * {@link io.github.dan2097.jnarinchi.RinchiOptions} should be used.
	 * 
	 * @param reaction reaction to generate RInChI for
	 * @param flags the option flags
	 * @return the RInChI generator object
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIGenerator getRInChIGenerator(IReaction reaction, RinchiFlag...flags) throws CDKException {
		if (flags == null)
			throw new IllegalArgumentException("Null flags");

		RinchiOptions options = new RinchiOptions.RinchiOptionsBuilder()
				.withFlag(flags)
				.build();

		return getRInChIGenerator(reaction, options);
	}

	/**
	 * Get a RInChI generator providing a {@link String} with options to customise the generation.
	 *
	 * @param reaction reaction to generate RInChI for
	 * @param options space or comma delimited string of options for the generation of the RInChI
	 * @return the RInChI generator
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIGenerator getRInChIGenerator(IReaction reaction, String options) throws CDKException {
		return (new RInChIGenerator(reaction, options));
	}

	/**
	 * Get a RInChI generator providing a {@link RinchiOptions} instance to customise the generation.
	 *
	 * @param reaction reaction to generate RInChI for
	 * @param options object that holds option for the calculation of the RInChI
	 * @return the RInChI generator
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIGenerator getRInChIGenerator(IReaction reaction, RinchiOptions options) throws CDKException {
		if (options == null)
			throw new IllegalArgumentException("Null flags");

		return (new RInChIGenerator(reaction, options));
	}

	/**
	 * Get a RInChI generator providing a {@link RinchiOptions} instance to customise the generation.
	 * This method also allows to specify whether the CDK IO capabilities should be used for
	 * writing the MDL RXN file that is passed to the native RInChI library.
	 *
	 * @param reaction reaction to generate RInChI for
	 * @param options object that holds option for the calculation of the RInChI
	 * @param useCDK_MDL_IO determines whether to use CDK MDL RXN Writer
	 * @return the RInChI generator
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIGenerator getRInChIGenerator(IReaction reaction, RinchiOptions options, boolean useCDK_MDL_IO) throws CDKException {
		return (new RInChIGenerator(reaction, options, useCDK_MDL_IO));
	}
	
	/**
	 * Returns an instance of {@link RInChIToReaction} that consumes a RInChI string and produces a {@link IReaction}.
	 *
	 * @param rinchi   RInChI to generate the reaction from
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIToReaction getRInChIToReaction(String rinchi) throws CDKException {
		return (new RInChIToReaction(rinchi));
	}
	
	/**
	 * Returns an instance of {@link RInChIToReaction} that consumes a RInChI string with an accompanying <i>AuxInfo</i> string and produces a {@link IReaction}.
	 *
	 * @param rinchi   RInChI to generate reaction from
	 * @param auxInfo   RInChI auxiliary information (<i>AuxInfo</i>)
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIToReaction getRInChIToReaction(String rinchi, String auxInfo) throws CDKException {
		return (new RInChIToReaction(rinchi, auxInfo));
	}
	
	
	/**
	 * Returns an instance of {@link RInChIToReaction} that consumes a RInChI string with an accompanying <i>AuxInfo</i> string and produces a {@link IReaction}.
	 * <br>
	 * This method also allows to specify whether the CDK IO capabilities should be used for
	 * reading the MDL RXN file that is passed from the native RInChI library.
	 *
	 * @param rinchi   RInChI to generate reaction from
	 * @param auxInfo   RInChI auxiliary information (<i>AuxInfo</i>)
	 * @param useCDK_MDL_IO determines whether to use CDK MDL RXN Reader
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIToReaction getRInChIToReaction(String rinchi, String auxInfo, boolean useCDK_MDL_IO) throws CDKException {
		return (new RInChIToReaction(rinchi, auxInfo, useCDK_MDL_IO));
	}
	
	/**
	 * Returns an instance of {@link RInChIToReaction} that consumes a RInChI string with an accompanying <i>AuxInfo</i> string and produces a {@link IReaction}.
	 * <br>
	 * This method also allows to specify whether the CDK IO capabilities should be used for
	 * reading the MDL RXN file that is passed from the native RInChI library.
	 * Optionally, the resulting CDK AtomContainer objects, storing reaction components, could be configured. 
	 *
	 * @param rinchi   RInChI to generate reaction from
	 * @param auxInfo   RInChI auxiliary information (<i>AuxInfo</i>)
	 * @param useCDK_MDL_IO determines whether to use CDK MDL RXN Reader
	 * @param configureReactionComponents determines whether to configure reaction components (CDK AtomContainer objects) 
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIToReaction getRInChIToReaction(String rinchi, String auxInfo, boolean useCDK_MDL_IO, boolean configureReactionComponents) throws CDKException {
		return (new RInChIToReaction(rinchi, auxInfo, useCDK_MDL_IO, configureReactionComponents));
	}
	
	/**
	 * Consumes a RInChI string and produces a {@link RInChIDecomposition}.
	 *
	 * @param rinchi  RInChI that is decomposed
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIDecomposition getRInChIDecomposition(String rinchi) throws CDKException {
		return (new RInChIDecomposition(rinchi));
	}
	
	/**
	 * Consumes a RInChI string with an accompanying <i>AuxInfo</i> string and produces a {@link RInChIDecomposition}.
	 *
	 * @param rinchi  RInChI that is decomposed
	 * @param auxInfo   RInChI auxiliary information (<i>AuxInfo</i>)
	 * @throws CDKException if the generator cannot be instantiated
	 */
	public RInChIDecomposition getRInChIDecomposition(String rinchi, String auxInfo) throws CDKException {
		return (new RInChIDecomposition(rinchi, auxInfo));
	}
}
