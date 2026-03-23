package com.muscleforger.api.service;

import com.muscleforger.api.dto.progress.CreateWeightLogRequest;
import com.muscleforger.api.dto.progress.WeightLogResponse;
import com.muscleforger.api.entity.User;
import com.muscleforger.api.entity.WeightLog;
import com.muscleforger.api.repository.WeightLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeightLogService {

    private final WeightLogRepository weightLogRepository;

    public List<WeightLogResponse> getLogs(User user, LocalDate from, LocalDate to) {
        List<WeightLog> logs;
        if (from != null && to != null) {
            logs = weightLogRepository.findByUserAndDateBetweenOrderByDateAsc(user, from, to);
        } else {
            logs = weightLogRepository.findByUserOrderByDateAsc(user);
        }
        return logs.stream()
                .map(l -> new WeightLogResponse(l.getId(), l.getWeight(), l.getDate()))
                .toList();
    }

    @Transactional
    public WeightLogResponse create(User user, CreateWeightLogRequest request) {
        if (weightLogRepository.existsByUserAndDate(user, request.date())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Weight log already exists for this date");
        }
        WeightLog log = new WeightLog();
        log.setUser(user);
        log.setWeight(request.weight());
        log.setDate(request.date());
        weightLogRepository.save(log);
        return new WeightLogResponse(log.getId(), log.getWeight(), log.getDate());
    }

    @Transactional
    public void delete(User user, Long logId) {
        WeightLog log = weightLogRepository.findById(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!log.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        weightLogRepository.delete(log);
    }
}
