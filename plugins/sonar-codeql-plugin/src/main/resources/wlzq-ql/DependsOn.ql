/**
 * depends on
 */
import java

Method dependsOn(Method c) {
  exists(Call call, Callable dep |
    call.getCaller() = c and
    call.getCallee().fromSource() and
    call.getCallee() = dep and
    result = dep
  )
  or
  exists(Call call, Method am, Method im |
    call.getCallee().fromSource() and
    call.getCaller() = c and
    call.getCallee() = am and
    am.isAbstract() and
    not im.isAbstract() and
    im.overrides(am) and
    result = im
  )
}

from Callable entry, string entryName, Callable dep
where
  entry.getQualifiedName() = entryName and
  entryName = "com.wlzq.activity.task.service.app.TaskService.dotask" and
  dep = dependsOn*(entry)
select dep, dep.getQualifiedName(), entryName
order by dep