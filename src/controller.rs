use rocket::post;
use rocket::serde::json::Json;
use log::info;

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

            // Ok(Json(FulfillmentResponse {
            //     request_id: request.request_id,
            //     payload: Payload::SyncPayload {
            //         agent_user_id: token.subject,
            //         devices: vec![
            //             SyncDevice {
            //                 id: "123".to_string(),
            //                 device_type: "action.devices.types.OUTLET".to_string(),
            //                 traits: vec![
            //                     "action.devices.traits.OnOff".to_string()
            //                 ],
            //                 name: SyncDeviceName {
            //                     default_names: vec![],
            //                     name: "Outlet 1".to_string(),
            //                     nicknames: vec![],
            //                 }
            //             },
            //             SyncDevice {
            //                 id: "456".to_string(),
            //                 device_type: "action.devices.types.OUTLET".to_string(),
            //                 traits: vec![
            //                     "action.devices.traits.OnOff".to_string()
            //                 ],
            //                 name: SyncDeviceName {
            //                     default_names: vec![],
            //                     name: "Outlet 2".to_string(),
            //                     nicknames: vec![],
            //                 }
            //             },
            //         ]
            //     }
            // }))
        }
    }
}
