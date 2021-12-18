use rocket::post;

use crate::models::valid_token::ValidToken;

#[post("/")]
pub async fn fulfillment(token: ValidToken) {
    println!("{:?}", token);
}
