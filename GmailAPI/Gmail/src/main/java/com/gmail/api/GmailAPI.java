package com.gmail.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.ModifyMessageRequest;

import org.json.JSONObject;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.StringUtils;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import javax.mail.Folder;

public class GmailAPI {


	/*
	1.Get code :
https://accounts.google.com/o/oauth2/v2/auth?
 scope=https://mail.google.com&
 access_type=offline&
 redirect_uri=http://localhost&
 response_type=code&
 client_id=[Client ID]

2. Get access_token and refresh_token
 curl \
--request POST \
--data "code=[Authentcation code from authorization link]&client_id=[Application Client Id]&client_secret=[Application Client Secret]&redirect_uri=http://localhost&grant_type=authorization_code" \
https://accounts.google.com/o/oauth2/token

3.Get new access_token using refresh_token
curl \
--request POST \
--data "client_id=[your_client_id]&client_secret=[your_client_secret]&refresh_token=[refresh_token]&grant_type=refresh_token" \
https://accounts.google.com/o/oauth2/token

*/
	private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String user = "me";
	static Gmail service = null;
	private static File filePath = new File(System.getProperty("user.dir") + "/credentials.json");

	public static void main(String[] args) throws IOException, GeneralSecurityException {

		getGmailService();
//		getListUnreadMails(service, "me", "newer:19/01/2023");
		getListUnreadMails(service, "me", "is:unread");

	}

	public static void getListAllMails() throws IOException {   //stampa la lista di tutte le mail
		Gmail.Users.Messages.List request = service.users().messages().list(user);
		ListMessagesResponse messagesResponse = request.execute();
		request.setPageToken(messagesResponse.getNextPageToken());
		List<Message> messages = messagesResponse.getMessages();

		for (Message m: messages) {
			   String id = m.getId();

			   Message message = service.users().messages().get(user, id).execute();

			   String emailBody = StringUtils.newStringUtf8(Base64.decodeBase64(message.getPayload().getParts().get(0).getBody().getData()));

			   List<MessagePartHeader> headerParts = message.getPayload().getHeaders();
			   for (MessagePartHeader mh: headerParts) {
			    switch (mh.getName()){
			     case "From":System.out.println(mh.getName() + " : " + mh.getValue()); break;
			     case "To":System.out.println(mh.getName() + " : " + mh.getValue()); break;
			     case "Date":System.out.println(mh.getName() + " : " + mh.getValue()); break;
			     case "Subject":System.out.println(mh.getName() + " : " + mh.getValue()); break;
			    }
			   }
			   System.out.println("Email body : " + emailBody);
		}
	}

	public static Gmail getGmailService() throws IOException, GeneralSecurityException {

		InputStream in = new FileInputStream(filePath); // Read credentials.json
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Credential builder

		Credential authorize = new GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
				.setJsonFactory(JSON_FACTORY)
				.setClientSecrets(clientSecrets.getDetails().getClientId().toString(),
						clientSecrets.getDetails().getClientSecret().toString())
				.build().setAccessToken(getAccessToken()).setRefreshToken(
						"1//09yzKAKUV-iJbCgYIARAAGAkSNwF-L9IrQv2x-c2pyEJ1thcphRN-nrMXh2UO-Q1V79MivHPTya69QZ93gaNEIOw-WYAZSp9dP84");//Replace this

		// Create Gmail service
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize)
				.setApplicationName(GmailAPI.APPLICATION_NAME).build();

		return service;
	}

	static void markAsRead(final Gmail service, final String userId, final String messageId) throws IOException {
        ModifyMessageRequest mods =
                new ModifyMessageRequest()
                .setAddLabelIds(Collections.singletonList("INBOX"))
                .setRemoveLabelIds(Collections.singletonList("UNREAD"));
        Message message = null;

        if(Objects.nonNull(messageId)) {
          message = service.users().messages().modify(userId, messageId, mods).execute();
          System.out.println("Message id marked as read: " + message.getId());
        }
      }

	private static String getAccessToken() {

		try {
			Map<String, Object> params = new LinkedHashMap<>();
			params.put("grant_type", "refresh_token");
			params.put("client_id", "89887470414-h7dbop1l29mjsafs0jgck7540gt6mm4u.apps.googleusercontent.com"); //Replace this
			params.put("client_secret", "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"); //Replace this
			params.put("refresh_token",
					"1//09yzKAKUV-iJbCgYIARAAGAkSNwF-L9IrQv2x-c2pyEJ1thcphRN-nrMXh2UO-Q1V79MivHPTya69QZ93gaNEIOw-WYAZSp9dP84"); //Replace this

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0) {
					postData.append('&');
				}
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");

			URL url = new URL("https://accounts.google.com/o/oauth2/token");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestMethod("POST");
			con.getOutputStream().write(postDataBytes);

			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuffer buffer = new StringBuffer();
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				buffer.append(line);
			}

			JSONObject json = new JSONObject(buffer.toString());
			String accessToken = json.getString("access_token");
			return accessToken;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private static void getListUnreadMails(Gmail service, String userId, String query) throws IOException {
		Gmail.Users.Messages.List request = service.users().messages().list(user);
		request.setQ(query);

		//request.setMaxResults(1L);


			try
			{
				ListMessagesResponse messagesResponse = request.execute();
				request.setPageToken(messagesResponse.getNextPageToken());
				List<Message> messages = messagesResponse.getMessages();
				Collections.reverse(messages);
				List<Message> mm = new ArrayList();

				if (messages.size()< 50) {
				for ( int i = 0; i < messages.size() ; i++)
					mm.add(messages.get(i));
				}else {
					for ( int i = 0; i < 50 ; i++)
						mm.add(messages.get(i));
				}

				for (Message m: mm) {
					String id = m.getId();

					Message message = service.users().messages().get(user, id).execute();


					String emailBody = StringUtils.newStringUtf8(Base64.decodeBase64(message.getPayload().getParts().get(0).getBody().getData()));

							List<MessagePartHeader> headerParts = message.getPayload().getHeaders();
							for (MessagePartHeader mh: headerParts) {
								switch (mh.getName()){
									case "From":System.out.println(mh.getName() + " : " + mh.getValue()); break;
									case "To":System.out.println(mh.getName() + " : " + mh.getValue()); break;
									case "Date":System.out.println(mh.getName() + " : " + mh.getValue()); break;
									case "Subject":System.out.println(mh.getName() + " : " + mh.getValue()); break;
								}
							}
							System.out.println("Email body : " + emailBody);



				}




			}
			catch (Exception e)
			{
				System.out.println("An error occurred: " + e.getMessage());
			}
	}

}
