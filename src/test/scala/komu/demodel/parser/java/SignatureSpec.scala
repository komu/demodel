package komu.demodel.parser.java

import org.specs._
import org.objectweb.asm.Type

class SignatureSpec extends SpecificationWithJUnit {

    "type parameters should be parsed" in {
        assertTypes("Ljava/util/List<Ljava/lang/String;>;", 
                    "java.util.List", "java.lang.String");
        assertTypes("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;",
                    "java.util.Map", "java.lang.String", "java.lang.Integer");
    }
    
    "weird type parameters" in {
        assertTypes("Ljava/util/Map<TKey;TValue;>$Entry;",
                    "java.util.Map$Entry");
        assertTypes("LMyType<TFoo;TBar;TBaz;>.MyInnerType.MyInnerType2;",
                    "MyType.MyInnerType.MyInnerType2");
    }
    
    "nested signatures" in {
        assertTypes("Ljava/util/Map<Ljava/lang/String;Ljava/util/Map<Ljava/lang/Integer;Ljava/lang/Float;>;>;",
                "java.util.Map", "java.lang.String", "java.util.Map", "java.lang.Integer", "java.lang.Float")
    }
    
    "method signature" in {
        assertMethodTypes("(Ljava/util/List<Lfoo/Bar;>;)Lfoo/Baz;",
                          "java.util.List", "foo.Bar", "foo.Baz")
    }

    "method signature without parameters" in {
        assertMethodTypes("()Lfoo/Baz;",
                          "foo.Baz")
    }
    
    "generic method signature" in {
        assertMethodTypes("<T:Ljava/lang/Object;>()Lfoo/bar/Baz$FieldSet<TT;>;",
                "java.lang.Object", "foo.bar.Baz$FieldSet")
    }

    "generic param" in {
        assertMethodTypes("(Ljava/lang/Class<*>;)V",
                "java.lang.Class")
    }
    
    "multiple generics params" in {
        assertMethodTypes("<K:Ljava/lang/Object;V:Ljava/lang/Object;>()Ljava/util/Map<TK;TV;>;",
            "java.lang.Object", "java.lang.Object", "java.util.Map")
    }
    
    "covariant signature" in {
      assertTypes("Ljava/lang/Class<+Lfoo/bar/Baz;>;",
          "java.lang.Class", "foo.bar.Baz")
    }
    
    "weird type" in {
      assertMethodTypes("<T::Lfoo/bar/Baz<TT;>;>(Ljava/lang/Class<TT;>;)Ljava/util/List<Lfoo/Quux<TT;>;>;)",
          "foo.bar.Baz", "java.lang.Class", "java.util.List", "foo.Quux")
    }
    
    private def assertTypes(signature: String, classNames: String*) =
        assertReturnTypes(classNames, SignatureUtils.getTypesFromGenericSignature(signature))

    private def assertMethodTypes(signature: String, classNames: String*) =
        assertReturnTypes(classNames, SignatureUtils.getTypesFromGenericMethodSignature(signature))

    private def assertReturnTypes(expectedTypes: Iterable[String], types: Iterable[Type]) {
        for ((expected,parsed) <- expectedTypes.zip(types))
            expected must_== parsed.getClassName
    }
}
