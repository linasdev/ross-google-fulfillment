use rocket::serde::uuid::Uuid;

pub struct Device {
    pub id: Uuid,
    pub account_id: Uuid,
    pub device_type: String,
    pub device_name: String,
    pub traits: Vec<String>,
}
