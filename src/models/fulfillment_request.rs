use serde::Deserialize;

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
}
