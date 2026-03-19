package com.muscleforger.api.controller;

import com.muscleforger.api.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping
    public Object getVideos(@RequestParam String exercise) {
        return videoService.searchVideos(exercise);
    }
}
