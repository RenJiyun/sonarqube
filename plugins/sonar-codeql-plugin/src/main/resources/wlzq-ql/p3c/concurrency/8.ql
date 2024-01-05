import java

from Field f
where
  f.getFile().isJavaSourceFile() and
  f.getType() instanceof RefType and
  f.getType().(RefType).getQualifiedName() = "java.util.Random"
// 还需要排除 Math.random() 的情况
select f, f.getType(), f.getLocation()
