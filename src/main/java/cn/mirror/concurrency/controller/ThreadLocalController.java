package cn.mirror.concurrency.controller;

import cn.mirror.concurrency.example.threadLocal.RequestHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/threadLocal")
public class ThreadLocalController {

    @GetMapping("test")
    public Long test() {
        return RequestHolder.get();
    }
}
