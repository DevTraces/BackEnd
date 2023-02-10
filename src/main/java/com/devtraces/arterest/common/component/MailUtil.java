package com.devtraces.arterest.common.component;

import com.devtraces.arterest.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MailUtil {

	private final JavaMailSender javaMailSender;

	public void sendMail(String mail, String subject, String text) {
		try {
			MimeMessagePreparator message = mimeMessage -> {
				MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,
					"UTF-8");
				mimeMessageHelper.setTo(mail);
				mimeMessageHelper.setSubject(subject);
				mimeMessageHelper.setText(text, true);
			};

			javaMailSender.send(message);
		} catch (MailException e) {
			log.error(e.getMessage());
			throw BaseException.FAILED_SEND_EMAIL;
		}
	}
}
