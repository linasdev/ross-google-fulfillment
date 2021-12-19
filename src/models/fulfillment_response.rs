use serde::Serialize;
use rocket::serde::uuid::Uuid;
use std::convert::TryFrom;

use crate::entities::device::{Device, DeviceState};

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
    },
    QueryPayload {
        devices: Vec<QueryDeviceStatus>,
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

#[derive(Serialize, Debug)]
#[serde(rename_all = "camelCase")]
pub struct QueryDeviceStatus {
    pub online: bool,
    pub status: QueryStatus,
    pub on: Option<bool>,
}

#[derive(Serialize, Debug)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum QueryStatus {
    Success,
    Offline,
    Exceptions,
    Error,
}

impl TryFrom<Device> for QueryDeviceStatus {
    type Error = serde_json::Error;

    fn try_from(device: Device) -> Result<Self, Self::Error> {
        let last_state = device.get_last_state()?;

        Ok(Self {
            online: true,
            status: QueryStatus::Success,
            on: if let DeviceState::OnOff(value) = last_state {
                Some(value)
            } else {
                None
            },
        })
    }
}
