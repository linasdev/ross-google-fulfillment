use rocket::request::{Request, FromRequest, Outcome};
use rocket::async_trait;
use rocket_db_pools::Connection;

use crate::Db;
use crate::entities::account::Account;
use crate::errors::ApiError;

pub struct AccountService {
    db: Connection<Db>,
}

impl AccountService {
    pub async fn get_or_create_account(&mut self, token_subject: &str) -> Result<Account, ApiError> {
        let account_option = sqlx::query_as!(Account, "SELECT * FROM account WHERE token_subject = $1", token_subject)
            .fetch_one(&mut *self.db)
            .await
            .ok();

        match account_option {
            Some(account) => Ok(account),
            None => {
                let insert_result = sqlx::query!("INSERT INTO account (token_subject) VALUES ($1)", token_subject)
                    .execute(&mut *self.db)
                    .await;

                if insert_result.is_err() {
                    Err(ApiError::DatabaseError)
                } else {
                    sqlx::query_as!(Account, "SELECT * FROM account WHERE token_subject = $1", token_subject)
                        .fetch_one(&mut *self.db)
                        .await
                        .map_err(|_| ApiError::DatabaseError)
                }
            }
        }
    }
}

#[async_trait]
impl<'r> FromRequest<'r> for AccountService {
    type Error = <Connection<Db> as FromRequest<'r>>::Error;

    async fn from_request(req: &'r Request<'_>) -> Outcome<Self, Self::Error> {
        match Connection::from_request(req).await {
            Outcome::Success(db) => {
                Outcome::Success(Self {
                    db,
                })
            }
            Outcome::Failure(e) => Outcome::Failure(e),
            Outcome::Forward(f) => Outcome::Forward(f),
        }
    }
}
