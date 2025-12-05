package com.example.timerecorder.controller;

import com.example.timerecorder.queue.TimeQueue;
import com.example.timerecorder.repository.TimeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class TimeController {

    private final TimeRecordRepository repository;
    private final TimeQueue timeQueue;

    @GetMapping({"/", "/times"})
    public String times(Model model) {
        model.addAttribute("records", repository.findAllNoOrder());
        model.addAttribute("queueSize", timeQueue.getSize());
        model.addAttribute("dbConnected", true);
        return "times";
    }

}