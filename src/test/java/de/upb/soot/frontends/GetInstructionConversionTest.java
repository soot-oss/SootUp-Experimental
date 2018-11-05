package de.upb.soot.frontends;

import static org.junit.Assert.assertTrue;

import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.util.printer.Printer;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

/**
 *
 * @author Linghui Luo
 *
 */
@Category(Java8Test.class)
public class GetInstructionConversionTest {

   private WalaClassLoader loader;
   private DefaultSignatureFactory sigFactory;
   private JavaClassSignature declareClassSig;

   @Before
   public void loadClassesWithWala() {
    String srcDir = "src/test/resources/wala-tests/";
     loader = new WalaClassLoader(srcDir, null);
     sigFactory = new DefaultSignatureFactory();
    declareClassSig = sigFactory.getClassSignature("alreadywalaunittests.InnerClassAA");
   }

   @Test
   public void test() {
     Optional<SootMethod> m = loader
         .getSootMethod(
            sigFactory.getMethodSignature("getA_X", declareClassSig, "int", Arrays.asList()));
     assertTrue(m.isPresent());
     SootMethod method = m.get();
     PrintWriter writer = new PrintWriter(System.out);
     Printer printer = new Printer();
     printer.printTo(method.getActiveBody(), writer);
     writer.flush();
     writer.close();
   }
}
