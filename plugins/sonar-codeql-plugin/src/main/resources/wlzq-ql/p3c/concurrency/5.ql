import java

from Field f
where
  f.getFile().isJavaSourceFile() and
  f.getType() instanceof RefType and
  f.getType().(RefType).getQualifiedName() = "java.text.SimpleDateFormat"
select f, f.getType(), f.getLocation()
