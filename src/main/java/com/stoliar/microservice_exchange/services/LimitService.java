package com.stoliar.microservice_exchange.services;

import com.stoliar.microservice_exchange.entities.Limit;
import com.stoliar.microservice_exchange.repositories.LimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LimitService {

    @Autowired
    public LimitService(LimitRepository limitRepository) {
        this.limitRepository = limitRepository;
    }

    private LimitRepository limitRepository;

    public Limit setLimit(Limit limit) {
        // Устанавливаем лимит для указанного счета и категории расходов
        return limitRepository.save(limit);
    }
}
