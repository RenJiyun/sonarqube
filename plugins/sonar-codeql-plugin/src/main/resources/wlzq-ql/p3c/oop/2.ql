import java
import semmle.code.java.security.HardcodedCredentialsComparison

from EqualsCall ec
where ec.getFile().isJavaSourceFile()
select ec, ec.getAChildExpr(), ec.getLocation(), ec.getAnArgument()
