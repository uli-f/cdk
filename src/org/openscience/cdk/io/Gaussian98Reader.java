/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2002-2003  The Jmol Development Team
 * Copyright (C) 2003  The Chemistry Development Kit (CDK) project
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307  USA.
 */
package org.openscience.cdk.io;

import org.openscience.cdk.*;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.tools.IsotopeFactory;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.StringTokenizer;
import javax.vecmath.Point3d;

/**
 * A reader for Gaussian98 output.
 * Gaussian 98 is a quantum chemistry program
 * by Gaussian, Inc. (http://www.gaussian.com/).
 *
 * <p> Molecular coordinates, energies, and normal coordinates of
 * vibrations are read. Each set of coordinates is added to the
 * ChemFile in the order they are found. Energies and vibrations
 * are associated with the previously read set of coordinates.
 *
 * <p> This reader was developed from a small set of
 * example output files, and therefore, is not guaranteed to
 * properly read all Gaussian98 output. If you have problems,
 * please contact the author of this code, not the developers
 * of Gaussian98.
 *
 * @author Bradley A. Smith <yeldar@home.com>
 * @author Egon Willighagen
 */
public class Gaussian98Reader extends DefaultChemObjectReader {

    private IsotopeFactory isotopeFactory;
    private BufferedReader input;

    /**
     * Create an Gaussian98 output reader.
     *
     * @param input source of Gaussian98 data
     */
    public Gaussian98Reader(Reader input) {
        try {
            isotopeFactory = IsotopeFactory.getInstance();
        } catch (Exception exception) {
            // should not happen
        }
        if (input instanceof BufferedReader) {
            this.input = (BufferedReader)input;
        } else {
            this.input = new BufferedReader(input);
        }
    }

    public boolean accepts(ChemObject object) {
        if (object instanceof ChemFile) {
            return true;
        } else {
            return false;
        }
    }
    
    public ChemObject read(ChemObject object) throws CDKException {
        if (object instanceof ChemFile) {
            ChemFile file = null;
            try {
                file = readChemFile();
            } catch (IOException exception) {
                throw new CDKException(
                    "Error while reading file: " + exception.toString()
                );
            }
            return file;
        } else {
            throw new CDKException("Reading of a " + object.getClass().getName() +
                " is not supported.");
        }
    }
    
    public void close() throws IOException {
        input.close();
    }

    /**
     * Read the Gaussian98 output.
     *
     * @return    a ChemFile with the coordinates, energies, and vibrations.
     * @exception IOException if an I/O error occurs
     */
    private ChemFile readChemFile() throws CDKException, IOException {
        ChemFile chemFile = new ChemFile();
        ChemSequence sequence = new ChemSequence();
        ChemModel model = null;
        String line = input.readLine();
        String levelOfTheory = null;
        
        // Find first set of coordinates by skipping all before "Standard orientation"
        while (input.ready() && (line != null)) {
            if (line.indexOf("Standard orientation:") >= 0) {
                
                // Found a set of coordinates
                model = new ChemModel();
                readCoordinates(model);
                break;
            }
            line = input.readLine();
        }
        if (model != null) {
            
            // Read all other data
            line = input.readLine();
            while (input.ready() && (line != null)) {
                if (line.indexOf("Standard orientation:") >= 0) {
                    
                    // Found a set of coordinates
                    // Add current frame to file and create a new one.
                    sequence.addChemModel(model);
                    fireFrameRead();
                    model = new ChemModel();
                    readCoordinates(model);
                } else if (line.indexOf("SCF Done:") >= 0) {
                    
                    // Found an energy
                    model.setProperty(CDKConstants.REMARK, line.trim());
                } else if (line.indexOf("Harmonic frequencies") >= 0) {
                    
                    // Found a set of vibrations
                    // readFrequencies(frame);
                } else if (line.indexOf("Magnetic shielding") >= 0) {
                    
                    // Found NMR data
                    // readNMRData(frame, line);
                    
                } else if (line.indexOf("GINC") >= 0) {
                    
                    // Found calculation level of theory
                    levelOfTheory = parseLevelOfTheory(line);
                }
                line = input.readLine();
            }
            
            // Add last frame to file
            sequence.addChemModel(model);
            fireFrameRead();
        }
        chemFile.addChemSequence(sequence);
        
        return chemFile;
    }
    
