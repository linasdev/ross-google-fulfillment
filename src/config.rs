use serde::Deserialize;

#[derive(Deserialize)]
pub struct SecurityConfig {
    pub jwks_url: String,
    pub token_header: String,
    pub token_prefix: String,
    pub token_audience: String,
}
