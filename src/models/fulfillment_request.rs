use serde::Deserialize;
use rocket::serde::uuid::Uuid;

#[derive(Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct FulfillmentRequest {
    pub request_id: String,
    pub inputs: Vec<Intent>,
}

#[derive(Deserialize, Debug)]
#[serde(tag = "intent", content = "payload", rename_all = "camelCase")]
pub enum Intent {
    #[serde(rename = "action.devices.SYNC")]
    Sync,
    #[serde(rename = "action.devices.QUERY")]
    Query {
        devices: Vec<QueryDevice>,
    },
}

#[derive(Deserialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct QueryDevice {
    pub id: Uuid,
    pub custom_data: Option<String>,
}
