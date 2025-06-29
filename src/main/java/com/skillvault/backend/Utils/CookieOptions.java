package com.skillvault.backend.Utils;

import org.springframework.stereotype.Component;

@Component
public class CookieOptions {
    public int maxAge = 7 * 24 * 60 * 60;
    public boolean httpOnly = true;
    public boolean secure = true;
    public String sameSite = "None";
    public String path = "/";
}
