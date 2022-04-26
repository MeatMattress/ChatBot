package com.messageBot;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Message;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@RestController
public class MessageBotApplication {

	private final ConcurrentHashMap<String, String> cachedMessages = new ConcurrentHashMap<>();

	@PostMapping(value = "/", produces = "application/xml")
	@ResponseBody
	public String handleSmsWebhook(
			@RequestParam("From") String from,
			@RequestParam("Body") String body){

		String prompt = "res:Hey, what are you up to?" +
				"req:Nothing much, I want to do something tonight." +
				"res:Do you want to hang out?" +
				"req:Yeah! I love to go bowling, or we could go eat somewhere. What do you think?" +
				"res:That sounds awesome, let's make something happen. This is gonna be fun!" +
				"req:";


//		cachedMessages.put(from, cachedMessages.getOrDefault(from, prompt + body) + prompt + body + "\n");
//		appendToCache(from, prompt, body);

		StringBuilder reply = new StringBuilder();


		OpenAiService service = new OpenAiService("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");
		CompletionRequest completionRequest = CompletionRequest.builder()
				.prompt(cachedMessages.getOrDefault(from, prompt))
				.echo(false)
				.maxTokens(300)
				.temperature(0.75)
				.n(1)
				.bestOf(3)
				.stop(Arrays.asList("req:", "res:"))
				.build();
		service.createCompletion("davinci", completionRequest).getChoices().stream()
				.map(CompletionChoice::getText)
				.forEach(c -> {
					reply.append(c);
					appendToCache(from, prompt, body, reply.toString());
				});

		System.out.println("Text from: " + from + ". prompt: " + cachedMessages.get(from) + "\n" + "Response: " + reply.toString());

		return new MessagingResponse.Builder()
				.message(new Message.Builder(reply.toString()).build())
				.build().toXml();
	}

	public void appendToCache(String from, String prompt, String body, String reply) {
		if (cachedMessages.containsKey(from)) {
			String cachedMessage = cachedMessages.get(from);
			if (cachedMessage.length() > 1600) {
				String[] arr = cachedMessage.split("req:|res:");
				String[] newArr = Arrays.copyOfRange(arr, arr.length / 2, arr.length);
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < newArr.length; i++) {
					if (i % 2 == 0) {
						sb.append("res:").append(newArr[i]);
					}
					else {
						sb.append("req:").append(newArr[i]);
					}
				}
				cachedMessage = sb.toString();
			}
			cachedMessages.put(from, cachedMessage + body + "\n" + reply + "\n");
		}
		else {
			cachedMessages.put(from, prompt + body + "\n" + reply + "\n");
		}
	}

	public static void main(String[] args) {

		SpringApplication.run(MessageBotApplication.class, args);
	}

}