    /**
     * Reads a set of coordinates into ChemFrame.
     *
     * @param frame  the destination ChemFrame
     * @exception IOException  if an I/O error occurs
     */
    private void readCoordinates(ChemModel model) throws CDKException, IOException {
        SetOfMolecules moleculeSet = new SetOfMolecules();
        Molecule molecule = new Molecule();
        String line = input.readLine();
        line = input.readLine();
        line = input.readLine();
        line = input.readLine();
        while (input.ready()) {
            line = input.readLine();
            if ((line == null) || (line.indexOf("-----") >= 0)) {
                break;
            }
            int atomicNumber = 0;
            StringReader sr = new StringReader(line);
            StreamTokenizer token = new StreamTokenizer(sr);
            token.nextToken();
            
            // ignore first token
            if (token.nextToken() == StreamTokenizer.TT_NUMBER) {
                atomicNumber = (int) token.nval;
                if (atomicNumber == 0) {
                    
                    // Skip dummy atoms. Dummy atoms must be skipped
                    // if frequencies are to be read because Gaussian
                    // does not report dummy atoms in frequencies, and
                    // the number of atoms is used for reading frequencies.
                    continue;
                }
            } else {
                throw new CDKException("Error while reading coordinates: expected integer.");
            }
            token.nextToken();
            
            // ignore third token
            double x = 0.0;
            double y = 0.0;
            double z = 0.0;
            if (token.nextToken() == StreamTokenizer.TT_NUMBER) {
                x = token.nval;
            } else {
                throw new IOException("Error reading x coordinate");
            }
            if (token.nextToken() == StreamTokenizer.TT_NUMBER) {
                y = token.nval;
            } else {
                throw new IOException("Error reading y coordinate");
            }
            if (token.nextToken() == StreamTokenizer.TT_NUMBER) {
                z = token.nval;
            } else {
                throw new IOException("Error reading z coordinate");
            }
            Atom atom = new Atom(isotopeFactory.getElementSymbol(atomicNumber));
            atom.setPoint3D(new Point3d(x, y, z));
            molecule.addAtom(atom);
        }
        moleculeSet.addMolecule(molecule);
        model.setSetOfMolecules(moleculeSet);
    }

    /**
     * Reads a set of vibrations into ChemFrame.
     *
     * @param frame  the destination ChemFrame
     * @exception IOException  if an I/O error occurs
     */
    private void readFrequencies(ChemModel model) throws IOException {
        /* FIXME: this is yet to be ported
        String line;
        line = input.readLine();
        line = input.readLine();
        line = input.readLine();
        line = input.readLine();
        line = input.readLine();
        while ((line != null) && line.startsWith(" Frequencies --")) {
            Vector currentVibs = new Vector();
            StringReader vibValRead = new StringReader(line.substring(15));
            StreamTokenizer token = new StreamTokenizer(vibValRead);
            while (token.nextToken() != StreamTokenizer.TT_EOF) {
                Vibration vib = new Vibration(Double.toString(token.nval));
                currentVibs.addElement(vib);
            }
            line = input.readLine();
            line = input.readLine();
            line = input.readLine();
            line = input.readLine();
            line = input.readLine();
            line = input.readLine();
            for (int i = 0; i < frame.getAtomCount(); ++i) {
                line = input.readLine();
                StringReader vectorRead = new StringReader(line);
                token = new StreamTokenizer(vectorRead);
                token.nextToken();
                
                // ignore first token
                token.nextToken();

                // ignore second token
                for (int j = 0; j < currentVibs.size(); ++j) {
                    double[] v = new double[3];
                    if (token.nextToken() == StreamTokenizer.TT_NUMBER) {
                        v[0] = token.nval;
                    } else {
                        throw new IOException("Error reading frequency");
                    }
                    if (token.nextToken() == StreamTokenizer.TT_NUMBER) {
                        v[1] = token.nval;
                    } else {
                        throw new IOException("Error reading frequency");
                    }
                    if (token.nextToken() == StreamTokenizer.TT_NUMBER) {
                        v[2] = token.nval;
                    } else {
                        throw new IOException("Error reading frequency");
                    }
                    ((Vibration) currentVibs.elementAt(j)).addAtomVector(v);
                }
            }
            for (int i = 0; i < currentVibs.size(); ++i) {
                frame.addVibration((Vibration) currentVibs.elementAt(i));
            }
            line = input.readLine();
            line = input.readLine();
            line = input.readLine();
        } */
    }

    /**
     * Reads NMR nuclear shieldings.
     */
    private void readNMRData(ChemModel model, String labelLine) throws CDKException {
        /* FIXME: this is yet to be ported
        // Determine label for properties
        String label;
        if (labelLine.indexOf("Diamagnetic") >= 0) {
            label = "Diamagnetic Magnetic shielding (Isotropic)";
        } else if (labelLine.indexOf("Paramagnetic") >= 0) {
            label = "Paramagnetic Magnetic shielding (Isotropic)";
        } else {
            label = "Magnetic shielding (Isotropic)";
        }
        int atomIndex = 0;
        for (int i = 0; i < frame.getAtomCount(); ++i) {
            String line = input.readLine().trim();
            while (line.indexOf("Isotropic") < 0) {
                if (line == null) {
                    return;
                }
                line = input.readLine().trim();
            }
            StringTokenizer st1 = new StringTokenizer(line);
            
            // Find Isotropic label
            while (st1.hasMoreTokens()) {
                if (st1.nextToken().equals("Isotropic")) {
                    break;
                }
            }
            
            // Find Isotropic value
            while (st1.hasMoreTokens()) {
                if (st1.nextToken().equals("=")) {
                    break;
                }
            }
            double shielding = Double.valueOf(st1.nextToken()).doubleValue();
            NMRShielding ns1 = new NMRShielding(label, shielding);
            ((org.openscience.jmol.Atom)frame.getAtomAt(atomIndex)).addProperty(ns1);
            ++atomIndex;
        } */
    }

    /**
     * Select the theory and basis set from the first archive line.
     */
    private String parseLevelOfTheory(String line) {
        
        StringTokenizer st1 = new StringTokenizer(line, "\\");
        
        // Must contain at least 6 tokens
        if (st1.countTokens() < 6) {
            return null;
        }
        
        // Skip first four tokens
        for (int i = 0; i < 4; ++i) {
            st1.nextToken();
        }
        return st1.nextToken() + "/" + st1.nextToken();
    }

}
