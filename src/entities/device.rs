use serde::{Serialize, Deserialize};
use rocket::serde::uuid::Uuid;

pub struct Device {
    pub id: Uuid,
    pub account_id: Uuid,
    pub device_type: String,
    pub device_name: String,
    pub traits: Vec<String>,
    pub last_state: String,
}

impl Device {
    pub fn set_last_state(&mut self, last_state: DeviceState) -> Result<(), serde_json::Error> {
        self.last_state = serde_json::to_string(&last_state)?;
        Ok(())
    }

    pub fn get_last_state(&self) -> Result<DeviceState, serde_json::Error> {
        serde_json::from_str(&self.last_state)
    }    
}

#[derive(Serialize, Deserialize)]
pub enum DeviceState {
    OnOff(bool),
    Single(u8),
    Rgb(u8, u8, u8),
    Rgbw(u8, u8, u8, u8),
}
