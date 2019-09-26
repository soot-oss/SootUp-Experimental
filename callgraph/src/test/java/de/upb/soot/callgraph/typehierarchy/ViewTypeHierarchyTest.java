package de.upb.soot.callgraph.typehierarchy;

import static de.upb.soot.util.ImmutableUtils.immutableList;
import static de.upb.soot.util.ImmutableUtils.immutableSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.upb.soot.IdentifierFactory;
import de.upb.soot.Project;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SourceType;
import de.upb.soot.frontends.EagerJavaClassSource;
import de.upb.soot.javabytecodefrontend.frontend.AsmJavaClassProvider;
import de.upb.soot.javabytecodefrontend.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.soot.types.ArrayType;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.NullType;
import de.upb.soot.types.PrimitiveType;
import de.upb.soot.types.Type;
import de.upb.soot.util.ImmutableUtils;
import de.upb.soot.views.View;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class ViewTypeHierarchyTest {

  private View view;
  private ViewTypeHierarchy typeHierarchy;
  private JavaClassPathAnalysisInputLocation analysisInputLocation;

  @Before
  public void setup() {

    String jarFile = MethodDispatchResolverTest.jarFile;
    assertTrue(new File(jarFile).exists());
    String currentClassPath =
        System.getProperty("java.class.path")
            + File.pathSeparator
            + ManagementFactory.getRuntimeMXBean().getBootClassPath();
    String rtJarClassPath =
        Arrays.stream(currentClassPath.split(File.pathSeparator))
            .filter(pathEntry -> pathEntry.endsWith(File.separator + "rt.jar"))
            .distinct()
            .collect(Collectors.joining(File.pathSeparator));
    analysisInputLocation =
        new JavaClassPathAnalysisInputLocation(
            jarFile + File.pathSeparator + rtJarClassPath, new AsmJavaClassProvider());
    Project<JavaClassPathAnalysisInputLocation> p = new Project<>(analysisInputLocation);
    view = p.createOnDemandView();
    typeHierarchy = new ViewTypeHierarchy(view);
  }

  @Test
  public void implementersOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    JavaClassType iNamespace = factory.getClassType("INamespace", "de.upb.soot.namespaces");
    Set<JavaClassType> implementers = typeHierarchy.implementersOf(iNamespace);
    ImmutableSet<JavaClassType> expectedImplementers =
        immutableSet(
            factory.getClassType("de.upb.soot.namespaces.PathBasedNamespace$ArchiveBasedNamespace"),
            factory.getClassType("de.upb.soot.namespaces.PathBasedNamespace"),
            factory.getClassType("de.upb.soot.namespaces.AbstractNamespace"),
            factory.getClassType("de.upb.soot.namespaces.JavaClassPathNamespace"),
            factory.getClassType(
                "de.upb.soot.namespaces.PathBasedNamespace$DirectoryBasedNamespace"));
    assertEquals(expectedImplementers, implementers);

    expectedImplementers.forEach(
        expectedImplementer ->
            assertTrue(typeHierarchy.isSubtype(iNamespace, expectedImplementer)));
  }

  @Test
  public void subclassesOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    JavaClassType abstractNamespace =
        factory.getClassType("AbstractNamespace", "de.upb.soot.namespaces");
    Set<JavaClassType> subclasses = typeHierarchy.subclassesOf(abstractNamespace);
    ImmutableSet<JavaClassType> expectedSubclasses =
        immutableSet(
            factory.getClassType("de.upb.soot.namespaces.PathBasedNamespace$ArchiveBasedNamespace"),
            factory.getClassType("de.upb.soot.namespaces.PathBasedNamespace"),
            factory.getClassType("de.upb.soot.namespaces.JavaClassPathNamespace"),
            factory.getClassType(
                "de.upb.soot.namespaces.PathBasedNamespace$DirectoryBasedNamespace"));
    assertEquals(expectedSubclasses, subclasses);
    assertFalse(
        "A class should not be a subclass of itself", subclasses.contains(abstractNamespace));
    expectedSubclasses.forEach(
        expectedSubclass ->
            assertTrue(typeHierarchy.isSubtype(abstractNamespace, expectedSubclass)));
  }

  @Test
  public void implementedInterfacesOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    JavaClassType javaClassPathNamespace =
        factory.getClassType("JavaClassPathNamespace", "de.upb.soot.namespaces");
    JavaClassType iNamespace = factory.getClassType("de.upb.soot.namespaces.INamespace");
    Set<JavaClassType> implementedInterfaces =
        typeHierarchy.implementedInterfacesOf(javaClassPathNamespace);
    assertEquals(immutableSet(iNamespace), implementedInterfaces);
    assertTrue(typeHierarchy.isSubtype(iNamespace, javaClassPathNamespace));

    // Test with an interface that extends another one, i.e. List extends Collection
    JavaClassType arrayList = factory.getClassType("ArrayList", "java.util");
    JavaClassType collection = factory.getClassType("Collection", "java.util");
    JavaClassType list = factory.getClassType("List", "java.util");
    Set<JavaClassType> implementedInterfacesOfArrayList =
        typeHierarchy.implementedInterfacesOf(arrayList);
    assertTrue(
        "ArrayList implements Collection", implementedInterfacesOfArrayList.contains(collection));
    assertTrue("ArrayList implements Collection", typeHierarchy.isSubtype(collection, arrayList));
    assertTrue("ArrayList implements List", implementedInterfacesOfArrayList.contains(list));
    assertTrue("ArrayList implements List", typeHierarchy.isSubtype(list, arrayList));
    assertTrue(
        "List is an implementer of Collection",
        typeHierarchy.implementersOf(collection).contains(list));
  }

  @Test
  public void superClassOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    JavaClassType javaClassPathNamespace =
        factory.getClassType("JavaClassPathNamespace", "de.upb.soot.namespaces");
    JavaClassType superClass = typeHierarchy.superClassOf(javaClassPathNamespace);
    assertEquals(factory.getClassType("de.upb.soot.namespaces.AbstractNamespace"), superClass);
    assertNull(
        "java.lang.Object should not have a superclass",
        typeHierarchy.superClassOf(factory.getClassType("java.lang.Object")));
    assertEquals(
        "In Soot, interfaces should have java.lang.Object as the superclass",
        factory.getClassType("java.lang.Object"),
        typeHierarchy.superClassOf(factory.getClassType("java.util.Collection")));
  }

  @Test
  public void superClassesOf() {
    IdentifierFactory factory = view.getIdentifierFactory();
    JavaClassType javaClassPathNamespace =
        factory.getClassType("JavaClassPathNamespace", "de.upb.soot.namespaces");
    List<JavaClassType> superClasses = typeHierarchy.superClassesOf(javaClassPathNamespace);
    ImmutableList<JavaClassType> expectedSuperClasses =
        immutableList(
            factory.getClassType("de.upb.soot.namespaces.AbstractNamespace"),
            factory.getClassType("java.lang.Object"));

    assertEquals(expectedSuperClasses, superClasses);
    expectedSuperClasses.forEach(
        expectedSuperClass ->
            assertTrue(typeHierarchy.isSubtype(expectedSuperClass, javaClassPathNamespace)));
  }

  @Test
  public void primitiveTypeSubtyping() {
    assertFalse(
        "Primitive types should not have subtype relations",
        typeHierarchy.isSubtype(PrimitiveType.getInt(), PrimitiveType.getInt()));
    assertFalse(
        "Primitive types should not have subtype relations",
        typeHierarchy.isSubtype(PrimitiveType.getDouble(), PrimitiveType.getInt()));
  }

  @Test
  public void nullTypeSubtyping() {
    IdentifierFactory factory = view.getIdentifierFactory();
    assertTrue(
        "null should be valid value for all reference types",
        typeHierarchy.isSubtype(factory.getClassType("java.lang.Object"), NullType.getInstance()));
    assertTrue(
        "null should be valid value for all reference types",
        typeHierarchy.isSubtype(factory.getClassType("java.lang.String"), NullType.getInstance()));
    assertTrue(
        "null should be valid value for all reference types",
        typeHierarchy.isSubtype(
            factory.getClassType("JavaClassPathNamespace", "de.upb.soot.namespaces"),
            NullType.getInstance()));
    assertFalse(
        "null should not be a valid value for primitive types",
        typeHierarchy.isSubtype(PrimitiveType.getInt(), NullType.getInstance()));
    assertFalse(
        "null should not be a valid value for primitive types",
        typeHierarchy.isSubtype(PrimitiveType.getDouble(), NullType.getInstance()));
  }

  @Test
  public void addType() {
    IdentifierFactory factory = view.getIdentifierFactory();
    EagerJavaClassSource classSource =
        new EagerJavaClassSource(
            analysisInputLocation,
            null,
            factory.getClassType("adummytype.Type"),
            factory.getClassType("de.upb.soot.namespaces.JavaClassPathNamespace"),
            Collections.emptySet(),
            null,
            Collections.emptySet(),
            Collections.emptySet(),
            null,
            EnumSet.of(Modifier.FINAL));
    SootClass sootClass = new SootClass(classSource, SourceType.Application);

    typeHierarchy.addType(sootClass);

    assertTrue(
        "Newly added type must be detected as a subtype",
        typeHierarchy
            .subclassesOf(factory.getClassType("de.upb.soot.namespaces.AbstractNamespace"))
            .contains(sootClass.getType()));
  }

  @Test
  public void arraySubtyping() {
    IdentifierFactory factory = view.getIdentifierFactory();
    ArrayType stringArrayDim1Type =
        factory.getArrayType(factory.getClassType("java.lang.String"), 1);
    ArrayType stringArrayDim3Type =
        factory.getArrayType(factory.getClassType("java.lang.String"), 3);

    ArrayType objectArrayDim1Type =
        factory.getArrayType(factory.getClassType("java.lang.Object"), 1);
    ArrayType objectArrayDim2Type =
        factory.getArrayType(factory.getClassType("java.lang.Object"), 2);
    ArrayType objectArrayDim3Type =
        factory.getArrayType(factory.getClassType("java.lang.Object"), 3);

    ArrayType doubleArrayDim1Type = factory.getArrayType(PrimitiveType.getDouble(), 1);
    ArrayType doubleArrayDim2Type = factory.getArrayType(PrimitiveType.getDouble(), 2);

    ArrayType collectionArrayDim1Type =
        factory.getArrayType(factory.getClassType("java.util.Collection"), 1);
    ArrayType arrayListDim1Type =
        factory.getArrayType(factory.getClassType("java.util.ArrayList"), 1);
    ArrayType arrayListDim2Type =
        factory.getArrayType(factory.getClassType("java.util.ArrayList"), 2);

    ArrayType serializableDim1Type =
        factory.getArrayType(factory.getClassType("java.io.Serializable"), 1);
    ArrayType serializableDim2Type =
        factory.getArrayType(factory.getClassType("java.io.Serializable"), 2);
    ArrayType cloneableDim1Type =
        factory.getArrayType(factory.getClassType("java.lang.Cloneable"), 1);

    JavaClassType objectType = factory.getClassType("java.lang.Object");
    JavaClassType stringType = factory.getClassType("java.lang.String");
    JavaClassType serializableType = factory.getClassType("java.io.Serializable");
    JavaClassType cloneableType = factory.getClassType("java.lang.Cloneable");
    JavaClassType arrayListType = factory.getClassType("java.util.ArrayList");

    // We don't consider types to be subtypes of itself
    Stream.of(
            stringArrayDim1Type,
            stringArrayDim3Type,
            objectArrayDim1Type,
            objectArrayDim2Type,
            objectArrayDim3Type,
            doubleArrayDim2Type,
            doubleArrayDim1Type)
        .forEach(type -> assertFalse(typeHierarchy.isSubtype(type, type)));

    Set<Pair<Type, Type>> subtypes =
        ImmutableUtils.immutableSet(
            Pair.of(serializableType, objectArrayDim1Type),
            Pair.of(serializableType, stringType),
            Pair.of(serializableDim1Type, stringArrayDim1Type),
            Pair.of(serializableDim1Type, stringArrayDim3Type),
            Pair.of(serializableDim1Type, objectArrayDim2Type),
            Pair.of(serializableDim2Type, objectArrayDim3Type),
            Pair.of(cloneableType, arrayListType),
            Pair.of(cloneableDim1Type, arrayListDim1Type),
            Pair.of(cloneableDim1Type, arrayListDim2Type),
            Pair.of(cloneableDim1Type, objectArrayDim2Type),
            Pair.of(objectArrayDim1Type, stringArrayDim1Type),
            Pair.of(objectArrayDim1Type, stringArrayDim3Type),
            Pair.of(objectArrayDim2Type, stringArrayDim3Type),
            Pair.of(objectArrayDim3Type, stringArrayDim3Type),
            Pair.of(objectType, objectArrayDim1Type),
            Pair.of(objectType, objectArrayDim2Type),
            Pair.of(objectType, doubleArrayDim1Type),
            Pair.of(serializableType, objectArrayDim1Type),
            Pair.of(serializableType, objectArrayDim2Type),
            Pair.of(serializableType, doubleArrayDim1Type),
            Pair.of(cloneableType, objectArrayDim1Type),
            Pair.of(cloneableType, objectArrayDim2Type),
            Pair.of(cloneableType, doubleArrayDim1Type),
            Pair.of(objectArrayDim1Type, doubleArrayDim2Type));

    subtypes.forEach(
        subtypePair -> {
          assertTrue(
              subtypePair.getRight() + " should be a subtype of " + subtypePair.getLeft(),
              typeHierarchy.isSubtype(subtypePair.getLeft(), subtypePair.getRight()));
          assertFalse(
              subtypePair.getLeft() + " should not be a subtype of " + subtypePair.getRight(),
              typeHierarchy.isSubtype(subtypePair.getRight(), subtypePair.getLeft()));
        });

    assertFalse(
        doubleArrayDim1Type + " should not be a subtype of " + objectArrayDim1Type,
        typeHierarchy.isSubtype(objectArrayDim1Type, doubleArrayDim1Type));
    assertFalse(
        stringArrayDim1Type + " should not be a subtype of " + objectArrayDim1Type,
        typeHierarchy.isSubtype(objectArrayDim2Type, stringArrayDim1Type));

    assertFalse(
        objectArrayDim1Type + " should not be a subtype of " + serializableDim1Type,
        typeHierarchy.isSubtype(serializableDim1Type, objectArrayDim1Type));
    assertFalse(
        objectArrayDim1Type + " should not be a subtype of " + cloneableDim1Type,
        typeHierarchy.isSubtype(cloneableDim1Type, objectArrayDim1Type));
    assertFalse(
        objectArrayDim2Type + " should not be a subtype of " + serializableDim2Type,
        typeHierarchy.isSubtype(serializableDim2Type, objectArrayDim2Type));
    assertFalse(
        arrayListDim2Type + " should not be a subtype of " + collectionArrayDim1Type,
        typeHierarchy.isSubtype(collectionArrayDim1Type, arrayListDim2Type));

    assertTrue(
        "Collection[] should be a subtype of Object[]",
        typeHierarchy.isSubtype(objectArrayDim1Type, collectionArrayDim1Type));
  }
}
