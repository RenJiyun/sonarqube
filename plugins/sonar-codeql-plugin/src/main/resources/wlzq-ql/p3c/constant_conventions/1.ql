import java

from StringLiteral sl, string enclosingMethodName, Stmt enclosingStmt, Callable enclosingCallable
where
  sl.getValue() != "" and
  sl.getFile().isJavaSourceFile() and
  sl.getEnclosingStmt() = enclosingStmt and
  not enclosingStmt instanceof ThrowStmt and
  enclosingStmt.getEnclosingCallable() = enclosingCallable and
  enclosingCallable.getName() = enclosingMethodName and
  enclosingMethodName != "toString" and
  enclosingMethodName != "<clinit>" and
  enclosingCallable instanceof Method and
  enclosingCallable.getDeclaringType().getASupertype().getQualifiedName() !=
    "com.wlzq.core.BaseService"
select sl, sl.getLocation(), sl.getEnclosingStmt().getEnclosingCallable()
