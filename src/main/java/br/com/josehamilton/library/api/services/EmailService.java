package br.com.josehamilton.library.api.services;

import java.util.List;

public interface EmailService {

    void sendMails(String message, List<String> mailList);

}
