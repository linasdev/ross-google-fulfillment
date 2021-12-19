use rocket::serde::uuid::Uuid;

pub struct Account {
    pub id: Uuid,
    pub token_subject: String,
}
