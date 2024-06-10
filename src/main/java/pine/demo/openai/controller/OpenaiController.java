package pine.demo.openai.controller;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pine.demo.openai.service.OpenaiService;


@Validated
@RequiredArgsConstructor
@RequestMapping("openai")
@RestController("OpenaiController")
public class OpenaiController {
    private final OpenaiService openaiService;

    @Operation(description = "chat SSE")
    @GetMapping("/chat/sse")
    public SseEmitter translateSse(@RequestParam() String text) {
        SseEmitter emitter = new SseEmitter(0L);
        openaiService.chatSse(text, emitter);

        return emitter;
    }

}