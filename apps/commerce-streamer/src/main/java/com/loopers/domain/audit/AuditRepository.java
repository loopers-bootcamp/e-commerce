package com.loopers.domain.audit;

public interface AuditRepository {

    boolean saveIfAbsent(EventLog eventLog);

}
