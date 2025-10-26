package com.bbangle.bbangle.board.customer.service.component;

import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

@Component
public class TaskComponent {

    public PeriodicTrigger setRunInterval(Integer seconds) {
        return new PeriodicTrigger(seconds);
    }
}
