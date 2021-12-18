use rocket::request::{Request, FromRequest, Outcome};
use rocket::http::Status;
use rocket::async_trait;
use jwks_client::keyset::KeyStore;

use crate::config::SecurityConfig;
use crate::errors::TokenError;

#[derive(Debug)]
pub struct ValidToken {
    pub subject: String,
}

#[async_trait]
impl<'r> FromRequest<'r> for ValidToken {
    type Error = TokenError;

    async fn from_request(req: &'r Request<'_>) -> Outcome<Self, Self::Error> {
        let security_config = req.rocket().state::<SecurityConfig>().unwrap();

        let token = match req.headers().get_one(security_config.token_header.as_str()) {
            Some(token) => token,
            None => return Outcome::Failure((Status::Unauthorized, TokenError::NoToken)),
        };

        let token = if token.starts_with(security_config.token_prefix.as_str()) {
            &token[security_config.token_prefix.len()..]
        } else {
            return Outcome::Failure((Status::Unauthorized, TokenError::InvalidPrefix));
        };

        let keyset = KeyStore::new_from(security_config.jwks_url.clone()).await.unwrap();

        println!("{}", token);

        match keyset.verify(token) {
            Ok(jwt) => {
                let payload = jwt.payload();

                let audience = match payload.aud() {
                    Some(audience) => audience.to_string(),
                    None => return Outcome::Failure((Status::Unauthorized, TokenError::NoAudience)),
                };

                if audience != security_config.token_audience {
                    return Outcome::Failure((Status::Unauthorized, TokenError::InvalidAudience));
                }

                let subject = match payload.sub() {
                    Some(subject) => subject.to_string(),
                    None => return Outcome::Failure((Status::Unauthorized, TokenError::NoSubject)),
                };

                Outcome::Success(ValidToken {
                    subject,
                })
            }
            Err(err) => {
                Outcome::Failure((Status::Unauthorized, TokenError::JwksError(err)))
            }
        }
    }
}
