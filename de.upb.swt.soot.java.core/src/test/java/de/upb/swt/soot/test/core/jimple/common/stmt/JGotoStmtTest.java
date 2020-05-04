/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 15.11.2018 Markus Schmidt
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

package de.upb.swt.soot.test.core.jimple.common.stmt;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JThrowStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Markus Schmidt & Linghui Luo */
@Category(Java8Test.class)
public class JGotoStmtTest {

  @Test
  public void test() {

    // FIXME: [ms] leftover refactoring->jgrapht/eliminateboxes/removing jumptarget infos from stmts
    // itself

    StmtPositionInfo nop = StmtPositionInfo.createNoStmtPositionInfo();
    JavaIdentifierFactory typeFactory = JavaIdentifierFactory.getInstance();

    Local local1 = new Local("$r0", typeFactory.getType("java.lang.Exception"));
    Local local2 = new Local("$r0", typeFactory.getType("somepackage.dummy.Exception"));

    // Stmt
    Stmt targetStmt = new JThrowStmt(local1, nop);
    Stmt gStmt = new JGotoStmt(nop);

    // toString
    Assert.assertEquals("goto [?= throw $r0]", gStmt.toString());

    // equivTo
    Assert.assertFalse(gStmt.equivTo(targetStmt));

    Assert.assertTrue(gStmt.equivTo(new JGotoStmt(nop)));
    Assert.assertFalse(gStmt.equivTo(new JGotoStmt(nop)));
  }
}
