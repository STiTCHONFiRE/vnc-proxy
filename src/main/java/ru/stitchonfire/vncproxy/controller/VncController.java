package ru.stitchonfire.vncproxy.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.stitchonfire.vncproxy.dto.VncDto;
import ru.stitchonfire.vncproxy.service.VncService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/vnc")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VncController {
    VncService vncService;

    @GetMapping()
    public Mono<List<VncDto>> getVncData() {
        return vncService.getVncData();
    }
}
