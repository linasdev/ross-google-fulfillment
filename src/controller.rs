use rocket::post;
use rocket::serde::json::Json;
use log::{info, warn};
use std::convert::TryInto;

use crate::models::fulfillment_request::{FulfillmentRequest, Intent};
use crate::models::fulfillment_response::{FulfillmentResponse, Payload};
use crate::models::valid_token::ValidToken;
use crate::errors::ApiError;
use crate::services::account_service::AccountService;
use crate::services::device_service::DeviceService;

#[post("/", data = "<request>")]
pub async fn fulfillment(request: Json<FulfillmentRequest>, token: ValidToken, mut account_service: AccountService, mut device_service: DeviceService) -> Result<Json<FulfillmentResponse>, Json<ApiError>> {
    let request = request.into_inner();

    info!("Handling request: {}", request.request_id);

    let account = account_service.get_or_create_account(&token.subject).await?;

    let intent = match request.inputs.first() {
        Some(intent) => intent,
        None => return Err(Json(ApiError::NoIntent)),
    };

    match intent {
        Intent::Sync => {
            let devices = device_service.get_all_devices_by_account_id(account.id)
                .await?
                .into_iter()
                .map(|device| device.into())
                .collect();

            Ok(Json(FulfillmentResponse {
                request_id: request.request_id,
                payload: Payload::SyncPayload {
                    agent_user_id: account.id.to_string(),
                    devices,
                }
            }))
        },
        Intent::Query { devices: query_devices } => {
            let devices = device_service.get_all_devices_by_account_id(account.id)
                .await?
                .into_iter()
                .filter(|device| query_devices.iter().any(|query_device| device.id == query_device.id))
                .filter_map(|device| match device.try_into() {
                    Ok(state) => Some(state),
                    Err(err) => {
                        warn!("Failed to convert device into query device state with error: {:?}", err);
                        None
                    }
                })
                .collect();

            Ok(Json(FulfillmentResponse {
                request_id: request.request_id,
                payload: Payload::QueryPayload {
                    devices,
                }
            }))
        }
    }
}
