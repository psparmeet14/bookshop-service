package com.psb.bookshop.oauth.interfaces;

import com.psb.bookshop.oauth.application.OAuthService;
import com.psb.bookshop.oauth.domain.AuthCode;
import com.psb.bookshop.oauth.domain.OAuthClient;
import com.psb.bookshop.shared.security.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    private final OAuthService oAuthService;
    private final JwtUtil jwtUtil;

    public OAuthController(OAuthService oAuthService, JwtUtil jwtUtil) {
        this.oAuthService = oAuthService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Step 1: Client app redirects user here.
     * Returns an HTML login + consent form.
     */
    @GetMapping(value = "/authorize", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> authorize(
            @RequestParam("response_type") String responseType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("scope") String scope,
            @RequestParam("state") String state) {

        oAuthService.validateClient(clientId, redirectUri);

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                  <title>Bookshop — Sign In</title>
                  <style>
                    body { font-family: system-ui, sans-serif; display: flex; justify-content: center;
                           padding-top: 80px; background: #f5f5f5; }
                    .card { background: white; padding: 32px; border-radius: 12px;
                            box-shadow: 0 2px 12px rgba(0,0,0,.1); width: 360px; }
                    h2 { margin: 0 0 4px; font-size: 20px; }
                    p  { color: #666; font-size: 13px; margin: 0 0 20px; }
                    label { font-size: 13px; font-weight: 600; display: block; margin-bottom: 4px; }
                    input { width: 100%%; padding: 8px 10px; border: 1px solid #ddd;
                            border-radius: 6px; font-size: 14px; box-sizing: border-box; margin-bottom: 14px; }
                    .scopes { background: #f0f4ff; border-radius: 6px; padding: 10px 14px;
                              font-size: 12px; color: #444; margin-bottom: 20px; }
                    button { width: 100%%; padding: 10px; background: #3b82f6; color: white;
                             border: none; border-radius: 6px; font-size: 15px; cursor: pointer; }
                    button:hover { background: #2563eb; }
                  </style>
                </head>
                <body>
                <div class="card">
                  <h2>📚 Bookshop</h2>
                  <p><strong>%s</strong> is requesting access to your account.</p>
                  <div class="scopes">Permissions requested: <strong>%s</strong></div>
                  <form method="POST" action="/oauth/consent">
                    <input type="hidden" name="client_id"    value="%s"/>
                    <input type="hidden" name="redirect_uri" value="%s"/>
                    <input type="hidden" name="scope"        value="%s"/>
                    <input type="hidden" name="state"        value="%s"/>
                    <label>Username</label>
                    <input type="text"     name="username" placeholder="alice or bob" required/>
                    <label>Password</label>
                    <input type="password" name="password" placeholder="Password" required/>
                    <button type="submit">Sign in &amp; Authorize</button>
                  </form>
                </div>
                </body>
                </html>
                """.formatted(clientId, scope, clientId, redirectUri, scope, state);

        return ResponseEntity.ok(html);
    }

    /**
     * Step 2: User submits credentials on the consent form.
     * Issues an auth code and redirects back to the client app.
     */
    @PostMapping("/consent")
    public ResponseEntity<Void> consent(@RequestParam MultiValueMap<String, String> params) {
        String clientId   = params.getFirst("client_id");
        String redirectUri = params.getFirst("redirect_uri");
        String scope      = params.getFirst("scope");
        String state      = params.getFirst("state");
        String username   = params.getFirst("username");
        String password   = params.getFirst("password");

        AuthCode code = oAuthService.issueCode(clientId, redirectUri, scope, username, password);

        String location = redirectUri + "?code=" + code.code() + "&state=" + state;
        return ResponseEntity.status(302).location(URI.create(location)).build();
    }

    /**
     * Step 3: Client app exchanges auth code for access token (server-side, never in browser).
     */
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> token(
            @RequestParam MultiValueMap<String, String> params) {

        String grantType    = params.getFirst("grant_type");
        String code         = params.getFirst("code");
        String clientId     = params.getFirst("client_id");
        String clientSecret = params.getFirst("client_secret");
        String redirectUri  = params.getFirst("redirect_uri");

        if (!"authorization_code".equals(grantType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "unsupported_grant_type"));
        }

        String token = oAuthService.exchangeCodeForToken(code, clientId, clientSecret, redirectUri);

        return ResponseEntity.ok(Map.of(
                "access_token", token,
                "token_type", "Bearer",
                "expires_in", jwtUtil.expiryMs() / 1000,
                "scope", "books:read profile:read"
        ));
    }
}
