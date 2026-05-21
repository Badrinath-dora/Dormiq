import { serve } from "https://deno.land/std@0.131.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.39.8";

console.log("Dormiq Push Notification Function initialized.");

// ── JWT + OAuth2 using native Web Crypto (no google-auth-library needed) ──────
function base64UrlEncode(str: string): string {
  return btoa(str).replace(/\+/g, "-").replace(/\//g, "_").replace(/=/g, "");
}

function base64UrlEncodeBytes(bytes: Uint8Array): string {
  return btoa(String.fromCharCode(...bytes))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=/g, "");
}

async function getFirebaseAccessToken(
  clientEmail: string,
  privateKeyPem: string
): Promise<string> {
  // Strip PEM headers and decode base64
  const pemBody = privateKeyPem
    .replace(/-----BEGIN PRIVATE KEY-----/g, "")
    .replace(/-----END PRIVATE KEY-----/g, "")
    .replace(/\s+/g, "");

  const binaryKey = Uint8Array.from(atob(pemBody), (c) => c.charCodeAt(0));

  const cryptoKey = await crypto.subtle.importKey(
    "pkcs8",
    binaryKey,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const now = Math.floor(Date.now() / 1000);
  const header = base64UrlEncode(JSON.stringify({ alg: "RS256", typ: "JWT" }));
  const payload = base64UrlEncode(
    JSON.stringify({
      iss: clientEmail,
      scope: "https://www.googleapis.com/auth/firebase.messaging",
      aud: "https://oauth2.googleapis.com/token",
      iat: now,
      exp: now + 3600,
    })
  );

  const signingInput = new TextEncoder().encode(`${header}.${payload}`);
  const signatureBytes = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    cryptoKey,
    signingInput
  );

  const jwt = `${header}.${payload}.${base64UrlEncodeBytes(
    new Uint8Array(signatureBytes)
  )}`;

  const tokenRes = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=${jwt}`,
  });

  const tokenData = await tokenRes.json();
  if (!tokenData.access_token) {
    throw new Error(`OAuth2 token error: ${JSON.stringify(tokenData)}`);
  }
  return tokenData.access_token;
}
// ─────────────────────────────────────────────────────────────────────────────

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", {
      headers: {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Methods": "POST",
        "Access-Control-Allow-Headers":
          "authorization, x-client-info, apikey, content-type",
      },
    });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

    const projectId   = Deno.env.get("FIREBASE_PROJECT_ID");
    const clientEmail = Deno.env.get("FIREBASE_CLIENT_EMAIL");
    const privateKey  = Deno.env.get("FIREBASE_PRIVATE_KEY");

    console.log(
      `Secrets check → project_id: ${projectId ? "✅" : "❌"}, client_email: ${clientEmail ? "✅" : "❌"}, private_key: ${privateKey ? "✅" : "❌"}`
    );

    if (!projectId || !clientEmail || !privateKey) {
      return new Response(
        JSON.stringify({ error: "Missing Firebase secrets." }),
        { status: 500, headers: { "Content-Type": "application/json" } }
      );
    }

    // Normalize \n escape sequences to real newlines
    const resolvedPrivateKey = privateKey.replace(/\\n/g, "\n");

    const { event_type, hostel_id, tenant_id, title, body } = await req.json();
    console.log(`Processing Event: "${event_type}" for Hostel: "${hostel_id}"`);

    const supabase = createClient(supabaseUrl, supabaseServiceKey);
    let targetUserIds: string[] = [];

    if (event_type === "notice") {
      const { data: tenants, error } = await supabase
        .from("tenants")
        .select("user_id")
        .eq("hostel_id", hostel_id)
        .eq("status", "active");
      if (error) throw error;
      targetUserIds = tenants.map((t: any) => t.user_id).filter(Boolean);
    } else if (event_type === "issue_created") {
      const { data: hostel, error } = await supabase
        .from("hostels")
        .select("owner_id")
        .eq("id", hostel_id)
        .single();
      if (error) throw error;
      if (hostel?.owner_id) targetUserIds = [hostel.owner_id];
    } else if (event_type === "issue_updated") {
      const { data: tenant, error } = await supabase
        .from("tenants")
        .select("user_id")
        .eq("id", tenant_id)
        .single();
      if (error) throw error;
      if (tenant?.user_id) targetUserIds = [tenant.user_id];
    } else if (event_type === "payment_created") {
      const { data: tenant, error } = await supabase
        .from("tenants")
        .select("user_id")
        .eq("id", tenant_id)
        .single();
      if (error) throw error;
      if (tenant?.user_id) targetUserIds = [tenant.user_id];
    } else if (event_type === "payment_paid") {
      const { data: hostel, error } = await supabase
        .from("hostels")
        .select("owner_id")
        .eq("id", hostel_id)
        .single();
      if (error) throw error;
      if (hostel?.owner_id) targetUserIds = [hostel.owner_id];
    }

    if (targetUserIds.length === 0) {
      console.log("No recipient user IDs resolved. Skipping push notification.");
      return new Response(
        JSON.stringify({ success: true, message: "No target users." }),
        { headers: { "Content-Type": "application/json" } }
      );
    }

    const { data: tokens, error: tokenError } = await supabase
      .from("fcm_tokens")
      .select("token")
      .in("user_id", targetUserIds);
    if (tokenError) throw tokenError;

    const registrationTokens = tokens
      .map((t: any) => t.token)
      .filter(Boolean);

    if (registrationTokens.length === 0) {
      console.log("No FCM tokens found for the resolved users. Skipping push notification.");
      return new Response(
        JSON.stringify({ success: true, message: "No active FCM tokens found." }),
        { headers: { "Content-Type": "application/json" } }
      );
    }

    console.log("Acquiring Firebase OAuth2 token via Web Crypto...");
    const accessToken = await getFirebaseAccessToken(clientEmail, resolvedPrivateKey);
    console.log("✅ OAuth2 token acquired successfully.");

    const resolvedCategory = event_type.includes("payment")
      ? "payment"
      : event_type.includes("issue")
      ? "issue"
      : "notice";

    console.log(`Broadcasting to ${registrationTokens.length} device(s)...`);

    const sendPromises = registrationTokens.map(async (deviceToken: string) => {
      const fcmPayload = {
        message: {
          token: deviceToken,
          notification: { title, body },
          data: { event_type: resolvedCategory, title, body },
          android: {
            priority: "high",
            notification: { channel_id: `channel_${resolvedCategory}` },
          },
        },
      };

      const res = await fetch(
        `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify(fcmPayload),
        }
      );

      if (!res.ok) {
        const errText = await res.text();
        console.error(`FCM error for token ${deviceToken.substring(0, 15)}...: ${errText}`);
      } else {
        console.log(`✅ Notification sent to token ${deviceToken.substring(0, 15)}...`);
      }
    });

    await Promise.all(sendPromises);

    return new Response(
      JSON.stringify({ success: true, deliveredCount: registrationTokens.length }),
      {
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*",
        },
      }
    );
  } catch (error: any) {
    console.error("Function execution error:", error);
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*",
      },
    });
  }
});
