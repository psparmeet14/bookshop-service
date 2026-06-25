package com.psb.bookshop.oauth.interfaces;

import com.psb.bookshop.oauth.application.OAuthService;
import com.psb.bookshop.oauth.domain.AuthCode;
import com.psb.bookshop.oauth.domain.OAuthClient;
import com.psb.bookshop.oauth.domain.OAuthScope;
import com.psb.bookshop.oauth.domain.PendingAuth;
import com.psb.bookshop.shared.security.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    private final OAuthService oAuthService;
    private final JwtUtil jwtUtil;

    public OAuthController(OAuthService oAuthService, JwtUtil jwtUtil) {
        this.oAuthService = oAuthService;
        this.jwtUtil = jwtUtil;
    }

    // ── Step 1: Show login form ───────────────────────────────────────────────

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
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <title>Sign in — Bookshop</title>
                  <style>
                    *{box-sizing:border-box;margin:0;padding:0}
                    body{font-family:system-ui,sans-serif;background:#f5f5f3;display:flex;
                         justify-content:center;align-items:center;min-height:100vh}
                    .card{background:#fff;border:1px solid #e5e5e3;border-radius:16px;
                          padding:32px;width:380px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
                    .logo{font-size:24px;margin-bottom:6px}
                    h2{font-size:18px;font-weight:600;color:#1a1a1a;margin-bottom:4px}
                    .sub{font-size:13px;color:#888;margin-bottom:24px}
                    label{font-size:12px;font-weight:500;color:#555;display:block;margin-bottom:4px}
                    input{width:100%%;padding:9px 12px;border:1px solid #ddd;border-radius:8px;
                          font-size:14px;margin-bottom:14px;outline:none}
                    input:focus{border-color:#1a1a1a}
                    .btn{width:100%%;padding:10px;background:#1a1a1a;color:#fff;border:none;
                         border-radius:8px;font-size:14px;font-weight:500;cursor:pointer}
                    .btn:hover{background:#333}
                    .hint{font-size:11px;color:#aaa;text-align:center;margin-top:12px}
                  </style>
                </head>
                <body>
                <div class="card">
                  <div class="logo">📚</div>
                  <h2>Sign in to Bookshop</h2>
                  <p class="sub"><strong>%s</strong> wants to access your account</p>
                  <form method="POST" action="/oauth/login">
                    <input type="hidden" name="client_id"    value="%s"/>
                    <input type="hidden" name="redirect_uri" value="%s"/>
                    <input type="hidden" name="scope"        value="%s"/>
                    <input type="hidden" name="state"        value="%s"/>
                    <label>Username</label>
                    <input type="text"     name="username" placeholder="alice or bob" autocomplete="username" required/>
                    <label>Password</label>
                    <input type="password" name="password" placeholder="Password" autocomplete="current-password" required/>
                    <button class="btn" type="submit">Continue</button>
                  </form>
                  <p class="hint">You'll review permissions on the next screen</p>
                </div>
                </body>
                </html>
                """.formatted(clientId, clientId, redirectUri, scope, state);

        return ResponseEntity.ok(html);
    }

    // ── Step 2: Validate login → show consent screen ─────────────────────────

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> login(@RequestParam MultiValueMap<String, String> params) {
        String clientId   = params.getFirst("client_id");
        String redirectUri = params.getFirst("redirect_uri");
        String scope      = params.getFirst("scope");
        String state      = params.getFirst("state");
        String username   = params.getFirst("username");
        String password   = params.getFirst("password");

        PendingAuth pending = oAuthService.loginAndCreatePendingAuth(
                username, password, clientId, redirectUri, scope, state);

        // Build scope rows for the consent screen
        List<String> requestedScopes = Arrays.stream(scope.split(" "))
                .filter(s -> !s.isBlank())
                .toList();

        StringBuilder scopeRows = new StringBuilder();
        for (String s : requestedScopes) {
            OAuthScope oauthScope = OAuthScope.fromValue(s).orElse(null);
            String title = oauthScope != null ? oauthScope.getTitle() : s;
            String desc  = oauthScope != null ? oauthScope.getDescription() : "";
            scopeRows.append("""
                    <label class="scope-row">
                      <div class="scope-info">
                        <span class="scope-title">%s</span>
                        <span class="scope-desc">%s</span>
                      </div>
                      <input type="checkbox" name="approved_scopes" value="%s" checked/>
                    </label>
                    """.formatted(title, desc, s));
        }

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <title>Authorize — Bookshop</title>
                  <style>
                    *{box-sizing:border-box;margin:0;padding:0}
                    body{font-family:system-ui,sans-serif;background:#f5f5f3;display:flex;
                         justify-content:center;align-items:center;min-height:100vh}
                    .card{background:#fff;border:1px solid #e5e5e3;border-radius:16px;
                          padding:32px;width:400px;box-shadow:0 1px 4px rgba(0,0,0,.06)}
                    .app-row{display:flex;align-items:center;gap:12px;margin-bottom:20px}
                    .app-icon{width:44px;height:44px;border-radius:10px;background:#f0f0ee;
                              display:flex;align-items:center;justify-content:center;font-size:22px}
                    .app-name{font-size:15px;font-weight:600;color:#1a1a1a}
                    .app-sub{font-size:12px;color:#888;margin-top:2px}
                    h2{font-size:16px;font-weight:600;color:#1a1a1a;margin-bottom:4px}
                    .user-pill{display:inline-block;background:#f0f0ee;border-radius:20px;
                               padding:3px 10px;font-size:12px;color:#555;margin-bottom:20px}
                    .section-label{font-size:11px;font-weight:500;color:#aaa;
                                   text-transform:uppercase;letter-spacing:.05em;margin-bottom:8px}
                    .scopes{border:1px solid #e5e5e3;border-radius:10px;overflow:hidden;margin-bottom:20px}
                    .scope-row{display:flex;align-items:center;justify-content:space-between;
                               padding:12px 14px;cursor:pointer;gap:12px}
                    .scope-row:not(:last-child){border-bottom:1px solid #f0f0ee}
                    .scope-row:hover{background:#fafafa}
                    .scope-info{display:flex;flex-direction:column;gap:2px}
                    .scope-title{font-size:13px;font-weight:500;color:#1a1a1a}
                    .scope-desc{font-size:12px;color:#888}
                    input[type=checkbox]{width:16px;height:16px;accent-color:#1a1a1a;
                                        flex-shrink:0;cursor:pointer}
                    .actions{display:flex;gap:10px}
                    .btn{flex:1;padding:10px;border-radius:8px;font-size:14px;
                         font-weight:500;cursor:pointer;border:none}
                    .btn-allow{background:#1a1a1a;color:#fff}
                    .btn-allow:hover{background:#333}
                    .btn-deny{background:#fff;color:#1a1a1a;border:1px solid #ddd}
                    .btn-deny:hover{background:#f5f5f3}
                    .footer{font-size:11px;color:#aaa;text-align:center;margin-top:14px;line-height:1.5}
                  </style>
                </head>
                <body>
                <div class="card">
                  <div class="app-row">
                    <div class="app-icon">🖥</div>
                    <div>
                      <div class="app-name">%s</div>
                      <div class="app-sub">wants access to your Bookshop account</div>
                    </div>
                  </div>
                  <h2>Signed in as</h2>
                  <div class="user-pill">👤 %s</div>
                  <div class="section-label">Permissions requested</div>
                  <form method="POST" action="/oauth/consent">
                    <input type="hidden" name="ticket"       value="%s"/>
                    <input type="hidden" name="redirect_uri" value="%s"/>
                    <input type="hidden" name="state"        value="%s"/>
                    <div class="scopes">%s</div>
                    <div class="actions">
                      <button type="submit" name="decision" value="allow" class="btn btn-allow">Allow</button>
                      <button type="submit" name="decision" value="deny"  class="btn btn-deny">Deny</button>
                    </div>
                  </form>
                  <p class="footer">
                    You can uncheck permissions you don't want to grant.<br>
                    You can revoke access at any time.
                  </p>
                </div>
                </body>
                </html>
                """.formatted(clientId, username, pending.ticket(),
                              redirectUri, state, scopeRows);

        return ResponseEntity.ok(html);
    }

    // ── Step 3: User approves/denies → issue auth code ────────────────────────

    @PostMapping(value = "/consent", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> consent(@RequestParam MultiValueMap<String, String> params) {
        String ticket      = params.getFirst("ticket");
        String redirectUri = params.getFirst("redirect_uri");
        String state       = params.getFirst("state");
        String decision    = params.getFirst("decision");

        if ("deny".equals(decision)) {
            String location = redirectUri + "?error=access_denied&state=" + state;
            return ResponseEntity.status(302).location(URI.create(location)).build();
        }

        List<String> approvedList = params.get("approved_scopes");
        Set<String> approvedScopes = approvedList != null
                ? approvedList.stream().collect(Collectors.toSet())
                : Set.of();

        AuthCode code = oAuthService.approveConsent(ticket, approvedScopes);

        String location = redirectUri + "?code=" + code.code() + "&state=" + state;
        return ResponseEntity.status(302).location(URI.create(location)).build();
    }

    // ── Token exchange (unchanged) ────────────────────────────────────────────

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> token(@RequestParam MultiValueMap<String, String> params) {
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
