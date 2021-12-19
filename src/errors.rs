use serde::Serialize;
use jwks_client::error::Error as JwksError;

#[derive(Serialize, Debug)]
#[serde(tag = "type", rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ApiError {
    NoIntent,
    DatabaseError,
}

#[derive(Debug)]
pub enum TokenError {
    NoToken,
    InvalidPrefix,
    NoAudience,
    InvalidAudience,
    NoSubject,
    JwksError(JwksError),
}
