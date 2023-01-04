package org.ptit.okrs.core.repository.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSchedule {

   private String userId;

    private String title;

    private Integer endDate;
}
