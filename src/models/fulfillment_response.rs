use serde::Serialize;
use rocket::serde::uuid::Uuid;

use crate::entities::device::Device;

#[derive(Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct FulfillmentResponse {
    pub request_id: String,
    pub payload: Payload,
}

#[derive(Serialize, Debug)]
#[serde(untagged, rename_all = "camelCase")]
pub enum Payload {
    SyncPayload {
        agent_user_id: String,
        devices: Vec<SyncDevice>,
    }
}

#[derive(Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SyncDevice {
    pub id: Uuid,
    #[serde(rename = "type")]
    pub device_type: String,
    pub traits: Vec<String>,
    pub name: SyncDeviceName,
}

#[derive(Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct SyncDeviceName {
    pub name: String,
}

impl From<Device> for SyncDevice {
    fn from(device: Device) -> Self {
        Self {
            id: device.id,
            device_type: device.device_type,
            name: SyncDeviceName {
                name: device.device_name,
            },
            traits: device.traits,
        }
    }
}
