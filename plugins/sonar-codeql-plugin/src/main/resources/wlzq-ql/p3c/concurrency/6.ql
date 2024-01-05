import java

from Call c
where
  c.getCallee().getDeclaringType().getQualifiedName() = "java.util.Timer" and
  c.getCallee().getName() = "schedule" and
  c.getFile().isJavaSourceFile()
select c, c.getCallee(), c.getLocation()
