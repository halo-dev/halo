package run.halo.app.core.endpoint.publicapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ReactiveExtensionStoreClient;

/**
 * Compatibility endpoints for member card portal.
 *
 * <p>PluginMemberCard 1.0.4 queries orders by spec.userName through indexed query, but this field
 * is not indexed, resulting in 500. We provide a compatible public orders endpoint by scanning and
 * filtering in memory and a tab-based member card page.
 */
@Slf4j
@Component
class MemberCardPortalCompatEndpoint {

    private static final String MEMBER_CARD_STORE_PREFIX =
        "/registry/membercard.halo.run/membercards";
    private static final String MEMBER_CARD_STORE_PATH_PREFIX =
        MEMBER_CARD_STORE_PREFIX + "/";

    private static final String MEMBER_CARD_ORDER_STORE_PREFIX =
        "/registry/membercard.halo.run/membercardorders";
    private static final String MEMBER_CARD_ORDER_STORE_PATH_PREFIX =
        MEMBER_CARD_ORDER_STORE_PREFIX + "/";

    private static final String PAYMENT_CONFIG_STORE_NAME =
        "/registry/configmaps/plugin-member-card-payment-config";

    private static final String GATEWAY_SUCCESS = "SUCCESS";

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 200;

    private final ReactiveExtensionStoreClient extensionStoreClient;
    private final ObjectMapper objectMapper;

    MemberCardPortalCompatEndpoint(ReactiveExtensionStoreClient extensionStoreClient,
        ObjectMapper objectMapper) {
        this.extensionStoreClient = extensionStoreClient;
        this.objectMapper = objectMapper;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 110)
    RouterFunction<ServerResponse> memberCardPortalCompatRoutes() {
        return RouterFunctions.route()
            .GET("/membercard/cards", this::listPublicCards)
            .GET("/membercard/orders", this::listOwnOrders)
            .POST("/membercard/orders", this::createOrderCompat)
            .GET("/membercard/portal/member-card", this::renderMemberCardPage)
            .GET("/membercard/portal/vip-guard.js", this::renderVipGuardScript)
            .build();
    }

    private Mono<ServerResponse> listPublicCards(ServerRequest request) {
        var now = Instant.now();
        return extensionStoreClient.listByNamePrefix(MEMBER_CARD_STORE_PATH_PREFIX)
            .flatMap(this::readCardAsJson)
            .filter(card -> isCardEnabled(card) && isCardInValidRange(card, now))
            .sort(Comparator.comparingInt(this::extractCardSort)
                .thenComparingLong(this::extractCreationTimestamp))
            .collectList()
            .flatMap(cards -> {
                var result = new ListResult<>(1, 500, cards.size(), cards);
                return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(result);
            });
    }

    private Mono<ServerResponse> listOwnOrders(ServerRequest request) {
        var page = parsePositiveInt(request.queryParam("page").orElse(null), DEFAULT_PAGE);
        var size = Math.min(
            parsePositiveInt(request.queryParam("size").orElse(null), DEFAULT_SIZE),
            MAX_SIZE
        );

        return getCurrentUserName()
            .flatMap(userName -> extensionStoreClient.listByNamePrefix(
                    MEMBER_CARD_ORDER_STORE_PATH_PREFIX)
                .flatMap(this::readOrderAsJson)
                .filter(order -> userName.equals(order.path("spec").path("userName").asText(null)))
                .sort(Comparator.comparingLong(this::extractCreationTimestamp).reversed())
                .collectList()
                .flatMap(orders -> {
                    var pageItems = ListResult.subList(orders, page, size);
                    var result = new ListResult<>(page, size, orders.size(), pageItems);
                    return ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(result);
                }));
    }

