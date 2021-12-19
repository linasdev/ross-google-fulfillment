use rocket::request::{Request, FromRequest, Outcome};
use rocket::serde::uuid::Uuid;
use rocket::async_trait;
use rocket_db_pools::Connection;

use crate::Db;
use crate::entities::device::Device;
use crate::errors::ApiError;

pub struct DeviceService {
    db: Connection<Db>,
}

impl DeviceService {
    pub async fn get_all_devices_by_account_id(&mut self, account_id: Uuid) -> Result<Vec<Device>, ApiError> {
        sqlx::query_as!(Device, "SELECT * FROM device WHERE account_id = $1", account_id)
            .fetch_all(&mut *self.db)
            .await
            .map_err(|_| ApiError::DatabaseError)
    }
}

#[async_trait]
impl<'r> FromRequest<'r> for DeviceService {
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
