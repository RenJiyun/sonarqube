import java

from Call c
where
  c.getCallee().getDeclaringType().getQualifiedName() = "java.util.concurrent.Executors" and
  c.getFile().isJavaSourceFile()
select c, c.getCallee(), c.getLocation()
