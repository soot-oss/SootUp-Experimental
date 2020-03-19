/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.javatestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import java.util.Collections;
import java.util.EnumSet;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author: Hasitha Rajapakse */
@Category(Java8Test.class)
public class NoModifierClassTest extends MinimalTestSuiteBase {

  @Test
  public void test() {
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertEquals(EnumSet.noneOf(Modifier.class), clazz.getModifiers());

    assertTrue(clazz.getMethod(getMethodSignature("private")).get().isPrivate());
    assertTrue(clazz.getMethod(getMethodSignature("protected")).get().isProtected());
    assertTrue(clazz.getMethod(getMethodSignature("public")).get().isPublic());
    assertTrue(clazz.getMethod(getMethodSignature("noModifier")).get().getModifiers().isEmpty());
  }

  public MethodSignature getMethodSignature(String modifier) {
    return identifierFactory.getMethodSignature(
        modifier + "Method", getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
