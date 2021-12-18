use rocket::response::Responder;
use jwks_client::error::Error as JwksError;

#[derive(Responder, Debug)]
pub enum ApiError {
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
