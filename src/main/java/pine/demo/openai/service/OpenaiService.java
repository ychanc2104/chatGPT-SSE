package pine.demo.openai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OpenaiService {
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    public OpenAiChatClient getChatClient() {
        OpenAiApi openAiApi = new OpenAiApi(apiKey);

        return new OpenAiChatClient(openAiApi,
                OpenAiChatOptions.builder()
                        .withModel(model)
                        .build());
    }

    public Flux<ChatResponse> chatFlux(String sentence) {
        Prompt prompt = new Prompt(this.getChatMessages(sentence));
        Flux<ChatResponse> stream = this.getChatClient()
                .stream(prompt);

        return stream;
    }

    public void chatSse(String sentence, SseEmitter emitter) {
        Flux<ChatResponse> stream = this.chatFlux(sentence);

        stream.doOnNext(it -> {
                    try {
                        if (it.getResults().get(0).getOutput().getContent() != null) {
                            String data = it.getResults().get(0).getOutput().getContent();
                            emitter.send(SseEmitter.event().name("text").data(data));
                        }
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                })
                .doOnError(emitter::completeWithError)
                .doOnComplete(emitter::complete)
                .subscribe();

    }

    public List<Message> getChatMessages(String sentence) {
        return List.of(
                new SystemMessage("you are a helpful assistant"),
                new UserMessage(sentence)
        );
    }

}
