import java

from LongLiteral ll
where ll.getLiteral().matches("%l")
select ll, ll.getLocation()
    