-- membership-service initial schema

CREATE TABLE membership_plans (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(80) NOT NULL UNIQUE,
    name            VARCHAR(120) NOT NULL,
    description     VARCHAR(1000),
    price_amount    NUMERIC(19, 4) NOT NULL,
    price_currency  VARCHAR(255) NOT NULL,
    billing_period  VARCHAR(20) NOT NULL,
    active          BOOLEAN NOT NULL,
    stripe_price_id VARCHAR(120),
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);

CREATE TABLE subscriptions (
    id                       BIGSERIAL PRIMARY KEY,
    user_id                  BIGINT NOT NULL,
    plan_id                  BIGINT NOT NULL REFERENCES membership_plans(id),
    status                   VARCHAR(20) NOT NULL,
    start_date               TIMESTAMP NOT NULL,
    current_period_end       TIMESTAMP NOT NULL,
    cancelled_at             TIMESTAMP,
    stripe_subscription_id   VARCHAR(120),
    stripe_customer_id       VARCHAR(120),
    created_at               TIMESTAMP NOT NULL,
    updated_at               TIMESTAMP NOT NULL
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_stripe_sub_id ON subscriptions(stripe_subscription_id);

CREATE TABLE payments (
    id                         BIGSERIAL PRIMARY KEY,
    user_id                    BIGINT NOT NULL,
    subscription_id            BIGINT NOT NULL,
    amount                     NUMERIC(19, 4) NOT NULL,
    currency                   VARCHAR(255) NOT NULL,
    status                     VARCHAR(20) NOT NULL,
    stripe_payment_intent_id   VARCHAR(120),
    description                VARCHAR(200),
    processed_at               TIMESTAMP,
    failure_reason             VARCHAR(500),
    created_at                 TIMESTAMP NOT NULL,
    updated_at                 TIMESTAMP NOT NULL
);

CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_subscription_id ON payments(subscription_id);

CREATE TABLE invoices (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    subscription_id     BIGINT NOT NULL,
    amount              NUMERIC(19, 4) NOT NULL,
    currency            VARCHAR(255) NOT NULL,
    status              VARCHAR(20) NOT NULL,
    stripe_invoice_id   VARCHAR(120),
    hosted_invoice_url  VARCHAR(500),
    issued_at           TIMESTAMP,
    paid_at             TIMESTAMP,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL
);

CREATE INDEX idx_invoices_user_id ON invoices(user_id);
