package com.messageBot;

import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Message;
import org.springframework.web.bind.annotation.*;

@RestController
public class ForwardMessageController {

    @GetMapping(value = "/forward", consumes = "application/xml", produces = "application/xml")
    @ResponseBody
    public String handleForwardRequest(
            @RequestParam("From") String from,
            @RequestParam("PersonReply") String personReply,
            @RequestParam("AIReply") String aiReply,
            @RequestParam("To") String to) {
        return new MessagingResponse.Builder()
                .message(new Message.Builder("From: " + from + "\nPerson's Text:\n" + personReply +
                        "\nAI Reply:\n" + aiReply).to(to).build())
                .build().toXml();
    }

}
