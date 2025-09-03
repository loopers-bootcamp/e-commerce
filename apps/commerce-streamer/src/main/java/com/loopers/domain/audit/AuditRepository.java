package com.loopers.domain.audit;

public interface AuditRepository {

    boolean save(EventLog eventLog);

}
