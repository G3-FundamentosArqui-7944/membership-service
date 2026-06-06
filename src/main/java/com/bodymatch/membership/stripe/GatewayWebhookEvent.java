package com.bodymatch.membership.stripe;

import java.util.Map;

public record GatewayWebhookEvent(String type, Map<String, String> data) {
}
