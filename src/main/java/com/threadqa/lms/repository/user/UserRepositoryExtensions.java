package com.threadqa.lms.repository.user;

import java.time.ZonedDateTime;

public interface UserRepositoryExtensions {
    
    Long countActiveUsersSince(ZonedDateTime since);
    
    String findFullNameById(Long userId);
}