    private Mono<JsonNode> readOrderAsJson(ExtensionStore extensionStore) {
        return Mono.fromCallable(() -> objectMapper.readTree(extensionStore.getData()))
            .onErrorResume(ex -> {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to parse member card order extension store {}.",
                        extensionStore.getName(), ex);
                }
                return Mono.empty();
            });
    }

    private Mono<JsonNode> readCardAsJson(ExtensionStore extensionStore) {
        return Mono.fromCallable(() -> objectMapper.readTree(extensionStore.getData()))
            .onErrorResume(ex -> {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to parse member card extension store {}.",
                        extensionStore.getName(), ex);
                }
                return Mono.empty();
            });
    }

    private int extractCardSort(JsonNode card) {
        return card.path("spec").path("sort").asInt(0);
    }

    private boolean isCardEnabled(JsonNode card) {
        var enabledNode = card.path("spec").path("enabled");
        if (enabledNode.isMissingNode() || enabledNode.isNull()) {
            return true;
        }
        return enabledNode.asBoolean(false);
    }

    private boolean isCardInValidRange(JsonNode card, Instant now) {
        var validFrom = parseInstant(card.path("spec").path("validFrom").asText(null));
        var validTo = parseInstant(card.path("spec").path("validTo").asText(null));
        var inStartRange = validFrom == null || !now.isBefore(validFrom);
        var inEndRange = validTo == null || !now.isAfter(validTo);
        return inStartRange && inEndRange;
    }

    private Instant parseInstant(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private long extractCreationTimestamp(JsonNode order) {
        var creationTimestamp = order.path("metadata").path("creationTimestamp").asText(null);
        if (!StringUtils.hasText(creationTimestamp)) {
            return 0L;
        }
        try {
            return Instant.parse(creationTimestamp).toEpochMilli();
        } catch (DateTimeParseException ex) {
            return 0L;
        }
    }

    private Mono<String> getCurrentUserName() {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .filter(this::isAuthenticatedUser)
            .map(Authentication::getName)
            .switchIfEmpty(Mono.error(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized")
            ));
    }

    private boolean isAuthenticatedUser(Authentication authentication) {
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken)
            && !"anonymousUser".equalsIgnoreCase(authentication.getName());
    }

    private int parsePositiveInt(String value, int defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            var parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private record GatewayCreateResult(String retCode, String retMsg, String payOrderId,
                                       String payUrl, String rawResponse) {
    }

    private Mono<ServerResponse> createOrderCompat(ServerRequest request) {
        return getCurrentUserName()
            .zipWith(request.bodyToMono(JsonNode.class)
                .defaultIfEmpty(objectMapper.createObjectNode()))
            .flatMap(tuple -> {
                var userName = tuple.getT1();
                var body = tuple.getT2();
                var cardName = body.path("cardName").asText(null);
                if (!StringUtils.hasText(cardName)) {
                    return Mono.error(badRequest("cardName is required."));
                }
                var requestReturnUrl = body.path("returnUrl").asText(null);
                var requestParam2 = body.path("param2").asText(null);
                return fetchCardByName(cardName)
                    .flatMap(card -> createOrderWithGateway(card, userName, requestReturnUrl,
                        requestParam2))
                    .flatMap(result -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(result));
            });
    }

    private Mono<ObjectNode> createOrderWithGateway(JsonNode card, String userName,
                                                    String requestReturnUrl, String requestParam2) {
        if (!isCardEnabled(card)) {
            return Mono.error(badRequest("Card is disabled."));
        }
        if (!isCardInValidRange(card, Instant.now())) {
            return Mono.error(badRequest("Card is out of valid time."));
        }

        var cardName = card.path("metadata").path("name").asText(null);
        if (!StringUtils.hasText(cardName)) {
            return Mono.error(badRequest("Member card not found."));
        }
        var cardTitle = card.path("spec").path("title").asText(cardName);
        var durationDays = parsePositiveInt(card.path("spec").path("durationDays").asText(null), 30);
        var amountInCents = calculateAmountInCents(card);
        var orderNo = generateOrderNo();

        return fetchPaymentConfig()
            .flatMap(config -> {
                if (!config.path("enabled").asBoolean(false)) {
                    return Mono.error(badRequest("Payment is disabled."));
                }
                var mchId = config.path("mchId").asLong(0L);
                var mchKey = config.path("mchKey").asText(null);
                var productId = config.path("productId").asInt(0);
                var createOrderUrl = config.path("createOrderUrl").asText(null);
                var notifyUrl = config.path("notifyUrl").asText(null);
                var defaultReturnUrl = config.path("defaultReturnUrl").asText(null);

                if (mchId <= 0) {
                    return Mono.error(badRequest("mchId must be set when payment is enabled."));
                }
                if (!StringUtils.hasText(mchKey)) {
                    return Mono.error(badRequest("mchKey must be set when payment is enabled."));
                }
                if (productId <= 0) {
                    return Mono.error(
                        badRequest("productId must be set to a positive integer when payment is enabled."));
                }
                if (!StringUtils.hasText(createOrderUrl)) {
                    return Mono.error(
                        badRequest("createOrderUrl must be set when payment is enabled."));
                }
                if (!StringUtils.hasText(notifyUrl)) {
                    return Mono.error(
                        badRequest("notifyUrl must be set when payment is enabled."));
                }

                var finalReturnUrl = StringUtils.hasText(requestReturnUrl)
                    ? requestReturnUrl
                    : defaultReturnUrl;
                var finalParam2 = StringUtils.hasText(requestParam2)
                    ? requestParam2
                    : userName;

                var formParams = new LinkedHashMap<String, String>();
                formParams.put("mchId", String.valueOf(mchId));
                formParams.put("productId", String.valueOf(productId));
                formParams.put("mchOrderNo", orderNo);
                formParams.put("amount", String.valueOf(amountInCents));
                formParams.put("notifyUrl", notifyUrl);
                if (StringUtils.hasText(finalReturnUrl)) {
                    formParams.put("returnUrl", finalReturnUrl);
                }
                if (StringUtils.hasText(finalParam2)) {
                    formParams.put("param2", finalParam2);
                }
                formParams.put("sign", signByMd5Upper(formParams, mchKey));

                var formData = new LinkedMultiValueMap<String, String>();
                formParams.forEach(formData::add);
                var requestWebClient = WebClient.builder().build();
                return requestWebClient.post()
                    .uri(createOrderUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(raw -> parseGatewayCreateOrderResult(raw, mchKey, orderNo))
                    .flatMap(result -> persistCompatOrder(orderNo, userName, cardName, cardTitle,
                        amountInCents, durationDays, productId, result))
                    .map(result -> {
                        var response = objectMapper.createObjectNode();
                        response.put("orderName", orderNo);
                        if (StringUtils.hasText(result.payOrderId())) {
                            response.put("payOrderId", result.payOrderId());
                        } else {
                            response.putNull("payOrderId");
                        }
                        if (StringUtils.hasText(result.payUrl())) {
                            response.put("payUrl", result.payUrl());
                        } else {
                            response.putNull("payUrl");
                        }
                        response.put("status", 1);
                        return response;
                    });
            });
    }

    private Mono<GatewayCreateResult> persistCompatOrder(String orderNo, String userName,
                                                         String cardName, String cardTitle,
                                                         int amountInCents, int durationDays,
                                                         int productId,
                                                         GatewayCreateResult createResult) {
        var order = objectMapper.createObjectNode();
        order.put("apiVersion", "membercard.halo.run/v1alpha1");
        order.put("kind", "MemberCardOrder");
        var metadata = order.putObject("metadata");
        metadata.put("name", orderNo);
        metadata.put("creationTimestamp", Instant.now().toString());
        var spec = order.putObject("spec");
        spec.put("mchOrderNo", orderNo);
        spec.put("userName", userName);
        spec.put("cardName", cardName);
        spec.put("cardTitle", cardTitle);
        spec.put("amount", amountInCents);
        spec.put("durationDays", durationDays);
        spec.put("productId", productId);
        if (StringUtils.hasText(createResult.payOrderId())) {
            spec.put("payOrderId", createResult.payOrderId());
        }
        if (StringUtils.hasText(createResult.payUrl())) {
            spec.put("payUrl", createResult.payUrl());
        }
        spec.put("status", 1);
        if (StringUtils.hasText(createResult.retCode())) {
            spec.put("retCode", createResult.retCode());
        }
        if (StringUtils.hasText(createResult.retMsg())) {
            spec.put("retMsg", createResult.retMsg());
        }
        spec.put("createdAt", Instant.now().toString());
        spec.put("rawNotify", createResult.rawResponse());
        return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(order))
            .flatMap(bytes -> extensionStoreClient.create(
                MEMBER_CARD_ORDER_STORE_PATH_PREFIX + orderNo, bytes))
            .thenReturn(createResult);
    }

    private Mono<GatewayCreateResult> parseGatewayCreateOrderResult(String rawResponse,
                                                                    String mchKey,
                                                                    String mchOrderNo) {
        return Mono.fromCallable(() -> objectMapper.readTree(rawResponse))
            .onErrorMap(ex -> badRequest("Failed to parse gateway response."))
            .flatMap(response -> {
                var retCode = response.path("retCode").asText("FAIL");
                var retMsg = response.path("retMsg").asText(null);
                var errCode = response.path("errCode").asText(null);
                var errDes = response.path("errDes").asText(null);
                if (!GATEWAY_SUCCESS.equalsIgnoreCase(retCode)) {
                    var message = firstNonBlank(errDes, retMsg, errCode,
                        "Create payment order failed.");
                    return Mono.error(badRequest(message));
                }
                if (!verifyGatewayResponseSign(response, mchKey)) {
                    // Gateway create_order response sign can be inconsistent for some channels.
                    // Keep order creation compatible while preserving full raw response for audit.
                    log.warn("Gateway create_order response signature mismatch for order {}: {}",
                        mchOrderNo, rawResponse);
                }
                var payOrderId = response.path("payOrderId").asText(null);
                var payUrl = response.path("payParams").path("payUrl").asText(null);
                return Mono.just(new GatewayCreateResult(retCode, retMsg, payOrderId, payUrl,
                    rawResponse));
            });
    }

    private Mono<JsonNode> fetchCardByName(String cardName) {
        return extensionStoreClient.fetchByName(MEMBER_CARD_STORE_PATH_PREFIX + cardName)
            .switchIfEmpty(Mono.error(badRequest("Member card not found.")))
            .flatMap(store -> Mono.fromCallable(() -> objectMapper.readTree(store.getData()))
                .onErrorMap(ex -> badRequest("Invalid member card data.")));
    }

    private Mono<JsonNode> fetchPaymentConfig() {
        return extensionStoreClient.fetchByName(PAYMENT_CONFIG_STORE_NAME)
            .switchIfEmpty(Mono.error(badRequest("Payment config not found.")))
            .flatMap(store -> Mono.fromCallable(() -> objectMapper.readTree(store.getData()))
                .onErrorMap(ex -> badRequest("Invalid payment config data.")))
            .flatMap(configMap -> {
                var paymentConfig = configMap.path("data").path("paymentConfig").asText(null);
                if (!StringUtils.hasText(paymentConfig)) {
                    return Mono.error(badRequest("Payment config not found."));
                }
                return Mono.fromCallable(() -> objectMapper.readTree(paymentConfig))
                    .onErrorMap(ex -> badRequest("Invalid payment config data."));
            });
    }

    private int calculateAmountInCents(JsonNode card) {
        BigDecimal salePrice = null;
        var salePriceNode = card.path("spec").path("salePrice");
        if (salePriceNode != null && !salePriceNode.isMissingNode() && !salePriceNode.isNull()) {
            if (salePriceNode.isNumber()) {
                salePrice = salePriceNode.decimalValue();
            } else {
                var textValue = salePriceNode.asText(null);
                if (StringUtils.hasText(textValue)) {
                    try {
                        salePrice = new BigDecimal(textValue.trim());
                    } catch (NumberFormatException ignored) {
                        salePrice = null;
                    }
                }
            }
        }
        if (salePrice == null || salePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw badRequest("Invalid salePrice of member card.");
        }
        return salePrice.multiply(BigDecimal.valueOf(100L))
            .setScale(0, RoundingMode.HALF_UP)
            .intValue();
    }

    private String generateOrderNo() {
        var suffix = Math.abs(System.nanoTime()) % 100000;
        return "HALO" + Instant.now().toEpochMilli() + suffix;
    }

    private boolean verifyGatewayResponseSign(JsonNode response, String mchKey) {
        var receivedSign = response.path("sign").asText(null);
        if (!StringUtils.hasText(receivedSign)) {
            return false;
        }
        var payload = new TreeMap<String, String>();
        response.fields().forEachRemaining(field -> {
            var key = field.getKey();
            if ("sign".equalsIgnoreCase(key)) {
                return;
            }
            var valueNode = field.getValue();
            if (valueNode == null || valueNode.isNull()) {
                return;
            }
            var value = valueNode.isContainerNode() ? valueNode.toString() : valueNode.asText(null);
            if (!StringUtils.hasText(value)) {
                return;
            }
            payload.put(key, value);
        });
        var calculatedSign = signByMd5Upper(payload, mchKey);
        return receivedSign.equalsIgnoreCase(calculatedSign);
    }

    private String signByMd5Upper(Map<String, String> payload, String mchKey) {
        var sorted = new TreeMap<String, String>();
        payload.forEach((key, value) -> {
            if ("sign".equalsIgnoreCase(key) || !StringUtils.hasText(value)) {
                return;
            }
            sorted.put(key, value);
        });
        var builder = new StringBuilder();
        sorted.forEach((key, value) -> {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(key).append('=').append(value);
        });
        builder.append("&key=").append(mchKey);
        return md5Upper(builder.toString());
    }

    private String md5Upper(String text) {
        try {
            var digest = MessageDigest.getInstance("MD5")
                .digest(text.getBytes(StandardCharsets.UTF_8));
            var builder = new StringBuilder(digest.length * 2);
            for (var current : digest) {
                builder.append(String.format("%02X", current));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("MD5 algorithm is unavailable.", ex);
        }
    }

    private String firstNonBlank(String... candidates) {
        for (var current : candidates) {
            if (StringUtils.hasText(current)) {
                return current;
            }
        }
        return "Bad request";
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private Mono<ServerResponse> renderMemberCardPage(ServerRequest request) {
        return ServerResponse.ok()
            .contentType(MediaType.TEXT_HTML)
            .bodyValue(MEMBER_CARD_PAGE_HTML);
    }

    private Mono<ServerResponse> renderVipGuardScript(ServerRequest request) {
        return ServerResponse.ok()
            .contentType(MediaType.valueOf("application/javascript"))
            .bodyValue(MEMBER_CARD_VIP_GUARD_SCRIPT);
    }

    private static final String MEMBER_CARD_PAGE_HTML = """
        <!doctype html>
        <html lang="zh-CN">
        <head>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1" />
          <title>会员卡中心</title>
          <style>
            :root { color-scheme: light; }
            body {
              margin: 0;
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
              background: #f6f8fa;
              color: #1f2328;
            }
            .container { max-width: 1100px; margin: 24px auto; padding: 0 16px 32px; }
            .header { margin-bottom: 16px; }
            .header-top {
              display: flex;
              justify-content: space-between;
              align-items: center;
              gap: 10px;
              flex-wrap: wrap;
            }
            .title { margin: 0; font-size: 24px; }
            .subtitle { margin: 6px 0 0; font-size: 14px; color: #57606a; }
            .panel {
              background: #fff;
              border: 1px solid #d0d7de;
              border-radius: 10px;
              padding: 16px;
              margin-top: 12px;
            }
            .status-line {
              display: flex;
              gap: 10px;
              flex-wrap: wrap;
              align-items: center;
              font-size: 14px;
            }
            .tag {
              border-radius: 999px;
              padding: 2px 10px;
              font-size: 12px;
              border: 1px solid transparent;
            }
            .tag.ok { background: #dafbe1; color: #1a7f37; border-color: #1a7f37; }
            .tag.warn { background: #fff8c5; color: #9a6700; border-color: #bf8700; }
            .tag.off { background: #f6f8fa; color: #57606a; border-color: #d0d7de; }
            .msg { font-size: 14px; color: #57606a; padding: 8px 0; }
            .error { color: #cf222e; }
            .login-box {
              margin-top: 10px;
              display: flex;
              gap: 10px;
              align-items: center;
              flex-wrap: wrap;
            }
            .tabs {
              display: inline-flex;
              gap: 8px;
              margin-bottom: 12px;
              padding: 4px;
              border: 1px solid #d0d7de;
              border-radius: 10px;
              background: #f6f8fa;
            }
            .tab-btn {
              border: 1px solid transparent;
              background: transparent;
              color: #57606a;
              border-radius: 8px;
              padding: 6px 12px;
              cursor: pointer;
              font-size: 14px;
            }
            .tab-btn.active {
              background: #ffffff;
              color: #1f2328;
              border-color: #d0d7de;
            }
            .tab-panel.hidden { display: none; }
            .toolbar {
              display: flex;
              justify-content: space-between;
              align-items: center;
              flex-wrap: wrap;
              gap: 8px;
              margin-bottom: 10px;
            }
            .grid {
              display: grid;
              gap: 12px;
              grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
              margin-top: 12px;
            }
            .card {
              border: 1px solid #d0d7de;
              border-radius: 10px;
              overflow: hidden;
              background: #fff;
              display: flex;
              flex-direction: column;
            }
            .cover {
              width: 100%;
              aspect-ratio: 16 / 9;
              object-fit: cover;
              background: #eef2f6;
            }
            .body { padding: 12px; display: flex; flex-direction: column; gap: 8px; flex: 1; }
            .card-title { font-weight: 700; font-size: 16px; line-height: 1.3; }
            .price { display: flex; align-items: baseline; gap: 8px; }
            .sale { color: #cf222e; font-weight: 700; font-size: 22px; }
            .origin { color: #8c959f; text-decoration: line-through; font-size: 14px; }
            .meta { color: #57606a; font-size: 13px; }
            .actions { margin-top: auto; }
            .order-actions { display: flex; gap: 6px; flex-wrap: wrap; }
            .empty { padding: 16px 8px; color: #57606a; text-align: center; }
            table { width: 100%; border-collapse: collapse; font-size: 14px; }
            th, td {
              border-bottom: 1px solid #d8dee4;
              padding: 10px 8px;
              text-align: left;
              vertical-align: middle;
            }
            th { color: #57606a; font-weight: 600; background: #f6f8fa; }
            button {
              border: 1px solid #1f883d;
              background: #2da44e;
              color: #fff;
              border-radius: 6px;
              padding: 8px 12px;
              cursor: pointer;
              font-size: 14px;
            }
            button.secondary { border-color: #d0d7de; color: #24292f; background: #f6f8fa; }
            button:disabled { opacity: .6; cursor: not-allowed; }
            .link-btn {
              display: inline-block;
              border: 1px solid #1f883d;
              background: #2da44e;
              color: #fff;
              border-radius: 6px;
              padding: 8px 12px;
              cursor: pointer;
              font-size: 14px;
              text-decoration: none;
              line-height: 1.2;
            }
            .link-btn.secondary { border-color: #d0d7de; color: #24292f; background: #f6f8fa; }
          </style>
        </head>
        <body>
        <main class="container">
          <header class="header">
            <div class="header-top">
              <h1 class="title">会员卡中心</h1>
              <a class="link-btn secondary" href="/">返回首页</a>
            </div>
            <p class="subtitle">购买会员卡后自动延长会员时长，视频访问权限实时生效。</p>
          </header>

          <section class="panel">
            <div class="status-line">
              <strong>账户状态：</strong>
              <span id="loginTag" class="tag off">未登录</span>
              <span id="vipTag" class="tag off">未知</span>
              <span id="expireText" class="msg"></span>
            </div>
            <div id="loginBox" class="login-box" style="display:none;">
              <span class="msg error">当前未登录，请先登录后查看订单和购买会员卡。</span>
              <a id="loginLink" href="/login">
                <button type="button" class="secondary">去登录</button>
              </a>
            </div>
          </section>

          <section class="panel">
            <div class="tabs">
              <button id="tabCardsBtn" class="tab-btn active" type="button">会员卡</button>
              <button id="tabOrdersBtn" class="tab-btn" type="button">订单列表</button>
            </div>

            <div id="cardsPanel" class="tab-panel">
              <div class="toolbar">
                <h2 style="margin:0;font-size:18px;">可购买会员卡</h2>
                <span id="cardsHint" class="msg">加载中...</span>
              </div>
              <div id="cardsGrid" class="grid"></div>
            </div>

            <div id="ordersPanel" class="tab-panel hidden">
              <div class="toolbar">
                <h2 style="margin:0;font-size:18px;">我的订单</h2>
                <button id="refreshOrdersBtn" class="secondary" type="button">刷新订单</button>
              </div>
              <div id="ordersWrap"><div class="msg">请切换到该 Tab 后加载订单...</div></div>
            </div>
          </section>
        </main>

        <script>
          const API_BASE = '/membercard';
          const STATUS_API = API_BASE + '/vip/status';
          const CARDS_API = API_BASE + '/cards';
          const ORDERS_API = API_BASE + '/orders';
          const qs = new URLSearchParams(window.location.search);
          const sourceRedirect = qs.get('redirect');

          const state = {
            loggedIn: false,
            vip: false,
            cards: [],
            orders: [],
            ordersLoaded: false,
            currentTab: 'cards'
          };

          const el = {
            loginTag: document.getElementById('loginTag'),
            vipTag: document.getElementById('vipTag'),
            expireText: document.getElementById('expireText'),
            loginBox: document.getElementById('loginBox'),
            loginLink: document.getElementById('loginLink'),
            tabCardsBtn: document.getElementById('tabCardsBtn'),
            tabOrdersBtn: document.getElementById('tabOrdersBtn'),
            cardsPanel: document.getElementById('cardsPanel'),
            ordersPanel: document.getElementById('ordersPanel'),
            cardsGrid: document.getElementById('cardsGrid'),
            cardsHint: document.getElementById('cardsHint'),
            ordersWrap: document.getElementById('ordersWrap'),
            refreshOrdersBtn: document.getElementById('refreshOrdersBtn')
          };

          function escapeHtml(text) {
            return String(text ?? '')
              .replaceAll('&', '&amp;')
              .replaceAll('<', '&lt;')
              .replaceAll('>', '&gt;')
              .replaceAll('"', '&quot;')
              .replaceAll("'", '&#39;');
          }

          function fmtDate(value) {
            if (!value) return '-';
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) return '-';
            return date.toLocaleString();
          }

          function fmtAmountFromYuan(value) {
            const num = Number(value);
            if (!Number.isFinite(num)) return 'CNY 0.00';
            return `CNY ${num.toFixed(2)}`;
          }

          function fmtAmountFromCent(value) {
            const num = Number(value);
            if (!Number.isFinite(num)) return 'CNY 0.00';
            return `CNY ${(num / 100).toFixed(2)}`;
          }

          function statusLabel(status) {
            switch (status) {
              case 0: return '已创建';
              case 1: return '支付中';
              case 2: return '支付成功';
              case 3: return '业务完成';
              default: return '-';
            }
          }

          async function requestJson(url, options = {}) {
            const response = await fetch(url, {
              method: options.method || 'GET',
              credentials: 'include',
              headers: {
                'Content-Type': 'application/json',
                ...(options.headers || {})
              },
              body: options.body ? JSON.stringify(options.body) : undefined
            });
            if (!response.ok) {
              let message = `HTTP ${response.status}`;
              try {
                const text = await response.text();
                if (text) {
                  try {
                    const payload = JSON.parse(text);
                    message = payload?.message
                      || payload?.detail
                      || payload?.title
                      || payload?.error
                      || message;
                  } catch (_) {
                    message = text;
                  }
                }
              } catch (_) {}
              throw new Error(message);
            }
            const text = await response.text();
            return text ? JSON.parse(text) : null;
          }

          function switchTab(tab) {
            state.currentTab = tab;
            const cardsActive = tab === 'cards';
            el.tabCardsBtn.classList.toggle('active', cardsActive);
            el.tabOrdersBtn.classList.toggle('active', !cardsActive);
            el.cardsPanel.classList.toggle('hidden', !cardsActive);
            el.ordersPanel.classList.toggle('hidden', cardsActive);
            if (!cardsActive) {
              loadOrders(false);
            }
          }

          async function loadVipStatus() {
            const loginRedirect = `/login?redirect_uri=${encodeURIComponent(window.location.href)}`;
            el.loginLink.setAttribute('href', loginRedirect);
            try {
              const status = await requestJson(STATUS_API);
              state.loggedIn = Boolean(status?.loggedIn);
              state.vip = Boolean(status?.vip);
              el.loginTag.className = `tag ${state.loggedIn ? 'ok' : 'off'}`;
              el.loginTag.textContent = state.loggedIn ? '已登录' : '未登录';
              el.vipTag.className = `tag ${state.loggedIn ? (state.vip ? 'ok' : 'warn') : 'off'}`;
              el.vipTag.textContent = state.loggedIn ? (state.vip ? 'VIP 用户' : '非 VIP 用户') : '未登录';
              if (!state.loggedIn) {
                el.expireText.textContent = '会员卡到期时间：请登录后查看';
              } else if (status?.expireAt) {
                el.expireText.textContent = `会员卡到期时间：${fmtDate(status.expireAt)}`;
              } else {
                el.expireText.textContent = '会员卡到期时间：暂无';
              }
              el.loginBox.style.display = state.loggedIn ? 'none' : 'flex';
            } catch (error) {
              state.loggedIn = false;
              state.vip = false;
              el.loginTag.className = 'tag off';
              el.loginTag.textContent = '未登录';
              el.vipTag.className = 'tag off';
              el.vipTag.textContent = '状态未知';
              el.expireText.textContent = `会员卡状态加载失败：${error.message}`;
              el.loginBox.style.display = 'flex';
            }
          }

          function renderCards() {
            if (!state.loggedIn) {
              el.cardsGrid.innerHTML = '<div class="empty">请先登录后查看并购买会员卡</div>';
              el.cardsHint.textContent = '登录后显示';
              return;
            }
            if (!state.cards.length) {
              el.cardsGrid.innerHTML = '<div class="empty">暂无可购买会员卡</div>';
              el.cardsHint.textContent = '无可售卡片';
              return;
            }
            el.cardsHint.textContent = `共 ${state.cards.length} 张会员卡`;
            const cardsHtml = state.cards.map(card => {
              const spec = card.spec || {};
              const title = escapeHtml(spec.title || card.metadata?.name || '会员卡');
              const image = escapeHtml(spec.image || '');
              const desc = escapeHtml(spec.description || '');
              const duration = Number(spec.durationDays || 30);
              const validFrom = spec.validFrom ? fmtDate(spec.validFrom) : '-';
              const validTo = spec.validTo ? fmtDate(spec.validTo) : '-';
              return `
                <article class="card">
                  ${image ? `<img class="cover" src="${image}" alt="${title}" />` : '<div class="cover"></div>'}
                  <div class="body">
                    <div class="card-title">${title}</div>
                    <div class="price">
                      <span class="sale">${fmtAmountFromYuan(spec.salePrice)}</span>
                      <span class="origin">${fmtAmountFromYuan(spec.originalPrice)}</span>
                    </div>
                    <div class="meta">时长：${Number.isFinite(duration) ? duration : 30} 天</div>
                    <div class="meta">有效期：${validFrom} ~ ${validTo}</div>
                    ${desc ? `<div class="meta">${desc}</div>` : ''}
                    <div class="actions">
                      <button type="button" data-action="buy" data-card-name="${escapeHtml(card.metadata?.name || '')}" ${state.loggedIn ? '' : 'disabled'}>立即下单</button>
                    </div>
                  </div>
                </article>
              `;
            }).join('');
            el.cardsGrid.innerHTML = cardsHtml;
          }

          function renderOrders() {
            if (!state.loggedIn) {
              el.ordersWrap.innerHTML = '<div class="msg">请先登录后查看订单。</div>';
              return;
            }
            if (!state.orders.length) {
              el.ordersWrap.innerHTML = '<div class="empty">暂无订单，可先购买会员卡。</div>';
              return;
            }
            const rows = state.orders.map(order => {
              const spec = order.spec || {};
              const name = escapeHtml(order.metadata?.name || '');
              const canPay = spec.status === 0 || spec.status === 1 || spec.status == null;
              const payUrl = escapeHtml(spec.payUrl || '');
              return `
                <tr>
                  <td>${name}</td>
                  <td>${escapeHtml(spec.cardTitle || spec.cardName || '-')}</td>
                  <td>${fmtAmountFromCent(spec.amount)}</td>
                  <td>${escapeHtml(statusLabel(spec.status))}</td>
                  <td>${fmtDate(spec.createdAt)}</td>
                  <td>
                    <div class="order-actions">
                      <button type="button" class="secondary" data-action="sync-order" data-order-name="${name}">同步状态</button>
                      <button type="button" data-action="go-pay" data-pay-url="${payUrl}" ${canPay && payUrl ? '' : 'disabled'}>去支付</button>
                    </div>
                  </td>
                </tr>
              `;
            }).join('');
            el.ordersWrap.innerHTML = `
              <table>
                <thead>
                  <tr>
                    <th>订单号</th>
                    <th>会员卡</th>
                    <th>金额</th>
                    <th>状态</th>
                    <th>下单时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>${rows}</tbody>
              </table>
            `;
          }

          async function loadCards() {
            if (!state.loggedIn) {
              state.cards = [];
              renderCards();
              return;
            }
            try {
              const result = await requestJson(CARDS_API);
              state.cards = Array.isArray(result?.items) ? result.items : [];
            } catch (error) {
              state.cards = [];
              el.cardsHint.textContent = `加载失败：${error.message}`;
            }
            renderCards();
          }

          async function loadOrders(forceReload) {
            if (!state.loggedIn) {
              state.orders = [];
              state.ordersLoaded = false;
              renderOrders();
              return;
            }
            if (!forceReload && state.ordersLoaded) {
              renderOrders();
              return;
            }
            el.ordersWrap.innerHTML = '<div class="msg">订单加载中...</div>';
            try {
              const result = await requestJson(`${ORDERS_API}?page=1&size=50`);
              state.orders = Array.isArray(result?.items) ? result.items : [];
              state.ordersLoaded = true;
              renderOrders();
            } catch (error) {
              state.orders = [];
              state.ordersLoaded = false;
              el.ordersWrap.innerHTML = `<div class="msg error">订单加载失败：${escapeHtml(error.message)}</div>`;
            }
          }

          async function createOrder(cardName) {
            const result = await requestJson(ORDERS_API, {
              method: 'POST',
              // Use plugin default returnUrl to avoid gateway rejecting dynamic callback URLs.
              body: { cardName }
            });
            switchTab('orders');
            await loadOrders(true);
            if (result?.payUrl) {
              window.open(result.payUrl, '_blank');
            } else {
              alert('下单成功，但未返回支付链接，请在订单列表中点击去支付。');
            }
          }

          async function syncOrder(orderName) {
            await requestJson(`${ORDERS_API}/${encodeURIComponent(orderName)}/sync`, {
              method: 'POST'
            });
            await loadOrders(true);
          }

          el.tabCardsBtn.addEventListener('click', () => switchTab('cards'));
          el.tabOrdersBtn.addEventListener('click', () => switchTab('orders'));

          el.cardsGrid.addEventListener('click', async (event) => {
            const target = event.target;
            if (!(target instanceof HTMLElement)) return;
            if (target.dataset.action !== 'buy') return;
            const cardName = target.dataset.cardName;
            if (!cardName) return;
            if (!state.loggedIn) {
              window.location.href = `/login?redirect_uri=${encodeURIComponent(window.location.href)}`;
              return;
            }
            target.setAttribute('disabled', 'disabled');
            const oldText = target.textContent;
            target.textContent = '下单中...';
            try {
              await createOrder(cardName);
            } catch (error) {
              alert(`下单失败：${error.message}`);
            } finally {
              target.removeAttribute('disabled');
              target.textContent = oldText;
            }
          });

          el.ordersWrap.addEventListener('click', async (event) => {
            const target = event.target;
            if (!(target instanceof HTMLElement)) return;
            const action = target.dataset.action;
            if (action === 'go-pay') {
              const payUrl = target.dataset.payUrl;
              if (payUrl) {
                window.open(payUrl, '_blank');
              }
              return;
            }
            if (action === 'sync-order') {
              const orderName = target.dataset.orderName;
              if (!orderName) return;
              target.setAttribute('disabled', 'disabled');
              const oldText = target.textContent;
              target.textContent = '同步中...';
              try {
                await syncOrder(orderName);
              } catch (error) {
                alert(`同步失败：${error.message}`);
              } finally {
                target.removeAttribute('disabled');
                target.textContent = oldText;
              }
            }
          });

          el.refreshOrdersBtn.addEventListener('click', async () => {
            el.refreshOrdersBtn.setAttribute('disabled', 'disabled');
            const oldText = el.refreshOrdersBtn.textContent;
            el.refreshOrdersBtn.textContent = '刷新中...';
            try {
              await loadOrders(true);
            } finally {
              el.refreshOrdersBtn.removeAttribute('disabled');
              el.refreshOrdersBtn.textContent = oldText;
            }
          });

          (async () => {
            await loadVipStatus();
            await loadCards();
            switchTab('cards');
          })();
        </script>
        </body>
        </html>
        """;

    private static final String MEMBER_CARD_VIP_GUARD_SCRIPT = """
        (() => {
          const STATUS_API = '/membercard/vip/status';
          const MEMBER_CARD_PAGE = '/membercard/portal/member-card';
          const currentScript = document.currentScript;
          const autoBind = currentScript && currentScript.dataset
            && currentScript.dataset.auto === 'true';
          const vipSelector = currentScript && currentScript.dataset
            ? currentScript.dataset.vipSelector : '';
          const vipKeywords = ((currentScript && currentScript.dataset
            ? currentScript.dataset.vipKeywords : '') || 'vip,vip专享')
            .split(',')
            .map(item => item.trim().toLowerCase())
            .filter(Boolean);
          const cacheMsRaw = currentScript && currentScript.dataset
            ? currentScript.dataset.cacheMs : '';
          const cacheMs = Number(cacheMsRaw || 30000) > 0
            ? Number(cacheMsRaw || 30000) : 30000;

          let statusCache = null;
          let statusCacheAt = 0;
          let dialogEl = null;

          const buildRechargeUrl = (memberCardPageUrl) => {
            const target = memberCardPageUrl || MEMBER_CARD_PAGE;
            const joinChar = target.includes('?') ? '&' : '?';
            return `${target}${joinChar}redirect=${encodeURIComponent(window.location.href)}`;
          };

          const buildLoginUrl = () =>
            `/login?redirect_uri=${encodeURIComponent(window.location.href)}`;

          const pageNeedsVip = () => {
            if (vipSelector) {
              return Boolean(document.querySelector(vipSelector));
            }
            if (document.body && document.body.dataset && document.body.dataset.vipOnly === 'true') {
              return true;
            }
            const tagNodes = Array.from(document.querySelectorAll(
              'a[href*="/tags/"], a[href*="/tag/"], [rel="tag"], .tag a, .post-tags a'
            ));
            const tagTexts = tagNodes
              .map(node => (node.textContent || '').trim().toLowerCase())
              .filter(Boolean);
            return tagTexts.some(text => vipKeywords.some(keyword => text.includes(keyword)));
          };

          const fetchVipStatus = async (forceRefresh = false) => {
            const now = Date.now();
            if (!forceRefresh && statusCache && now - statusCacheAt < cacheMs) {
              return statusCache;
            }
            try {
              const response = await fetch(STATUS_API, { credentials: 'include' });
              if (!response.ok) {
                statusCache = {
                  loggedIn: false,
                  vip: false,
                  memberCardPageUrl: MEMBER_CARD_PAGE
                };
                statusCacheAt = now;
                return statusCache;
              }
              const data = await response.json();
              statusCache = data || {
                loggedIn: false,
                vip: false,
                memberCardPageUrl: MEMBER_CARD_PAGE
              };
              statusCacheAt = now;
              return statusCache;
            } catch (e) {
              statusCache = {
                loggedIn: false,
                vip: false,
                memberCardPageUrl: MEMBER_CARD_PAGE
              };
              statusCacheAt = now;
              return statusCache;
            }
          };

          const ensureDialogStyle = () => {
            if (document.getElementById('membercard-vip-guard-style')) {
              return;
            }
            const style = document.createElement('style');
            style.id = 'membercard-vip-guard-style';
            style.textContent = `
              .membercard-vip-guard-mask {
                position: fixed;
                inset: 0;
                background: rgba(0, 0, 0, 0.55);
                z-index: 2147483000;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 16px;
                box-sizing: border-box;
              }
              .membercard-vip-guard-dialog {
                width: min(92vw, 420px);
                border-radius: 12px;
                background: #ffffff;
                box-shadow: 0 18px 48px rgba(0, 0, 0, 0.28);
                padding: 20px 18px 16px;
                color: #1f2328;
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
              }
              .membercard-vip-guard-title {
                margin: 0 0 10px;
                font-size: 18px;
                font-weight: 700;
              }
              .membercard-vip-guard-desc {
                margin: 0;
                line-height: 1.65;
                color: #57606a;
                font-size: 14px;
              }
              .membercard-vip-guard-actions {
                margin-top: 18px;
                display: flex;
                justify-content: flex-end;
                gap: 10px;
              }
              .membercard-vip-guard-actions button {
                border-radius: 8px;
                font-size: 14px;
                line-height: 20px;
                padding: 7px 14px;
                border: 1px solid #d0d7de;
                background: #f6f8fa;
                color: #24292f;
                cursor: pointer;
              }
              .membercard-vip-guard-actions button.primary {
                border-color: #1f883d;
                background: #2da44e;
                color: #ffffff;
              }
            `;
            document.head.appendChild(style);
          };

          const closeDialog = () => {
            if (dialogEl) {
              dialogEl.remove();
              dialogEl = null;
            }
          };

          const showVipDialog = (status) => {
            ensureDialogStyle();
            closeDialog();
            const loggedIn = Boolean(status && status.loggedIn);
            const title = loggedIn ? '仅 VIP 用户可播放' : '请先登录';
            const tip = loggedIn
              ? '当前账号未开通 VIP，开通会员后即可播放该视频。'
              : '当前未登录，请先登录，登录后再校验 VIP 权限。';
            const primaryText = loggedIn ? '去充值' : '去登录';

            dialogEl = document.createElement('div');
            dialogEl.className = 'membercard-vip-guard-mask';
            dialogEl.innerHTML = `
              <div class="membercard-vip-guard-dialog" role="dialog" aria-modal="true">
                <h3 class="membercard-vip-guard-title">${title}</h3>
                <p class="membercard-vip-guard-desc">${tip}</p>
                <div class="membercard-vip-guard-actions">
                  <button type="button" data-action="cancel">稍后再说</button>
                  <button type="button" class="primary" data-action="go">${primaryText}</button>
                </div>
              </div>
            `;
            document.body.appendChild(dialogEl);

            const cancelButton = dialogEl.querySelector('[data-action="cancel"]');
            const goButton = dialogEl.querySelector('[data-action="go"]');
            if (cancelButton) {
              cancelButton.addEventListener('click', closeDialog);
            }
            if (goButton) {
              goButton.addEventListener('click', () => {
                if (loggedIn) {
                  window.location.href = buildRechargeUrl(status && status.memberCardPageUrl);
                } else {
                  window.location.href = buildLoginUrl();
                }
              });
            }
            dialogEl.addEventListener('click', (event) => {
              if (event.target === dialogEl) {
                closeDialog();
              }
            });
          };

          const checkVipAndRedirect = async () => {
            if (!pageNeedsVip()) {
              return true;
            }
            const status = await fetchVipStatus();
            if (!status || !status.loggedIn) {
              showVipDialog({ loggedIn: false, vip: false, memberCardPageUrl: MEMBER_CARD_PAGE });
              return false;
            }
            if (status.vip) {
              return true;
            }
            showVipDialog(status);
            return false;
          };

          const handleVideoPlay = async (event) => {
            const target = event.target;
            if (!(target instanceof HTMLVideoElement)) {
              return;
            }
            const allowed = await checkVipAndRedirect();
            if (allowed) {
              return;
            }
            target.pause();
            target.currentTime = 0;
            event.preventDefault();
            event.stopImmediatePropagation();
          };

          window.MemberCardVipGuard = {
            checkVipAndRedirect,
            pageNeedsVip,
            showVipDialog,
            memberCardPage: MEMBER_CARD_PAGE
          };

          if (!autoBind) {
            return;
          }
          document.addEventListener('play', handleVideoPlay, true);
        })();
        """;
}

