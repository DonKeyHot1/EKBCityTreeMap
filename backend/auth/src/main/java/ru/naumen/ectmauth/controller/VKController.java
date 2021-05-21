package ru.naumen.ectmauth.controller;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.naumen.ectmauth.config.SocialNetworkConf;
import ru.naumen.ectmauth.entity.Provider;
import ru.naumen.ectmauth.entity.Token;
import ru.naumen.ectmauth.entity.User;
import ru.naumen.ectmauth.service.JWTService;
import ru.naumen.ectmauth.service.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/auth/vk")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class VKController {

    private final UserServiceImpl userService;
    private final JWTService jwtService;
    private final SocialNetworkConf conf;

    private final String clientSecret = System.getenv("VK_APP_SECRET_KEY");
    private final int clientId = Integer.parseInt(System.getenv("VK_APP_ID"));

    private final String host = System.getenv("HOST");
    private final Integer port = Integer.valueOf(System.getenv("PORT"));


    TransportClient transportClient = HttpTransportClient.getInstance();
    VkApiClient vk = new VkApiClient(transportClient);


    @Operation(summary = "Авторизоваться через Вконтакте")
    @GetMapping("/authorize")
    @ResponseBody
    public void authorize(@RequestBody(required = false) Map<String, String> json, HttpServletResponse response) throws IOException {
        System.out.println("мы в authorize");
        String redirAuth = getOAuthUrl();
        System.out.println(redirAuth);
        response.sendRedirect(redirAuth);

    }

    @Hidden
    @GetMapping("/callback")
    public void callback(@RequestParam("code") String code, HttpServletResponse response) throws ServletException, ClientException, ApiException, IOException {
        System.out.println("мы в callback");
        try {

            UserAuthResponse authResponse = vk.oAuth().userAuthorizationCodeFlow(clientId, clientSecret, getRedirectUri(), code).execute();
            User user = userService.findByVk_id(String.valueOf(authResponse.getUserId()));
            if (user == null) {
                user = createNewUser(authResponse.getUserId(), authResponse.getAccessToken());
            }
            System.out.println(1 + " callback_VK " + user.getFirstName() + " " + user.getLastName() + " " + user.getVk_id());
            Map<String, String> tokens = jwtService.createNewTokensWithSocialNetwork(user.getUser_id(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getProvider(), authResponse.getAccessToken());
            Cookie cookie_access_token = new Cookie("access_token", tokens.get("access_token"));
            cookie_access_token.setHttpOnly(true);
            response.addCookie(cookie_access_token);
            Cookie cookie_refresh_token = new Cookie("refresh_token", tokens.get("refresh_token"));
            cookie_refresh_token.setHttpOnly(true);
            response.addCookie(cookie_refresh_token);
            Cookie cookie_access_token_VK = new Cookie("access_token_VK", authResponse.getAccessToken());
            cookie_access_token_VK.setHttpOnly(true);
            response.addCookie(cookie_access_token_VK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().println("error");
            response.setContentType("text/html;charset=utf-8");
            e.printStackTrace();
        }

    }

    private String getOAuthUrl() {

        return "https://oauth.vk.com/authorize?client_id=" + clientId + "&display=page&redirect_uri=" + getRedirectUri() + "&scope=groups&response_type=code";
    }

    private String getRedirectUri() {
        return String.format("http://%s:%d", host, port) + "/auth/vk/callback";
    }


    private User createNewUser(Integer user_vk_id, String token) {

        User user = new User();
        try {
            UserActor actor = new UserActor(user_vk_id, token);
            List<GetResponse> getUsersResponse = null;

            getUsersResponse = vk.users().get(actor).userIds(String.valueOf(user_vk_id)).execute();

            com.vk.api.sdk.objects.users.responses.GetResponse userR = getUsersResponse.get(0);


            user.setFirstName(userR.getFirstName());
            user.setLastName(userR.getLastName());
            user.setVk_id(String.valueOf(userR.getId()));
            user.setProvider(Provider.VK);

            Token t = new Token();
            t.setAccess_token_VK(token);
            Set<Token> tokenSet = new HashSet<>();
            tokenSet.add(t);
            user.setTokens(tokenSet);
            userService.save(user);

        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return user;
    }
}
