/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2003  The Jmol project
 * Copyright (C) 2003  The Chemistry Development Kit (CDK) project
 * 
 * Contact: cdk-devel@lists.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the
 * beginning of your source code files, and to any copyright notice that
 * you may distribute with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.openscience.cdk.graph.rebond;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.exception.CDKException;

/**
 * Provides tools to rebond a molecule from 3D coordinates only.
 * The algorithm uses an efficient algorithm using a
 * Binary Space Partitioning Tree (Bspt). It requires that the 
 * atom types are configured such that the covalent bond radii
 * for all atoms are set. The AtomTypeFactory can be used for this.
 *
 * @keyword    rebonding
 * @keyword    bond, recalculation
 * 
 * @see org.openscience.cdk.graph.rebond.Bspt
 */
public class RebondTool {

  private double maxCovalentRadius;
  private double minBondDistance;
  private double bondTolerance;
    
  private Bspt bspt;
    
  public RebondTool(double maxCovalentRadius, double minBondDistance,
                    double bondTolerance) {
    this.maxCovalentRadius = maxCovalentRadius;
    this.bondTolerance = bondTolerance;
    this.minBondDistance = minBondDistance;    
    this.bspt = null;
  }
    
  /**
   * Rebonding using a Binary Space Partition Tree. Note, that any bonds
   * defined will be deleted first.
   *
   * @author  Miguel Howard
   * @created 2003-05-23
   */
  public void rebond(AtomContainer container) throws CDKException {
    container.removeAllBonds();
    maxCovalentRadius = 0.0;
    // construct a new binary space partition tree
    bspt = new Bspt(3);
    Atom[] atoms = container.getAtoms();
    for (int i = atoms.length; --i >= 0; ) {
      Atom atom = atoms[i];
      double myCovalentRadius = atom.getCovalentRadius();
      if (myCovalentRadius == 0.0) {
          throw new CDKException("Atom(s) does not have covalentRadius defined.");
      }
      if (myCovalentRadius > maxCovalentRadius)
        maxCovalentRadius = myCovalentRadius;
      TupleAtom tupleAtom = new TupleAtom(atom);
      bspt.addTuple(tupleAtom);
    }
    // rebond all atoms
    for (int i = atoms.length; --i >= 0; ) {
      bondAtom(container, atoms[i]);
    }
  }
    
  /**
   * Rebonds one atom by looking up nearby atom using the binary space partition tree.
   */
  private void bondAtom(AtomContainer container, Atom atom) {
    double myCovalentRadius = atom.getCovalentRadius();
    double searchRadius = myCovalentRadius + maxCovalentRadius + bondTolerance;
    Point tupleAtom = new Point(atom.getX3D(), atom.getY3D(), atom.getZ3D());
    for (Bspt.EnumerateSphere e = bspt.enumHemiSphere(tupleAtom, searchRadius); e.hasMoreElements(); ) {
      Atom atomNear = ((TupleAtom)e.nextElement()).getAtom();
      if (atomNear != atom && container.getBond(atom, atomNear) == null) {
        boolean isBonded = isBonded(atom, myCovalentRadius,
                                    atomNear, atomNear.getCovalentRadius(),
                                    e.foundDistance2());
        if (isBonded) {
          Bond bond = new Bond(atom, atomNear, 1.0);
          container.addBond(bond);
        }
      }
    }
  }

  /** 
   * Returns the bond order for the bond. At this moment, it only returns
   * 0 or 1, but not 2 or 3, or aromatic bond order.
   */
  private boolean isBonded(Atom atomA, double covalentRadiusA,
                           Atom atomB, double covalentRadiusB,
                           double distance2) {
    double maxAcceptable =
      covalentRadiusA + covalentRadiusB + bondTolerance;
    double maxAcceptable2 = maxAcceptable * maxAcceptable;
    double minBondDistance2 = this.minBondDistance*this.minBondDistance;
    if (distance2 < minBondDistance2)
      return false;
    if (distance2 <= maxAcceptable2)
      return true;
    return false;
  }
    
  class TupleAtom implements Bspt.Tuple {
    Atom atom;
        
    TupleAtom(Atom atom) {
      this.atom = atom;
    }
        
    public double getDimValue(int dim) {
      if (dim == 0)
        return atom.getX3D();
      if (dim == 1)
        return atom.getY3D();
      return atom.getZ3D();
    }
        
    public Atom getAtom() {
      return this.atom;
    }
        
    public String toString() {
      return ("<" + atom.getX3D() + "," + atom.getY3D() + "," +
              atom.getZ3D() + ">");
    }
  }
    
}
