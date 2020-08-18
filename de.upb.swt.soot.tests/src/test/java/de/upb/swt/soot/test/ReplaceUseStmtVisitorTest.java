package de.upb.swt.soot.test;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.ref.Ref;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.ReplaceUseStmtVisitor;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class ReplaceUseStmtVisitorTest {
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  JavaJimple javaJimple = JavaJimple.getInstance();
  JavaClassType intType = factory.getClassType("int");
  JavaClassType testClass = factory.getClassType("TestClass");
  JavaClassType voidType = factory.getClassType("void");

  Local op1 = JavaJimple.newLocal("op1", intType);
  Local op2 = JavaJimple.newLocal("op2", intType);
  Local newOp = JavaJimple.newLocal("op#", intType);
  Local var = JavaJimple.newLocal("var", intType);

  Local base = JavaJimple.newLocal("base", testClass);
  Local arg1 = JavaJimple.newLocal("arg1", intType);
  Local arg2 = JavaJimple.newLocal("arg2", intType);
  Local arg3 = JavaJimple.newLocal("arg3", intType);
  Local newArg = JavaJimple.newLocal("argn", intType);

  FieldSignature fieldSignature = new FieldSignature(testClass, "field", intType);
  MethodSignature methodeWithOutParas =
      new MethodSignature(testClass, "invokeExpr", Collections.emptyList(), voidType);

  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  /** Test use replacing in case JAssignStmt. */
  @Test
  public void testcaseAssignStmt() {

    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(op1, newOp);

    // rValue is a Expr
    Expr addExpr = JavaJimple.newAddExpr(op1, op2);
    Stmt stmt = JavaJimple.newAssignStmt(var, addExpr, noStmtPositionInfo);
    stmt.accept(visitor);
    Stmt newStmt = visitor.getNewStmt();

    List<Value> expectedUses = new ArrayList<>();
    expectedUses.add(JavaJimple.newAddExpr(newOp, op2));
    expectedUses.add(newOp);
    expectedUses.add(op2);

    boolean isExpected = false;
    for (int i = 0; i < 3; i++) {
      isExpected = newStmt.getUses().get(i).equivTo(expectedUses.get(i));
      if (!isExpected) {
        break;
      }
    }
    assertTrue(isExpected);

    // rValue is a Ref
    Ref ref = javaJimple.newArrayRef(op1, op1);
    stmt = JavaJimple.newAssignStmt(var, ref, noStmtPositionInfo);
    stmt.accept(visitor);
    newStmt = visitor.getNewStmt();

    expectedUses.set(0, javaJimple.newArrayRef(newOp, newOp));
    expectedUses.set(2, newOp);

    isExpected = false;
    for (int i = 0; i < 3; i++) {
      isExpected = newStmt.getUses().get(i).equivTo(expectedUses.get(i));
      if (!isExpected) {
        break;
      }
    }
    assertTrue(isExpected);

    // rValue is a Local
    stmt = JavaJimple.newAssignStmt(var, op1, noStmtPositionInfo);
    stmt.accept(visitor);
    stmt = visitor.getNewStmt();

    expectedUses.clear();
    expectedUses.add(newOp);

    assertTrue(stmt.getUses().equals(expectedUses));
  }

  /** Test use replacing in case JInvokeStmt and JIfStmt Here JInvokeStmt is as an example */
  @Test
  public void testcaseInvokeStmt() {

    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(base, newOp);

    // invokeExpr
    AbstractInvokeExpr invokeExpr =
        JavaJimple.newSpecialInvokeExpr(base, methodeWithOutParas, Collections.emptyList());
    Stmt stmt = JavaJimple.newInvokeStmt(invokeExpr, noStmtPositionInfo);
    stmt.accept(visitor);
    Stmt newStmt = visitor.getNewStmt();

    List<Value> expectedUses = new ArrayList<>();
    expectedUses.add(newOp);
    expectedUses.add(
        JavaJimple.newSpecialInvokeExpr(newOp, methodeWithOutParas, Collections.emptyList()));

    boolean isExpected = false;
    for (int i = 0; i < 2; i++) {
      isExpected = newStmt.getUses().get(i).equivTo(expectedUses.get(i));
      if (!isExpected) {
        break;
      }
    }
    assertTrue(isExpected);
  }

  @Test
  /** Test use replacing in other cases Here JReturnStmt is as an example */
  public void testCaseReturnStmt() {
    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(op1, newOp);
    Stmt stmt = JavaJimple.newRetStmt(op1, noStmtPositionInfo);
    stmt.accept(visitor);
    Stmt newStmt = visitor.getNewStmt();

    List<Value> expectedUses = new ArrayList<>();
    expectedUses.add(newOp);
    assertTrue(newStmt.getUses().equals(expectedUses));
  }
}
