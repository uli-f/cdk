/**
 * <h1>Overview</h1>
 * This package provides functionality related to <a href="https://dx.doi.org/10.1186/s13321-018-0277-8">RInChI</a>.
 * <ol>
 *     <li>{@link org.openscience.cdk.rinchi.RInChIGenerator}: Given an {@link org.openscience.cdk.interfaces.IReaction}
 *     generate RInChI, RAuxInfo, Long-RInChIKey, Short-RInChIKey and Web-RInChIKey.</li>
 *     <li>{@link org.openscience.cdk.rinchi.RInChIToReaction}: Given a RInChI and optionally an associated RAuxInfo
 *     generate an {@link org.openscience.cdk.interfaces.IReaction}.</li>
 *     <li>{@link org.openscience.cdk.rinchi.RInChIDecomposition}: Given a RInChI and optionally an associated RAuxInfo
 *     decompose the RInChI into its constituent InChIs and AuxInfo.</li>
 * </ol>
 * To provide this functionality the library <a href="https://github.com/dan2097/jna-inchi">jna-inchi</a> is used
 * which in turn places calls to methods of the <a href="https://github.com/IUPAC-InChI/RInChI/">native RInChI library</a>
 * by means of a <a href="https://github.com/java-native-access/jna">JNA</a> wrapper.
 * <h1>Chemical Information Routing Process</h1>
 * <p>
 *     The precompiled binaries (i.e., dll and so files) of the IUPAC RInChI native C++ code provide access to their
 *     functionality only by consuming MDL CTAB file text formats RXN and RDfile. Direct access of the C++ internal
 *     data structures of the IUPAC RInChI library is <i>not</i> available.
 * </p>
 * <img src="org.openscience.cdk.rinchi.Figure.1.png" alt="Diagram of the chemical information conversion process.">
 * <p>
 *     The conversion from a CDK {@link org.openscience.cdk.interfaces.IReaction} to RInChI can be done in two ways:
 * </p>
 * <ol>
 *     <li>CDK IReaction -> RinChIInput &#xF0E0; RXN or RDfile &#xF0E0; RInChI</li>
 *     <li>CDK IReaction -> RXN (written by the MDL RXN Writer of CDK) -> RInChI</li>
 * </ol>
 * <p>
 *     Analogously, there are also two routes to convert from RInChI to CDK IReaction:
 * </p>
 * <ol>
 *     <li>RInChI -> RXN or RDfile -> RinChIInput -> CDK IReaction</li>
 *     <li>RInChI -> RXN (read by the MDL RXN Reader of CDK) -> CDK Reaction</li>
 * </ol>
 * <p>
 *     The route that goes en route RInChIInput is the default route as it allows the handling of the complete reaction
 *     information including agents (e.g., catalysts and solvents) via MDL RDfile format (see <i>Known Limitations</i> below).
 * </p>
 * <p>
 *     The singleton class {@link org.openscience.cdk.rinchi.RInChIGeneratorFactory} is the universal entry point and provides
 *     access to {@link org.openscience.cdk.rinchi.RInChIGenerator}, {@link org.openscience.cdk.rinchi.RInChIToReaction} and
 *     {@link org.openscience.cdk.rinchi.RInChIDecomposition}. It's instance is obtained with
 *     {@code RInChIGeneratorFactory factory = RInChIGeneratorFactory.getInstance();}. If there is an issue with loading the
 *     native RInChI library the call to {@code RInChIGeneratorFactory.getInstance()} is expected to fail with an exception.
 *     If an error is encountered corresponding messages can be accessed by calling {@code getErrorMessage()} of
 *     RInChIGenerator, RInChIToReaction and RInChIDecomposition.
 * </p>
 * <h1>Good to know</h1>
 * <h2>Aromaticity</h2>
 * <p>
 *    The usage of "aromatic" bonds is strongly discouraged. Instead <b>Kekule</b> structures are <b>recommended</b>.
 *    <br>
 *    The MDL CTAB V2000 format specifies "aromatic" bonds only for query molecules. However, if "aromatic bonds"
 *    are encountered they are converted. Test cases show that this conversion works. However, this approach is
 *    prone to errors; for example, {@link org.openscience.cdk.io.MDLRXNWriter} throws an exception when trying to
 *    convert an <code>IReaction</code> object with "aromatic bonds" to RInChI.
 * </p>
 * <h2>Implicit Hydrogen Atoms</h2>
 * <p>
 *     The reaction components returned from the native RInChI library in the MDL RXN or RDFile format do not indicate the
 *     number of implicit hydrogen atoms. In the <code>jna-rinchi</code> library, the implicit valence (hydrogen atom count)
 *     for a given atom is determined by taking into account the element, charge and explicit valence (sum of all bond orders)
 *     by using the MDL valence model (see also <code>org.openscience.cdk.io.MDLValence</code>).
 * </p>
 * <h1>Known Limitations</h1>
 * <p>
 *     The functionality of the native RInChI library, the capabilities of CDK, and the required interconversions
 *     of their inputs and outputs results in known limitations.
 * </p>
 * <h2>Stereochemical Information</h2>
 * <h3>Reaction to RInChI</h3>
 * <p>
 *    CDK and jna-inchi support the following stereo elements: tetrahedral chiral atoms, allene atoms and double
 *    bond stereo configurations. The native RInChI library only supports MDL CTAB formats RXN V2000 and RDFile as
 *    input formats to consume reactions. As allene atoms are not supported in this file format, the stereochemical
 *    information of allene atoms is not captured in RInChI even if this information is present in the IReaction used
 *    as an input.
 *    <br>
 *    The MDL CTAB V2000 format allows the specification of tetrahedral chirality by both 2D/3D atom coordinates and
 *    without atom coordinates by stereo parity attributes, written on the atom line of the MDL CTAB block.
 *    The stereo parity approach is not supported by the native RInChI library (more precisely: when RXN or RDFile are
 *    read by the native RInChI library the chirality flag of MDL CTAB block counts line is stored in the RAuxInfo string).
 *    Generally, the stereo information for tetrahedral chiral atoms and double bonds is only taken into account by the native
 *    RInChI library if it can be inferred from 2D or 3D coordinates, that is, a CDK <code>IReaction</code> must have
 *    2D or 3D coordinates for the atoms of its constituent components. Moreover, the stereochemical information is lost
 *    even if the stereo elements of the components (e.g., chiral atoms, double bonds) are specified topologically using
 *    <code>StereoCenters.StereoElement</code> of CDK.
 * </p>
 * <h3>RInChI to Reaction</h3>
 * <p>
 *    If a RInChI does not come with an associated RAuxInfo that includes 2D/3D coordinates for its reaction components,
 *    stereochemical information for tetrahedral chiral atoms and double bonds that is present in the stereochemical layer
 *    of the RInChI string will not be present in the CDK <code>IReaction</code> yielded as a result of converting the RInChI.
 *    <br>
 *    When converting a (RInChI, RAuxInfo) pair to a chemical object of the type RinchiInput, the stereo information is stored
 *    implicitly in the atom coordinates. In jna-rinchi, there is no functionality to infer stereo elements from 2D or 3D
 *    coordinates. Such a functionality, however, is available in CDK. An IReaction object resulting from a conversion of a
 *    (RInChI, RAuxInfo) pair where the RAuxInfo has 2D or 3D coordinates will contain 2D/3D coordinates as well as a list
 *    of stereo elements.
 * </p>
 * The stereo elements within IReaction objects are generated along both routes a RInChI-to-Reaction conversion can take:
 * <ol>
 *     <li>the default route: a RInchiInput instance is converted to an IReaction instance and then calls are placed to
 *     {@link org.openscience.cdk.stereo.StereoElementFactory} for all reaction components of the IReaction instance</li>
 *     <li>alternate route using the MDL RXN V2000 reader of CDK: StereoElementFactory is invoked by the CDK
 *     {@link org.openscience.cdk.io.MDLRXNV2000Reader}}</li>
 * </ol>
 * <h2>Agents</h2>
 * <p>
 *     The official RXN syntax as specified in <i>CTFile Formats Biova Databases 2020</i> by Dassault Syst&egrave;mes only
 *     includes reagents and products on the <i>count line</i> of the RXN. Moreover, agents are also omitted from the list
 *     of Molfiles of the RXN (but may be specified in its data block). There is an unofficial agent-extension
 *     that was originally suggested by ChemAxon where the counts of agents is added to the <i>counts line</i> of
 *     the RXN and the structural information of agents is added as Molfiles after the Molfiles of reactants and products.
 * </p>
 * <p>
 *     If there are any agents present in the <code>IReaction</code> object and CDK is used to write a RXN V2000 file using
 *     its {@link org.openscience.cdk.io.MDLRXNWriter}, the agents are written according to the unofficial agent-extension.
 *     Unfortunately, the native RInChI library does not support the unofficial agent-extension of RXN and crashes on the
 *     <i>counts line</i> when trying to read such an RXN.
 *     <br>
 *     However, the native RInChI library is able to consider agents if they are specified within an RDfile, that is,
 *     in the counts line of an RXN record of an RDfile. This is the reason that RDfile is the preferred (and default)
 *     input format to feed into the native RInChI library when consuming a reaction to producing a RInChI.
 * </p>
 * There are two conversion routes when converting Reaction-to-RInChI. The non-default route is selected by explicitly setting
 * the flag <code>useCDK_MDL_IO</code> to <code>true</code>.
 * <ol>
 *     <li><code>useCDK_MDL_IO</code> set to <code>false</code> (default): CDK's <code>IReaction</code> object is converted to
 *     a <code>RinchiInput</code> object. The <code>RinchiInput</code> instance is consumed by and converted to an RDfile by
 *     the <code>jna-rinchi</code> library. The RDfile is then used as an input for the native RInChI library. Any information
 *     regarding agents is successfully retained in all objects along this chain and processed by the native RInChI library.</li>
 *     <li><code>useCDK_MDL_IO</code> set to <code>true</code>: This uses the capability of CDK to convert the <code>IReaction</code>
 *     object to an RXN V2000. Prior to this conversion any agents present in the reaction are removed to ensure that the resultant
 *     RXN V2000 can be consumed by the native RInChI library. Next, this agent-less RXN V2000 is consumed by the
 *     <code>jna-rinchi</code> library and passed on to the native RInChI library as an input.</li>
 * </ol>
 * <p>
 *     The flag <code>useCDK_MDL_IO</code> is used to specify the input format for {@link org.openscience.cdk.rinchi.RInChIGenerator}.
 *     Calls to RInChIGenerator that explicitly specify the flag <code>useCDK_MDL_IO</code> are placed via the RInChIGenerator, more specifically
 *     {@link org.openscience.cdk.rinchi.RInChIGeneratorFactory#getRInChIGenerator(org.openscience.cdk.interfaces.IReaction, io.github.dan2097.jnarinchi.RinchiOptions, boolean)}.
 *     Please also note that the agent type information is not supported by the native RInChI library,
 *     thus different agent types (e.g. catalysts, solvents etc.) cannot be distinguished.
 * </p>
 * <h2>Radicals</h2>
 * <p>
 *    Radicals are supported by the InChI and RInChI standard as well as by the <code>jna-inchi</code> and CDK libraries.
 *    The conversion of radical information as represented in CDK to and from its representation in RInChI is implemented.
 *    In case of RInChI-to-Reaction conversion the correct handling of radical information stored in RInChI requires the
 *    usage of RAuxInfo (comparable to the processing of stereochemical information), in particular for peculiar cases
 *    of molecules with two radicals (which is chemically rare anyway).
 * </p>
 */
package org.openscience.cdk.rinchi;