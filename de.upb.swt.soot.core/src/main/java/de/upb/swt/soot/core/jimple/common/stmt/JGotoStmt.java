/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.swt.soot.core.jimple.common.stmt;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.List;
import javax.annotation.Nonnull;

/** Unconditionally jumps to a target Stmt */
public final class JGotoStmt extends Stmt implements Copyable, BranchingStmt {

  public JGotoStmt(StmtPositionInfo positionInfo) {
    super(positionInfo);
  }

  @Override
  public String toString() {
    /*    TODO: [ms] Stmt t = getTarget();
    String target = "(branch)";
    if (!t.branches()) {
      target = t.toString();
    }
    */
    return Jimple.GOTO; // + " [?= " + target + "]";
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.GOTO);
    /*  TODO: [ms]  up.literal(" ");
       up.stmtRef(getTarget(), true);

    */
  }

  public Stmt getTarget(Body body) {
    // TODO: [ms] validate in builder!
    return getTargetStmts(body).get(0);
  }

  @Override
  @Nonnull
  public List<Stmt> getTargetStmts(Body body) {
    return body.getBranchTargets(this);
  }

  @Override
  public void accept(@Nonnull Visitor sw) {
    ((StmtVisitor) sw).caseGotoStmt(this);
  }

  @Override
  public boolean fallsThrough() {
    return false;
  }

  @Override
  public boolean branches() {
    return true;
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseGotoStmt(this, o);
  }

  // TODO: [ms] method is now useless w/o target information!
  @Override
  public int equivHashCode() {
    // return target.equivHashCode();
    return -42;
  }

  @Nonnull
  public JGotoStmt withPositionInfo(@Nonnull StmtPositionInfo positionInfo) {
    return new JGotoStmt(positionInfo);
  }

  /** This class is for internal use only. It will be removed in the future. */
  @Deprecated
  // FIXME: [ms] leftover
  public static class $Accessor {
    // This class deliberately starts with a $-sign to discourage usage
    // of this Soot implementation detail.

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void setTarget(@Nonnull JGotoStmt stmt, @Nonnull Stmt target) {
      // stmt.setTarget(target);

    }

    private $Accessor() {}
  }
}
