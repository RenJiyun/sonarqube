import java

from ControlFlowNode cfn 
where cfn.getFile().isJavaSourceFile()
select cfn, cfn.getEnclosingCallable().getQualifiedName() as ec, cfn.getLocation()
order by ec